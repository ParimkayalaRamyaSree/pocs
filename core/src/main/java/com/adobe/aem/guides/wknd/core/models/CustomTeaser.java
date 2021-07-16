package com.adobe.aem.guides.wknd.core.models;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.wcm.core.components.models.Component;
import com.adobe.cq.wcm.core.components.models.ListItem;
import com.adobe.cq.wcm.core.components.models.Teaser;

import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Via;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.via.ResourceSuperType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Model(
        adaptables = {Resource.class,SlingHttpServletRequest.class},
        adapters = {Teaser.class, Component.class, ComponentExporter.class, ListItem.class},
        resourceType = "wknd/components/teaser",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@Slf4j
public class CustomTeaser implements Teaser {

 	private static final Logger LOGGER = LoggerFactory.getLogger(CustomTeaser.class);
	
    @Self
    @Via(type = ResourceSuperType.class)
    @Delegate (types = Teaser.class,excludes = DelegationExclusion.class)
    private Teaser delegate;
    
    @SlingObject 
    private SlingHttpServletRequest request;

    @Inject
    private List<String> ctaTargets;
    
    @Inject
    private String test;
    
    @PostConstruct
    private void init() {
    	LOGGER.debug(" hi is {}");
    	Resource teaserRes = request.getResource();
    	LOGGER.debug(" resource is {}", teaserRes);
    	ctaTargets = new LinkedList<String>();
    	Resource actions = teaserRes.getChildren().iterator().next();
		Iterable<Resource> tabItr = actions.getChildren();
		LOGGER.debug("resource name is {} ", teaserRes.getName());
		for (Resource res : tabItr) {
			ValueMap vMap = res.getValueMap();
			ctaTargets.add(vMap.get("target", String.class));
			LOGGER.debug("imgpath is {} ", vMap.get("target", String.class));
		}
    }

    public List<String> getCtaTargets() {
    	LOGGER.debug(" resource is {}", request.getResource());
    	Resource teaserRes = request.getResource();
    	LOGGER.debug(" resource is {}", teaserRes);
    	ctaTargets = new LinkedList<String>();
    	Resource actions = teaserRes.getChildren().iterator().next();
		Iterable<Resource> tabItr = actions.getChildren();
		LOGGER.debug("resource name is {} ", teaserRes.getName());
		for (Resource res : tabItr) {
			ValueMap vMap = res.getValueMap();
			ctaTargets.add(vMap.get("target", String.class));
			LOGGER.debug("imgpath is {} ", vMap.get("target", String.class));
		}
		return ctaTargets;
	}
    
    public String getTest() {
		return "Hello";
	}

	@Override
    public String getTitle() {
    	LOGGER.debug(" hi title is {}", delegate.getTitle());
    	return delegate.getTitle();
    }
    
    private interface DelegationExclusion {
        String getTitle();
    }
}