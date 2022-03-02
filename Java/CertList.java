/**
 *  CertList.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.tools;

import java.io.*;
import java.util.*;
import java.security.cert.*;

/**
 * List Certificate
 * 
 * usage: CertList [base64/pkcs7] fileName
 */
class CertList {
	private static String type;
	private static String filename;

	public static void main(String args[]) {
		if (args.length != 1 && args.length != 2) {
			System.out.println("usage: CertList [base64] file");
			System.exit(1);
		}
		if (args.length == 1) {
			type = "pkcs7";
			filename = args[0];
		} else {
			type = args[0];
			filename = args[1];
		}
		if (type.equals("pkcs7")) {
			try {
				FileInputStream fis = new FileInputStream(filename);
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				Collection<?> c = cf.generateCertificates(fis);
				Iterator<?> i = c.iterator();

				int count = 1;
				while (i.hasNext()) {
					System.out.println("< Certificate No. " + count + " >");
					//Certificate cert1 = (Certificate)i.next();
					//System.out.println(cert1);

					X509Certificate cert = (X509Certificate) i.next();
					System.out.println(" Version: V" + cert.getVersion());
					System.out.println(" Subject: " + cert.getSubjectDN());
					System.out.println(" Validity:");
					System.out.println("  From: " + cert.getNotBefore());
					System.out.println("  To  : " + cert.getNotAfter());
					System.out.println(" Issure: " + cert.getIssuerDN());
					System.out.println(" Sign Algorithm: "
							+ cert.getSigAlgName());
					System.out.println(" Sign Algorithm OID: "
							+ cert.getSigAlgOID());
					System.out
							.println(" Serial No.: " + cert.getSerialNumber());

					Set<?> critSet = cert.getCriticalExtensionOIDs();
					if (critSet != null && !critSet.isEmpty()) {
						System.out.println("Set of critical extensions:");
						for (Iterator<?> j = critSet.iterator(); j.hasNext();) {
							String oid = (String) j.next();
							System.out.println(oid);
							System.out.println(toHexStr(cert
									.getExtensionValue(oid)));
						}
					}
					Set<?> nonCritSet = cert.getNonCriticalExtensionOIDs();
					if (nonCritSet != null && !nonCritSet.isEmpty()) {
						System.out.println("Set of non critical extensions:");
						for (Iterator<?> j = nonCritSet.iterator(); j.hasNext();) {
							String oid = (String) j.next();
							System.out.println(oid);
							System.out.println(toHexStr(cert
									.getExtensionValue(oid)));
						}
					}

					count++;
				}
			} catch (Exception e) {
				System.err.println("Exception : " + e);
			}
		} else {
			try {
				FileInputStream fis = new FileInputStream(filename);
				DataInputStream dis = new DataInputStream(fis);

				CertificateFactory cf = CertificateFactory.getInstance("X.509");

				byte[] bytes = new byte[dis.available()];
				dis.readFully(bytes);
				ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

				while (bais.available() > 0) {
					Certificate cert = cf.generateCertificate(bais);
					System.out.println(cert.toString());
				}
				dis.close();
			} catch (Exception e) {
				System.err.println("Exception : " + e);
			}
		}
	}

	static String toHexStr(byte data[]) {
		StringBuffer buf = new StringBuffer(data.length * 2);

		for (int i = 0; i < data.length; i++) {
			if ((data[i] & 0xff) < 0x10) {
				buf.append("0");
			}
			buf.append(Long.toString(data[i] & 0xff, 16));
		}
		return buf.toString();
	}
}
