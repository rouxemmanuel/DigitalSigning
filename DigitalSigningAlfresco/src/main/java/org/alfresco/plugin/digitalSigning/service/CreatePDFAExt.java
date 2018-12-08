package org.alfresco.plugin.digitalSigning.service;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.examples.pdfa.CreatePDFA;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.XmpSerializationException;
import org.apache.xmpbox.xml.XmpSerializer;

public class CreatePDFAExt extends CreatePDFA{

	
	   /**
     * Constructor.
     */
    public CreatePDFAExt()
    {
        super();
    }

    /**
     * Create a simple PDF/A document.
     * 
     * This example is based on HelloWorld example.
     * 
     * As it is a simple case, to conform the PDF/A norm, are added :
     * - the font used in the document
     * - a light xmp block with only PDF identification schema (the only mandatory)
     * - an output intent
     *
     * @param file The file to write the PDF to.
     * @param message The message to write in the file.
     *
     * @throws Exception If something bad occurs
     */
    public void doIt( String file, String message) throws Exception
    {
        // the document
        PDDocument doc = null;
        try
        {
            doc = new PDDocument();

            PDPage page = new PDPage();
            doc.addPage( page );

            // load the font from pdfbox.jar
            InputStream fontStream = CreatePDFA.class.getResourceAsStream("/org/apache/pdfbox/resources/ttf/ArialMT.ttf");
            PDFont font = PDTrueTypeFont.loadTTF(doc, fontStream);
            
            // create a page with the message where needed
            PDPageContentStream contentStream = new PDPageContentStream(doc, page);
            contentStream.beginText();
            contentStream.setFont( font, 12 );
            contentStream.moveTextPositionByAmount( 100, 700 );
            contentStream.drawString( message );
            contentStream.endText();
            contentStream.saveGraphicsState();
            contentStream.close();
            
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
            catch(XmpSerializationException xmpException)
            {
                System.err.println(xmpException.getMessage());
            }
    
            InputStream colorProfile = CreatePDFA.class.getResourceAsStream("/org/apache/pdfbox/resources/pdfa/sRGB Color Space Profile.icm");
            // create output intent
            PDOutputIntent oi = new PDOutputIntent(doc, colorProfile); 
            oi.setInfo("sRGB IEC61966-2.1"); 
            oi.setOutputCondition("sRGB IEC61966-2.1"); 
            oi.setOutputConditionIdentifier("sRGB IEC61966-2.1"); 
            oi.setRegistryName("http://www.color.org"); 
            cat.addOutputIntent(oi);
            
            doc.save( file );
           
        }
        finally
        {
            if( doc != null )
            {
                doc.close();
            }
        }
    }
    
    public void doIt(File inputPdf,File outputPdfa) throws Exception
    {     
        //https://apache.googlesource.com/pdfbox/+/4df9353eaac3c4ee2124b09da05312376f021b2c/examples/src/main/java/org/apache/pdfbox/examples/pdfa/CreatePDFA.java
		PDDocument doc = null;
		try{
			doc = PDDocument.load(inputPdf);
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
            // load the font from pdfbox.jar
            InputStream fontStream = CreatePDFA.class.getResourceAsStream("/org/apache/pdfbox/resources/ttf/ArialMT.ttf");
            PDFont font = PDTrueTypeFont.loadTTF(doc, fontStream);

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
            catch(XmpSerializationException xmpException)
            {
                System.err.println(xmpException.getMessage());
            }
    
            InputStream colorProfile = CreatePDFA.class.getResourceAsStream("/org/apache/pdfbox/resources/pdfa/sRGB Color Space Profile.icm");
            // create output intent
            PDOutputIntent oi = new PDOutputIntent(doc, colorProfile); 
            oi.setInfo("sRGB IEC61966-2.1"); 
            oi.setOutputCondition("sRGB IEC61966-2.1"); 
            oi.setOutputConditionIdentifier("sRGB IEC61966-2.1"); 
            oi.setRegistryName("http://www.color.org"); 
            cat.addOutputIntent(oi);
            
            doc.save(outputPdfa);
           
        }
        finally
        {
            if( doc != null )
            {
                doc.close();
            }
        }
    }
}
