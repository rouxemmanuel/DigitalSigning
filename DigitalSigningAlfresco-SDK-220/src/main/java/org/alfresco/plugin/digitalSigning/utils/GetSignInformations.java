package org.alfresco.plugin.digitalSigning.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

public class GetSignInformations {

	public static void main(String[] args) {
		try {
			String sFile = "C:\\Users\\tenti\\Downloads\\Potts-Optaros-CMIS-Tutorial-200912.pdf";//args[0]
			String password = "";//args[1];
			final KeyStore ks = KeyStore.getInstance("pkcs12");
			ks.load(new FileInputStream(sFile), password.toCharArray());
			
			final Enumeration<String> aliases = ks.aliases();
			while(aliases.hasMoreElements()) {
				final String alias = aliases.nextElement();
				final X509Certificate c = (X509Certificate) ks.getCertificate(alias);
				
				System.out.println("* Certificate info for alias : " + alias);
				System.out.println("    - Version : " + c.getVersion());
			    System.out.println("    - Serial number : " + c.getSerialNumber().toString(16));
			    System.out.println("    - Subjetc DN : " + c.getSubjectDN());
			    System.out.println("    - Issuer DN : " + c.getIssuerDN());
			    System.out.println("    - Valide from : " + c.getNotBefore());
			    System.out.println("    - Valide to : " + c.getNotAfter());
			    System.out.println("    - Algorithm : " + c.getSigAlgName());
			}
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
