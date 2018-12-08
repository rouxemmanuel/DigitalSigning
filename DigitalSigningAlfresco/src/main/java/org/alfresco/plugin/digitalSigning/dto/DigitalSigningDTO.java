/**
 * 
 */
package org.alfresco.plugin.digitalSigning.dto;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Signing DTO.
 * 
 * @author Emmanuel ROUX
 *
 */
public class DigitalSigningDTO {
	
	/**
	 * Signing page.
	 */
	public static final String PAGE_FIRST = "first";
	public static final String PAGE_LAST = "last";
	/*
	public static final String PAGE_ALL = "all";
	public static final String PAGE_ODD = "odd";
    public static final String PAGE_EVEN = "even";
    */
    public static final String PAGE_SPECIFIC = "specific";

    /**
     * Sign position.
     */
    public static final String POSITION_CENTER = "center";
    public static final String POSITION_TOPLEFT = "topleft";
    public static final String POSITION_TOPRIGHT = "topright";
    public static final String POSITION_BOTTOMLEFT = "bottomleft";
    public static final String POSITION_BOTTOMRIGHT = "bottomright";
    public static final String POSITION_CUSTOM = "custom";
    
    /**
     * Image depth.
     */
    public static final String DEPTH_UNDER = "under";
    public static final String DEPTH_OVER = "over";
	
	
	/**
	 * File to sign.
	 */
	private List<NodeRef> filesToSign;
	
	/**
	 * Key file to sign.
	 */
	private NodeRef keyFile;
	
	/**
	 * Key password.
	 */
	private String keyPassword;
	
	/**
	 * Generated file destination folder.
	 */
	private NodeRef destinationFolder;
	
	/**
	 * Signing reason.
	 */
	private String signReason;
	
	/**
	 * Signing location.
	 */
	private String signLocation;
	
	/**
	 * Signing contact.
	 */
	private String signContact;
	
	/**
	 * Sign image file.
	 */
	private NodeRef image;
	
	/**
	 * Signing field.
	 */
	private String signingField;
	
	/**
	 * Sign position.
	 */
	private String position = POSITION_TOPRIGHT;
	
	/**
	 * Signing page.
	 */
	private String pages = PAGE_FIRST;
	
	/**
	 * Sign depth.
	 */
	private String depth = DEPTH_OVER;
	
	/**
	 * Sign X location (in pixel).
	 */
	private Integer locationX = 100;
    
	/**
	 * Sign Y position (in pixel).
	 */
	private Integer locationY = 100;
	
	/**
	 * X margin (in pixel).
	 */
	private Integer xMargin = 100;
	
	/**
	 * Y margin (in pixel).
	 */
	private Integer yMargin = 100;
	
	/**
	 * Sign width (in pixel).
	 */
	private Integer signWidth = 150;
	
	/**
	 * Sign height (in pixel).
	 */
	private Integer signHeight = 50;
	
	/**
	 * Page number to sign.
	 */
	private Integer pageNumber;
	
	/**
	 * Detached signature for XML.
	 */
	private boolean isDetached = false;
	
	/**
	 * PDF file transformed in PDF/A-1 before signing ?
	 */
	private boolean transformToPdfA = true;
	
	private String locale;
	
	/**
	 * @return the fileToSign
	 */
	public final List<NodeRef> getFilesToSign() {
		return filesToSign;
	}

	/**
	 * @param fileToSign the fileToSign to set
	 */
	public final void setFilesToSign(List<NodeRef> filesToSign) {
		this.filesToSign = filesToSign;
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
	 * @return the signContact
	 */
	public final String getSignContact() {
		return signContact;
	}

	/**
	 * @param signContact the signContact to set
	 */
	public final void setSignContact(String signContact) {
		this.signContact = signContact;
	}

	/**
	 * @return the image
	 */
	public final NodeRef getImage() {
		return image;
	}

	/**
	 * @param image the image to set
	 */
	public final void setImage(NodeRef image) {
		this.image = image;
	}

	/**
	 * @return the position
	 */
	public final String getPosition() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public final void setPosition(String position) {
		this.position = position;
	}

	/**
	 * @return the pages
	 */
	public final String getPages() {
		return pages;
	}

	/**
	 * @param pages the pages to set
	 */
	public final void setPages(String pages) {
		this.pages = pages;
	}

	/**
	 * @return the depth
	 */
	public final String getDepth() {
		return depth;
	}

	/**
	 * @param depth the depth to set
	 */
	public final void setDepth(String depth) {
		this.depth = depth;
	}

	/**
	 * @return the locationX
	 */
	public final Integer getLocationX() {
		return locationX;
	}

	/**
	 * @param locationX the locationX to set
	 */
	public final void setLocationX(Integer locationX) {
		this.locationX = locationX;
	}

	/**
	 * @return the locationY
	 */
	public final Integer getLocationY() {
		return locationY;
	}

	/**
	 * @param locationY the locationY to set
	 */
	public final void setLocationY(Integer locationY) {
		this.locationY = locationY;
	}

	/**
	 * @return the xMargin
	 */
	public final Integer getxMargin() {
		return xMargin;
	}

	/**
	 * @param xMargin the xMargin to set
	 */
	public final void setxMargin(Integer xMargin) {
		this.xMargin = xMargin;
	}

	/**
	 * @return the yMargin
	 */
	public final Integer getyMargin() {
		return yMargin;
	}

	/**
	 * @param yMargin the yMargin to set
	 */
	public final void setyMargin(Integer yMargin) {
		this.yMargin = yMargin;
	}

	/**
	 * @return the signWidth
	 */
	public final Integer getSignWidth() {
		return signWidth;
	}

	/**
	 * @param signWidth the signWidth to set
	 */
	public final void setSignWidth(Integer signWidth) {
		this.signWidth = signWidth;
	}

	/**
	 * @return the signHeight
	 */
	public final Integer getSignHeight() {
		return signHeight;
	}

	/**
	 * @param signHeight the signHeight to set
	 */
	public final void setSignHeight(Integer signHeight) {
		this.signHeight = signHeight;
	}

	/**
	 * @return the signingField
	 */
	public final String getSigningField() {
		return signingField;
	}

	/**
	 * @param signingField the signingField to set
	 */
	public final void setSigningField(String signingField) {
		this.signingField = signingField;
	}

	/**
	 * @return the destinationFolder
	 */
	public final NodeRef getDestinationFolder() {
		return destinationFolder;
	}

	/**
	 * @param destinationFolder the destinationFolder to set
	 */
	public final void setDestinationFolder(NodeRef destinationFolder) {
		this.destinationFolder = destinationFolder;
	}

	/**
	 * @return the pageNumber
	 */
	public final Integer getPageNumber() {
		return pageNumber;
	}

	/**
	 * @param pageNumber the pageNumber to set
	 */
	public final void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	/**
	 * @return the isDetached
	 */
	public final boolean isDetached() {
		return isDetached;
	}

	/**
	 * @param isDetached the isDetached to set
	 */
	public final void setDetached(boolean isDetached) {
		this.isDetached = isDetached;
	}

	/**
	 * @return the locale
	 */
	public final String getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public final void setLocale(String locale) {
		this.locale = locale;
	}

	/**
	 * @return the transformToPdfA
	 */
	public final boolean isTransformToPdfA() {
		return transformToPdfA;
	}

	/**
	 * @param transformToPdfA the transformToPdfA to set
	 */
	public final void setTransformToPdfA(boolean transformToPdfA) {
		this.transformToPdfA = transformToPdfA;
	}
	
}
