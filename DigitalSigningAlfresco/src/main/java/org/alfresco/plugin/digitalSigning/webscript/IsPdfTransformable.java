/**
 * 
 */
package org.alfresco.plugin.digitalSigning.webscript;

import java.io.IOException;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.ContentTransformerRegistry;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Verify if nodeRef is transformable in PDF or not.
 * 
 * @author Emmanuel ROUX
 */
public class IsPdfTransformable extends AbstractWebScript {
	
	/**
	 * Logger.
	 */
	private final Log log = LogFactory.getLog(IsPdfTransformable.class);
	
	/**
	 * RetryingTransactionHelper bean.
	 */
	private RetryingTransactionHelper retryingTransactionHelper;
	
	/**
	 * Content Transformer registry
	 */
	private ContentTransformerRegistry contentTransformerRegistry;
	
	/**
	 * Node service.
	 */
	private NodeService nodeService;
	
	/**
	 * Dictionary service.
	 */
	private DictionaryService dictionaryService;
	
	/**
	 * Content service.
	 */
	private ContentService contentService;
	
	/**
	 * WebScript execution method.
	 * @param req request
	 * @param res response
	 */
	public void execute(final WebScriptRequest req, final WebScriptResponse res) throws IOException {
		final RetryingTransactionCallback<Object> processCallBack = new RetryingTransactionCallback<Object>() {
			public Object execute() throws Throwable {
			String result = "KO";
			
			final String sNodeRef = req.getParameter("noderef");
			if (sNodeRef == null) {
				log.error("'noderef' parameter is required.");
				throw new WebScriptException("'noderef' parameter is required.");
			}
			
			final NodeRef nodeRef = new NodeRef(sNodeRef);
			if (nodeRef != null) {
		        final QName typeQName = nodeService.getType(nodeRef);
		        if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT) == false) {
		            // it is not content, so can't transform
		            return null;
		        }
		        // Get the content reader
		        ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
		        if (contentReader != null) {
		        	final ContentTransformer transformer = contentTransformerRegistry.getTransformer(contentReader.getMimetype(), contentReader.getSize(), MimetypeMap.MIMETYPE_PDF, new TransformationOptions());
		        	if (transformer != null) {
		        		result = "OK";
		        	}
		        } else {
		        	log.error("Unable to get document content.");
		        	throw new WebScriptException("Unable to get document content.");
		        }
			}
			res.getWriter().write(result);
			
			return null;
			}
		};
		
		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {

			public Object doWork() throws Exception {
				retryingTransactionHelper.doInTransaction(processCallBack,
						false, false);
				return null;
			}
		}, AuthenticationUtil.getSystemUserName());
	}

	/**
	 * @param retryingTransactionHelper the retryingTransactionHelper to set
	 */
	public final void setRetryingTransactionHelper(
			RetryingTransactionHelper retryingTransactionHelper) {
		this.retryingTransactionHelper = retryingTransactionHelper;
	}

	/**
	 * @param contentTransformerRegistry the contentTransformerRegistry to set
	 */
	public final void setContentTransformerRegistry(
			ContentTransformerRegistry contentTransformerRegistry) {
		this.contentTransformerRegistry = contentTransformerRegistry;
	}

	/**
	 * @param nodeService the nodeService to set
	 */
	public final void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * @param dictionaryService the dictionaryService to set
	 */
	public final void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * @param contentService the contentService to set
	 */
	public final void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

}
