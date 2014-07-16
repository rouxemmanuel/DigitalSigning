/**
 * 
 */
package org.alfresco.plugin.digitalSigning.webscript;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.alfresco.model.ContentModel;
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
	 * Get key informations.
	 * 
	 * @param keyNodeRef key nodeRef
	 * @param keyAlias key alias
	 * @param keyType key type
	 * @param keyPassword key password
	 * @return key informations
	 */
	protected KeyInfoDTO getKeyInformation(final NodeRef keyNodeRef, final String keyAlias, final String keyType, final String keyPassword, final String alert) {
		final KeyInfoDTO keyInfoDTO = new KeyInfoDTO() ;
		try {
			if (SigningConstants.KEY_TYPE_X509.equals(keyType)) {
				final KeyStore ks = KeyStore.getInstance("pkcs12");
				final QName typeQName = nodeService.getType(keyNodeRef);
				if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT) == false) {
			    	return null;
			    }
			    final ContentReader keyContentReader = contentService.getReader(keyNodeRef, ContentModel.PROP_CONTENT);
			    if (keyContentReader != null && ks != null && keyPassword != null) {
				    ks.load(keyContentReader.getContentInputStream(), keyPassword.toCharArray());
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
			        }
			    }
			}
		} catch (KeyStoreException e) {
			return null;
		} catch (ContentIOException e) {
			return null;
		} catch (NoSuchAlgorithmException e) {
			return null;
		} catch (CertificateException e) {
			return null;
		} catch (IOException e) {
			return null;
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

}
