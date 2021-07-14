package com.adobe.aem.guides.wknd.core.models;

import com.day.cq.wcm.api.components.ComponentManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.day.cq.wcm.api.components.Component;

/**
 * @author parramya1
 */
@Model(adaptables = { SlingHttpServletRequest.class,
        Resource.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CustomTabEditor {

	//TO DO - Post Construct
	
    @Self
    private SlingHttpServletRequest request;

    private Resource container;

    private List<CustomTabItem> items;

    private void readChildren() {
        items = new ArrayList<>();
        String containerPath = request.getRequestPathInfo().getSuffix();
        if (StringUtils.isNotEmpty(containerPath)) {
            ResourceResolver resolver = request.getResourceResolver();
            container = resolver.getResource(containerPath);
            if (container != null) {
                ComponentManager componentManager = request.getResourceResolver().adaptTo(ComponentManager.class);
                if (componentManager != null){
                    for (Resource resource : container.getChildren()) {
                        if (resource != null) {
                            Component component = componentManager.getComponentOfResource(resource);
                            if (component != null) {
                                items.add(new CustomTabItem(request, resource));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Retrieves the child items associated with this children editor.
     *
     * @return a list of child items
     */
    public List<CustomTabItem> getItems() {
    	readChildren();
        return Collections.unmodifiableList(items);
    }

    /**
     * Retrieves the container resource associated with this children editor.
     *
     * @return the container resource, or {@code null} if no container can be found
     */
    public Resource getContainer() {
        return container;
    }
}
