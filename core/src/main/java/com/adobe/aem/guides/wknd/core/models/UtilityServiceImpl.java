package com.adobe.aem.guides.wknd.core.models;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = UtilityService.class, property = { Constants.SERVICE_DESCRIPTION + "=Utility Service"})
public class UtilityServiceImpl implements UtilityService {

	private static final Logger LOG = LoggerFactory.getLogger(UtilityServiceImpl.class);

	@Reference(cardinality=ReferenceCardinality.MANDATORY,policy = ReferencePolicy.STATIC)
	private ResourceResolverFactory resolverFactory;	
	
	private ResourceResolver resourceResolver;

	@Override
	public ResourceResolver getResourceResolver() {
		try {
			Map<String, Object> param = new HashMap<String, Object>();	
			param.put(ResourceResolverFactory.SUBSERVICE, "serviceuser");
			LOG.debug("resolverFactory is {}",resolverFactory);
			resourceResolver = resolverFactory.getServiceResourceResolver(param);
			LOG.debug("resourceResolver is {}",resourceResolver);	       
		} catch (LoginException e) {
			LOG.error("Exception in getServiceResourceResolver {}",e);
		}
		return resourceResolver;
	}
}
