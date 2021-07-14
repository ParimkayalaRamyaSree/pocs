package com.adobe.aem.guides.wknd.core.models;

import java.util.LinkedList;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class TabIcons {

	protected static final Logger log = LoggerFactory.getLogger(TabIcons.class);
	
	@SlingObject
    private Resource resource;
	
	private List<String> tabIcons;
	
	private List<String> tabToolTips;
	
	public List<String> getTabIcons(){
		tabIcons = new LinkedList<String>();
		Iterable<Resource> tabItr = resource.getChildren();
		log.debug("resource name is {} ", resource.getName());
		for (Resource res : tabItr) {
			ValueMap vMap = res.getValueMap();
			tabIcons.add(vMap.get("imageURL", String.class));
			log.debug("imgpath is {} ", vMap.get("imageURL", String.class));

		}
		return tabIcons;
	}
	
	public List<String> getTabToolTips(){
		tabToolTips = new LinkedList<String>();
		Iterable<Resource> tabItr = resource.getChildren();
		log.debug("resource name is {} ", resource.getName());
		for (Resource res : tabItr) {
			ValueMap vMap = res.getValueMap();
			tabToolTips.add(vMap.get("toolTipTxt", String.class));
			log.debug("toolTipTxt is {} ", vMap.get("toolTipTxt", String.class));

		}
		return tabToolTips;
	}
	
}
