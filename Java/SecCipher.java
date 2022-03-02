/**
 * SecCipher.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.tools;

import java.io.*;
import java.util.*;
import java.security.spec.*;
//import java.security.KeyFactory;

import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * SecCipher class
 */
class SecCipher {

	/** Initial data file name */
	private static String iniFile = "CipherInfo.ini";

	/** Security Provider */
	private static String provider = "SunJCE";
	/** Secret Cipher Algorithm */
	private static String algorithm = "DESede";
	/** Mode of operation */
	private static String mode = "CBC";
	/** Padding */
	private static String pad = "PKCS5Padding";
	private static String opmode = "DESede/CBC/PKCS5Padding";

	private static byte[] key = null;

	private static String keyFile;
	private static String srcFile;
	private static String dstFile;

	private static int encode = 1;

	private static Cipher cipher;

	//IV Length = 8 for DES
	private static String ivStr = "12345678";

		public static void main(String args[]) {

		FileInputStream fin = null;
		Properties pro = null;

		if (args.length != 4) {
			System.out.println("usage: SecCipher (-e|-d) key inText outText");
			System.out.println("  -e|-d    : -e for encrypt / -d for decrypt");
			System.out.println("  key      : Secret Key file");
			System.out.println("  inText   : Input file");
			System.out.println("  outText  : Output file");
			System.exit(1);
		}

		if (args[0].equals("-e")) {
			encode = 1;
		} else if (args[0].equals("-d")) {
			encode = 0;
		} else {
			System.out.println("Parameter error");
			System.exit(1);
		}
		keyFile = args[1];
		srcFile = args[2];
		dstFile = args[3];

		try {
			fin = new FileInputStream(iniFile);
			pro = new Properties();
			pro.load(fin);

			provider = pro.getProperty("Provider", provider);
			algorithm = pro.getProperty("Algorithm", algorithm);
			mode = pro.getProperty("Mode", mode);
			pad = pro.getProperty("Padding", pad);
			opmode = algorithm + "/" + mode + "/" + pad;

			fin.close();
		} catch (Exception e) {
			System.out.println("Initial file " + iniFile + " can not be found.");
			System.out.println("Use default value.");
		}

		key = readFile(keyFile);
		if (key.length == 0) {
			System.out.println("Key file read error.");
			System.exit(1);
		}

		try {
			setCipher();
		} catch (Exception e) {
			System.err.println("Cipher init error");
		}
		System.out.println("Provider  : " + provider);
		System.out.println("Algorithm : " + algorithm);
		System.out.println("Mode      : " + opmode);

		if (encode == 1) {
			encrypt();
		} else {
			decrypt();
		}
	}

	public static void setCipher() throws Exception {

		try {
			KeySpec kspec = null;
			AlgorithmParameterSpec params = null;

			cipher = Cipher.getInstance(opmode, provider);
			SecretKeyFactory skeyFact = SecretKeyFactory.getInstance(algorithm,
					provider);

			params = new IvParameterSpec(ivStr.getBytes());
			if (algorithm.equals("DES")) {
				kspec = new DESKeySpec(key);
			} else if (algorithm.equals("DESede")) {
				kspec = new DESedeKeySpec(key);
			}
			SecretKey seckey = skeyFact.generateSecret(kspec);

			if (encode == 1) {
				System.out.println("< Encrypt File >");
				cipher.init(Cipher.ENCRYPT_MODE, seckey, params);
			} else {
				System.out.println("< Decrypt File >");
				cipher.init(Cipher.DECRYPT_MODE, seckey, params);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
	}

	static void encrypt() {
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(
					srcFile));
			OutputStream out = new CipherOutputStream(new BufferedOutputStream(
					new FileOutputStream(dstFile)), cipher);

			try {
				byte[] buf = new byte[1024];
				for (;;) {
					int len = in.read(buf);
					if (len <= 0) {
						break;
					}
					out.write(buf, 0, len);
				}
				in.close();
				out.flush();
				out.close();
			} catch (Exception e) {
				System.err.println("Encrypt error: " + e);
				return;
			}
		} catch (Exception e) {
			System.err.println("Encrypt error: " + e);
			return;
		}
	}
	   
	static void decrypt() {
		try {
			InputStream in = new CipherInputStream(new BufferedInputStream(
					new FileInputStream(srcFile)), cipher);
			OutputStream out = new BufferedOutputStream(new FileOutputStream(
					dstFile));
			try {
				byte[] buf = new byte[1024];
				for (;;) {
					int len = in.read(buf);
					if (len <= 0) {
						break;
					}
					out.write(buf, 0, len);
				}
				in.close();
				out.flush();
				out.close();
			} catch (Exception e) {
				System.out.println("Decrypt error.");
			}
		} catch (Exception e) {
			System.out.println("Decrypt error.");
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
