/**
 * 
 */
package org.alfresco.plugin.digitalSigning.webscript;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import org.alfresco.plugin.digitalSigning.model.SigningConstants;
import org.alfresco.plugin.digitalSigning.model.SigningModel;

/**
 * Get image Web Script.
 * 
 * @author Emmanuel ROUX
 */
public class Image extends AbstractWebScript {

	/**
	 * Logger.
	 */
	private final Log log = LogFactory.getLog(Image.class);
	
	
	/**
	 * Authentication service.
	 */
	private AuthenticationService authenticationService;
	
	/**
	 * RetryingTransactionHelper.
	 */
	private RetryingTransactionHelper retryingTransactionHelper;
	
	/**
	 * Person service.
	 */
	private PersonService personService;
	
	/**
	 * Node service.
	 */
	private NodeService nodeService;
	
	/**
	 * Content service.
	 */
	private ContentService contentService;
	
	/**
	 * @param authenticationService the authenticationService to set
	 */
	public final void setAuthenticationService(
			AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}


	/**
	 * @param retryingTransactionHelper the retryingTransactionHelper to set
	 */
	public final void setRetryingTransactionHelper(
			RetryingTransactionHelper retryingTransactionHelper) {
		this.retryingTransactionHelper = retryingTransactionHelper;
	}


	/**
	 * @param personService the personService to set
	 */
	public final void setPersonService(PersonService personService) {
		this.personService = personService;
	}


	public void execute(final WebScriptRequest req, final WebScriptResponse res)
			throws IOException {
		final String currentUser = authenticationService.getCurrentUserName();

		final RetryingTransactionCallback<Void> processCallBack = new RetryingTransactionCallback<Void>() {
			public Void execute() throws Throwable {
				
				final NodeRef currentUserNodeRef = personService.getPerson(currentUser);
				if (currentUserNodeRef != null) {
					final NodeRef currentUserHomeFolder = (NodeRef) nodeService.getProperty(currentUserNodeRef, ContentModel.PROP_HOMEFOLDER);
					if (currentUserHomeFolder != null) {
						final NodeRef signingFolderNodeRef = nodeService.getChildByName(currentUserHomeFolder, ContentModel.ASSOC_CONTAINS, SigningConstants.KEY_FOLDER);
						if (signingFolderNodeRef != null) {
							final List<ChildAssociationRef> children = nodeService.getChildAssocs(signingFolderNodeRef);
							if (children != null && children.size() > 0) {
								final Iterator<ChildAssociationRef> itChildren = children.iterator();
								NodeRef imageNodeRef = null;
								boolean foundImage = false;
								while (itChildren.hasNext() && !foundImage) {
									final ChildAssociationRef childAssoc = itChildren.next();
									final NodeRef child = childAssoc.getChildRef();
									if (nodeService.hasAspect(child, SigningModel.ASPECT_IMAGE)) {
										imageNodeRef = child;
										foundImage = true;
									}
								}
								
								if (imageNodeRef != null) {
									
									final Date lastModifiedDate = (Date) nodeService.getProperty(imageNodeRef, ContentModel.PROP_MODIFIED);
									
									ContentReader reader = contentService.getReader(imageNodeRef, ContentModel.PROP_CONTENT);
									if (reader != null) {
										String mimetype = reader.getMimetype();
										res.setContentType(mimetype);
								        res.setContentEncoding(reader.getEncoding());
								        res.setHeader("Content-Length", Long.toString(reader.getSize()));
										
								        // set caching
								        Cache cache = new Cache();
								        cache.setNeverCache(false);
								        cache.setMustRevalidate(true);
								        cache.setMaxAge(0L);
								        cache.setLastModified(lastModifiedDate);
								        cache.setETag(String.valueOf(lastModifiedDate.getTime()));
								        res.setCache(cache);
								        
								        reader.getContent(res.getOutputStream());
									} else {
										log.error("Unable to get image content.");
									}
								}
							} else {
								log.error("No image file uploaded for user " + currentUser + ".");
							}
						} else {
							log.error("No image file uploaded for user " + currentUser + ".");
						}
					} else {
						log.error("User '" + currentUser + "' have no home folder.");
					}
				} else {
					log.error("Unable to get current user.");
				}
				
				return null;
			}
		};

		AuthenticationUtil.runAs(
				new AuthenticationUtil.RunAsWork<Void>() {
					public Void doWork() throws Exception {
						retryingTransactionHelper.doInTransaction(processCallBack, true, false);
						return null;
					}
				}, currentUser);
		
	}


	/**
	 * @param nodeService the nodeService to set
	 */
	public final void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}


	/**
	 * @param contentService the contentService to set
	 */
	public final void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

}
