package com.adobe.aem.guides.wknd.core.models;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;

/**
 * The Class CommonOneUtils.
 */
@Component(service = CommonOneUtils.class, name="One Wellington Common Utility")
@Model(adaptables = { SlingHttpServletRequest.class, Resource.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CommonOneUtils {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CommonOneUtils.class);
	
	@Reference(cardinality=ReferenceCardinality.MANDATORY,policy = ReferencePolicy.STATIC)
	@Inject 
	private ResourceResolverFactory resolverFactory;
	
	@SlingObject
	private SlingHttpServletRequest request;
	
	private String assetPath;
	
	private String imageAltText;
	
	public String getImageAltText() {
        LOGGER.debug("request is {}",request);
		//assetPath = (String)request.getAttribute("assetPath");
		
        ResourceResolver resourceResolver = null;

		try {
			Map<String, Object> param = new HashMap<String, Object>();	
			param.put(ResourceResolverFactory.SUBSERVICE, "serviceuser");
			resourceResolver = resolverFactory.getServiceResourceResolver(param);
	        request = resourceResolver.adaptTo(SlingHttpServletRequest.class);
	        LOGGER.debug("request adapt is {}",request);

	        Resource res = resourceResolver.getResource(assetPath);
	        Asset asset = res.adaptTo(Asset.class);
	        String dcTitle = asset.getMetadataValue("dc:title");
	        LOGGER.debug("dcTitle is {}",dcTitle);
	        imageAltText = StringUtils.isNotEmpty(dcTitle) ? dcTitle :  asset.getName();
	        resourceResolver.close();
		} catch (LoginException e) {
	        LOGGER.error("Exception in getServiceResourceResolver {}",e);
		}
        LOGGER.debug("assetPath is in activate {}",assetPath);
		return imageAltText;
	}
}
