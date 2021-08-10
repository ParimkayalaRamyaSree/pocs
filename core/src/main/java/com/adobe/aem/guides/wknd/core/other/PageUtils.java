import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.commons.jcr.JcrConstants;

public final class PageUtils {

	@Reference
	private static Session session;
	
	protected static final Logger log = LoggerFactory.getLogger(PageUtils.class);
	
	private static final String SESSION_LOGGER = "Session object is null {}";
	
	   /** The Constant compList. */
    protected static final List<String> excludeCompList = Arrays.asList(ApplicationConstants.TEXTAREA_KEY,
            ApplicationConstants.EXITMODAL_KEY, ApplicationConstants.VIDEO, ApplicationConstants.CUD_KEY, "content");
	
	private PageUtils() {
	}

	public static String getPropertyFromComponentsResourcePath(Resource resource, ResourceResolver resourceResolver,
			String componentResourcePath, String propertyName) {

		String currentPagePath = resource.getPath();
		String navigationLogoPropertyValue = "";
		Resource navigationComponentResource = resourceResolver.getResource(currentPagePath + componentResourcePath);
		if (navigationComponentResource != null) {
			navigationLogoPropertyValue = navigationComponentResource.getValueMap().get(propertyName, String.class);
		}
		return navigationLogoPropertyValue;
	}

	public static String getPropertyFromComponentsResourcePath(Resource itemResource, String propertyName) {
		String nodePropertyValue = "";
		if (itemResource != null) {
			nodePropertyValue = itemResource.getValueMap().get(propertyName, String.class);
		}
		return nodePropertyValue;
	}

	public static String getRteId(String rteText) {
		return StringUtils.isNotBlank(rteText) ? String.valueOf(Math.abs(rteText.hashCode() - 1)) : StringUtils.EMPTY;
	}

	public static Boolean pageContainsVideo(Resource searchRoot, ResourceResolver resourceResolver,
			QueryBuilder queryBuilder) {
		Boolean isVideo = false;
		int count = 1;

		if (searchRoot != null) {
			try {
				Map<String, String> predicatesMap = searchRootPath(searchRoot, resourceResolver, QueryConstants.GROUP, count);
				session = resourceResolver.adaptTo(Session.class);
				Query query = queryBuilder.createQuery(PredicateGroup.create(predicatesMap), session);
				SearchResult result = query.getResult();

				if (!result.getHits().isEmpty()) {
					isVideo = true;
				}
			} finally {
				if (session == null) {
					log.error(SESSION_LOGGER, session);
				}
			}
		}
		return isVideo;
	}

	public static Query doQuery(ResourceResolver resourceResolver,QueryBuilder queryBuilder, String nodeName, boolean isPage, String subNodeName, String queryPath){
		Map<String, String> queryMap = new HashMap<String, String>();
		session = resourceResolver.adaptTo(Session.class);
		int count = 1;
		if (isPage){
		 String searchPageDirectory = new StringBuilder().append(queryPath).append(CUDConstants.PAGE_NODE).toString();
			String pagePropertyName = "pagepaths";
			Resource rootNode = resourceResolver.getResource(searchPageDirectory);

			if (rootNode != null) {
				queryPredicatesForDashboard (rootNode, resourceResolver, count,pagePropertyName, queryMap);
			}else {
				queryMap.put(new StringBuilder().append(QueryConstants.GROUP).append(count)
						.append(ApplicationConstants.UNDERSCORE).append(QueryConstants.PATH_KEY).toString(), ResourceTypeConstants.CONSUMER_PATH);
			}
			
		} else{
			String searchXfDirectory = new StringBuilder().append(queryPath).append(CUDConstants.XF_NODE).toString();
			String xfPropertyName = "xfpaths";
			Resource rootNode = resourceResolver.getResource(searchXfDirectory);

			if (rootNode != null) {
				queryPredicatesForDashboard (rootNode, resourceResolver, count,xfPropertyName, queryMap);
			}
			else {
				queryMap.put(new StringBuilder().append(QueryConstants.GROUP).append(count).append(ApplicationConstants.UNDERSCORE).append(QueryConstants.PATH_KEY).toString(), "/content/experience-fragments/consumer_marketing");
			}
		}
        queryMap.put(QueryConstants.OR_PREDICATE, ApplicationConstants.FLAG_TRUE);
        queryMap.put(QueryConstants.PROPERTY_ONE_KEY, QueryConstants.PROP_RESOURCE_TYPE);
        if (!excludeCompList.contains(nodeName)) {
            if (subNodeName == null && StringUtils.isBlank(subNodeName)) {
            queryMap.put(QueryConstants.PROPERTY_ONE_VALUE_KEY,
            		ResourceTypeConstants.COMPONENT_RES_TYPE + ApplicationConstants.DELIMETER + nodeName);
            } else {
            queryMap.put(QueryConstants.PROPERTY_ONE_VALUE_KEY, ResourceTypeConstants.COMPONENT_RES_TYPE
                    + ApplicationConstants.DELIMETER + nodeName + ApplicationConstants.DELIMETER + subNodeName);
            }
        }else {
            queryMap.put(QueryConstants.PROPERTY_ONE_VALUE_KEY, ResourceTypeConstants.SUBCOMPONENT_RES_TYPE
                    + ApplicationConstants.DELIMETER + nodeName);
        }
        queryMap.put(QueryConstants.PROPERTY_ONE_OPERATION_KEY, QueryConstants.LIKE);
        queryMap.put(QueryConstants.RESULT_LIMIT_KEY, QueryConstants.RESULT_LIMIT_ALL);
        queryMap.put(QueryConstants.ORDER_BY_KEY, QueryConstants.PATH_KEY);

		return queryBuilder.createQuery(PredicateGroup.create(queryMap), session);
	}
				
	public static void queryPredicatesForDashboard(Resource rootNode, ResourceResolver resourceResolver, int count,
			String propertyName, Map<String, String> queryMap) {
		String pathValue=null;
		Iterator<Resource> resourceIterator = rootNode.listChildren();
		while (resourceIterator.hasNext()) {
			Resource childResource = resourceResolver.getResource(resourceIterator.next().getPath());
			if (childResource != null) {
				pathValue = childResource.getValueMap().get(propertyName, String.class);	
				queryMap.put(new StringBuilder().append(QueryConstants.GROUP).append(count)
						.append(ApplicationConstants.UNDERSCORE).append(QueryConstants.PATH_KEY).toString(), pathValue);
				count++;
			}
		}
	}
	
	public static int countPageTemplates(ResourceResolver resourceResolver,
										  QueryBuilder queryBuilder, String nodeName,String subNodeName,String pagePath) {
		int hit = 0;
		try {
			Query query = doQuery(resourceResolver,queryBuilder,nodeName,true,subNodeName,pagePath);
			SearchResult result = query.getResult();
			hit = result.getHits().size();

		} finally {
			if (session == null) {
				log.error(SESSION_LOGGER, session);
			}
		}
		return hit;
	}
	public static int countEFTemplates(ResourceResolver resourceResolver,
										QueryBuilder queryBuilder, String nodeName, String subNodeName, String xfPath) {
		int hit = 0;
		JSONArray hitsArray = new JSONArray();
		try {
			Query query = doQuery(resourceResolver,queryBuilder,nodeName,false,subNodeName,xfPath);
			SearchResult result = query.getResult();
			hit = result.getHits().size();
			hitsArray.put(hit);
		} finally {
			if (session == null) {
				log.error(SESSION_LOGGER, session);
			}
		}
		return hit;
	}
	public static JSONArray getChildren(ResourceResolver resourceResolver,
										 QueryBuilder queryBuilder, String nodeName, String subNodeName, String queryPath){
		JSONArray children = new JSONArray();
		boolean isPage = true;
		try {
			for (int i=0; i<2;i++){
				if(i==1){
					isPage=false;
				}
				Query query = doQuery(resourceResolver,queryBuilder,nodeName,isPage,subNodeName,queryPath);
				SearchResult result = query.getResult();
				Iterator<Resource> iterator = result.getResources();
				while(iterator.hasNext()) {
					JSONObject grandChildren = getGrandChildren(resourceResolver, isPage, iterator);
					children.put(grandChildren);
				}
			}
		} catch (JSONException e) {
            e.printStackTrace();
        } finally {
			if (session == null) {
				log.error(SESSION_LOGGER, session);
			}
		}
		return children;
	}

	private static JSONObject getGrandChildren(ResourceResolver resourceResolver, boolean isPage,
			Iterator<Resource> iterator) throws JSONException {
		String pageName = null;
		String modifiedDate = null;
		Resource resource = iterator.next();
		JSONObject grandChildren = new JSONObject();
		String pathTo = resource.getPath();
		String [] parts = pathTo.split(ApplicationConstants.DELIMETER);
		String cleanPath = StringUtils.EMPTY;
		for (String part : parts) {
			if (!part.equals(ApplicationConstants.JCR_CONTENT)) {
				cleanPath = (cleanPath + part + ApplicationConstants.DELIMETER);
			} else {
				break;
			}
		}
		Resource newResource = resourceResolver.getResource(cleanPath + ApplicationConstants.JCR_CONTENT);
		ValueMap property = newResource.adaptTo(ValueMap.class);
		 Date date = property.get(NameConstants.PN_PAGE_LAST_MOD, Date.class);

		pageName = property.get(JcrConstants.JCR_TITLE, String.class);
		if(pageName != null){
			String [] templateArray = pageName.split(ApplicationConstants.DELIMETER);
			pageName = templateArray[templateArray.length-1];
		} else {
			pageName = CUDConstants.NOT_FOUND_TEXT;
		}

		if(date != null){
			modifiedDate = date.toString();
		} else {
			modifiedDate = CUDConstants.NOT_MODIFIED_TEXT;
		}
		grandChildren.put(CUDConstants.PAGE_NAME_KEY,pageName);
		grandChildren.put(CUDConstants.PATH_TO_KEY,StringUtils.chop(cleanPath)+".html");
		grandChildren.put(CUDConstants.MODIFIED_DATE_KEY, modifiedDate);
		if(isPage){
			grandChildren.put(QueryConstants.TYPE_KEY, CUDConstants.PAGES_TEXT);
		} else {
			grandChildren.put(QueryConstants.TYPE_KEY, CUDConstants.XF_TEXT);
		}
		return grandChildren;
	}
	
	public static List<Hit> pageContainsComponent(Resource searchRoot, ResourceResolver resourceResolver,
			QueryBuilder queryBuilder, String resType) {
		int count = 1;
		String groupPrefix = QueryConstants.GROUP + count + ApplicationConstants.UNDERSCORE ; 

		if (searchRoot != null) {
			try {
				Map<String, String> predicatesMap = new HashMap<>();
				predicatesMap = updatePredicateMap(predicatesMap, searchRoot, groupPrefix, 
						QueryConstants.PROP_RESOURCE_TYPE, resType, false, false);
				
				for (Resource child : searchRoot.getChildren()) {
					if (child.getResourceType().matches(ResourceTypeConstants.COMPONENT_RESOURCE_TYPE_EXPERIENCE_FRAGMENT)) {
						count = getXFPredicateMap(resourceResolver, count, predicatesMap, child);
					}
				}
				session = resourceResolver.adaptTo(Session.class);
				Query query = queryBuilder.createQuery(PredicateGroup.create(predicatesMap), session);
				SearchResult result = query.getResult();
				if(result != null)
					return result.getHits();
			} finally {
				if (session == null) {
					log.error(SESSION_LOGGER, session);
				}
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Updates Predicate map for any experience fragment on page.
	 * @param resourceResolver
	 * @param count
	 * @param predicatesMap
	 * @param child
	 * @return int
	 */
	private static int getXFPredicateMap(ResourceResolver resourceResolver, int count,
			Map<String, String> predicatesMap, Resource child) {
		Resource searchRoot = null;
		String groupPrefix = StringUtils.EMPTY;
		String experienceFragmentPath = child.getValueMap().get(ApplicationConstants.FRAGMENT_PATH_PRPERTY,
				String.class);
		if (StringUtils.isNotEmpty(experienceFragmentPath)) {
			count++;
			groupPrefix = QueryConstants.GROUP + count + ApplicationConstants.UNDERSCORE ;
			searchRoot = resourceResolver
					.getResource(experienceFragmentPath + ApplicationConstants.JCR_ROOT_RESOURCE_PATH);
			if (searchRoot != null) {
				predicatesMap.put(QueryConstants.OR_PREDICATE, Boolean.toString(true));
				predicatesMap.put(groupPrefix + QueryConstants.PATH_KEY, searchRoot.getPath());
			}
		}
		return count;
	}

	private static Map<String, String> updatePredicateMap(Map<String, String> predicatesMap, Resource searchRoot,
			String groupPrefix, String propertyKey, String propertyValue, Boolean isLikeOpt, Boolean prefixToProp) {
		predicatesMap.put(groupPrefix + QueryConstants.PATH_KEY, searchRoot.getPath());
		if(prefixToProp) {
			predicatesMap.put(groupPrefix + QueryConstants.PROPERTY_KEY, propertyKey);
			predicatesMap.put(groupPrefix + QueryConstants.PROPERTY_VALUE_KEY, propertyValue);
		}else {
			predicatesMap.put(QueryConstants.PROPERTY_KEY, propertyKey);
			predicatesMap.put(QueryConstants.PROPERTY_VALUE_KEY, propertyValue);
		}
		if(isLikeOpt) {
			if(prefixToProp){
				predicatesMap.put(groupPrefix + QueryConstants.PROPERTY_OPERATION_KEY, QueryConstants.LIKE);
			}else {
				predicatesMap.put(QueryConstants.PROPERTY_OPERATION_KEY, QueryConstants.LIKE);
			}
		}
		return predicatesMap;
	}

	private static Map<String, String> searchRootPath(Resource searchRoot, ResourceResolver resourceResolver,
			String group, int count) {
		Map<String, String> predicatesMap = new HashMap<>();
		String groupPrefix = group + count + ApplicationConstants.UNDERSCORE + group; 
		predicatesMap = updatePredicateMap(predicatesMap, searchRoot, groupPrefix, 
				QueryConstants.PROP_MEDIA_TYPE , ApplicationConstants.VIDEO, true, true);
		count++;
		groupPrefix = group + count + ApplicationConstants.UNDERSCORE + group;
		predicatesMap = updatePredicateMap(predicatesMap, searchRoot, groupPrefix, 
				QueryConstants.PROP_RESOURCE_TYPE , ResourceTypeConstants.VIDEO_PLAYLIST_RES_TYPE, false, true);
		predicatesMap.put(QueryConstants.OR_PREDICATE, Boolean.toString(true));
		for (Resource child : searchRoot.getChildren()) {
			if (child.getResourceType().matches(ResourceTypeConstants.COMPONENT_RESOURCE_TYPE_EXPERIENCE_FRAGMENT)) {
				String experienceFragmentPath = child.getValueMap().get(ApplicationConstants.FRAGMENT_PATH_PRPERTY,
						String.class);
				if (StringUtils.isNotEmpty(experienceFragmentPath)) {
					count++;
					groupPrefix = group + count + ApplicationConstants.UNDERSCORE + group;
					searchRoot = resourceResolver
							.getResource(experienceFragmentPath + ApplicationConstants.JCR_ROOT_RESOURCE_PATH);
					if (searchRoot != null) {
						predicatesMap = updatePredicateMap(predicatesMap, searchRoot, groupPrefix, 
								QueryConstants.PROP_RESOURCE_TYPE , ResourceTypeConstants.VIDEO_PLAYER_RES_TYPE, false, true);
						groupPrefix = group + (count+1) + ApplicationConstants.UNDERSCORE + group;
						predicatesMap = updatePredicateMap(predicatesMap, searchRoot, groupPrefix, 
								QueryConstants.PROP_MEDIA_TYPE , ApplicationConstants.VIDEO, true, true);
						groupPrefix = group + (count+2) + ApplicationConstants.UNDERSCORE + group;
						predicatesMap = updatePredicateMap(predicatesMap, searchRoot, groupPrefix, 
								QueryConstants.PROP_RESOURCE_TYPE , ResourceTypeConstants.VIDEO_PLAYLIST_RES_TYPE, false, true);
					}
				}
			}
		}

		return predicatesMap;
	}

	public static String getFormatDate(Date dateToFormat) {
		String formatedDate = null;
		if (dateToFormat != null) {
			formatedDate = new SimpleDateFormat(CUDConstants.OUTUT_DATE_FORMAT).format(dateToFormat);
		}
		return formatedDate;
	}

	public static String getFormatDate(String dateToFormat) {
		DateFormat inputFormat = new SimpleDateFormat(CUDConstants.INPUT_DATE_FORMAT);
		DateFormat outputFormat = new SimpleDateFormat(CUDConstants.OUTUT_DATE_FORMAT);
		Date date = null;
		try {
			if (dateToFormat != null) {
				date = inputFormat.parse(dateToFormat);
			}
		} catch (ParseException e) {
			log.info("exception occured in catch block {}", e);
		}
		return outputFormat.format(date);
	}

    public static Pdfcontent getpdfPageData(String objective, ResourceResolver resourceResolver) {
        objective = objective + ApplicationConstants.JCR_ROOT_RESOURCE_PATH;
        Pdfcontent pageobjetcivedata = new Pdfcontent();
        Resource pathResource = resourceResolver.getResource(objective);
        Iterator<Resource> resourceItr = pathResource == null ? Collections.emptyIterator() : pathResource.listChildren();
        while (resourceItr.hasNext()) {               
        final Resource poXfRes = resourceResolver.getResource(resourceItr.next().getPath());           
        if (poXfRes != null) {
            ValueMap pageObjectivePathNodes = poXfRes.adaptTo(ValueMap.class);
            
				pageobjetcivedata.setTitle(pageObjectivePathNodes.get(CUDConstants.TITLE_KEY,StringUtils.EMPTY));						
				pageobjetcivedata.setOwnerName(pageObjectivePathNodes.get(CUDConstants.OWNER_NAME_KEY,StringUtils.EMPTY));
				pageobjetcivedata.setBriefDescription(pageObjectivePathNodes.get(CUDConstants.BRIEF_DESC_KEY,StringUtils.EMPTY));			
				pageobjetcivedata.setChannel(pageObjectivePathNodes.get(CUDConstants.CHANNEL_KEY,StringUtils.EMPTY));			
				pageobjetcivedata.setAudience(pageObjectivePathNodes.get(CUDConstants.AUDIENCE_KEY,StringUtils.EMPTY));					
				String formatedMarketDate = getFormatDate(pageObjectivePathNodes.get(CUDConstants.MARKET_DATE_KEY, Date.class));
				pageobjetcivedata.setMarketDate(formatedMarketDate);						
				String formatedSubmitDate = getFormatDate(pageObjectivePathNodes.get(CUDConstants.SUBMIT_DATE_KEY, Date.class));
				pageobjetcivedata.setSubmitDate(formatedSubmitDate);
			
		  }
        }
		return pageobjetcivedata;
		        
	}

	/**
	 * Normalize the URL after checking if the URL is internal or external.
	 *
	 * @param resolver the resolver
	 * @param url      the url
	 * @return the string
	 */
	public static String normalizeUrl(ResourceResolver resolver, String url) {
		if (url != null && !url.isEmpty() && null != resolver) {
			String updatedUrl = url;
			StringBuilder extension = new StringBuilder();
			extension = extension.append(ApplicationConstants.HTML);
			try {
				if (StringUtils.startsWith(url, ApplicationConstants.CONTENT_PATH)
						&& (!StringUtils.contains(url, ApplicationConstants.DAM_PATH))) {
					updatedUrl = (StringUtils.endsWith(updatedUrl, ApplicationConstants.JSON)
							|| StringUtils.contains(updatedUrl, extension.toString())) ? updatedUrl
									: updatedUrl.concat(extension.toString());
					return StringUtils.isEmpty(updatedUrl) ? updatedUrl : URLDecoder.decode(updatedUrl, ApplicationConstants.UTF_8);
				}
			} catch (UnsupportedEncodingException ex) {
				log.error("exception shortenUrl {} ", ex);
			}
			return updatedUrl;
		}
		return StringUtils.EMPTY;
	}

}
