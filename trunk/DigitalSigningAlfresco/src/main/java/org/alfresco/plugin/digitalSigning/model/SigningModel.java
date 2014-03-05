/**
 * 
 */
package org.alfresco.plugin.digitalSigning.model;

import org.alfresco.service.namespace.QName;

/**
 * Models.
 * 
 * @author Emmanuel ROUX
 */
public class SigningModel {
	// Namespace
    public static final String DIGITAL_SIGNING_MODEL_1_0_URI = "http://www.alfresco.com/model/digital/signing/1.0";
    
    // Signed aspect and properties
    public static final QName ASPECT_SIGNED = QName.createQName(DIGITAL_SIGNING_MODEL_1_0_URI, "signed");
    public static final QName PROP_SIGNATUREDATE = QName.createQName(DIGITAL_SIGNING_MODEL_1_0_URI, "signaturedate");
    public static final QName PROP_REASON = QName.createQName(DIGITAL_SIGNING_MODEL_1_0_URI, "reason");
    public static final QName PROP_LOCATION = QName.createQName(DIGITAL_SIGNING_MODEL_1_0_URI, "location");
    public static final QName PROP_SIGNEDBY = QName.createQName(DIGITAL_SIGNING_MODEL_1_0_URI, "signedby");
    public static final QName PROP_VALIDITY = QName.createQName(DIGITAL_SIGNING_MODEL_1_0_URI, "validity");
    
    // Key aspect and properties
    public static final QName ASPECT_KEY = QName.createQName(DIGITAL_SIGNING_MODEL_1_0_URI, "key");
    public static final QName PROP_KEYTYPE = QName.createQName(DIGITAL_SIGNING_MODEL_1_0_URI, "keyType");
    public static final QName PROP_KEYALIAS = QName.createQName(DIGITAL_SIGNING_MODEL_1_0_URI, "keyAlias");
    public static final QName PROP_KEYSUBJECT = QName.createQName(DIGITAL_SIGNING_MODEL_1_0_URI, "keySubject");
    public static final QName PROP_KEYFIRSTVALIDITY = QName.createQName(DIGITAL_SIGNING_MODEL_1_0_URI, "keyFirstValidity");
    public static final QName PROP_KEYLASTVALIDITY = QName.createQName(DIGITAL_SIGNING_MODEL_1_0_URI, "keyLastValidity");
    public static final QName PROP_KEYALGORITHM = QName.createQName(DIGITAL_SIGNING_MODEL_1_0_URI, "keyAlgorithm");
    
    // Image aspect and properties
    public static final QName ASPECT_IMAGE = QName.createQName(DIGITAL_SIGNING_MODEL_1_0_URI, "image");
    
}
