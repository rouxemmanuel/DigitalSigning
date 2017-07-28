/**
 * 
 */
package org.alfresco.plugin.digitalSigning.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.plugin.digitalSigning.dto.DigitalSigningDTO;
import org.alfresco.plugin.digitalSigning.model.SigningConstants;
import org.alfresco.plugin.digitalSigning.model.SigningModel;
import org.alfresco.plugin.digitalSigning.service.SigningService;
import org.alfresco.plugin.digitalSigning.utils.SigningUtils;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Signing action
 * 
 */
public class SigningActionExecuter extends ActionExecuterAbstractBase {
	
	/**
	 * Logger.
	 */
	private final Log log = LogFactory.getLog(SigningActionExecuter.class);
	
	/**
	 * Action name.
	 */
	public static final String NAME = "DigitalSigning";

	public static final String PARAM_PRIVATE_KEY = "key-file";
	public static final String PARAM_KEY_PASSWORD = "key-password";
	public static final String PARAM_KEY_TYPE = "key-type";
	
	public static final String PARAM_DESTINATION_FOLDER = "destination";
	public static final String PARAM_REASON = "reason";
	public static final String PARAM_LOCATION = "location";
	public static final String PARAM_CONTACT = "contact";
	
	public static final String PARAM_IMAGE = "image";
	public static final String PARAM_FIELD = "field";
	public static final String PARAM_POSITION = "position";
	public static final String PARAM_PAGE = "page";
	public static final String PARAM_DEPTH = "depth";
	public static final String PARAM_LOCATION_X = "locationX";
	public static final String PARAM_LOCATION_Y = "locationY";
	public static final String PARAM_MARGIN_X = "marginX";
	public static final String PARAM_MARGIN_Y = "marginY";
    public static final String PARAM_WIDTH = "width";
    public static final String PARAM_HEIGHT = "height";
    public static final String PARAM_PAGE_NUMBER = "pageNumber";
    public static final String PARAM_DETACHED_SIGNATURE = "detachedSignature";
    public static final String PARAM_LOCALE = "locale";
    public static final String PARAM_TRANSFORM_PDF_A = "transformInPdfA";
	
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
	
	@Override
	protected void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef) {
		final NodeRef privateKey = (NodeRef)ruleAction.getParameterValue(PARAM_PRIVATE_KEY);
		final String keyPassword = (String)ruleAction.getParameterValue(PARAM_KEY_PASSWORD);
		final NodeRef destinationFolder = (NodeRef)ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER);
		final String reason = (String)ruleAction.getParameterValue(PARAM_REASON);
		final String location = (String)ruleAction.getParameterValue(PARAM_LOCATION);
		final String contact = (String)ruleAction.getParameterValue(PARAM_CONTACT);
		final NodeRef image = (NodeRef)ruleAction.getParameterValue(PARAM_IMAGE);
		final String field = (String)ruleAction.getParameterValue(PARAM_FIELD);
		final String position = (String)ruleAction.getParameterValue(PARAM_POSITION);
		final String page = (String)ruleAction.getParameterValue(PARAM_PAGE);
		final String depth = (String)ruleAction.getParameterValue(PARAM_DEPTH);
		final Integer locationX = getInteger(ruleAction.getParameterValue(PARAM_LOCATION_X));
		final Integer locationY = getInteger(ruleAction.getParameterValue(PARAM_LOCATION_Y));
		final Integer marginX = getInteger(ruleAction.getParameterValue(PARAM_MARGIN_X));
		final Integer marginY = getInteger(ruleAction.getParameterValue(PARAM_MARGIN_Y));
		final Integer height = getInteger(ruleAction.getParameterValue(PARAM_HEIGHT));
		final Integer width = getInteger(ruleAction.getParameterValue(PARAM_WIDTH));
		final Integer pageNumber = getInteger(ruleAction.getParameterValue(PARAM_PAGE_NUMBER));
		final Boolean detachedSignature = (Boolean) ruleAction.getParameterValue(PARAM_DETACHED_SIGNATURE);
		String locale = (String) ruleAction.getParameterValue(PARAM_LOCALE);
		final Boolean transformInPdfA = (Boolean) ruleAction.getParameterValue(PARAM_TRANSFORM_PDF_A);
		
		final DigitalSigningDTO signingDTO = new DigitalSigningDTO();
		
		if (actionedUponNodeRef != null) {
			List<NodeRef> nodeRefs = new ArrayList<NodeRef>();
			nodeRefs.add(actionedUponNodeRef);
			signingDTO.setFilesToSign(nodeRefs);
		}
		
		if (privateKey != null) {
			signingDTO.setKeyFile(privateKey);
		} else {
			// Get current user key
			final String currentUser = authenticationService.getCurrentUserName();
			final NodeRef currentUserNodeRef = personService.getPerson(currentUser);
			if (currentUserNodeRef != null) {
				if (locale == null) {
					locale = ((Locale) nodeService.getProperty(currentUserNodeRef, ContentModel.PROP_LOCALE)).toString();
				}
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
								log.error("No key file uploaded for user " + currentUser + ".");
								throw new AlfrescoRuntimeException("No key file uploaded for user " + currentUser + ".");
							}
						} else {
							log.error("No key file uploaded for user " + currentUser + ".");
							throw new AlfrescoRuntimeException("No key file uploaded for user " + currentUser + ".");
						}
					} else {
						log.error("No key file uploaded for user " + currentUser + ".");
						throw new AlfrescoRuntimeException("No key file uploaded for user " + currentUser + ".");
					}
				} else {
					log.error("User '" + currentUser + "' have no home folder.");
					throw new AlfrescoRuntimeException("User '" + currentUser + "' have no home folder.");
				}
			} else {
				log.error("Unable to get current user.");
				throw new AlfrescoRuntimeException("Unable to get current user.");
			}
		}
		if (keyPassword != null) {
			signingDTO.setKeyPassword(keyPassword);
		} else {
			log.error("key-password parameter is required.");
			throw new AlfrescoRuntimeException("key-password parameter is required.");
		}
		if (destinationFolder != null) {
			signingDTO.setDestinationFolder(destinationFolder);
		} else {
			log.error("destination parameter is required.");
			throw new AlfrescoRuntimeException("destination parameter is required.");
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
		if (image != null) {
			signingDTO.setImage(image);
		} else {
			// Get current user image
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
		if (locale != null) {
			signingDTO.setLocale(locale);
		}
		if (transformInPdfA) {
			signingDTO.setTransformToPdfA(true);
		} else {
			signingDTO.setTransformToPdfA(false);
		}
		
		// Validate DTO
		SigningUtils.validateSignInfo(signingDTO);
		
		digitalSigningService.sign(signingDTO);
	}

	@Override
	protected void addParameterDefinitions(final List<ParameterDefinition> paramList) {
		paramList.add(new ParameterDefinitionImpl(PARAM_PRIVATE_KEY, DataTypeDefinition.NODE_REF, true, getParamDisplayLabel(PARAM_PRIVATE_KEY)));
		paramList.add(new ParameterDefinitionImpl(PARAM_KEY_PASSWORD, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_KEY_PASSWORD)));
		paramList.add(new ParameterDefinitionImpl(PARAM_KEY_TYPE, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_KEY_TYPE)));
		paramList.add(new ParameterDefinitionImpl(PARAM_DESTINATION_FOLDER, DataTypeDefinition.NODE_REF, true, getParamDisplayLabel(PARAM_DESTINATION_FOLDER)));
		paramList.add(new ParameterDefinitionImpl(PARAM_REASON, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_REASON)));
		paramList.add(new ParameterDefinitionImpl(PARAM_LOCATION, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_LOCATION)));
		paramList.add(new ParameterDefinitionImpl(PARAM_CONTACT, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_CONTACT)));
		paramList.add(new ParameterDefinitionImpl(PARAM_IMAGE, DataTypeDefinition.NODE_REF, true, getParamDisplayLabel(PARAM_IMAGE)));
		paramList.add(new ParameterDefinitionImpl(PARAM_FIELD, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_FIELD)));
		paramList.add(new ParameterDefinitionImpl(PARAM_POSITION, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_POSITION)));
		paramList.add(new ParameterDefinitionImpl(PARAM_PAGE, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_PAGE)));
		paramList.add(new ParameterDefinitionImpl(PARAM_DEPTH, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_DEPTH)));
		paramList.add(new ParameterDefinitionImpl(PARAM_LOCATION_X, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_LOCATION_X)));
		paramList.add(new ParameterDefinitionImpl(PARAM_LOCATION_Y, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_LOCATION_Y)));
		paramList.add(new ParameterDefinitionImpl(PARAM_MARGIN_X, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_MARGIN_X)));
		paramList.add(new ParameterDefinitionImpl(PARAM_MARGIN_Y, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_MARGIN_Y)));
        paramList.add(new ParameterDefinitionImpl(PARAM_WIDTH, DataTypeDefinition.INT, false, getParamDisplayLabel(PARAM_WIDTH)));
        paramList.add(new ParameterDefinitionImpl(PARAM_HEIGHT, DataTypeDefinition.INT, false, getParamDisplayLabel(PARAM_HEIGHT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_PAGE_NUMBER, DataTypeDefinition.INT, false, getParamDisplayLabel(PARAM_PAGE_NUMBER)));
        paramList.add(new ParameterDefinitionImpl(PARAM_DETACHED_SIGNATURE, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_DETACHED_SIGNATURE)));
        paramList.add(new ParameterDefinitionImpl(PARAM_LOCALE, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_LOCALE)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TRANSFORM_PDF_A, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_TRANSFORM_PDF_A)));
	}
	
	/**
	 * Get int value form serialized object.
	 * 
	 * @param val serialized object
	 * @return Integer value of serialized object
	 */
	protected Integer getInteger(Serializable val) {
        if(val == null) { 
        	return null;
        }
        try {
        	return Integer.parseInt(val.toString());
        } catch(NumberFormatException nfe) {
        	return null;
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

	/* (non-Javadoc)
	 * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#init()
	 */
	@Override
	public void init() {
		publicAction = false;
	}

}
