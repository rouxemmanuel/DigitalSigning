/**
 * 
 */
package org.alfresco.plugin.digitalSigning.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.ContentTransformerRegistry;
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
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.alfresco.plugin.digitalSigning.dto.DigitalSigningDTO;
import org.alfresco.plugin.digitalSigning.dto.VerifyResultDTO;
import org.alfresco.plugin.digitalSigning.dto.VerifyingDTO;
import org.alfresco.plugin.digitalSigning.model.SigningConstants;
import org.alfresco.plugin.digitalSigning.model.SigningModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.AcroFields.FieldPosition;
import com.itextpdf.text.pdf.PdfPKCS7;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
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
	 * Content Transformer registry
	 */
	private ContentTransformerRegistry contentTransformerRegistry;
	
	/**
	 * Sign file.
	 * 
	 * @param signingDTO sign informations
	 * @param pdfSignedFile signed pdf returned
	 * 
	 */
	public void sign(final DigitalSigningDTO signingDTO) {
		if (signingDTO != null) {
			File tempDir = null;
			File fileConverted = null;
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
							ks.load(keyContentReader.getContentInputStream(), signingDTO.getKeyPassword().toCharArray());
							
							final String alias = (String) nodeService.getProperty(signingDTO.getKeyFile(), SigningModel.PROP_KEYALIAS);
							
							final PrivateKey key = (PrivateKey)ks.getKey(alias, signingDTO.getKeyPassword().toCharArray());
							final Certificate[] chain = ks.getCertificateChain(alias);
							
							ContentReader fileToSignContentReader = getReader(signingDTO.getFileToSign());
							if (fileToSignContentReader != null) {
								// Check if document is PDF or transform it
								if (!MimetypeMap.MIMETYPE_PDF.equals(fileToSignContentReader.getMimetype())) {
									// Transform document in PDF document
									final ContentTransformer tranformer = contentTransformerRegistry.getTransformer(fileToSignContentReader.getMimetype(), fileToSignContentReader.getSize(), MimetypeMap.MIMETYPE_PDF, new TransformationOptions());
									
									if (tranformer != null) {
										
										tempDir = new File(alfTempDir.getPath() + File.separatorChar + signingDTO.getFileToSign().getId());
								        if (tempDir != null) {
											tempDir.mkdir();
									        fileConverted = new File(tempDir, fileFolderService.getFileInfo(signingDTO.getFileToSign()).getName() + ".pdf");
											if (fileConverted != null) {
										        final ContentWriter newDoc = new FileContentWriter(fileConverted);
										        if (newDoc != null) {
													newDoc.setMimetype(MimetypeMap.MIMETYPE_PDF);
													tranformer.transform(fileToSignContentReader, newDoc);
													fileToSignContentReader = new FileContentReader(fileConverted);
										        }
											}
								        }
									} else {
										log.error("No suitable converter found to convert the document in PDF.");
										throw new AlfrescoRuntimeException("No suitable converter found to convert the document in PDF.");
									}
								}
							
								final PdfReader reader = new PdfReader(fileToSignContentReader.getContentInputStream());
						        
								if (signingDTO.getFileToSign() != null) {
							        tempDir = new File(alfTempDir.getPath() + File.separatorChar + signingDTO.getFileToSign().getId());
							        if (tempDir != null) {
								        tempDir.mkdir();
								        final File file = new File(tempDir, fileFolderService.getFileInfo(signingDTO.getFileToSign()).getName());
								        
								        if (file != null) {
									        final FileOutputStream fout = new FileOutputStream(file);
									        final PdfStamper stp = PdfStamper.createSignature(reader, fout, '\0');
									        
									        if (stp != null) {
												final PdfSignatureAppearance sap = stp.getSignatureAppearance();
												if (sap != null) {
													sap.setCrypto(key, chain, null, PdfSignatureAppearance.WINCER_SIGNED);
													sap.setReason(signingDTO.getSignReason());
													sap.setLocation(signingDTO.getSignLocation());
													sap.setContact(signingDTO.getSignContact());
													sap.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
													sap.setImageScale(1);
													
													// digital signature
											        final ContentReader imageContentReader = getReader(signingDTO.getImage());
													if (signingDTO.getSigningField() != null && !signingDTO.getSigningField().trim().equalsIgnoreCase("")) {
														Image img = null;
														if (imageContentReader != null) {
															final AcroFields af = reader.getAcroFields();
															if (af != null) {
																final List<FieldPosition> positions = af.getFieldPositions(signingDTO.getSigningField());
																if (positions != null && positions.size() > 0 && positions.get(0) != null && positions.get(0).position != null) {
																	final BufferedImage newImg = scaleImage(ImageIO.read(imageContentReader.getContentInputStream()), BufferedImage.TYPE_INT_RGB, Float.valueOf(positions.get(0).position.getWidth()).intValue(), Float.valueOf(positions.get(0).position.getHeight()).intValue());
																	img = Image.getInstance(newImg, null);
																} else {
																	log.error("The field '" + signingDTO.getSigningField() + "' doesn't exist in the document.");
																	throw new AlfrescoRuntimeException("The field '" + signingDTO.getSigningField() + "' doesn't exist in the document.");
																}
															}
													        if (img == null) {
													        	img = Image.getInstance(ImageIO.read(imageContentReader.getContentInputStream()), null);
													        }
													        sap.setImage(img);
														}
														sap.setVisibleSignature(signingDTO.getSigningField());
													} else {
														if (imageContentReader != null) {
															// Resize image
													        final BufferedImage newImg = scaleImage(ImageIO.read(imageContentReader.getContentInputStream()), BufferedImage.TYPE_INT_RGB, signingDTO.getSignWidth(), signingDTO.getSignHeight());
													        final Image img = Image.getInstance(newImg, null);
															sap.setImage(img);
														}
														if(signingDTO.getPosition() != null && !DigitalSigningDTO.POSITION_CUSTOM.equalsIgnoreCase(signingDTO.getPosition().trim())) {
											                final Rectangle pageRect = reader.getPageSizeWithRotation(1);
											                sap.setVisibleSignature(positionSignature(signingDTO.getPosition(), pageRect, signingDTO.getSignWidth(), signingDTO.getSignHeight(), signingDTO.getxMargin(), signingDTO.getyMargin()), 1, null);
														} else {
											                sap.setVisibleSignature(new Rectangle(signingDTO.getLocationX(), signingDTO.getLocationY(), signingDTO.getLocationX() + signingDTO.getSignWidth(), signingDTO.getLocationY() - signingDTO.getSignHeight()), 1, null);
														}
													}
													stp.close();
												
										            final NodeRef destinationNode = createDestinationNode(file.getName(), signingDTO.getDestinationFolder(), signingDTO.getFileToSign());
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
												            
												            final X509Certificate c = (X509Certificate) ks.getCertificate(alias);
												            nodeService.setProperty(destinationNode, SigningModel.PROP_VALIDITY, c.getNotAfter());
											            }
										            } else {
										            	log.error("Destination folder is not a valid NodeRef.");
										    			throw new AlfrescoRuntimeException("Destination folder is not a valid NodeRef.");
										            }
												} else {
													log.error("Unable to get PDF appearance signature.");
													throw new AlfrescoRuntimeException("Unable to get PDF appearance signature.");
												}
									        } else {
									        	log.error("Unable to create PDF signature.");
												throw new AlfrescoRuntimeException("Unable to create PDF signature.");
											}
								        }
							        }
								} else {
									log.error("Unable to get document to sign content.");
									throw new AlfrescoRuntimeException("Unable to get document to sign content.");
								}
							} else {
								log.error("The document has no content.");
								throw new AlfrescoRuntimeException("The document has no content.");
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
			} catch (UnrecoverableKeyException e) {
				log.error(e);
				throw new AlfrescoRuntimeException(e.getMessage(), e);
			} catch (DocumentException e) {
				log.error(e);
				throw new AlfrescoRuntimeException(e.getMessage(), e);
			} catch (Exception e) {
				log.error(e);
				throw new AlfrescoRuntimeException(e.getMessage(), e);
			} finally {
	            if (tempDir != null) {
	                try {
	                    tempDir.delete();
	                } catch (Exception ex) {
	                	log.error(ex);
	                    throw new AlfrescoRuntimeException(ex.getMessage(), ex);
	                }
	            }
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
					ks.load(keyContentReader.getContentInputStream(), verifyingDTO.getKeyPassword().toCharArray());
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
								            final Calendar cal = pk.getSignDate();
								            final Certificate[] pkc = pk.getCertificates();
								            Object fails[] = PdfPKCS7.verifyCertificates(pkc, ks, null, cal);
								 		   	if (fails == null) {
								 		   		verifyResultDTO.setIsSignValid(true);
								 		   	} else {
								 		   		verifyResultDTO.setIsSignValid(false);
								 		   		verifyResultDTO.setFailReason(fails[1]);   
								 		   	}
								            verifyResultDTO.setSignSubject(PdfPKCS7.getSubjectFields(pk.getSigningCertificate()).toString());           
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
        if (pages.equals(DigitalSigningDTO.PAGE_EVEN) || pages.equals(DigitalSigningDTO.PAGE_ODD)) {
            if (current % 2 == 0) {
                markPage = true;
            }
        } else if (pages.equals(DigitalSigningDTO.PAGE_ODD)) {
            if (current % 2 != 0) {
                markPage = true;
            }
        } else if (pages.equals(DigitalSigningDTO.PAGE_FIRST)) {
            if (current == 1) {
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
}