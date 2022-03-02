/**
 * CryptFile.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.tools;

import java.io.*;
import java.security.spec.*;

import javax.crypto.*;
import javax.crypto.spec.*;

public class CryptFile {
	private static String provider = "SunJCE";
	private static String algorithm = "PBEWithMD5AndDES";
	private static String opmode = "CBC/PKCS5Padding";

	static byte salt[] = { (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04,
			(byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08 };
	static int iCount = 5;

	public static void main(String args[]) {
		Cipher cipher = null;
		SecretKeyFactory keyFact;
		AlgorithmParameterSpec params;
		PBEKeySpec pbeKeySpec;
		SecretKey secretKey;

		if (args.length != 4) {
			System.out.println("CryptFile {enc|dec} Password SrcFile DestFile");
			System.exit(1);
		}
		String mode = args[0];
		String password = args[1];
		String srcFile = args[2];
		String destFile = args[3];

		opmode = algorithm + "/" + opmode;
		try {
			cipher = Cipher.getInstance(opmode, provider);
		} catch (Exception ex) {
			System.err.println(ex.toString());
			System.exit(-1);
		}

		byte[] src;
		if ((src = readFile(srcFile)) == null) {
			System.err.println("Can't open source file!");
			return;
		}
		byte[] dst = null;

		pbeKeySpec = new PBEKeySpec(password.toCharArray(), salt, iCount);
		params = new PBEParameterSpec(salt, iCount);

		try {
			keyFact = SecretKeyFactory.getInstance(algorithm, provider);
			secretKey = keyFact.generateSecret(pbeKeySpec);
		} catch (Exception e) {
			System.err.println(e.toString());
			return;
		}

		try {
			// Encrypt
			if (mode.equals("enc")) {
				cipher.init(Cipher.ENCRYPT_MODE, secretKey, params);
			} else if (mode.equals("dec")) { // Decrypt
				cipher.init(Cipher.DECRYPT_MODE, secretKey, params);
			} else {
				System.err.println("parameter (mdde) error!");
				System.exit(-1);
			}
			dst = cipher.doFinal(src);
		} catch (Exception ex) {
			System.err.println(ex.toString());
			System.exit(-1);
		}

		writeFile(destFile, dst);
		System.out.println("Written " + mode + "file : " + destFile);
	}

	static byte[] readFile(String fname) {
		FileInputStream in = null;
		byte[] data;

		try {
			in = new FileInputStream(fname);
			int length = in.available();
			data = new byte[length];
			in.read(data, 0, length);
			in.close();
		} catch (Exception e) {
			System.err.println(e.toString());
			return null;
		}
		return data;
	}

	static boolean writeFile(String fname, byte data[]) {
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(fname);
			out.write(data, 0, data.length);
			out.close();
		} catch (Exception e) {
			System.err.println(e.toString());
			return false;
		}
		return true;
	}
}
