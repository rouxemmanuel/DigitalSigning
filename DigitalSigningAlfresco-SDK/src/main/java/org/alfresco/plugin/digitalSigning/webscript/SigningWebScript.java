/**
 * 
 */
package org.alfresco.plugin.digitalSigning.webscript;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.alfresco.plugin.digitalSigning.dto.KeyInfoDTO;
import org.alfresco.plugin.digitalSigning.model.SigningConstants;
import org.alfresco.plugin.digitalSigning.utils.CryptUtils;
import org.apache.commons.io.IOUtils;

/**
 * Global signing class.
 * 
 * @author Emmanuel ROUX
 */
public class SigningWebScript extends DeclarativeWebScript {
	
	/**
	 * Node service.
	 */
	protected NodeService nodeService;
	
	/**
	 * Dictionary service.
	 */
	protected DictionaryService dictionaryService;
	
	/**
	 * Content service.
	 */
	protected ContentService contentService;
	
	/**
	 * Metadata encryptor bean.
	 */
	protected MetadataEncryptor metadataEncryptor;
	
	/**
	 * Get key informations.
	 * 
	 * @param keyNodeRef key nodeRef
	 * @param keyAlias key alias
	 * @param keyType key type
	 * @param keyPassword key password
	 * @param keySecretCrypt key secret crypt
	 * @return key informations
	 */
	protected KeyInfoDTO getKeyInformation(final NodeRef keyNodeRef, final String keyAlias, final String keyType, final String keyPassword, final String alert, final String keySecretCrypt) {
		final KeyInfoDTO keyInfoDTO = new KeyInfoDTO() ;
		keyInfoDTO.setError(null);
		try {
			if (SigningConstants.KEY_TYPE_X509.equals(keyType)) {
				final KeyStore ks = KeyStore.getInstance("pkcs12");
				final QName typeQName = nodeService.getType(keyNodeRef);
				if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT) == false) {
			    	return null;
			    }
			    final ContentReader keyContentReader = contentService.getReader(keyNodeRef, ContentModel.PROP_CONTENT);
			    if (keyContentReader != null && ks != null && keyPassword != null) {
				    
			    	// Decrypt key content
					final InputStream decryptedKeyContent = CryptUtils.decrypt(keySecretCrypt, keyContentReader.getContentInputStream());
			    	
			    	ks.load(new ByteArrayInputStream(IOUtils.toByteArray(decryptedKeyContent)), keyPassword.toCharArray());
			        final X509Certificate c = (X509Certificate) ks.getCertificate(keyAlias);
			        if (c != null) {
				        final Principal subject = c.getSubjectDN();
				        
				        keyInfoDTO.setAlias(keyAlias);
				        keyInfoDTO.setAlert(alert);
				        keyInfoDTO.setAlgorithm(c.getSigAlgName());
				        if (subject != null) {
				        	keyInfoDTO.setSubject(subject.toString());
				        }
				        keyInfoDTO.setType(c.getType());
				        keyInfoDTO.setFirstDayValidity(c.getNotBefore());
				        keyInfoDTO.setLastDayValidity(c.getNotAfter());
				        
				        final Date now = new Date();
						long diff = keyInfoDTO.getLastDayValidity().getTime() - now.getTime();
						long diffDays = diff / (24 * 60 * 60 * 1000);
						if (diffDays < 0) {
							keyInfoDTO.setHasExpired(true);
						} else {
							keyInfoDTO.setHasExpired(false);
							keyInfoDTO.setExpire(Long.toString(diffDays));
						}
			        } else {
			        	keyInfoDTO.setError("No alias '" + keyAlias + "' found in certificate.");
			        }
			    }
			}
		} catch (KeyStoreException e) {
			keyInfoDTO.setError(e.getMessage());
		} catch (ContentIOException e) {
			keyInfoDTO.setError(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			keyInfoDTO.setError(e.getMessage());
		} catch (CertificateException e) {
			keyInfoDTO.setError(e.getMessage());
		} catch (IOException e) {
			keyInfoDTO.setError(e.getMessage());
		} catch (Throwable e) {
			keyInfoDTO.setError(e.getMessage());
		}
		
		return keyInfoDTO;
	}

	/**
	 * @param nodeService the nodeService to set
	 */
	public final void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * @param dictionaryService the dictionaryService to set
	 */
	public final void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * @param contentService the contentService to set
	 */
	public final void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}
	
	/**
	 * @param metadataEncryptor the metadataEncryptor to set
	 */
	public final void setMetadataEncryptor(MetadataEncryptor metadataEncryptor) {
		this.metadataEncryptor = metadataEncryptor;
	}

}
