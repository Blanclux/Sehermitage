/**
 * Sign.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.tools;

import java.io.*;
import java.security.*;
import java.security.spec.*;

/**
 * Sign class
 */
class Sign {
	private static String provider = "SunJSSE";
	private static String signAlg = "SHA1withRSA";
	private static String keyAlg = "RSA";

	private static String usage = "usage: Sign keyFile textFile signFile\n	keyFile  : Private key file name\n	textFile : Text file name\n	signFile : Signature file name\n";

	public static void main(String args[]) {

		String keyFile;
		String txtFile;
		String sigFile;
		byte keyData[] = null;
		byte txtData[] = null;
		byte sigData[] = null;
		Signature sign = null;
		PrivateKey priKey = null;

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

		// Read Private key
		keyData = readFile(keyFile);
		if (keyData.length == 0) {
			System.out.println("Key file read error !");
			System.exit(1);
		}
		System.out.println("Key data  : " + keyData.length);

		try {
			PKCS8EncodedKeySpec priKeySpec = new PKCS8EncodedKeySpec(keyData);
			KeyFactory keyFactory = KeyFactory.getInstance(keyAlg, provider);
			priKey = keyFactory.generatePrivate(priKeySpec);
		} catch (Exception e) {
			System.out.println("Private key read error !");
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

		try {
			sign = Signature.getInstance(signAlg, provider);
		} catch (Exception e) {
			System.out.println(e.getMessage() + "No Such Algorithm");
			System.exit(1);
		}

		try {
			sign.initSign(priKey);
			sign.update(txtData);
			sigData = sign.sign();
		} catch (Exception e) {
			System.out.println(e.getMessage() + "Signature error");
			System.exit(1);
		}
		System.out.println("Sign data : " + sigData.length);

		// Write the signature to the sigFile
		writeFile(sigFile, sigData);
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

	static boolean writeFile(String fname, byte data[]) {

		try {
			FileOutputStream out = new FileOutputStream(fname);
			try {
				out.write(data, 0, data.length);
			} catch (Exception e) {
				System.err.println(e.toString());
				return false;
			} finally {
				out.close();
			}
		} catch (Exception e) {
			System.err.println(e.toString());
			return false;
		}
		return true;
	}

}
