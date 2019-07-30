/**
 * 
 */
package org.alfresco.plugin.digitalSigning.webscript;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.plugin.digitalSigning.dto.KeyInfoDTO;
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
 * Signature informations WebScript.
 * 
 * @author Emmanuel ROUX
 */
public class SignatureInformation extends SigningWebScript {

	/**
	 * Logger.
	 */
	private final Log log = LogFactory.getLog(SignatureInformation.class);
	
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
	protected final Map<String, Object> executeImpl(final WebScriptRequest req,
			final Status status, final Cache cache) {
				final String currentUser = authenticationService.getCurrentUserName();

				final RetryingTransactionCallback<Map<String, Object>> processCallBack = new RetryingTransactionCallback<Map<String, Object>>() {
					public Map<String, Object> execute() throws Throwable {
						final Map<String, Object> model = new HashMap<String, Object>();
						try {
							final NodeRef currentUserNodeRef = personService.getPerson(currentUser);
							if (currentUserNodeRef != null) {
								NodeRef keyNodeRef = null;
								
								final NodeRef currentUserHomeFolder = (NodeRef) nodeService.getProperty(currentUserNodeRef, ContentModel.PROP_HOMEFOLDER);
								if (currentUserHomeFolder != null) {
									final NodeRef signingFolderNodeRef = nodeService.getChildByName(currentUserHomeFolder, ContentModel.ASSOC_CONTAINS, SigningConstants.KEY_FOLDER);
									if (signingFolderNodeRef != null) {
										final List<ChildAssociationRef> children = nodeService.getChildAssocs(signingFolderNodeRef);
										if (children != null && children.size() > 0) {
											final Iterator<ChildAssociationRef> itChildren = children.iterator();
											boolean foundKey = false;
											boolean foundImage = false;
											while (itChildren.hasNext()) {
												final ChildAssociationRef childAssoc = itChildren.next();
												final NodeRef child = childAssoc.getChildRef();
												if (nodeService.hasAspect(child, SigningModel.ASPECT_KEY)) {
													keyNodeRef = child;
													foundKey = true;
												}
												if (nodeService.hasAspect(child, SigningModel.ASPECT_IMAGE)) {
													foundImage = true;
												}
											}
											if (!foundKey) {
												model.put("errorNumber", "1");
											} else {
												
												final String alias = (String) nodeService.getProperty(keyNodeRef, SigningModel.PROP_KEYALIAS);
												if (alias == null || "".equals(alias)) {
													log.error("No alias defined for certificate. Please add a key again.");
													model.put("errorNumber", "3");
												} else {
													final KeyInfoDTO keyInfoDTO = new KeyInfoDTO();
													keyInfoDTO.setAlgorithm((String) nodeService.getProperty(keyNodeRef, SigningModel.PROP_KEYALGORITHM));
													keyInfoDTO.setAlias(alias);
													keyInfoDTO.setFirstDayValidity((Date) nodeService.getProperty(keyNodeRef, SigningModel.PROP_KEYFIRSTVALIDITY));
													keyInfoDTO.setLastDayValidity((Date) nodeService.getProperty(keyNodeRef, SigningModel.PROP_KEYLASTVALIDITY));
													keyInfoDTO.setSubject((String) nodeService.getProperty(keyNodeRef, SigningModel.PROP_KEYSUBJECT));
													keyInfoDTO.setType((String) nodeService.getProperty(keyNodeRef, SigningModel.PROP_KEYTYPE));
													keyInfoDTO.setAlert((String) nodeService.getProperty(keyNodeRef, SigningModel.PROP_KEYALERT));
													
													final Date now = new Date();
													if (keyInfoDTO.getLastDayValidity() != null) {
														long diff = keyInfoDTO.getLastDayValidity().getTime() - now.getTime();
														long diffDays = diff / (24 * 60 * 60 * 1000);
														if (diffDays < 0) {
															keyInfoDTO.setHasExpired(true);
														} else {
															keyInfoDTO.setHasExpired(false);
															if (diffDays < 100) {
																keyInfoDTO.setExpire(Long.toString(diffDays));
															} else {
																keyInfoDTO.setExpire(null);
															}
														}
													}
													
													model.put("keyInfos", keyInfoDTO);
												}
											}
											if (foundImage) {
												model.put("hasImage", true);
											} else {
												model.put("hasImage", false);
											}
										} else {
											log.error("No key file uploaded for user " + currentUser + ".");
											model.put("errorNumber", "1");
										}
									} else {
										log.error("No key file uploaded for user " + currentUser + ".");
										model.put("errorNumber", "1");
									}
								} else {
									log.error("User '" + currentUser + "' have no home folder.");
									model.put("errorNumber", "1");
								}
							} else {
								log.error("Unable to get current user.");
								model.put("errorNumber", "1");
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
