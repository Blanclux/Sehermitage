/*
 * Verify.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.tools;

import java.io.*;
import java.security.*;
import java.security.spec.*;

/**
 * Verify class
 */
class Verify {
	private static String provider = "SunJSSE";
	private static String signAlg = "SHA1withRSA";
	private static String keyAlg = "RSA";

	private static String usage = "usage: Verify keyFile textFile signFile\n	keyFile  : Public key file name\n	textFile : Text file name\n	signFile : Signature file name\n";

	public static void main(String args[]) {
		String keyFile;
		String txtFile;
		String sigFile;
		byte keyData[] = null;
		byte txtData[] = null;
		byte sigData[] = null;
		Signature sign = null;
		PublicKey pubKey = null;

		int argc = args.length;
		if (argc != 3) {
			System.out.println("parameter error !");
			System.out.println(usage);
			System.exit(1);
		}
		keyFile = args[0];
		txtFile = args[1];
		sigFile = args[2];

		System.out.println("Private key Alg : " + keyAlg);
		System.out.println("Signature Alg   : " + signAlg);

		// Read Public key
		keyData = readFile(keyFile);
		if (keyData.length == 0) {
			System.out.println("Key file read error !");
			System.exit(1);
		}
		System.out.println("Key data  : " + keyData.length);

		try {
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(keyData);
			KeyFactory keyFactory = KeyFactory.getInstance(keyAlg, provider);
			pubKey = keyFactory.generatePublic(pubKeySpec);
		} catch (Exception e) {
			System.out.println("Public key read error !");
			System.err.println(e.toString());
			System.exit(1);
		}

		// Read text data
		txtData = readFile(txtFile);
		if (txtData.length == 0) {
			System.out.println("Text file read error !");
			System.exit(1);
		}
		System.out.println("Text data : " + txtData.length);

		// Read signature data
		sigData = readFile(sigFile);
		if (sigData.length == 0) {
			System.out.println("Signature file read error !");
			System.exit(1);
		}
		System.out.println("Signature data : " + sigData.length);

		try {
			sign = Signature.getInstance(signAlg, provider);
		} catch (Exception e) {
			System.out.println(e.getMessage() + "No Such Algorithm");
			System.exit(1);
		}

		boolean ret = false;
		try {
			sign.initVerify(pubKey);
			sign.update(txtData);
			ret = sign.verify(sigData);
		} catch (Exception e) {
			System.out.println(e.getMessage() + "Signature error");
			System.exit(1);
		}
		if (ret) {
			System.out.println("Signature verify OK");
		} else {
			System.out.println("Signature verify NG");
		}
	}

	static byte[] readFile(String fname) {
		byte[] data = new byte[0];

		try {
			FileInputStream in = new FileInputStream(fname);
			try {
				int length = in.available();
				data = new byte[length];
				in.read(data, 0, length);
			} catch (Exception e) {
				System.err.println(e.toString());
				return new byte[0];
			} finally {
				in.close();
			}
		} catch (Exception e) {
			System.err.println(e.toString());
			return data;
		}
		return data;
	}

}
