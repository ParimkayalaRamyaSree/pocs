package com.adobe.aem.guides.wknd.core.models;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;

@Model(adaptables = { Resource.class, SlingHttpServletRequest.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CustomImageModel {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CustomImageModel.class);
	
	@Inject
	private UtilityService utilService;
	
	@Inject
	private String assetPath;
		
	private String imageAltText;
	
	public String getImageAltText() {
        LOGGER.debug("assetPath is {}",assetPath);
    	ResourceResolver resourceResolver = utilService.getResourceResolver();
    	LOGGER.debug("resourceResolver is {}",resourceResolver);
        Resource res = resourceResolver.getResource(assetPath);
        LOGGER.debug("res is {}",res);
        Asset asset = res.adaptTo(Asset.class);
        String dcTitle = asset.getMetadataValue("dc:title");
        LOGGER.debug("dcTitle is {}",dcTitle);
        imageAltText = StringUtils.isNotEmpty(dcTitle) ? dcTitle :  asset.getName();	        
        LOGGER.debug("assetPath is in activate {}",assetPath);
		return imageAltText;
	}
}
