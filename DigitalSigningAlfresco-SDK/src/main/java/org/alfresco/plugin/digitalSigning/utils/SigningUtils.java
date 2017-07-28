/**
 * 
 */
package org.alfresco.plugin.digitalSigning.utils;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.plugin.digitalSigning.dto.DigitalSigningDTO;
import org.alfresco.plugin.digitalSigning.script.SigningServiceScript;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Signing utils class.
 * 
 * @author Emmanuel ROUX
 */
public class SigningUtils {
	/**
	 * Logger.
	 */
	//private final static Log log = LogFactory.getLog(SigningUtils.class);
	private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SigningUtils.class);


	/**
	 * Validate DigitalSigningDTO object.
	 * 
	 * @param digitalSigningDTO object to validate
	 */
	public static void validateSignInfo (final DigitalSigningDTO digitalSigningDTO) {
		if (digitalSigningDTO != null) {
			if (digitalSigningDTO.getKeyFile() == null) {
				throw new AlfrescoRuntimeException("key file parameter is required.");
			}
			
			if (digitalSigningDTO.getKeyPassword() == null) {
				throw new AlfrescoRuntimeException("key password parameter is required.");
			}
			
			/*
			if (digitalSigningDTO.getDestinationFolder() == null) {
				throw new AlfrescoRuntimeException("destination folder parameter is required.");
			}
			*/
			
			if (digitalSigningDTO.getFilesToSign() == null && digitalSigningDTO.getFilesToSign().size() == 0) {
				throw new AlfrescoRuntimeException("document(s) to sign parameter is required.");
			}
			
			if (DigitalSigningDTO.POSITION_CUSTOM.equalsIgnoreCase(digitalSigningDTO.getPosition()) && digitalSigningDTO.getLocationX() == null && digitalSigningDTO.getLocationY() == null) {
				throw new AlfrescoRuntimeException("locationX and locationY parameters are required when position is set to custom.");
			}
			
			if (DigitalSigningDTO.PAGE_SPECIFIC.equalsIgnoreCase(digitalSigningDTO.getPages()) && digitalSigningDTO.getPageNumber() == null) {
				throw new AlfrescoRuntimeException("page number parameter is required when page signing is set to 'specific'.");
			}
			
		}
	}
}
