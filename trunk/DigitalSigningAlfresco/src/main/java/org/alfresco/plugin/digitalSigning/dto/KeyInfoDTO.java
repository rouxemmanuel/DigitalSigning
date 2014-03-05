/**
 * 
 */
package org.alfresco.plugin.digitalSigning.dto;

import java.util.Date;

/**
 * Key informations DTO.
 * 
 * @author Emmanuel ROUX
 */
public class KeyInfoDTO {
	
	/**
	 * Alias.
	 */
	private String alias;
	
	/**
	 * Subject.
	 */
    private String subject;
    
    /**
     * Type.
     */
    private String type;
    
    /**
     * First day validity.
     */
    private Date firstDayValidity;
    
    /**
     * Last day validity.
     */
    private Date lastDayValidity;
    
    /**
     * Algotithm.
     */
    private String algorithm;
    
    /**
     * Day before the key expire.
     */
    private String expire;
    
    /**
     * Indicate if key has expired or not.
     */
    private boolean hasExpired;

	/**
	 * @return the alias
	 */
	public final String getAlias() {
		return alias;
	}

	/**
	 * @param alias the alias to set
	 */
	public final void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 * @return the subject
	 */
	public final String getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public final void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * @return the type
	 */
	public final String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public final void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the firstDayValidity
	 */
	public final Date getFirstDayValidity() {
		return firstDayValidity;
	}

	/**
	 * @param firstDayValidity the firstDayValidity to set
	 */
	public final void setFirstDayValidity(Date firstDayValidity) {
		this.firstDayValidity = firstDayValidity;
	}

	/**
	 * @return the lastDayValidity
	 */
	public final Date getLastDayValidity() {
		return lastDayValidity;
	}

	/**
	 * @param lastDayValidity the lastDayValidity to set
	 */
	public final void setLastDayValidity(Date lastDayValidity) {
		this.lastDayValidity = lastDayValidity;
	}

	/**
	 * @return the algorithm
	 */
	public final String getAlgorithm() {
		return algorithm;
	}

	/**
	 * @param algorithm the algorithm to set
	 */
	public final void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * @return the expire
	 */
	public final String getExpire() {
		return expire;
	}

	/**
	 * @param expire the expire to set
	 */
	public final void setExpire(String expire) {
		this.expire = expire;
	}

	/**
	 * @return the hasExpired
	 */
	public final boolean isHasExpired() {
		return hasExpired;
	}

	/**
	 * @param hasExpired the hasExpired to set
	 */
	public final void setHasExpired(boolean hasExpired) {
		this.hasExpired = hasExpired;
	}

}
