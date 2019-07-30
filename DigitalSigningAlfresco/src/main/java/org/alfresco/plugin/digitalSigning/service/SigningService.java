/**
 * 
 */
package org.alfresco.plugin.digitalSigning.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.plugin.digitalSigning.dto.DigitalSigningDTO;
import org.alfresco.plugin.digitalSigning.dto.VerifyResultDTO;
import org.alfresco.plugin.digitalSigning.dto.VerifyingDTO;
import org.alfresco.plugin.digitalSigning.model.SigningConstants;
import org.alfresco.plugin.digitalSigning.model.SigningModel;
import org.alfresco.plugin.digitalSigning.utils.CryptUtils;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.ContentTransformerRegistry;
import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.examples.pdfa.CreatePDFA;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.ElementProxy;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.XmpSerializer;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.SignedData;
import org.bouncycastle.asn1.cms.Time;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSSignedGenerator;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.xml.sax.SAXException;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.AcroFields.FieldPosition;
import com.itextpdf.text.pdf.PdfPKCS7;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfSignatureAppearance.RenderingMode;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * Sign service for Alfresco
 * 
 * @author Emmanuel ROUX
 */
public class SigningService {
	
	/**
	 * Logger.
	 */
	private final Log log = LogFactory.getLog(SigningService.class);
	

	/**
	 * Node service.
	 */
	private NodeService nodeService;
	
	/**
	 * Dictionary service.
	 */
	private DictionaryService dictionaryService;
	
	/**
	 * Content service.
	 */
	private ContentService contentService;
	
	/**
	 * File folder service.
	 */
	private FileFolderService fileFolderService;
	
	/**
	 * Content Transformer registry.
	 */
	private ContentTransformerRegistry contentTransformerRegistry;
	
	/**
	 * Metadata encryptor.
	 */
	private MetadataEncryptor metadataEncryptor;
	
	/**
	 * PADES Signing.
	 * 
	 * @param nodeRefToSign NodeRef to sign
	 * @param signingDTO SigningDTO object
	 * @param alfTempDir Alfresco temporary directoru
	 * @param alias Sign Alias to use
	 * @param ks KeyStore
	 * @param key Key
	 * @param chain Chain
	 * @param userLocale user locale
	 * @return null or an error during signing process
	 */
	private AlfrescoRuntimeException signPDFFile(final NodeRef nodeRefToSign, final DigitalSigningDTO signingDTO, final File alfTempDir, final String alias, final KeyStore ks, final PrivateKey key, final Certificate[] chain, final Locale userLocale) {
		final String fileNameToSign = fileFolderService.getFileInfo(nodeRefToSign).getName();
		
		File fileConverted = null;
		File tempDir = null;
		try {
			ContentReader fileToSignContentReader = getReader(nodeRefToSign);
			
			if (fileToSignContentReader != null) {
				String newName = null;
				
				// Check if document is PDF or transform it
				if (!MimetypeMap.MIMETYPE_PDF.equals(fileToSignContentReader.getMimetype())) {
					// Transform document in PDF document
					final ContentTransformer tranformer = contentTransformerRegistry.getTransformer(fileToSignContentReader.getMimetype(), fileToSignContentReader.getSize(), MimetypeMap.MIMETYPE_PDF, new TransformationOptions());
					
					if (tranformer != null) {
						
						tempDir = new File(alfTempDir.getPath() + File.separatorChar + nodeRefToSign.getId());
				        if (tempDir != null) {
							tempDir.mkdir();
					        fileConverted = new File(tempDir, fileNameToSign + "_" + System.currentTimeMillis() + ".pdf");
							if (fileConverted != null) {
						        final ContentWriter newDoc = new FileContentWriter(fileConverted);
						        if (newDoc != null) {
									newDoc.setMimetype(MimetypeMap.MIMETYPE_PDF);
									final TransformationOptions transformationOptions = null;
									tranformer.transform(fileToSignContentReader, newDoc, transformationOptions);
									fileToSignContentReader = new FileContentReader(fileConverted);
									
									final String originalName = (String) nodeService.getProperty(nodeRefToSign, ContentModel.PROP_NAME);
									
									if (originalName.lastIndexOf(".") == -1) {
										newName = originalName + ".pdf";
									} else {
										newName = originalName.substring(0, originalName.lastIndexOf(".")) + ".pdf";
									}
						        }
							}
				        }
					} else {
						log.error("[" + fileNameToSign + "] No suitable converter found to convert the document in PDF.");
						return new AlfrescoRuntimeException("[" + fileNameToSign + "] No suitable converter found to convert the document in PDF.");
					}
				}
			
				// Convert PDF in PDF/A format only if not field signing
				PdfReader reader = null;
				File pdfAFile = null;
				if (signingDTO.getSigningField() == null || "".equals(signingDTO.getSigningField())) {
					if (signingDTO.isTransformToPdfA()) {
						pdfAFile = convertPdfToPdfA(fileToSignContentReader.getContentInputStream());
						reader = new PdfReader(new FileInputStream(pdfAFile));
					} else {
						reader = new PdfReader(fileToSignContentReader.getContentInputStream());
					}
				} else {
					reader = new PdfReader(fileToSignContentReader.getContentInputStream());
				}
				
				if (nodeRefToSign != null) {
			        tempDir = new File(alfTempDir.getPath() + File.separatorChar + nodeRefToSign.getId());
			        if (tempDir != null) {
				        tempDir.mkdir();
				        final File file = new File(tempDir, fileNameToSign);
				        
				        if (file != null) {
					        final FileOutputStream fout = new FileOutputStream(file);
					        //final PdfStamper stp = PdfStamper.createSignature(reader, fout, '\0');
					        
					        final File tempFile = new File(tempDir, fileNameToSign + "_tmp");
					        // Use parameter "true" to allow multiple signature on the PDF
					        final PdfStamper stp = PdfStamper.createSignature(reader, fout, '\0', tempFile, true);
					        
					        if (stp != null) {
								final PdfSignatureAppearance sap = stp.getSignatureAppearance();
								if (sap != null) {
									//sap.setCrypto(key, chain, null, PdfSignatureAppearance.WINCER_SIGNED);
									final StringBuffer signingText = new StringBuffer();
									int missingInfo = 0;
									if (signingDTO.getSignContact() != null && !"".equals(signingDTO.getSignContact())) {
										sap.setContact(signingDTO.getSignContact());
										signingText.append(signingDTO.getSignContact()).append('\n');
									} else {
										missingInfo ++;
									}
									final SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm", userLocale);
									//final SimpleDateFormat df = new SimpleDateFormat("EEEE dd MMM yyyy HH:mm", userLocale);
									signingText.append(df.format(new Date())).append('\n');
									if (signingDTO.getSignReason() != null && !"".equals(signingDTO.getSignReason())) {
										sap.setReason(signingDTO.getSignReason());
										signingText.append(signingDTO.getSignReason()).append('\n');
									} else {
										missingInfo ++;
									}
									if (signingDTO.getSignLocation() != null && !"".equals(signingDTO.getSignLocation())) {
										sap.setLocation(signingDTO.getSignLocation());
										signingText.append(signingDTO.getSignLocation()).append('\n');
									} else {
										missingInfo ++;
									}
									final Font signingFont = new Font();
									signingFont.setSize(6);
									
									for (int i=0;i<missingInfo;i++) {
										signingText.insert(0, '\n');
									}
									
									sap.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
									sap.setImageScale(1);
									
									//ITEXT 5.5.11
									//final ExternalSignature es = new PrivateKeySignature(key, "SHA-256", "BC");
							        //final ExternalDigest digest = new BouncyCastleDigest();
									
									// digital signature
									if (signingDTO.getSigningField() != null && !signingDTO.getSigningField().trim().equalsIgnoreCase("")) {
										Image img = null;
										if (signingDTO.getImage() != null) {
											final ContentReader imageContentReader = getReader(signingDTO.getImage());
											final AcroFields af = reader.getAcroFields();
											if (af != null) {
												final List<FieldPosition> positions = af.getFieldPositions(signingDTO.getSigningField());
												if (positions != null && positions.size() > 0 && positions.get(0) != null && positions.get(0).position != null) {
													final BufferedImage newImg = scaleImage(ImageIO.read(imageContentReader.getContentInputStream()), BufferedImage.TYPE_INT_ARGB, Float.valueOf(positions.get(0).position.getWidth()).intValue(), Float.valueOf(positions.get(0).position.getHeight()).intValue());
													img = Image.getInstance(newImg, null);
												}
											} else {
												log.error("[" + fileNameToSign + "] The field '" + signingDTO.getSigningField() + "' doesn't exist in the document.");
												return new AlfrescoRuntimeException("[" + fileNameToSign + "] The field '" + signingDTO.getSigningField() + "' doesn't exist in the document.");
											}
									        if (img == null) {
									        	img = Image.getInstance(ImageIO.read(imageContentReader.getContentInputStream()), null);
									        }
									        sap.setImage(img);
									        //sap.setSignatureGraphic(img);
									        //sap.setRenderingMode(RenderingMode.GRAPHIC_AND_DESCRIPTION);
										}
										sap.setVisibleSignature(signingDTO.getSigningField());
										//ITEXT 5.5.11
										//MakeSignature.signDetached(sap, digest, es, chain, null, null, null, 0, CryptoStandard.CMS);
										//ITEXT 5.1.3
										signDetached(sap,signingDTO,key,chain);
									} else {
										int pageToSign = 1;
										sap.setLayer2Font(signingFont);
										sap.setLayer2Text(signingText.toString());
						                if (DigitalSigningDTO.PAGE_LAST.equalsIgnoreCase(signingDTO.getPages().trim())) {
						                	pageToSign = reader.getNumberOfPages();
						                } else if (DigitalSigningDTO.PAGE_SPECIFIC.equalsIgnoreCase(signingDTO.getPages().trim())) {
						                	if (signingDTO.getPageNumber() > 0 && signingDTO.getPageNumber() <= reader.getNumberOfPages()) {
						                		pageToSign = signingDTO.getPageNumber();
						                	} else {
						                		throw new AlfrescoRuntimeException("Page number is out of bound.");
						                	}
						                }
										if (signingDTO.getImage() != null) {
											final ContentReader imageContentReader = getReader(signingDTO.getImage());
											// Resize image
									        //final BufferedImage newImg = scaleImage(ImageIO.read(imageContentReader.getContentInputStream()), BufferedImage.TYPE_INT_ARGB, signingDTO.getSignWidth(), signingDTO.getSignHeight());
									        //final Image img = Image.getInstance(newImg, null);
									        final Image img = Image.getInstance(ImageIO.read(imageContentReader.getContentInputStream()), null);
											//sap.setImage(img);
											sap.setSignatureGraphic(img);
											sap.setRenderingMode(RenderingMode.GRAPHIC_AND_DESCRIPTION);
										}
										if(signingDTO.getPosition() != null && !DigitalSigningDTO.POSITION_CUSTOM.equalsIgnoreCase(signingDTO.getPosition().trim())) {
											final Rectangle pageRect = reader.getPageSizeWithRotation(1);
							                sap.setVisibleSignature(positionSignature(signingDTO.getPosition(), pageRect, signingDTO.getSignWidth(), signingDTO.getSignHeight(), signingDTO.getxMargin(), signingDTO.getyMargin()), pageToSign, null);
							                //MakeSignature.signDetached(sap, digest, es, chain, null, null, null, 0, CryptoStandard.CMS);
											//ITEXT 5.1.3
											signDetached(sap,signingDTO,key,chain);
										} else {
							                sap.setVisibleSignature(new Rectangle(signingDTO.getLocationX(), signingDTO.getLocationY(), signingDTO.getLocationX() + signingDTO.getSignWidth(), signingDTO.getLocationY() - signingDTO.getSignHeight()), pageToSign, null);
							                //MakeSignature.signDetached(sap, digest, es, chain, null, null, null, 0, CryptoStandard.CMS);
											//ITEXT 5.1.3
											signDetached(sap,signingDTO,key,chain);
										}
									}
									
									stp.close();
								
									NodeRef destinationNode = null;
									NodeRef originalDoc = null;
									boolean addAsNewVersion = false;
									if (signingDTO.getDestinationFolder() == null) {
										destinationNode = nodeRefToSign;
										nodeService.addAspect(destinationNode, ContentModel.ASPECT_VERSIONABLE, null);
										addAsNewVersion = true;
									} else {
										originalDoc = nodeRefToSign;
										destinationNode = createDestinationNode(file.getName(), signingDTO.getDestinationFolder(), nodeRefToSign);
									}
						            
						            if (destinationNode != null) {
							            
						            	final ContentWriter writer = contentService.getWriter(destinationNode, ContentModel.PROP_CONTENT, true);
							            if (writer != null) {
								            writer.setEncoding(fileToSignContentReader.getEncoding());
								            writer.setMimetype("application/pdf");
								            writer.putContent(file);
								            file.delete();
								            
								            if (fileConverted != null) {
								            	fileConverted.delete();
								            }
							            
								            nodeService.addAspect(destinationNode, SigningModel.ASPECT_SIGNED, new HashMap<QName, Serializable>());
								            nodeService.setProperty(destinationNode, SigningModel.PROP_REASON, signingDTO.getSignReason());
								            nodeService.setProperty(destinationNode, SigningModel.PROP_LOCATION, signingDTO.getSignLocation());
								            nodeService.setProperty(destinationNode, SigningModel.PROP_SIGNATUREDATE, new java.util.Date());
								            nodeService.setProperty(destinationNode, SigningModel.PROP_SIGNEDBY, AuthenticationUtil.getRunAsUser());
								            
								            if (newName != null) {
								            	nodeService.setProperty(destinationNode, ContentModel.PROP_NAME, newName);
								            }
								            
								            final X509Certificate c = (X509Certificate) ks.getCertificate(alias);
								            nodeService.setProperty(destinationNode, SigningModel.PROP_VALIDITY, c.getNotAfter());
								            nodeService.setProperty(destinationNode, SigningModel.PROP_ORIGINAL_DOC, originalDoc);
								            
								            if (!addAsNewVersion) {
									            if (!nodeService.hasAspect(originalDoc, SigningModel.ASPECT_ORIGINAL_DOC)) {
									            	nodeService.addAspect(originalDoc, SigningModel.ASPECT_ORIGINAL_DOC, new HashMap<QName, Serializable>());
									            }
									            nodeService.createAssociation(originalDoc, destinationNode, SigningModel.PROP_RELATED_DOC);
								            }
								            
							            }
						            } else {
						            	log.error("[" + fileNameToSign + "] Destination node is not a valid NodeRef.");
						            	return new AlfrescoRuntimeException("[" + fileNameToSign + "] Destination node is not a valid NodeRef.");
						            }
								} else {
									log.error("[" + fileNameToSign + "] Unable to get PDF appearance signature.");
									return new AlfrescoRuntimeException("[" + fileNameToSign + "] Unable to get PDF appearance signature.");
								}
					        } else {
					        	log.error("[" + fileNameToSign + "] Unable to create PDF signature.");
					        	return new AlfrescoRuntimeException("[" + fileNameToSign + "] Unable to create PDF signature.");
							}
				        }
			        }
				} else {
					log.error("[" + fileNameToSign + "] Unable to get document to sign content.");
					return new AlfrescoRuntimeException("[" + fileNameToSign + "] Unable to get document to sign content.");
				}
				
				if (pdfAFile != null) {
					pdfAFile.delete();
				}
				
				return null;
				
			} else {
				log.error("[" + fileNameToSign + "] The document has no content.");
				return new AlfrescoRuntimeException("[" + fileNameToSign + "] The document has no content.");
			}
		} catch (KeyStoreException e) {
			log.error("[" + fileNameToSign + "] " + e);
			return new AlfrescoRuntimeException("[" + fileNameToSign + "] " + e.getMessage(), e);
		} catch (ContentIOException e) {
			log.error("[" + fileNameToSign + "] " + e);
			return new AlfrescoRuntimeException("[" + fileNameToSign + "] " + e.getMessage(), e);
		} catch (IOException e) {
			log.error("[" + fileNameToSign + "] " + e);
			return new AlfrescoRuntimeException("[" + fileNameToSign + "] " + e.getMessage(), e);
		} catch (DocumentException e) {
			log.error("[" + fileNameToSign + "] " + e);
			return new AlfrescoRuntimeException("[" + fileNameToSign + "] " + e.getMessage(), e);
		//} catch (GeneralSecurityException e) {
		//	log.error("[" + fileNameToSign + "] " + e);
		//	return new AlfrescoRuntimeException("[" + fileNameToSign + "] " + e.getMessage(), e);
		} finally {
            if (tempDir != null) {
                try {
                    tempDir.delete();
                } catch (Exception ex) {
                	log.error("[" + fileNameToSign + "] " + ex);
                	return new AlfrescoRuntimeException("[" + fileNameToSign + "] " + ex.getMessage(), ex);
                }
            }
        }
	}
	
	/**
	 * XADES signing.
	 * 
	 * @param nodeRefToSign NodeRef to sign
	 * @param signingDTO SigningDTO object
	 * @param alfTempDir Alfresco temporary directoru
	 * @param alias Sign Alias to use
	 * @param ks KeyStore
	 * @param key Key
	 * @param chain Chain
	 * @return null or an error during signing process
	 */
	private AlfrescoRuntimeException signXMLFile(final NodeRef nodeRefToSign, final DigitalSigningDTO signingDTO, final File alfTempDir, final String alias, final KeyStore ks, final PrivateKey key, final Certificate[] chain) {
		final String fileNameToSign = fileFolderService.getFileInfo(nodeRefToSign).getName();
		File tempDir = null;
		String newName = null;
		if (fileNameToSign.lastIndexOf(".") == -1) {
			newName = fileNameToSign + ".xml";
		} else {
			newName = fileNameToSign.substring(0, fileNameToSign.lastIndexOf(".")) + ".xml";
		}
		
		try {
			ContentReader fileToSignContentReader = getReader(nodeRefToSign);
			tempDir = new File(alfTempDir.getPath() + File.separatorChar + nodeRefToSign.getId());
	        if (tempDir != null) {
				tempDir.mkdir();
				final File file = new File(tempDir, fileNameToSign);
				NodeRef destinationNode = null;
				NodeRef originalDoc = null;
				boolean addAsNewVersion = false;
				if (signingDTO.getDestinationFolder() == null) {
					destinationNode = nodeRefToSign;
					nodeService.addAspect(destinationNode, ContentModel.ASPECT_VERSIONABLE, null);
					addAsNewVersion = true;
				} else {
					originalDoc = nodeRefToSign;
					destinationNode = createDestinationNode(file.getName(), signingDTO.getDestinationFolder(), nodeRefToSign);
				}
		            
		        if (destinationNode != null) {
		        	final ContentWriter writer = contentService.getWriter(destinationNode, ContentModel.PROP_CONTENT, true);
			        if (writer != null) {
			        	writer.setEncoding(fileToSignContentReader.getEncoding());
			        	writer.setMimetype(MimetypeMap.MIMETYPE_XML);
				        
			        	final org.w3c.dom.Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fileToSignContentReader.getContentInputStream());
				        Init.init();
				        ElementProxy.setDefaultPrefix(Constants.SignatureSpecNS, "");
				        //final KeyStore ks = KeyStore.getInstance("pkcs12");
				        //ks.load(fileInputStream, password.toCharArray());
				        final XMLSignature sig = new XMLSignature(doc, null, XMLSignature.ALGO_ID_SIGNATURE_RSA);
				        final Transforms transforms = new Transforms(doc);
				        transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
				        sig.addDocument("", transforms, Constants.ALGO_ID_DIGEST_SHA1);
				        //final Key key = ks.getKey(alias, password.toCharArray());
				        final X509Certificate cert = (X509Certificate)ks.getCertificate(alias);
				        sig.addKeyInfo(cert);
				        sig.addKeyInfo(cert.getPublicKey());
				        sig.sign(key);
				        
				        
				        if (signingDTO.isDetached()) {
				        	writer.putContent(new ByteArrayInputStream(Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS).canonicalizeSubtree(sig.getElement())));
				        } else {
				        	doc.getDocumentElement().appendChild(sig.getElement());
				        	writer.putContent(new ByteArrayInputStream(Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS).canonicalizeSubtree(doc)));
				        }
				        file.delete();
			        	
			        	nodeService.addAspect(destinationNode, SigningModel.ASPECT_SIGNED, new HashMap<QName, Serializable>());
			        	nodeService.setProperty(destinationNode, SigningModel.PROP_REASON, signingDTO.getSignReason());
			        	nodeService.setProperty(destinationNode, SigningModel.PROP_LOCATION, signingDTO.getSignLocation());
			        	nodeService.setProperty(destinationNode, SigningModel.PROP_SIGNATUREDATE, new java.util.Date());
			        	nodeService.setProperty(destinationNode, SigningModel.PROP_SIGNEDBY, AuthenticationUtil.getRunAsUser());
				            
			        	if (newName != null) {
			        		nodeService.setProperty(destinationNode, ContentModel.PROP_NAME, newName);
			        	}
				            
			        	final X509Certificate c = (X509Certificate) ks.getCertificate(alias);
			        	nodeService.setProperty(destinationNode, SigningModel.PROP_VALIDITY, c.getNotAfter());
			        	nodeService.setProperty(destinationNode, SigningModel.PROP_ORIGINAL_DOC, originalDoc);
				            
			        	if (!addAsNewVersion) {
			        		if (!nodeService.hasAspect(originalDoc, SigningModel.ASPECT_ORIGINAL_DOC)) {
			        			nodeService.addAspect(originalDoc, SigningModel.ASPECT_ORIGINAL_DOC, new HashMap<QName, Serializable>());
			        		}
			        		nodeService.createAssociation(originalDoc, destinationNode, SigningModel.PROP_RELATED_DOC);
			        	}
		            } else {
		            	log.error("[" + fileNameToSign + "] Destination node is not a valid NodeRef.");
		            	return new AlfrescoRuntimeException("[" + fileNameToSign + "] Destination node is not a valid NodeRef.");
		            }
				}
	        }
	        return null;
		} catch (ContentIOException e) {
			log.error("[" + fileNameToSign + "] " + e);
			return new AlfrescoRuntimeException("[" + fileNameToSign + "] " + e.getMessage(), e);
		} catch (SAXException e) {
			log.error("[" + fileNameToSign + "] " + e);
			return new AlfrescoRuntimeException("[" + fileNameToSign + "] " + e.getMessage(), e);
		} catch (IOException e) {
			log.error("[" + fileNameToSign + "] " + e);
			return new AlfrescoRuntimeException("[" + fileNameToSign + "] " + e.getMessage(), e);
		} catch (ParserConfigurationException e) {
			log.error("[" + fileNameToSign + "] " + e);
			return new AlfrescoRuntimeException("[" + fileNameToSign + "] " + e.getMessage(), e);
		} catch (XMLSecurityException e) {
			log.error("[" + fileNameToSign + "] " + e);
			return new AlfrescoRuntimeException("[" + fileNameToSign + "] " + e.getMessage(), e);
		} catch (KeyStoreException e) {
			log.error("[" + fileNameToSign + "] " + e);
			return new AlfrescoRuntimeException("[" + fileNameToSign + "] " + e.getMessage(), e);
		} finally {
            if (tempDir != null) {
                try {
                    tempDir.delete();
                } catch (Exception ex) {
                	log.error("[" + fileNameToSign + "] " + ex);
                	return new AlfrescoRuntimeException("[" + fileNameToSign + "] " + ex.getMessage(), ex);
                }
            }
        }
    }
	
	/**
	 * CADES Signing.
	 * 
	 * @param nodeRefToSign NodeRef to sign
	 * @param signingDTO SigningDTO object
	 * @param alfTempDir Alfresco temporary directoru
	 * @param alias Sign Alias to use
	 * @param ks KeyStore
	 * @param key Key
	 * @param chain Chain
	 * @return null or an error during signing process
	 */
	private AlfrescoRuntimeException signFile(final NodeRef nodeRefToSign, final DigitalSigningDTO signingDTO, final File alfTempDir, final String alias, final KeyStore ks, final PrivateKey key, final Certificate[] chain) {	        
		final String fileNameToSign = fileFolderService.getFileInfo(nodeRefToSign).getName();
    	
		//boolean addsignature = true; //TODO add check if file already signed
    	boolean isDetached = signingDTO.isDetached();
		
		File tempDir = null;
		String newName = null;
		if (fileNameToSign.lastIndexOf(".") == -1) {
			if(isDetached){
				newName = fileNameToSign + ".p7s";
			}else{
				newName = fileNameToSign + ".p7m";
			}
		} else {
			if(isDetached){
				//newName = fileNameToSign.substring(0, fileNameToSign.lastIndexOf(".")) + ".p7s";
				newName = fileNameToSign + ".p7s";
			}else{
				//newName = fileNameToSign.substring(0, fileNameToSign.lastIndexOf(".")) + ".p7m";
				newName = fileNameToSign + ".p7m";
			}							
		}	
		tempDir = new File(alfTempDir.getPath() + File.separatorChar + nodeRefToSign.getId());
	    
        try{
        
			if (tempDir != null) {
				tempDir.mkdir();
				final File file = new File(tempDir, fileNameToSign);
	        
		        ContentReader fileToSignContentReader = getReader(nodeRefToSign);
		      
				NodeRef destinationNode = null;
				NodeRef originalDoc = null;
				boolean addAsNewVersion = false;
		        if (signingDTO.getDestinationFolder() == null) {
					destinationNode = nodeRefToSign;
					nodeService.addAspect(destinationNode, ContentModel.ASPECT_VERSIONABLE, null);
					addAsNewVersion = true;
				} else {
					originalDoc = nodeRefToSign;
					destinationNode = createDestinationNode(file.getName(), signingDTO.getDestinationFolder(), nodeRefToSign);
				}
		        
		        final ContentWriter writer = contentService.getWriter(destinationNode, ContentModel.PROP_CONTENT, true);
		        if (writer != null) {
		        	writer.setEncoding(fileToSignContentReader.getEncoding());
		        	writer.setMimetype("application/pkcs7-mime");
		        	try{
		        		
			        	Certificate certificate = ks.getCertificate(alias);
						List<Certificate> certList = new ArrayList<Certificate>();
						certList.add((X509Certificate) certificate);
						if(chain!=null & chain.length>0)certList.addAll(Arrays.asList(chain));

						CMSSignedDataGenerator signGen = new CMSSignedDataGenerator(); // Building the CMS Signature		
						CertStore certs = CertStore.getInstance("Collection", new CollectionCertStoreParameters(certList), "BC");// Adding CRLs and certificates
						signGen.addSigner(key, (X509Certificate) certificate,  CMSSignedDataGenerator.DIGEST_SHA256); // TODO make selectable from form			
						signGen.addCertificatesAndCRLs(certs);				

						byte[] contentToSign = IOUtils.toByteArray(fileToSignContentReader.getContentInputStream());						
					    CMSSignedData signedData = signGen.generate((CMSProcessable)new CMSProcessableByteArray(contentToSign), true, "BC");
					    
					    CMSProcessable content=null;

						// Load content to sign
						//if (isDetached ){
						//	content =  ((CMSProcessableByteArray)signedData.getSignedContent());
						//}else{
							//content = new CMSProcessableByteArray(contentToSign);	
				        	content =  ((CMSProcessableByteArray)signedData.getSignedContent());
						//}
						
				        @SuppressWarnings("unused")
						boolean addsignature = certVerify(signedData, certs,certificate,true);
				        
				    	//Load existing signature
				        /*
						if (addsignature) {
							if (signedData!=null){
								SignerInformationStore signers = signedData.getSignerInfos();
								ContentInfo ci = signedData.getContentInfo();
								CertStore existingCerts=signedData.getCertificatesAndCRLs("Collection", "BC");
								X509Store x509Store=signedData.getAttributeCertificates("Collection", "BC");					
								signGen.addCertificatesAndCRLs(existingCerts);	//add existing certs					
								signGen.addAttributeCertificates(x509Store);//add existing certs attributes						
								signGen.addSigners(signers);//add existing signers
							}
						}
						*/
				        	        
						// Generate CMS/PKCS#7 Signature
						signedData = signGen.generate(CMSSignedGenerator.DATA, content, !isDetached, "BC");							
						SignedData  sd = SignedData.getInstance(signedData.getContentInfo().getContent());					
						ContentInfo contentInfo = new ContentInfo(CMSObjectIdentifiers.signedData, sd);	
						
						writer.putContent(new ByteArrayInputStream(contentInfo.getEncoded()));
		        	}catch(Exception ex){
		        		return new AlfrescoRuntimeException(ex.getMessage(),ex);
		        	}
			        file.delete();
		        	
		        	nodeService.addAspect(destinationNode, SigningModel.ASPECT_SIGNED, new HashMap<QName, Serializable>());
		        	nodeService.setProperty(destinationNode, SigningModel.PROP_REASON, signingDTO.getSignReason());
		        	nodeService.setProperty(destinationNode, SigningModel.PROP_LOCATION, signingDTO.getSignLocation());
		        	nodeService.setProperty(destinationNode, SigningModel.PROP_SIGNATUREDATE, new java.util.Date());
		        	nodeService.setProperty(destinationNode, SigningModel.PROP_SIGNEDBY, AuthenticationUtil.getRunAsUser());
			            
		        	if (newName != null) {
		        		nodeService.setProperty(destinationNode, ContentModel.PROP_NAME, newName);
		        	}
			            
		        	final X509Certificate c = (X509Certificate) ks.getCertificate(alias);
		        	nodeService.setProperty(destinationNode, SigningModel.PROP_VALIDITY, c.getNotAfter());
		        	nodeService.setProperty(destinationNode, SigningModel.PROP_ORIGINAL_DOC, originalDoc);
			            
		        	if (!addAsNewVersion) {
		        		if (!nodeService.hasAspect(originalDoc, SigningModel.ASPECT_ORIGINAL_DOC)) {
		        			nodeService.addAspect(originalDoc, SigningModel.ASPECT_ORIGINAL_DOC, new HashMap<QName, Serializable>());
		        		}
		        		nodeService.createAssociation(originalDoc, destinationNode, SigningModel.PROP_RELATED_DOC);
		        	}
		        }
			}
	        return null;
        }catch(Exception ex){
        	return new AlfrescoRuntimeException(ex.getMessage(),ex);
        }   
	}
	
	/**
	 * Sign file.
	 * 
	 * @param signingDTO sign informations
	 * @param pdfSignedFile signed pdf returned
	 * 
	 */
	public void sign(final DigitalSigningDTO signingDTO) {
		if (signingDTO != null) {
			
			try {
				Security.addProvider(new BouncyCastleProvider());
				final File alfTempDir = TempFileProvider.getTempDir();
				
				if (alfTempDir != null) {
					final String keyType = (String) nodeService.getProperty(signingDTO.getKeyFile(), SigningModel.PROP_KEYTYPE);
					
					if (SigningConstants.KEY_TYPE_X509.equals(keyType)) {
						// Sign the file
						final KeyStore ks = KeyStore.getInstance("pkcs12");
						final ContentReader keyContentReader = getReader(signingDTO.getKeyFile());
						
						if (keyContentReader != null && ks != null && signingDTO.getKeyPassword() != null) {
							
							final List<AlfrescoRuntimeException> errors = new ArrayList<AlfrescoRuntimeException>();
							
							// Get crypted secret key and decrypt it
							final Serializable encryptedPropertyValue = nodeService.getProperty(signingDTO.getKeyFile(), SigningModel.PROP_KEYCRYPTSECRET);
							final Serializable decryptedPropertyValue = metadataEncryptor.decrypt(SigningModel.PROP_KEYCRYPTSECRET, encryptedPropertyValue);
							
							// Decrypt key content
							InputStream decryptedKeyContent;
							try {
								decryptedKeyContent = CryptUtils.decrypt(decryptedPropertyValue.toString(), keyContentReader.getContentInputStream());
							} catch (Throwable e) {
								log.error(e);
								throw new AlfrescoRuntimeException(e.getMessage(), e);
							}
							
							ks.load(new ByteArrayInputStream(IOUtils.toByteArray(decryptedKeyContent)), signingDTO.getKeyPassword().toCharArray());
							
							final String alias = (String) nodeService.getProperty(signingDTO.getKeyFile(), SigningModel.PROP_KEYALIAS);
							
							final PrivateKey key = (PrivateKey)ks.getKey(alias, signingDTO.getKeyPassword().toCharArray());
							final Certificate[] chain = ks.getCertificateChain(alias);
							
							final Iterator<NodeRef> itFilesToSign = signingDTO.getFilesToSign().iterator();
							while (itFilesToSign.hasNext()) {
								final NodeRef nodeRefToSign = itFilesToSign.next();
								AlfrescoRuntimeException exception = null;
								
								final String fileToSignName = (String) nodeService.getProperty(nodeRefToSign, ContentModel.PROP_NAME);
								// XADES
								if (fileToSignName.endsWith(".xml")) {
									exception = signXMLFile(nodeRefToSign, signingDTO, alfTempDir, alias, ks, key, chain);
								}
								// PADES
								else if(fileToSignName.endsWith(".pdf")){
									final Locale signLocale = new Locale(signingDTO.getLocale());
									exception = signPDFFile(nodeRefToSign, signingDTO, alfTempDir, alias, ks, key, chain, signLocale);
								}
								// CADES
								else{			
									exception = signFile(nodeRefToSign, signingDTO, alfTempDir, alias, ks, key, chain);
								}
								if (exception != null) {
									// Error on the file process
									errors.add(exception);
								}
							}
						
							if (errors != null && errors.size() > 0) {
								final StringBuffer allErrors = new StringBuffer();
								final Iterator<AlfrescoRuntimeException> itErrors = errors.iterator();
								if (errors.size() > 1) {
									allErrors.append("\n");
								}
								while (itErrors.hasNext()) {
									final AlfrescoRuntimeException alfrescoRuntimeException = itErrors.next();
									allErrors.append(alfrescoRuntimeException.getMessage());
									if (itErrors.hasNext()) {
										allErrors.append("\n");
									}
								}
								throw new RuntimeException(allErrors.toString());
							}
						
						} else {
							log.error("Unable to get key content, key type or key password.");
							throw new AlfrescoRuntimeException("Unable to get key content, key type or key password.");
						}
					}
				} else {
					log.error("Unable to get temporary directory.");
					throw new AlfrescoRuntimeException("Unable to get temporary directory.");
				}
			} catch (KeyStoreException e) {
				log.error(e);
				throw new AlfrescoRuntimeException(e.getMessage(), e);
			} catch (NoSuchAlgorithmException e) {
				log.error(e);
				throw new AlfrescoRuntimeException(e.getMessage(), e);
			} catch (CertificateException e) {
				log.error(e);
				throw new AlfrescoRuntimeException(e.getMessage(), e);
			} catch (IOException e) {
				log.error(e);
				throw new AlfrescoRuntimeException(e.getMessage(), e);
			} catch (UnrecoverableKeyException e) {
				log.error(e);
				throw new AlfrescoRuntimeException(e.getMessage(), e);
			}
		} else {
			log.error("No object with signing informations.");
			throw new AlfrescoRuntimeException("No object with signing informations.");
		}
	}
	
	
	public List<VerifyResultDTO> verifySign(final VerifyingDTO verifyingDTO) {
		final List<VerifyResultDTO> result = new ArrayList<VerifyResultDTO>();
		try {
			if (verifyingDTO != null) {
				final String keyType = (String) nodeService.getProperty(verifyingDTO.getKeyFile(), SigningModel.PROP_KEYTYPE);
				
				final KeyStore ks = KeyStore.getInstance(keyType);
				final ContentReader keyContentReader = getReader(verifyingDTO.getKeyFile());
				if (keyContentReader != null && ks != null  && verifyingDTO.getKeyPassword() != null) {
					
					// Get crypted secret key and decrypt it
					final Serializable encryptedPropertyValue = nodeService.getProperty(verifyingDTO.getKeyFile(), SigningModel.PROP_KEYCRYPTSECRET);
					final Serializable decryptedPropertyValue = metadataEncryptor.decrypt(SigningModel.PROP_KEYCRYPTSECRET, encryptedPropertyValue);
					
					// Decrypt key content
					final InputStream decryptedKeyContent = CryptUtils.decrypt(decryptedPropertyValue.toString(), keyContentReader.getContentInputStream());
					
					ks.load(new ByteArrayInputStream(IOUtils.toByteArray(decryptedKeyContent)), verifyingDTO.getKeyPassword().toCharArray());
					
					
					final ContentReader fileToVerifyContentReader = getReader(verifyingDTO.getFileToVerify());
					if (fileToVerifyContentReader != null) {
						final PdfReader reader = new PdfReader(fileToVerifyContentReader.getContentInputStream());
						if (reader != null) {
							final AcroFields af = reader.getAcroFields();
							if (af != null) {
								final ArrayList<String> names = af.getSignatureNames();
								if (names != null) {
									for (int k = 0; k < names.size(); ++k) {
										final VerifyResultDTO verifyResultDTO = new VerifyResultDTO();
										final String name = (String)names.get(k);
										verifyResultDTO.setName(name);
										verifyResultDTO.setSignatureCoversWholeDocument(af.signatureCoversWholeDocument(name));
										verifyResultDTO.setRevision(af.getRevision(name));
										verifyResultDTO.setTotalRevision(af.getTotalRevisions());
									   
										final PdfPKCS7 pk = af.verifySignature(name);
										if (pk != null) {
								            @SuppressWarnings("unused")
											final Calendar cal = pk.getSignDate();
								            @SuppressWarnings("unused")
											final Certificate[] pkc = pk.getCertificates();
								            //ITEXT 5.5.11
								            //final List<VerificationException> errors = CertificateVerification.verifyCertificates(pkc, ks, null, cal);
								            //ITEXT 5.1.3
							                X509Certificate certificate = pk.getSigningCertificate();
							                Date date = (pk.getSignDate() != null) ?pk.getSignDate().getTime():null;
							        		String principal = certificate.getSubjectX500Principal().toString();
							        		String serialNumber =  certificate.getSerialNumber().toString();
							        	    Date certificateNotAfter = certificate.getNotAfter();
							        	    String certificateIssuer =  certificate.getIssuerX500Principal().toString();   
							        	    final List<Exception> errors = Arrays.asList(new Exception("Date="+date.getTime()+", Principal="+principal+", SerialNumber="+serialNumber+", CertificateNotAfter="+certificateNotAfter+", Issuer="+certificateIssuer));
								            
								            if (errors.size() == 0) {
								            	verifyResultDTO.setIsSignValid(true);
								            } else {
								            	verifyResultDTO.setIsSignValid(false);
								 		   		verifyResultDTO.setFailReason(errors.get(0).getMessage());
								            }
								            
								            verifyResultDTO.setSignSubject(pk.getSigningCertificate().getSubjectDN().getName());           
											verifyResultDTO.setIsDocumentModified(!pk.verify());
											verifyResultDTO.setSignDate(pk.getSignDate());
											verifyResultDTO.setSignLocation(pk.getLocation());
											verifyResultDTO.setSignInformationVersion(pk.getSigningInfoVersion());
											verifyResultDTO.setSignReason(pk.getReason());
											verifyResultDTO.setSignVersion(pk.getVersion());
											verifyResultDTO.setSignName(pk.getSignName());
											
											result.add(verifyResultDTO);
										} else {
											log.error("Unable to verify signature.");
											throw new AlfrescoRuntimeException("Unable to verify signature.");
										}
									}
								} else {
									log.error("Unable to get signature names.");
									throw new AlfrescoRuntimeException("Unable to get signature names.");
								}
							} else {
								log.error("Unable to get PDF fields.");
								throw new AlfrescoRuntimeException("Unable to get PDF fields.");
							}
						}
					} else {
						log.error("Unable to get document to verify content.");
						throw new AlfrescoRuntimeException("Unable to get document to verify content.");
					}
				} else {
					log.error("Unable to get key content, key type or key password.");
					throw new AlfrescoRuntimeException("Unable to get key content, key type or key password.");
				}
			} else {
				log.error("No object with verification informations.");
				throw new AlfrescoRuntimeException("No object with verification informations.");
			}
		} catch (KeyStoreException e) {
			log.error(e);
			throw new AlfrescoRuntimeException(e.getMessage(), e);
		} catch (ContentIOException e) {
			log.error(e);
			throw new AlfrescoRuntimeException(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			log.error(e);
			throw new AlfrescoRuntimeException(e.getMessage(), e);
		} catch (CertificateException e) {
			log.error(e);
			throw new AlfrescoRuntimeException(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e);
			throw new AlfrescoRuntimeException(e.getMessage(), e);
		} catch (GeneralSecurityException e) {
			log.error(e);
			throw new AlfrescoRuntimeException(e.getMessage(), e);
		} catch (Throwable e) {
			log.error(e);
			throw new AlfrescoRuntimeException(e.getMessage(), e);
		}
		
		return result;
	}
	

	
	
    /**
     * Create a rectangle for the visible signature using the selected position and signature size
     * 
     * @param position
     * @param width
     * @param height
     * @return
     */
    private static Rectangle positionSignature(final String position, final Rectangle pageRect, final int width, final int height, final int marginX, final int marginY) {
        final float pageHeight = pageRect.getHeight();
        final float pageWidth = pageRect.getWidth();
        Rectangle r = null;
        if (position.equals(DigitalSigningDTO.POSITION_BOTTOMLEFT)) {
            r = new Rectangle(marginX, marginY, width + marginX, height + marginY);
        } else if (position.equals(DigitalSigningDTO.POSITION_BOTTOMRIGHT)) {
            r = new Rectangle(pageWidth - width - marginX, height + marginY, pageWidth - marginX, marginY);
        } else if (position.equals(DigitalSigningDTO.POSITION_TOPLEFT)) {
            r = new Rectangle(marginX, pageHeight - marginY , width + marginX, pageHeight - height - marginY);
        } else if (position.equals(DigitalSigningDTO.POSITION_TOPRIGHT)) {
        	r = new Rectangle(pageWidth - width - marginX, pageHeight - marginY, pageWidth - marginX, pageHeight - height - marginY);
        } else if (position.equals(DigitalSigningDTO.POSITION_CENTER)) {
            r = new Rectangle((pageWidth / 2) - (width / 2), (pageHeight / 2) - (height / 2),
                                (pageWidth / 2) + (width / 2), (pageHeight / 2) + (height / 2));
        }
        return r;
    }
    
    /**
     * Determines whether or not a watermark should be applied to a given page
     * 
     * @param pagespage choice
     * @param current current page
     * @param numpages total number of page
     * @return if current page match with page choice 
     */
    protected static boolean checkPage(final String pages, final int current, final int numpages) {
        boolean markPage = false;
        /*
        if (pages.equals(DigitalSigningDTO.PAGE_EVEN) || pages.equals(DigitalSigningDTO.PAGE_ODD)) {
            if (current % 2 == 0) {
                markPage = true;
            }
        } else if (pages.equals(DigitalSigningDTO.PAGE_ODD)) {
            if (current % 2 != 0) {
                markPage = true;
            }
        } else 
        */
        if (pages.equals(DigitalSigningDTO.PAGE_FIRST)) {
            if (current == 1) {
                markPage = true;
            }
        } else if (pages.equals(DigitalSigningDTO.PAGE_LAST)) {
            if (current == numpages) {
                markPage = true;
            }
        } else if (pages.equals(DigitalSigningDTO.PAGE_LAST)) {
            if (current == numpages) {
                markPage = true;
            }
        } else {
            markPage = true;
        }
        
        return markPage;
    }
	
    /**
     * Gets the X value for centering the watermark image
     * 
     * @param r rectangle
     * @param img image
     * @return
     */
    protected static float getCenterX(final Rectangle r, final Image img) {
        float x = 0;
        final float pdfwidth = r.getWidth();
        final float imgwidth = img.getWidth();
        x = (pdfwidth - imgwidth) / 2;
        return x;
    }


    /**
     * Gets the Y value for centering the watermark image
     * 
     * @param r rectangle
     * @param img image
     * @return
     */
    protected static float getCenterY(final Rectangle r, final Image img) {
        float y = 0;
        final float pdfheight = r.getHeight();
        final float imgheight = img.getHeight();
        y = (pdfheight - imgheight) / 2;
        return y;
    }
    
    
    protected static BufferedImage scaleImage(final BufferedImage image, final int imageType, int newWidth, int newHeight) {
        // Make sure the aspect ratio is maintained, so the image is not distorted
    	final double thumbRatio = (double) newWidth / (double) newHeight;
    	final int imageWidth = image.getWidth(null);
    	final int imageHeight = image.getHeight(null);
    	final double aspectRatio = (double) imageWidth / (double) imageHeight;

        if (thumbRatio < aspectRatio) {
            newHeight = (int) (newWidth / aspectRatio);
        } else {
            newWidth = (int) (newHeight * aspectRatio);
        }

        // Draw the scaled image
        final BufferedImage newImage = new BufferedImage(newWidth, newHeight, imageType);
        final Graphics2D graphics2D = newImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(image, 0, 0, newWidth, newHeight, null);
        graphics2D.dispose();

        return newImage;
    }
    
    /**
     * @param actionedUponNodeRef
     * @return
     */
    protected ContentReader getReader(NodeRef nodeRef) {
        // First check that the node is a sub-type of content
        QName typeQName = nodeService.getType(nodeRef);
        if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT) == false) {
            // it is not content, so can't transform
            return null;
        }

        // Get the content reader
        ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

        return contentReader;
    }
    
    /**
     * @param ruleAction
     * @param filename
     * @return
     */
    protected NodeRef createDestinationNode(String filename, NodeRef destinationParent, NodeRef target) {
        NodeRef destinationNode;
        FileInfo fileInfo = fileFolderService.create(destinationParent, filename, ContentModel.TYPE_CONTENT);
        destinationNode = fileInfo.getNodeRef();

        return destinationNode;
    }

    private File convertPdfToPdfA(final InputStream source) throws IOException, DocumentException {
        File tempFile = null;       
        File pdfAFile = null;
        OutputStream output = null;
        try {
            tempFile = TempFileProvider.createTempFile("pre_pdfa", ".pdf");           
            FileUtils.copyInputStreamToFile(source, tempFile);          
            pdfAFile = TempFileProvider.createTempFile("digitalSigning-" + System.currentTimeMillis(), ".pdf");
            //METHOD WITH ITEXT 5
            /*
            java.net.URL url = getClass().getResource("/org/alfresco/plugin/digitalSigning/service/sRGB_CS_profile.icm");
        	byte[] bytes = IOUtils.toByteArray(url.openStream());
        	final ICC_Profile icc = ICC_Profile.getInstance(bytes);
        	
            //Reads a PDF document. 
            PdfReader reader = new PdfReader(source); 
            //PdfStamper: Applies extra content to the pages of a PDF document. This extra content can be all the objects allowed in 
            //PdfContentByte including pages from other Pdfs. 
            //A generic Document class. 
            Document document = new Document(); 
            // we create a writer that listens to the document
            PdfAWriter writer = PdfAWriter.getInstance(document, new FileOutputStream(pdfAFile), PdfAConformanceLevel.PDF_A_1A);       
            int numberPages = reader.getNumberOfPages(); 

            //PdfDictionary:A dictionary is an associative table containing pairs of objects. 
            //The first element of each pair is called the key and the second  element is called the value 
            //<CODE>PdfName</CODE> is an object that can be used as a name in a PDF-file 
            //PdfDictionary outi = new PdfDictionary(PdfName.OUTPUTINTENT); 
            //outi.put(PdfName.OUTPUTCONDITIONIDENTIFIER, new PdfString("sRGB IEC61966-2.1")); 
            //outi.put(PdfName.INFO, new PdfString("sRGB IEC61966-2.1")); 
            //outi.put(PdfName.S, PdfName.GTS_PDFA1); 
            //writer.getExtraCatalog().put(PdfName.OUTPUTINTENTS, new PdfArray(outi)); 

            writer.setTagged();
            writer.createXmpMetadata();
            document.open(); 
            
            writer.setOutputIntents("Custom", "", "http://www.color.org", "sRGB IEC61966-2.1", icc);

            //Add pages 
            PdfImportedPage p = null; 
            Image image; 
            for(int i=0; i< numberPages; i++){ 
            	document.newPage(); 
                p = writer.getImportedPage(reader, i+1); 
                image = Image.getInstance(p);
                // Scale PDF page to fit with PDF/A format
                image.scaleAbsolute(writer.getPageSize());
                // Center the image into the PDF/A page
                image.setAlignment(Element.ALIGN_CENTER);
                
                // Set the position of the image into the PDF/A page
                image.setAbsolutePosition(0, 0);
                document.setMargins(0, 0, 0, 0);
                document.add(image); 
            }
            
            document.close(); 
            writer.flush();

            return pdfAFile;
            */
            //METHOD WITH PDFBOX 1.8.10
            //https://apache.googlesource.com/pdfbox/+/4df9353eaac3c4ee2124b09da05312376f021b2c/examples/src/main/java/org/apache/pdfbox/examples/pdfa/CreatePDFA.java
    		PDDocument doc = null;
    		try{
    			doc = PDDocument.load(tempFile);
    		}catch(IOException ex){
    			if(ex.getMessage().contains("expected='endstream'")){
    				//https://issues.apache.org/jira/browse/PDFBOX-1541
    				//https://www.programcreek.com/java-api-examples/?code=jmrozanec/pdf-converter/pdf-converter-master/src/main/java/pdf/converter/txt/TxtCreator.java				
    				File tmpfile = File.createTempFile(String.format("txttmp-%s", UUID.randomUUID().toString()), null);
    	            try{
    					org.apache.pdfbox.io.RandomAccessFile raf = new org.apache.pdfbox.io.RandomAccessFile(tmpfile, "rw");
    		            doc = PDDocument.loadNonSeq(tmpfile,raf);	   	
    	            }finally{
    	            	FileUtils.deleteQuietly(tmpfile);
    	            }
    			}else{
    				throw ex;
    			}
    		}   
            try
            {           
//                // load the font from pdfbox.jar
//                InputStream fontStream = CreatePDFA.class.getResourceAsStream("/org/apache/pdfbox/resources/ttf/ArialMT.ttf");
//                PDFont font = PDTrueTypeFont.loadTTF(doc, fontStream);
//                
//                
//                // create a page with the message where needed
//                PDPageContentStream contentStream = new PDPageContentStream(doc, page);
//                contentStream.beginText();
//                contentStream.setFont( font, 12 );
//                contentStream.moveTextPositionByAmount( 100, 700 );
//                contentStream.drawString( message );
//                contentStream.endText();
//                contentStream.saveGraphicsState();
//                contentStream.close();

                PDDocumentCatalog cat = doc.getDocumentCatalog();
                PDMetadata metadata = new PDMetadata(doc);
                cat.setMetadata(metadata);

                XMPMetadata xmp = XMPMetadata.createXMPMetadata();
                try
                {
                    PDFAIdentificationSchema pdfaid = xmp.createAndAddPFAIdentificationSchema();
                    pdfaid.setConformance("B");
                    pdfaid.setPart(1);
                    pdfaid.setAboutAsSimple("PDFBox PDFA sample");
                    XmpSerializer serializer = new XmpSerializer();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    serializer.serialize(xmp, baos, true);
                    metadata.importXMPMetadata( baos.toByteArray() );
                }
                catch(BadFieldValueException badFieldexception)
                {
                    // can't happen here, as the provided value is valid
                }
                //catch(XmpSerializationException xmpException)
                //{
                //    System.err.println(xmpException.getMessage());
                //}
                //COLOR SPACE BUG
                InputStream colorProfile = CreatePDFA.class.getResourceAsStream("/org/apache/pdfbox/resources/pdfa/sRGB Color Space Profile.icm");
                //InputStream colorProfile = getClass().getResourceAsStream("/org/alfresco/plugin/digitalSigning/service/sRGB_CS_profile.icm");
                // create output intent
                PDOutputIntent oi = new PDOutputIntent(doc, colorProfile); 
                oi.setInfo("sRGB IEC61966-2.1"); 
                oi.setOutputCondition("sRGB IEC61966-2.1");
                oi.setOutputConditionIdentifier("sRGB IEC61966-2.1"); 
                oi.setRegistryName("http://www.color.org"); 
                cat.addOutputIntent(oi);
                
                doc.save(pdfAFile);
               
            }
            finally
            {
                if( doc != null )
                {
                    doc.close();
                }
            }
            return pdfAFile;
        }
        catch (Exception ex) {
        	log.error("Can't convert PDF to PDF/A.  Error during conversion to PDF/A.", ex);
        	throw new IOException("Can't convert PDF to PDF/A.  Error during conversion to PDF/A.", ex);
        }   
        finally {
            if (tempFile != null) {
                tempFile.delete();
            }
            if(output!=null){
            	output.close();
            }
        }
    	
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
	 * @param fileFolderService the fileFolderService to set
	 */
	public final void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}


	/**
	 * @param contentTransformerRegistry the contentTransformerRegistry to set
	 */
	public final void setContentTransformerRegistry(
			ContentTransformerRegistry contentTransformerRegistry) {
		this.contentTransformerRegistry = contentTransformerRegistry;
	}


	/**
	 * @param metadataEncryptor the metadataEncryptor to set
	 */
	public final void setMetadataEncryptor(MetadataEncryptor metadataEncryptor) {
		this.metadataEncryptor = metadataEncryptor;
	}
	
	/**
	 * ITEXT 5.1.3 METHOD FOR DETACHED SINGATURE
	 * @param sap
	 * @param digitalSigningDTO
	 * @param key
	 * @param chain
	 */
	private void signDetached(PdfSignatureAppearance sap,DigitalSigningDTO digitalSigningDTO,PrivateKey key,Certificate[] chain){
        sap.setCrypto(key, chain, null, PdfSignatureAppearance.WINCER_SIGNED);
        // set reason for signature, location of signer, and date
        sap.setReason(digitalSigningDTO.getSignReason());
        sap.setLocation(digitalSigningDTO.getSignLocation());
        sap.setSignDate(Calendar.getInstance());
        sap.setContact(digitalSigningDTO.getSignContact());
	}
	
	protected boolean certVerify(final CMSSignedData cmsSignedData, final CertStore certs,Certificate cert, boolean accepSelfSignedCertificate) throws Exception {
		boolean verify = false;
		@SuppressWarnings("unchecked")
		final Iterator<SignerInformation> iterator = (Iterator<SignerInformation>)cmsSignedData.getSignerInfos().getSigners().iterator();		
		while (iterator.hasNext()) {
			final SignerInformation signerInformation = (SignerInformation)iterator.next();
			final AttributeTable signedAttributes = signerInformation.getSignedAttributes();
			if (signedAttributes != null) {
				@SuppressWarnings("unused")
				Date signingTime = Time.getInstance((Object)signedAttributes.get((DERObjectIdentifier)CMSAttributes.signingTime).getAttrValues().getObjectAt(0)).getDate();
			}
			Collection<? extends Certificate> certificates = null;
			try {
				certificates = certs.getCertificates((CertSelector)signerInformation.getSID());
			}
			catch (CertStoreException ex) {}
			for (final Certificate certificate : certificates) {
				X509Certificate x509Certificate = (X509Certificate) certificate;
				if (cert != null) {
					try {
						x509Certificate.verify(cert.getPublicKey(), "BC");
					}
					catch (Exception cause) {
						log.error(cause.getMessage(), (Throwable)cause);
						throw new Exception(cause.getMessage(), cause);
					}					
				}
				try {
					verify = signerInformation.verify(x509Certificate, "BC");
					if(!verify && accepSelfSignedCertificate){
						//verify = isSelfSignedCertificate(x509Certificate);
						try {
							certificate.verify(certificate.getPublicKey());
							return true;
						} catch (GeneralSecurityException e) {
							return false;
						}						
					}
				}
				catch (CertificateExpiredException cause6) {
					if(accepSelfSignedCertificate){
						try {
							certificate.verify(certificate.getPublicKey());
							return true;
						} catch (GeneralSecurityException e) {
							return false;
						}	
					}else{
						log.error(cause6.getMessage(), (Throwable)cause6);
						throw new Exception(cause6.getMessage(), cause6);
					}
				}
			}
		}
		return verify;
	}
}
