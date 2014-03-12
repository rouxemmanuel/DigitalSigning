/**
 * 
 */
package org.alfresco.plugin.digitalSigning.utils;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.plugin.digitalSigning.dto.DigitalSigningDTO;

/**
 * Signing utils class.
 * 
 * @author Emmanuel ROUX
 */
public class SigningUtils {

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
			
			if (digitalSigningDTO.getDestinationFolder() == null) {
				throw new AlfrescoRuntimeException("destination folder parameter is required.");
			}
			
			if (digitalSigningDTO.getFileToSign() == null) {
				throw new AlfrescoRuntimeException("document to sign parameter is required.");
			}
			
			if (DigitalSigningDTO.POSITION_CUSTOM.equalsIgnoreCase(digitalSigningDTO.getPosition()) && digitalSigningDTO.getLocationX() == null && digitalSigningDTO.getLocationY() == null) {
				throw new AlfrescoRuntimeException("locationX and locationY parameters are required when position is set to custom.");
			}
		}
	}
}
