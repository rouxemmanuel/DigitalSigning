/**
 * 
 */
package org.alfresco.plugin.digitalSigning.dto;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Verifying DTO.
 * 
 * @author Emmanuel ROUX
 */
public class VerifyingDTO {

	/**
	 * File to verify.
	 */
	private NodeRef fileToVerify;
	
	/**
	 * Key file to sign.
	 */
	private NodeRef keyFile;
	
	/**
	 * Key password.
	 */
	private String keyPassword;

	/**
	 * @return the fileToVerify
	 */
	public final NodeRef getFileToVerify() {
		return fileToVerify;
	}

	/**
	 * @param fileToVerify the fileToVerify to set
	 */
	public final void setFileToVerify(NodeRef fileToVerify) {
		this.fileToVerify = fileToVerify;
	}

	/**
	 * @return the keyFile
	 */
	public final NodeRef getKeyFile() {
		return keyFile;
	}

	/**
	 * @param keyFile the keyFile to set
	 */
	public final void setKeyFile(NodeRef keyFile) {
		this.keyFile = keyFile;
	}

	/**
	 * @return the keyPassword
	 */
	public final String getKeyPassword() {
		return keyPassword;
	}

	/**
	 * @param keyPassword the keyPassword to set
	 */
	public final void setKeyPassword(String keyPassword) {
		this.keyPassword = keyPassword;
	}

	@Override
	public String toString() {
		return "VerifyingDTO [fileToVerify=" + fileToVerify + ", keyFile=" + keyFile + ", keyPassword=" + keyPassword
				+ "]";
	}
	
	
}
