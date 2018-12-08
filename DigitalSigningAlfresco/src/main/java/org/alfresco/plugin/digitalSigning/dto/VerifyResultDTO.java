/**
 * 
 */
package org.alfresco.plugin.digitalSigning.dto;

import java.util.Calendar;

/**
 * Verify result.
 * 
 * @author Emmanuel ROUX
 */
public class VerifyResultDTO {

	/**
	 * Signing name.
	 */
	private String name;
	
	/**
	 * Signature cover whole document ?
	 */
	private Boolean signatureCoversWholeDocument;
	
	/**
	 * Revision.
	 */
	private int revision;
	
	/**
	 * Total revision.
	 */
	private int totalRevision;
	
	/**
	 * Sign is valid ?
	 */
	private Boolean isSignValid;
	
	/**
	 * Fail reason.
	 */
	private Object failReason;
	
	/**
	 * Signing reason.
	 */
	private String signReason;
	
	/**
	 * Signing location.
	 */
	private String signLocation;
	
	/**
	 * Signing date.
	 */
	private Calendar signDate;
	
	/**
	 * Sign name.
	 */
	private String signName;
	
	/**
	 * Signing version.
	 */
	private int signVersion;
	
	/**
	 * Signing informations version.
	 */
	private int signInformationVersion;
	
	/**
	 * Sign subject.
	 */
	private String signSubject;
	
	/**
	 * Document is modified ?
	 */
	private Boolean isDocumentModified;

	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public final void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the signatureCoversWholeDocument
	 */
	public final Boolean getSignatureCoversWholeDocument() {
		return signatureCoversWholeDocument;
	}

	/**
	 * @param signatureCoversWholeDocument the signatureCoversWholeDocument to set
	 */
	public final void setSignatureCoversWholeDocument(
			Boolean signatureCoversWholeDocument) {
		this.signatureCoversWholeDocument = signatureCoversWholeDocument;
	}

	/**
	 * @return the revision
	 */
	public final int getRevision() {
		return revision;
	}

	/**
	 * @param revision the revision to set
	 */
	public final void setRevision(int revision) {
		this.revision = revision;
	}

	/**
	 * @return the totalRevision
	 */
	public final int getTotalRevision() {
		return totalRevision;
	}

	/**
	 * @param totalRevision the totalRevision to set
	 */
	public final void setTotalRevision(int totalRevision) {
		this.totalRevision = totalRevision;
	}

	/**
	 * @return the isSignValid
	 */
	public final Boolean getIsSignValid() {
		return isSignValid;
	}

	/**
	 * @param isSignValid the isSignValid to set
	 */
	public final void setIsSignValid(Boolean isSignValid) {
		this.isSignValid = isSignValid;
	}

	/**
	 * @return the failReason
	 */
	public final Object getFailReason() {
		return failReason;
	}

	/**
	 * @param failReason the failReason to set
	 */
	public final void setFailReason(Object failReason) {
		this.failReason = failReason;
	}

	/**
	 * @return the signReason
	 */
	public final String getSignReason() {
		return signReason;
	}

	/**
	 * @param signReason the signReason to set
	 */
	public final void setSignReason(String signReason) {
		this.signReason = signReason;
	}

	/**
	 * @return the signLocation
	 */
	public final String getSignLocation() {
		return signLocation;
	}

	/**
	 * @param signLocation the signLocation to set
	 */
	public final void setSignLocation(String signLocation) {
		this.signLocation = signLocation;
	}

	/**
	 * @return the signDate
	 */
	public final Calendar getSignDate() {
		return signDate;
	}

	/**
	 * @param signDate the signDate to set
	 */
	public final void setSignDate(Calendar signDate) {
		this.signDate = signDate;
	}

	/**
	 * @return the signVersion
	 */
	public final int getSignVersion() {
		return signVersion;
	}

	/**
	 * @param signVersion the signVersion to set
	 */
	public final void setSignVersion(int signVersion) {
		this.signVersion = signVersion;
	}

	/**
	 * @return the signInformationVersion
	 */
	public final int getSignInformationVersion() {
		return signInformationVersion;
	}

	/**
	 * @param signInformationVersion the signInformationVersion to set
	 */
	public final void setSignInformationVersion(int signInformationVersion) {
		this.signInformationVersion = signInformationVersion;
	}

	/**
	 * @return the signSubject
	 */
	public final String getSignSubject() {
		return signSubject;
	}

	/**
	 * @param signSubject the signSubject to set
	 */
	public final void setSignSubject(String signSubject) {
		this.signSubject = signSubject;
	}

	/**
	 * @return the isDocumentModified
	 */
	public final Boolean getIsDocumentModified() {
		return isDocumentModified;
	}

	/**
	 * @param isDocumentModified the isDocumentModified to set
	 */
	public final void setIsDocumentModified(Boolean isDocumentModified) {
		this.isDocumentModified = isDocumentModified;
	}

	/**
	 * @return the signName
	 */
	public final String getSignName() {
		return signName;
	}

	/**
	 * @param signName the signName to set
	 */
	public final void setSignName(String signName) {
		this.signName = signName;
	}
}
