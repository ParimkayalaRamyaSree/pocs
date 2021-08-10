import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.metadata.MetaDataMap;

/**
 * Class for Workflow related common utility methods
 * @author parramya1
 *
 */
public final class WorkflowUtils {
	
	private WorkflowUtils() {
	}

	/**
	 * Gets the resource resolver.
	 * @param resolverFactory 
	 * @param workflowSession
	 * @return
	 * @throws LoginException
	 */
	public static ResourceResolver getResourceResolver(ResourceResolverFactory resolverFactory,WorkflowSession workflowSession) throws LoginException {
		Session jcrSession = workflowSession.getSession();
		Map<String, Object> authMap = new HashMap<String, Object>();
		authMap.put("user.jcr.session", jcrSession);
		return resolverFactory.getResourceResolver(authMap);
	}
	
	/**
	 * Method to fetch the arguments from WF and create a map using those values.
	 * @param metaDataMap the meta data map
	 * @return the process args map
	 */
	public static Map<String, String> getProcessArgsMap(MetaDataMap metaDataMap) {
		final Map<String, String> map = new LinkedHashMap<String, String>();
		final String processArgs = metaDataMap.get(WorkflowConstants.PROCESS_ARGS, "");
		final String[] lines = StringUtils.split(processArgs, ",\n");

		for (final String line : lines) {
			final String[] entry = StringUtils.split(line, "=");
			if (entry.length == 2) {
				map.put(StringUtils.trim(entry[0]), StringUtils.trim(entry[1]));
			}
		}
		return map;
	}
}