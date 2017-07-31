package org.alfresco.plugin.digitalSigning.webscript;

import java.io.Serializable;
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;

/**
 * Save alias chosen Web Script.
 * 
 * @author Emmanuel ROUX
 */
public class ChooseAlias extends SigningWebScript  {
	/**
	 * Logger.
	 */
	private final Log log = LogFactory.getLog(ChooseAlias.class);
	
	
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
							if (log.isDebugEnabled()) {
								log.debug("Retrieve parameters");
							}
							
							String password = null;
							String alias = null;
							
							final Object formReq = req.parseContent();
							if (formReq != null && formReq instanceof FormData) {
								final FormData formData = (FormData) formReq;
								final FormField[] formFields = formData.getFields();
								for (int i = 0; i < formFields.length; i++) {
									final FormField field = formFields[i];
									if (field != null) {
										if ("password".equals(field.getName().toLowerCase())) {
											password = field.getValue();
										}
										if ("alias".equals(field.getName().toLowerCase())) {
											alias = field.getValue();
										}
									}
								}
							} else {
								throw new WebScriptException("Unable to parse form datas.");
							}

							// Verification des parametres
							if (StringUtils.isBlank(password)) {
								throw new WebScriptException("Parameter 'password' is required.");
							}
							if (StringUtils.isBlank(alias)) {
								throw new WebScriptException("Parameter 'alias' is required.");
							}
							
							
							
							// Get current user key
							final String currentUser = authenticationService.getCurrentUserName();
							final NodeRef currentUserNodeRef = personService.getPerson(currentUser);
							NodeRef keyNodeRef = null;
							if (currentUserNodeRef != null) {
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
												log.error("No key file uploaded for user " + currentUser + ".");
												throw new WebScriptException("No key file uploaded for user " + currentUser + ".");
											} else {
												final Serializable encryptedPropertyValue = nodeService.getProperty(keyNodeRef, SigningModel.PROP_KEYCRYPTSECRET);
												final Serializable decryptedPropertyValue = metadataEncryptor.decrypt(SigningModel.PROP_KEYCRYPTSECRET, encryptedPropertyValue);
												
												final KeyInfoDTO keyInfoDTO = getKeyInformation(keyNodeRef, alias, (String) nodeService.getProperty(keyNodeRef, SigningModel.PROP_KEYTYPE), password, (String) nodeService.getProperty(keyNodeRef, SigningModel.PROP_KEYALERT), decryptedPropertyValue.toString());
												if (keyInfoDTO.getError() != null) {
													throw new WebScriptException(keyInfoDTO.getError());
												}
												keyInfoDTO.setHasAlerted(false);
												
												nodeService.setProperty(keyNodeRef, SigningModel.PROP_KEYALIAS, alias);
												nodeService.setProperty(keyNodeRef, SigningModel.PROP_KEYALGORITHM, keyInfoDTO.getAlgorithm());
												nodeService.setProperty(keyNodeRef, SigningModel.PROP_KEYFIRSTVALIDITY, keyInfoDTO.getFirstDayValidity());
												nodeService.setProperty(keyNodeRef, SigningModel.PROP_KEYLASTVALIDITY, keyInfoDTO.getLastDayValidity());
												nodeService.setProperty(keyNodeRef, SigningModel.PROP_KEYSUBJECT, keyInfoDTO.getSubject());
												nodeService.setProperty(keyNodeRef, SigningModel.PROP_KEYHASALERT, keyInfoDTO.getHasAlerted());
												
												if (keyInfoDTO.getExpire() != null && Integer.parseInt(keyInfoDTO.getExpire()) >= 100) {
													keyInfoDTO.setExpire(null);
												}
												
												model.put("signingKey", keyNodeRef);
												model.put("keyInfos", keyInfoDTO);
												model.put("hasImage", foundImage);
											}
										} else {
											log.error("No key file uploaded for user " + currentUser + ".");
											throw new WebScriptException("No key file uploaded for user " + currentUser + ".");
										}
									} else {
										log.error("No key file uploaded for user " + currentUser + ".");
										throw new WebScriptException("No key file uploaded for user " + currentUser + ".");
									}
								} else {
									log.error("User '" + currentUser + "' have no home folder.");
									throw new WebScriptException("User '" + currentUser + "' have no home folder.");
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
