/**
 * 
 */
package org.alfresco.plugin.digitalSigning.script;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.plugin.digitalSigning.dto.DigitalSigningDTO;
import org.alfresco.plugin.digitalSigning.dto.VerifyResultDTO;
import org.alfresco.plugin.digitalSigning.dto.VerifyingDTO;
import org.alfresco.plugin.digitalSigning.model.SigningConstants;
import org.alfresco.plugin.digitalSigning.model.SigningModel;
import org.alfresco.plugin.digitalSigning.service.SigningService;
import org.alfresco.plugin.digitalSigning.utils.SigningUtils;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;

/**
 * Sign script service.
 * 
 * @author Emmanuel ROUX
 */
public class SigningServiceScript extends BaseScopableProcessorExtension {
	
	/**
	 * Logger.
	 */
	//private final Log log = LogFactory.getLog(SigningServiceScript.class);
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SigningServiceScript.class);

	/**
	 * Sign service.
	 */
	private SigningService digitalSigningService;
	
	/**
	 * Authentication service.
	 */
	private AuthenticationService authenticationService;
	
	/**
	 * Person service.
	 */
	private PersonService personService;
	
	/**
	 * Node service.
	 */
	private NodeService nodeService;
	
	
	public void signSimple(final NativeObject parameters) {
		log.info("Start SignSimple");
		final DigitalSigningDTO signingDTO = new DigitalSigningDTO();
		String keyPassword = null;
		if (parameters.get("keyPassword", null) instanceof String) {
			keyPassword = (String) parameters.get("keyPassword", null);
		}
		String filesToSignStr = null;
		if (parameters.get("document", null) instanceof String) {
			filesToSignStr = (String) parameters.get("document", null);
		}
		String destinationFolderStr = null;
		if (parameters.get("destination", null) instanceof String) {
			destinationFolderStr = (String) parameters.get("destination", null);
		}
		String reason = null;
		if (parameters.get("reason", null) instanceof String) {
			reason = (String) parameters.get("reason", null);
		}
		String location = null;
		if (parameters.get("location", null) instanceof String) {
			location = (String) parameters.get("location", null);
		}
		String contact = null;
		if (parameters.get("contact", null) instanceof String) {
			contact = (String) parameters.get("contact", null);
		}
		String imageStr = null;
		if (parameters.get("image", null) instanceof String) {
			imageStr = (String) parameters.get("image", null);
		}
		String field = null;
		if (parameters.get("field", null) instanceof String) {
			field = (String) parameters.get("field", null);
		}
				
		// Get current user key
		final String currentUser = authenticationService.getCurrentUserName();
		final NodeRef currentUserNodeRef = personService.getPerson(currentUser);
				
		signingDTO.setCurrentUser(currentUser);
		signingDTO.setKeyPassword(keyPassword);
		signingDTO.setOpt(imageStr); //TODO chiedere cosa è OPT
		signingDTO.setLocale(location);
		signingDTO.setSignReason(reason);
		signingDTO.setSigningField(field);

		//Controllo se la verifica deve essere pades o no
		
		// Get file(s) to sign
		if (filesToSignStr != null) {
			final String[] nodeRefs = filesToSignStr.split(",");
			final List<NodeRef> nodeRefsToSign = new ArrayList<NodeRef>();
			signingDTO.setFilesToSign(nodeRefsToSign);
			
			for (int i = 0; i < nodeRefs.length ; i++) {
				final String nodeRef = nodeRefs[i];
				try {
					final NodeRef fileToSign = new NodeRef(nodeRef);
					if (fileToSign != null) {
						signingDTO.getFilesToSign().add(fileToSign);
					}
				} catch (Exception e) {
					log.error("document must be a valid nodeRef.");
					throw new AlfrescoRuntimeException("document must be a valid nodeRef : " + nodeRef);
				}
			}
			log.info("End SignSimple");
		} else {
			log.error("document(s) parameter is required.");
			throw new AlfrescoRuntimeException("document parameter is required.");
		}
		try{		
			digitalSigningService.sign(signingDTO);
		}catch(Exception e){
			log.error("Can't sign the obejct: " + signingDTO.toString());
			throw e;
		}
		
	}
	
	/**
	 * Sign a document.
	 * Questo metodo è il più complesso e completo allo stesos tempo
	 * pemrette all'utente un pieno controllo della firma
	 * 
	 * @param parameters sign parameters
	 */
	public void sign(final NativeObject parameters) {
		String privateKeyStr = null;
		if (parameters.get("keyFile", null) instanceof String) {
			privateKeyStr = (String) parameters.get("keyFile", null);
		}
		String keyPassword = null;
		if (parameters.get("keyPassword", null) instanceof String) {
			keyPassword = (String) parameters.get("keyPassword", null);
		}
		String filesToSignStr = null;
		if (parameters.get("document", null) instanceof String) {
			filesToSignStr = (String) parameters.get("document", null);
		}
		String destinationFolderStr = null;
		if (parameters.get("destination", null) instanceof String) {
			destinationFolderStr = (String) parameters.get("destination", null);
		}
		String reason = null;
		if (parameters.get("reason", null) instanceof String) {
			reason = (String) parameters.get("reason", null);
		}
		String location = null;
		if (parameters.get("location", null) instanceof String) {
			location = (String) parameters.get("location", null);
		}
		String contact = null;
		if (parameters.get("contact", null) instanceof String) {
			contact = (String) parameters.get("contact", null);
		}
		String imageStr = null;
		if (parameters.get("image", null) instanceof String) {
			imageStr = (String) parameters.get("image", null);
		}
		String field = null;
		if (parameters.get("field", null) instanceof String) {
			field = (String) parameters.get("field", null);
		}
		String position = null;
		if (parameters.get("position", null) instanceof String) {
			position = (String) parameters.get("position", null);
		}
		String page = null;
		if (parameters.get("page", null) instanceof String) {
			page = (String) parameters.get("page", null);
		}
		String depth = null;
		if (parameters.get("depth", null) instanceof String) {
			depth = (String) parameters.get("depth", null);
		}
		Integer locationX = null;
		if (parameters.get("locationX", null) instanceof String && "".compareTo((String) parameters.get("locationX", null)) != 0) {
			if ((String) parameters.get("locationX", null) != null) {
				locationX = getInteger((String) parameters.get("locationX", null));
			}
		}
		Integer locationY = null;
		if (parameters.get("locationY", null) instanceof String && "".compareTo((String) parameters.get("locationY", null)) != 0) {
			if ((String) parameters.get("locationY", null) != null) {
				locationY = getInteger((String) parameters.get("locationY", null));
			}
		}
		Integer marginX = null;
		if (parameters.get("marginX", null) instanceof String && "".compareTo((String) parameters.get("marginX", null)) != 0) {
			if ((String) parameters.get("marginX", null) != null) {
				marginX = getInteger((String) parameters.get("marginX", null));
			}
		}
		Integer marginY = null;
		if (parameters.get("marginY", null) instanceof String && "".compareTo((String) parameters.get("marginY", null)) != 0) {
			if ((String) parameters.get("marginY", null) != null) {
				marginY = getInteger((String) parameters.get("marginY", null));
			}
		}
		Integer height = null;
		if (parameters.get("height", null) instanceof String && "".compareTo((String) parameters.get("height", null)) != 0) {
			if ((String) parameters.get("height", null) != null) {
				height = getInteger((String) parameters.get("height", null));
			}
		}
		Integer width = null;
		if (parameters.get("width", null) instanceof String) {
			if ((String) parameters.get("width", null) != null && "".compareTo((String) parameters.get("width", null)) != 0) {
				width = getInteger((String) parameters.get("width", null));
			}
		}
		Integer pageNumber = null;
		if (parameters.get("pageNumber", null) instanceof String) {
			if ((String) parameters.get("pageNumber", null) != null && "".compareTo((String) parameters.get("pageNumber", null)) != 0) {
				pageNumber = getInteger((String) parameters.get("pageNumber", null));
			}
		}
		
		boolean detachedSignature = false;
		if (parameters.get("detachedSignature", null) instanceof String) {
			if ((String) parameters.get("detachedSignature", null) != null && "".compareTo((String) parameters.get("detachedSignature", null)) != 0) {
				detachedSignature = Boolean.valueOf((String) parameters.get("detachedSignature", null));
			}
		}
		
		boolean transformInPdfA = false;
		if (parameters.get("transformInPdfA", null) instanceof String) {
			if ((String) parameters.get("transformInPdfA", null) != null && "".compareTo((String) parameters.get("transformInPdfA", null)) != 0) {
				transformInPdfA = Boolean.valueOf((String) parameters.get("transformInPdfA", null));
			}
		}
		
		
		final DigitalSigningDTO signingDTO = new DigitalSigningDTO();
		
		if (privateKeyStr != null) {
			try {
				final NodeRef privateKey = new NodeRef(privateKeyStr);
				if (privateKey != null) {
					signingDTO.setKeyFile(privateKey);
				}
			} catch (Exception e) {
				log.error("keyFile must be a valid nodeRef.");
				throw new AlfrescoRuntimeException("keyFile must be a valid nodeRef.");
			}
		} else {
			// Get current user key
			final String currentUser = authenticationService.getCurrentUserName();
			final NodeRef currentUserNodeRef = personService.getPerson(currentUser);
			if (currentUserNodeRef != null) {
				final NodeRef currentUserHomeFolder = (NodeRef) nodeService.getProperty(currentUserNodeRef, ContentModel.PROP_HOMEFOLDER);
				if (currentUserHomeFolder != null) {
					final NodeRef signingFolderNodeRef = nodeService.getChildByName(currentUserHomeFolder, ContentModel.ASSOC_CONTAINS, SigningConstants.KEY_FOLDER);
					if (signingFolderNodeRef != null) {
						final List<ChildAssociationRef> children = nodeService.getChildAssocs(signingFolderNodeRef);
						if (children != null && children.size() > 0) {
							final Iterator<ChildAssociationRef> itChildren = children.iterator();
							boolean foundKey = false;
							while (itChildren.hasNext() && !foundKey) {
								final ChildAssociationRef childAssoc = itChildren.next();
								final NodeRef child = childAssoc.getChildRef();
								if (nodeService.hasAspect(child, SigningModel.ASPECT_KEY)) {
									signingDTO.setKeyFile(child);
									foundKey = true;
								}
							}
							if (!foundKey) {
								String msg = "No key file uploaded for user " + currentUser + ". No foundkey for signingDTO: " + signingDTO.toString();
								throw new AlfrescoRuntimeException(msg);
							}
						} else {
							String msg = "No key file uploaded for user " + currentUser + ". No Children for signingFolderNodeRef: " + signingFolderNodeRef + " make sure to have insert some certificate";
							throw new AlfrescoRuntimeException(msg);
						}
					} else {
						String msg = "No key file uploaded for user " + currentUser + ". No valid signingFolderNodeRef is NULL, make sure to have created the folder"+ SigningConstants.KEY_FOLDER + " under " + currentUserHomeFolder;
						throw new AlfrescoRuntimeException(msg);
					}
				} else {
					String msg = "User '" + currentUser + "' have no home folder.";
					throw new AlfrescoRuntimeException(msg);
				}
			}
		}
		if (keyPassword != null) {
			signingDTO.setKeyPassword(keyPassword);
		} else {
			log.error("key-password parameter is required.");
			throw new AlfrescoRuntimeException("key-password parameter is required.");
		}
		if (destinationFolderStr != null && destinationFolderStr.compareTo("") != 0) {
			try {
				final NodeRef destinationFolder = new NodeRef(destinationFolderStr);
				if (destinationFolder != null) {
					signingDTO.setDestinationFolder(destinationFolder);
				}
			} catch (Exception e) {
				log.error("destination must be a valid nodeRef.");
				throw new AlfrescoRuntimeException("destination must be a valid nodeRef.");
			}
		} else {
			signingDTO.setDestinationFolder(null);
			//log.error("destination parameter is required.");
			//throw new AlfrescoRuntimeException("destination parameter is required.");
		}
		if (reason != null) {
			signingDTO.setSignReason(reason);
		}
		if (location != null) {
			signingDTO.setSignLocation(location);
		}
		if (contact != null) {
			signingDTO.setSignContact(contact);
		}
		if (imageStr != null) {
			try {
				final NodeRef image = new NodeRef(imageStr);
				if (image != null) {
				signingDTO.setImage(image);
				} 
			} catch (Exception e) {
				log.error("image must be a valid nodeRef.");
				throw new AlfrescoRuntimeException("image must be a valid nodeRef.");
			}
		} else {
			// Get current user image
			final String currentUser = authenticationService.getCurrentUserName();
			final NodeRef currentUserNodeRef = personService.getPerson(currentUser);
			if (currentUserNodeRef != null) {
				signingDTO.setLocale(((Locale) nodeService.getProperty(currentUserNodeRef, ContentModel.PROP_LOCALE)).toString());
				final NodeRef currentUserHomeFolder = (NodeRef) nodeService.getProperty(currentUserNodeRef, ContentModel.PROP_HOMEFOLDER);
				if (currentUserHomeFolder != null) {
					final NodeRef signingFolderNodeRef = nodeService.getChildByName(currentUserHomeFolder, ContentModel.ASSOC_CONTAINS, SigningConstants.KEY_FOLDER);
					if (signingFolderNodeRef != null) {
						final List<ChildAssociationRef> children = nodeService.getChildAssocs(signingFolderNodeRef);
						if (children != null && children.size() > 0) {
							final Iterator<ChildAssociationRef> itChildren = children.iterator();
							boolean foundImage = false;
							while (itChildren.hasNext() && !foundImage) {
								final ChildAssociationRef childAssoc = itChildren.next();
								final NodeRef child = childAssoc.getChildRef();
								if (nodeService.hasAspect(child, SigningModel.ASPECT_IMAGE)) {
									signingDTO.setImage(child);
									foundImage = true;
								}
							}
						}
					}
				}
			}
		}
		if (field != null) {
			signingDTO.setSigningField(field);
		}
		if (position != null) {
			signingDTO.setPosition(position);
		}
		if (page != null) {
			signingDTO.setPages(page);
		}
		if (depth != null) {
			signingDTO.setDepth(depth);
		}
		if (locationX != null) {
			signingDTO.setLocationX(locationX);
		}
		if (locationY != null) {
			signingDTO.setLocationY(locationY);
		}
		if (marginX != null) {
			signingDTO.setxMargin(marginX);
		}
		if (marginY != null) {
			signingDTO.setyMargin(marginY);
		}
		if (width != null) {
			signingDTO.setSignWidth(width);
		}
		if (height != null) {
			signingDTO.setSignHeight(height);
		}
		if (pageNumber != null) {
			signingDTO.setPageNumber(pageNumber);
		}
		if (detachedSignature) {
			signingDTO.setDetached(true);
		}
		if (transformInPdfA) {
			signingDTO.setTransformToPdfA(true);
		} else {
			signingDTO.setTransformToPdfA(false);
		}
				
		// Get file(s) to sign
		if (filesToSignStr != null) {
			final String[] nodeRefs = filesToSignStr.split(",");
			final List<NodeRef> nodeRefsToSign = new ArrayList<NodeRef>();
			signingDTO.setFilesToSign(nodeRefsToSign);
			
			for (int i = 0; i < nodeRefs.length ; i++) {
				final String nodeRef = nodeRefs[i];
				try {
					final NodeRef fileToSign = new NodeRef(nodeRef);
					if (fileToSign != null) {
						signingDTO.getFilesToSign().add(fileToSign);
					}
				} catch (Exception e) {
					log.error("document must be a valid nodeRef.");
					throw new AlfrescoRuntimeException("document must be a valid nodeRef : " + nodeRef);
				}
			}
		} else {
			log.error("document(s) parameter is required.");
			throw new AlfrescoRuntimeException("document parameter is required.");
		}
		
		// Validate DTO
		SigningUtils.validateSignInfo(signingDTO);
		
		digitalSigningService.sign(signingDTO);
	}
	
	/**
	 * Verify sign.
	 * 
	 * @param parameters parameter
	 * @return verify result
	 */
	public Scriptable verify(final NativeObject parameters) {
		final VerifyingDTO verifyingDTO = new VerifyingDTO();
		
		String privateKeyStr = null;
		if (parameters.get("keyFile", null) instanceof String) {
			privateKeyStr = (String) parameters.get("keyFile", null);
		}
		String keyPassword = null;
		if (parameters.get("keyPassword", null) instanceof String) {
			keyPassword = (String) parameters.get("keyPassword", null);
		}
		String fileToVerifyStr = null;
		if (parameters.get("document", null) instanceof String) {
			fileToVerifyStr = (String) parameters.get("document", null);
		}
		
		if (privateKeyStr != null) {
			try {
				final NodeRef privateKey = new NodeRef(privateKeyStr);
				if (privateKey != null) {
					verifyingDTO.setKeyFile(privateKey);
				}
			} catch (Exception e) {
				log.error("keyFile must be a valid nodeRef.");
				throw new AlfrescoRuntimeException("keyFile must be a valid nodeRef.");
			}
		} else {
			// Get current user key
			final String currentUser = authenticationService.getCurrentUserName();
			final NodeRef currentUserNodeRef = personService.getPerson(currentUser);
			if (currentUserNodeRef != null) {
				final NodeRef currentUserHomeFolder = (NodeRef) nodeService.getProperty(currentUserNodeRef, ContentModel.PROP_HOMEFOLDER);
				if (currentUserHomeFolder != null) {
					final NodeRef signingFolderNodeRef = nodeService.getChildByName(currentUserHomeFolder, ContentModel.ASSOC_CONTAINS, SigningConstants.KEY_FOLDER);
					if (signingFolderNodeRef != null) {
						final List<ChildAssociationRef> children = nodeService.getChildAssocs(signingFolderNodeRef);
						if (children != null && children.size() > 0) {
							final Iterator<ChildAssociationRef> itChildren = children.iterator();
							boolean foundKey = false;
							while (itChildren.hasNext() && !foundKey) {
								final ChildAssociationRef childAssoc = itChildren.next();
								final NodeRef child = childAssoc.getChildRef();
								if (nodeService.hasAspect(child, SigningModel.ASPECT_KEY)) {
									verifyingDTO.setKeyFile(child);
									foundKey = true;
								}
							}
							if (!foundKey) {
								log.error("No key file uploaded for user " + currentUser + ".");
								log.error("No foundkey for verifyingDTO: " + verifyingDTO.toString());
								throw new AlfrescoRuntimeException("No key file uploaded for user " + currentUser + ".");
							}
						} else {
							log.error("No key file uploaded for user " + currentUser + ".");
							log.error("No Children for signingFolderNodeRef: " + signingFolderNodeRef);
							throw new AlfrescoRuntimeException("No key file uploaded for user " + currentUser + ".");
						}
					} else {
						log.error("No key file uploaded for user " + currentUser + ".");
						log.error("No valid signingFolderNodeRef is NULL, make sure to have created the folder"+ SigningConstants.KEY_FOLDER + " under " + currentUserHomeFolder);
						throw new AlfrescoRuntimeException("No key file uploaded for user " + currentUser + ".");
					}
				} else {
					log.error("User '" + currentUser + "' have no home folder.");
					throw new AlfrescoRuntimeException("User '" + currentUser + "' have no home folder.");
				}
			}
		}
		if (keyPassword != null) {
			verifyingDTO.setKeyPassword(keyPassword);
		} else {
			log.error("key-password parameter is required.");
			throw new AlfrescoRuntimeException("key-password parameter is required.");
		}
		if (fileToVerifyStr != null) {
			try {
				final NodeRef fileToVerify = new NodeRef(fileToVerifyStr);
				if (fileToVerify != null) {
					verifyingDTO.setFileToVerify(fileToVerify);
				}
			} catch (Exception e) {
				log.error("document must be a valid nodeRef.");
				throw new AlfrescoRuntimeException("document must be a valid nodeRef.");
			}
		} else {
			log.error("document parameter is required.");
			throw new AlfrescoRuntimeException("document parameter is required.");
		}
		
		final List<VerifyResultDTO> result = digitalSigningService.verifySign(verifyingDTO);
		
		return Context.getCurrentContext().newArray(getScope(), result.toArray());
	}

	/**
	 * Get int value form serialized object.
	 * 
	 * @param val serialized object
	 * @return int value of serialized object
	 */
	protected int getInteger(Serializable val) {
        if(val == null) { 
        	return 0;
        }
        try {
        	return Integer.parseInt(val.toString());
        } catch(NumberFormatException nfe) {
        	return 0;
        }
    }

	/**
	 * @param signService the signService to set
	 */
	public final void setDigitalSigningService(SigningService digitalSigningService) {
		this.digitalSigningService = digitalSigningService;
	}


	/**
	 * @param authenticationService the authenticationService to set
	 */
	public final void setAuthenticationService(
			AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}


	/**
	 * @param personService the personService to set
	 */
	public final void setPersonService(PersonService personService) {
		this.personService = personService;
	}


	/**
	 * @param nodeService the nodeService to set
	 */
	public final void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
}
