/**
 * 
 */
package org.alfresco.plugin.digitalSigning.dto;

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
	public static final String PAGE_ALL = "all";
    public static final String PAGE_ODD = "odd";
    public static final String PAGE_EVEN = "even";
    public static final String PAGE_FIRST = "first";
    public static final String PAGE_LAST = "last";

    /**
     * Sign position.
     */
    public static final String POSITION_CENTER = "center";
    public static final String POSITION_TOPLEFT = "topleft";
    public static final String POSITION_TOPRIGHT = "topright";
    public static final String POSITION_BOTTOMLEFT = "bottomleft";
    public static final String POSITION_BOTTOMRIGHT = "bottomright";
    
    /**
     * Image depth.
     */
    public static final String DEPTH_UNDER = "under";
    public static final String DEPTH_OVER = "over";
	
	
	/**
	 * File to sign.
	 */
	private NodeRef fileToSign;
	
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
	private int locationX = 100;
    
	/**
	 * Sign Y position (in pixel).
	 */
	private int locationY = 100;
	
	/**
	 * X margin (in pixel).
	 */
	private int xMargin = 100;
	
	/**
	 * Y margin (in pixel).
	 */
	private int yMargin = 100;
	
	/**
	 * Sign width (in pixel).
	 */
	private int signWidth = 100;
	
	/**
	 * Sign height (in pixel).
	 */
	private int signHeight = 100;
	
	/**
	 * @return the fileToSign
	 */
	public final NodeRef getFileToSign() {
		return fileToSign;
	}

	/**
	 * @param fileToSign the fileToSign to set
	 */
	public final void setFileToSign(NodeRef fileToSign) {
		this.fileToSign = fileToSign;
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
	public final int getLocationX() {
		return locationX;
	}

	/**
	 * @param locationX the locationX to set
	 */
	public final void setLocationX(int locationX) {
		this.locationX = locationX;
	}

	/**
	 * @return the locationY
	 */
	public final int getLocationY() {
		return locationY;
	}

	/**
	 * @param locationY the locationY to set
	 */
	public final void setLocationY(int locationY) {
		this.locationY = locationY;
	}

	/**
	 * @return the xMargin
	 */
	public final int getxMargin() {
		return xMargin;
	}

	/**
	 * @param xMargin the xMargin to set
	 */
	public final void setxMargin(int xMargin) {
		this.xMargin = xMargin;
	}

	/**
	 * @return the yMargin
	 */
	public final int getyMargin() {
		return yMargin;
	}

	/**
	 * @param yMargin the yMargin to set
	 */
	public final void setyMargin(int yMargin) {
		this.yMargin = yMargin;
	}

	/**
	 * @return the signWidth
	 */
	public final int getSignWidth() {
		return signWidth;
	}

	/**
	 * @param signWidth the signWidth to set
	 */
	public final void setSignWidth(int signWidth) {
		this.signWidth = signWidth;
	}

	/**
	 * @return the signHeight
	 */
	public final int getSignHeight() {
		return signHeight;
	}

	/**
	 * @param signHeight the signHeight to set
	 */
	public final void setSignHeight(int signHeight) {
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
	
}
