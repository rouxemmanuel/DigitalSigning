/**
 * 
 */
package org.alfresco.plugin.digitalSigning.action.evaluator;

import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.Response;

/**
 * Is PDF transformable evaluator.
 * 
 * @author Emmanuel ROUX
 */
public class IsPdfTransformable extends BaseEvaluator {

	@Override
	public boolean evaluate(JSONObject jsonObject) {
    	try {
			final String sNodeRef = (String) jsonObject.get("nodeRef");
			final String uri = "/api/digitalSigning/isPdfTransformable?noderef=" + sNodeRef;

			final RequestContext context = ThreadLocalRequestContext.getRequestContext();
			final Connector connector = context.getServiceRegistry().getConnectorService().getConnector("alfresco", context.getUserId(), ServletUtil.getSession());
			final Response res = connector.call(uri);
			final String alfrescoWebScriptResponse = res.getResponse();
			
			if ("OK".compareTo(alfrescoWebScriptResponse) == 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			return false;
		}
	}

}
