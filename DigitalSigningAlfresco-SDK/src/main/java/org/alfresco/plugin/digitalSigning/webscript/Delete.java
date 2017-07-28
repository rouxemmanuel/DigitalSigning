package org.alfresco.plugin.digitalSigning.webscript;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.plugin.digitalSigning.model.SigningConstants;
import org.alfresco.plugin.digitalSigning.model.SigningModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Delete key and image Web Script.
 * 
 * @author Emmanuel ROUX
 */
public class Delete extends SigningWebScript  {
	/**
	 * Logger.
	 */
	private final Log log = LogFactory.getLog(Delete.class);
	
	
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
	 * Process.
	 * 
	 * @param req request
	 * @param status status
	 * @param cache cache
	 * 
	 * @return model
	 */
	protected final Map<String, Object> executeImpl(final WebScriptRequest req, final Status status, final Cache cache) {
		final String currentUser = authenticationService.getCurrentUserName();

		final RetryingTransactionCallback<Map<String, Object>> processCallBack = new RetryingTransactionCallback<Map<String, Object>>() {
			public Map<String, Object> execute() throws Throwable {
				final Map<String, Object> model = new HashMap<String, Object>();
				try {
					// Get current user key and image
					final String currentUser = authenticationService.getCurrentUserName();
					final NodeRef currentUserNodeRef = personService.getPerson(currentUser);
					NodeRef keyNodeRef = null;
					NodeRef imageNodeRef = null;
					if (currentUserNodeRef != null) {
						final NodeRef currentUserHomeFolder = (NodeRef) nodeService.getProperty(currentUserNodeRef, ContentModel.PROP_HOMEFOLDER);
						if (currentUserHomeFolder != null) {
							final NodeRef signingFolderNodeRef = nodeService.getChildByName(currentUserHomeFolder, ContentModel.ASSOC_CONTAINS, SigningConstants.KEY_FOLDER);
							if (signingFolderNodeRef != null) {
								final List<ChildAssociationRef> children = nodeService.getChildAssocs(signingFolderNodeRef);
								if (children != null && children.size() > 0) {
									final Iterator<ChildAssociationRef> itChildren = children.iterator();
									while (itChildren.hasNext()) {
										final ChildAssociationRef childAssoc = itChildren.next();
										final NodeRef child = childAssoc.getChildRef();
										if (nodeService.hasAspect(child, SigningModel.ASPECT_KEY)) {
											keyNodeRef = child;
										}
										if (nodeService.hasAspect(child, SigningModel.ASPECT_IMAGE)) {
											imageNodeRef = child;
										}
									}
									if (keyNodeRef != null) {
										nodeService.deleteNode(keyNodeRef);
									}
									if (imageNodeRef != null) {
										nodeService.deleteNode(imageNodeRef);
									}
								} else {
									log.error("No key file uploaded for user " + currentUser + ".");
								}
							} else {
								log.error("No key file uploaded for user " + currentUser + ".");
							}
						} else {
							log.error("User '" + currentUser + "' have no home folder.");
						}
					}
				} catch (final WebScriptException e) {
					log.error(e.getMessage(), e);
					model.put("errorNumber", "2");
					model.put("errorMessage", e.getMessage());

				} catch (final Exception e) {
					log.error(e.getMessage(), e);
					model.put("errorNumber", "2");
					if (e.getCause() != null) {
						model.put("errorMessage", e.getMessage());
					}
				}

				return model;

			}
		};

		return AuthenticationUtil.runAs(
				new AuthenticationUtil.RunAsWork<Map<String, Object>>() {
					public Map<String, Object> doWork() throws Exception {
						return retryingTransactionHelper.doInTransaction(processCallBack, true, false);
					}
				}, currentUser);
	}


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
}
