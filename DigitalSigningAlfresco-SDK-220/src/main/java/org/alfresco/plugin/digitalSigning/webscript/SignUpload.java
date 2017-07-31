/**
 * 
 */
package org.alfresco.plugin.digitalSigning.webscript;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.security.KeyStore;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.plugin.digitalSigning.model.SigningConstants;
import org.alfresco.plugin.digitalSigning.model.SigningModel;
import org.alfresco.plugin.digitalSigning.utils.CryptUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.apache.commons.io.IOUtils;
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
 * Sign upload Web Script.
 * 
 * @author Emmanuel ROUX
 */
public class SignUpload extends SigningWebScript {

	/**
	 * Logger.
	 */
	private final Log log = LogFactory.getLog(SignUpload.class);
	
	
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
	 * File folder service.
	 */
	private FileFolderService fileFolderService;
	
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
							
							String keyFilename = null;
							InputStream keyContent1 = null;
							InputStream keyContent2 = null;
							String keyMimetype = null;
							String keyType = null;
							String imageFilename = null;
							InputStream imageContent = null;
							String imageMimetype = null;
							String password = null;
							//String alias = null;
							String alert = null;
							
							final Object formReq = req.parseContent();
							if (formReq != null && formReq instanceof FormData) {
								final FormData formData = (FormData) formReq;
								final FormField[] formFields = formData.getFields();
								for (int i = 0; i < formFields.length; i++) {
									final FormField field = formFields[i];
									if (field != null) {
										if ("key".equals(field.getName().toLowerCase())	&& field.getIsFile()) {
											byte[] byteArray = IOUtils.toByteArray(field.getInputStream());     
											keyContent1 = new ByteArrayInputStream(byteArray); 
											keyContent2 = new ByteArrayInputStream(byteArray);
											keyFilename = field.getFilename();
											keyMimetype = field.getMimetype();
										}
										if ("image".equals(field.getName().toLowerCase())	&& field.getIsFile()) {
											imageContent = field.getInputStream();
											imageFilename = field.getFilename();
											imageMimetype = field.getMimetype();
										}
										if ("keytype".equals(field.getName().toLowerCase())) {
											keyType = field.getValue();
										}
										if ("password".equals(field.getName().toLowerCase())) {
											password = field.getValue();
										}
										//if ("alias".equals(field.getName().toLowerCase())) {
										//	alias = field.getValue();
										//}
										if ("alert".equals(field.getName().toLowerCase())) {
											alert = field.getValue();
										}
									}
								}
							} else {
								throw new WebScriptException("Unable to parse form datas.");
							}

							// Verification des parametres
							if (StringUtils.isBlank(keyFilename) || keyContent1 == null || keyContent2 == null ) {
								throw new WebScriptException("Parameter 'key' is required.");
							}
							if (StringUtils.isBlank(keyType)) {
								throw new WebScriptException("Parameter 'keyType' is required.");
							}
							if (StringUtils.isBlank(password)) {
								throw new WebScriptException("Parameter 'password' is required.");
							}
							//if (StringUtils.isBlank(alias)) {
							//	throw new WebScriptException("Parameter 'alias' is required.");
							//}
							if (StringUtils.isBlank(alert)) {
								throw new WebScriptException("Parameter 'alert' is required.");
							}
							
							final NodeRef currentUserNodeRef = personService.getPerson(currentUser);
							if (currentUserNodeRef != null) {
								final NodeRef currentUserHomeFolder = (NodeRef) nodeService.getProperty(currentUserNodeRef, ContentModel.PROP_HOMEFOLDER);
								if (currentUserHomeFolder != null) {
									NodeRef signingFolderNodeRef = nodeService.getChildByName(currentUserHomeFolder, ContentModel.ASSOC_CONTAINS, SigningConstants.KEY_FOLDER);
									if (signingFolderNodeRef == null) {
										// Create the folder
										signingFolderNodeRef = fileFolderService.create(currentUserHomeFolder, SigningConstants.KEY_FOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
									}
									
									final List<ChildAssociationRef> children = nodeService.getChildAssocs(signingFolderNodeRef);
									final Iterator<ChildAssociationRef> itChildren = children.iterator();
									NodeRef keyNodeRef = null;
									NodeRef imageNodeRef = null;
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
									
									if (keyNodeRef == null) {
										// Create new key file
										final NodeRef keyFile = fileFolderService.searchSimple(signingFolderNodeRef, keyFilename); 
										if (keyFile != null) {
											fileFolderService.delete(keyFile);
										}
										final FileInfo fileInfo = fileFolderService.create(signingFolderNodeRef, keyFilename, ContentModel.TYPE_CONTENT);
										keyNodeRef = fileInfo.getNodeRef();
									}
									
									nodeService.setProperty(keyNodeRef, ContentModel.PROP_NAME, keyFilename);
									final ContentWriter keyContentWriter = contentService.getWriter(keyNodeRef, ContentModel.PROP_CONTENT, true);
									if (keyContentWriter != null) {
										
										// Generate random crypt secret key and encrypt it
										final String secret = currentUser + "_" + CryptUtils.getRamdomInt() + "_" + System.currentTimeMillis();
										
										// Encrypt secret key with Alfresco mechanism
										Map<QName, Serializable> encryptedProperties = new PropertyMap();
										encryptedProperties.put(SigningModel.PROP_KEYCRYPTSECRET, secret);
										encryptedProperties = metadataEncryptor.encrypt(encryptedProperties);
										
										// Crypt key content
										final InputStream encryptedKeyContent = CryptUtils.encrypt(secret, keyContent1);
										
										keyContentWriter.setMimetype(keyMimetype);
										keyContentWriter.putContent(encryptedKeyContent);
										
										// Add aspect and properties on key file
										if (!nodeService.hasAspect(keyNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
											nodeService.addAspect(keyNodeRef, ContentModel.ASPECT_VERSIONABLE, null);
										}
										if (nodeService.hasAspect(keyNodeRef, SigningModel.ASPECT_KEY)) {
											nodeService.removeAspect(keyNodeRef, SigningModel.ASPECT_KEY);
										}
										
										//final KeyInfoDTO keyInfoDTO = getKeyInformation(keyNodeRef, alias, keyType, password, alert, secret);
										//if (keyInfoDTO.getError() != null) {
										//	throw new WebScriptException(keyInfoDTO.getError());
										//}
										//keyInfoDTO.setHasAlerted(false);
										
										final Map<QName, Serializable> keyProperties = new HashMap<QName, Serializable>();
										keyProperties.put(SigningModel.PROP_KEYCRYPTSECRET, encryptedProperties.get(SigningModel.PROP_KEYCRYPTSECRET));
										//keyProperties.put(SigningModel.PROP_KEYALIAS, alias);
										keyProperties.put(SigningModel.PROP_KEYTYPE, keyType);
										//keyProperties.put(SigningModel.PROP_KEYALGORITHM, keyInfoDTO.getAlgorithm());
										//keyProperties.put(SigningModel.PROP_KEYFIRSTVALIDITY, keyInfoDTO.getFirstDayValidity());
										//keyProperties.put(SigningModel.PROP_KEYLASTVALIDITY, keyInfoDTO.getLastDayValidity());
										//keyProperties.put(SigningModel.PROP_KEYSUBJECT, keyInfoDTO.getSubject());
										keyProperties.put(SigningModel.PROP_KEYALERT, alert);
										//keyProperties.put(SigningModel.PROP_KEYHASALERT, keyInfoDTO.getHasAlerted());
										
										nodeService.addAspect(keyNodeRef, SigningModel.ASPECT_KEY, keyProperties);
										
										//if (keyInfoDTO.getExpire() != null && Integer.parseInt(keyInfoDTO.getExpire()) >= 100) {
										//	keyInfoDTO.setExpire(null);
										//}
										//model.put("signingKey", keyNodeRef);
										
										
										String aliasList = "";
										final KeyStore ks = KeyStore.getInstance("pkcs12");
										ks.load(keyContent2, password.toCharArray());
										final Enumeration<String> aliases = ks.aliases();
										while(aliases.hasMoreElements()) {
											final String alias = aliases.nextElement();
											aliasList += alias + ";";
										}
										aliasList = aliasList.substring(0, aliasList.length()-1);
										model.put("aliasList", aliasList);

										
										
										if (imageContent != null && imageFilename != null && imageFilename.compareTo("") != 0 && imageMimetype != null) {
											if (imageNodeRef == null) {
												// Create new image file
												final NodeRef imageFile = fileFolderService.searchSimple(signingFolderNodeRef, imageFilename); 
												if (imageFile != null) {
													fileFolderService.delete(imageFile);
												}
												final FileInfo fileInfo = fileFolderService.create(signingFolderNodeRef, imageFilename, ContentModel.TYPE_CONTENT);
												imageNodeRef = fileInfo.getNodeRef();
											}
											
											nodeService.setProperty(imageNodeRef, ContentModel.PROP_NAME, imageFilename);
											final ContentWriter imageContentWriter = contentService.getWriter(imageNodeRef, ContentModel.PROP_CONTENT, true);
											if (imageContentWriter != null) {
												imageContentWriter.setMimetype(imageMimetype);
												imageContentWriter.putContent(imageContent);
												
												// Add aspect and properties on key file
												if (!nodeService.hasAspect(imageNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
													nodeService.addAspect(imageNodeRef, ContentModel.ASPECT_VERSIONABLE, null);
												}
												if (!nodeService.hasAspect(imageNodeRef, SigningModel.ASPECT_IMAGE)) {
													nodeService.addAspect(imageNodeRef, SigningModel.ASPECT_IMAGE, null);
												}
												
												model.put("hasImage", true);
											} else {
												throw new WebScriptException("Unable to get image content.");
											}
										} else {
											if (imageNodeRef != null) {
												nodeService.deleteNode(imageNodeRef);
											}
											model.put("hasImage", false);
										}
									} else {
										throw new WebScriptException("Unable to get key content.");
									}
								} else{
									throw new WebScriptException("User '" + currentUser + "' have no home folder.");
								}
							} else {
								throw new WebScriptException("Unable to get current user.");
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


	/**
	 * @param fileFolderService the fileFolderService to set
	 */
	public final void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

}
