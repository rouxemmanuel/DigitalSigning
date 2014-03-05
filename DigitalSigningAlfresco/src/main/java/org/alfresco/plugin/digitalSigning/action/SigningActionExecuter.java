/**
 * 
 */
package org.alfresco.plugin.digitalSigning.action;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
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

import org.alfresco.plugin.digitalSigning.dto.DigitalSigningDTO;
import org.alfresco.plugin.digitalSigning.model.SigningConstants;
import org.alfresco.plugin.digitalSigning.model.SigningModel;
import org.alfresco.plugin.digitalSigning.service.SigningService;

/**
 * Signing action
 * 
 * @author Emmanuel ROUX
 */
public class SigningActionExecuter extends ActionExecuterAbstractBase {

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
		
		final int locationX = getInteger(ruleAction.getParameterValue(PARAM_LOCATION_X));
		final int locationY = getInteger(ruleAction.getParameterValue(PARAM_LOCATION_Y));
		final int marginX = getInteger(ruleAction.getParameterValue(PARAM_MARGIN_X));
		final int marginY = getInteger(ruleAction.getParameterValue(PARAM_MARGIN_Y));
		final int height = getInteger(ruleAction.getParameterValue(PARAM_HEIGHT));
		final int width = getInteger(ruleAction.getParameterValue(PARAM_WIDTH));
		
		final DigitalSigningDTO signingDTO = new DigitalSigningDTO();
		
		if (privateKey != null) {
			signingDTO.setKeyFile(privateKey);
		} else {
			// Get key from current user
			final String currentUser = authenticationService.getCurrentUserName();
			final NodeRef currentUserNodeRef = personService.getPerson(currentUser);
			if (currentUserNodeRef != null) {
				final NodeRef currentUserHomeFolder = (NodeRef) nodeService.getProperty(currentUserNodeRef, ContentModel.PROP_HOMEFOLDER);
				final NodeRef signingFolderNodeRef = nodeService.getChildByName(currentUserHomeFolder, ContentModel.ASSOC_CONTAINS, SigningConstants.KEY_FOLDER);
				final List<ChildAssociationRef> children = nodeService.getChildAssocs(signingFolderNodeRef);
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
					throw new AlfrescoRuntimeException("No key file uploaded for user " + currentUser);
				}
			}
		}
		if (keyPassword != null) {
			signingDTO.setKeyPassword(keyPassword);
		} else {
			throw new AlfrescoRuntimeException("key-password parameter is required");
		}
		if (destinationFolder != null) {
			signingDTO.setDestinationFolder(destinationFolder);
		} else {
			throw new AlfrescoRuntimeException("destination parameter is required");
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
			// Get image from current user
			final String currentUser = authenticationService.getCurrentUserName();
			final NodeRef currentUserNodeRef = personService.getPerson(currentUser);
			if (currentUserNodeRef != null) {
				final NodeRef currentUserHomeFolder = (NodeRef) nodeService.getProperty(currentUserNodeRef, ContentModel.PROP_HOMEFOLDER);
				final NodeRef signingFolderNodeRef = nodeService.getChildByName(currentUserHomeFolder, ContentModel.ASSOC_CONTAINS, SigningConstants.KEY_FOLDER);
				final List<ChildAssociationRef> children = nodeService.getChildAssocs(signingFolderNodeRef);
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
		signingDTO.setLocationX(locationX);
		signingDTO.setLocationY(locationY);
		signingDTO.setxMargin(marginX);
		signingDTO.setyMargin(marginY);
		signingDTO.setSignWidth(width);
		signingDTO.setSignHeight(height);
		
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
