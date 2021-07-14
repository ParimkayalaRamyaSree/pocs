package com.adobe.aem.guides.wknd.core.models;

import java.util.Optional;

import com.adobe.cq.wcm.core.components.commons.editor.dialog.childreneditor.Item;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author parramya1
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CustomTabItem extends Item {

 	private static final Logger LOGGER = LoggerFactory.getLogger(CustomTabEditor.class);
 	
 	private static final String IMAGE_KEY = "imageURL"; 
 	
 	private static final String TOOL_TIP_KEY = "toolTipTxt"; 
	
    public CustomTabItem(SlingHttpServletRequest request, Resource resource) {
		super(request, resource);
		ValueMap vm = resource.getValueMap();
		imageURL = Optional.ofNullable(vm.get(IMAGE_KEY, String.class))
                .orElseGet(() -> StringUtils.EMPTY);
		toolTipTxt = Optional.ofNullable(vm.get(TOOL_TIP_KEY, String.class))
                .orElseGet(() -> StringUtils.EMPTY);
		LOGGER.debug("CustomTabItem imageURL is  {}", imageURL);
	}
        
	private String imageURL;
	
	private String toolTipTxt;

	public String getImageURL() {
		return imageURL;
	}

	public String getToolTipTxt() {
		return toolTipTxt;
	}
}
