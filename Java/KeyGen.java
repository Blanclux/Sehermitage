/**
 * Key Generation
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.tools;

import java.io.*;
import java.util.*;
import java.security.*;
import java.security.spec.*;

import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * Key Generation
 * 
 * usage: KeyGen [initialFileName]
 */
class KeyGen {
	/** Security Provider */
	private static String provider = "SunJSSE";
	/** Secret Key Algorithm */
	private static String secKeyAlg = "DESede";
	/** Public Key Algorithm */
	private static String pubKeyAlg = "RSA";
	/** Length of Secret key (bit) */
	private static String secKeyLen = "112"; // for DESede
	/** Length of Public Cipher Key (bit) */
	private static String pubKeyLen = "1024"; // for RSA
	/** Secret key file name */
	private static String secKeyFile = "DESedeKey.ck";
	/** Private key file name */
	private static String priKeyFile = "RsaKey.sk";
	/** Public key file name */
	private static String pubKeyFile = "RsaKey.pk";

	/** Initial data file name */
	private static String iniFile = "keygen.ini";

	public static void main(String args[]) {
		// Initial file name set
		if (args.length == 1) {
			iniFile = args[0];

			// Get key file names from the property file
			FileInputStream fin = null;
			Properties pro = null;

			try {
				fin = new FileInputStream(iniFile);
				pro = new Properties();
				pro.load(fin);

				provider = pro.getProperty("Provider", provider);
				secKeyAlg = pro.getProperty("SecretKeyAlgorithm", secKeyAlg);
				pubKeyAlg = pro.getProperty("PublicKeyAlgorithm", pubKeyAlg);
				secKeyLen = pro.getProperty("SecretKeyLength", secKeyLen);
				pubKeyLen = pro.getProperty("PublicKeyLength", pubKeyLen);
				secKeyFile = pro.getProperty("SecretKeyPath", secKeyFile);
				priKeyFile = pro.getProperty("PrivateKeyPath", priKeyFile);
				pubKeyFile = pro.getProperty("PublicKeyPath", pubKeyFile);

				fin.close();
			} catch (Exception e) {
				System.err.println("Initial file " + iniFile
						+ " can not found.");
				System.err.println("Use default file name.");
			}
		}
		int sKeyLen = Integer.parseInt(secKeyLen);
		int pKeyLen = Integer.parseInt(pubKeyLen);

		// Random number generator
		Random rnd = new Random();
		byte[] seed = new byte[32];
		rnd.nextBytes(seed);
		SecureRandom rng = new SecureRandom(seed);

		// SecretKey key generation
		System.out.println("< Secretkey Generation >");
		System.out.println(" Algorithm  : " + secKeyAlg);
		System.out.println(" Key Length : " + sKeyLen);
		KeyGenerator keyGen = null;
		try {
			keyGen = KeyGenerator.getInstance(secKeyAlg);
		} catch (Exception ex) {
			System.err.println(ex.toString());
			System.exit(1);
		}

		keyGen.init(sKeyLen, rng);
		SecretKey secKey = keyGen.generateKey();

		System.out.println(" Secret Key format : " + secKey.getFormat());
		byte[] secKeyByte = secKey.getEncoded();
		writeKey(secKeyFile, secKeyByte);

		// Public key generation
		System.out.println("< Publickey Generation >");
		System.out.println(" Algorithm  : " + pubKeyAlg);
		System.out.println(" Key Length : " + pKeyLen);
		KeyPairGenerator keyPair = null;
		try {
			keyPair = KeyPairGenerator.getInstance(pubKeyAlg, provider);
		} catch (Exception ex) {
			System.err.println(ex.toString());
			System.exit(1);
		}

		keyPair.initialize(pKeyLen, rng);
		KeyPair keyp = keyPair.generateKeyPair();

		PrivateKey priKey = keyp.getPrivate();
		PublicKey pubKey = keyp.getPublic();

		System.out.println(" Private Key format : " + priKey.getFormat());
		System.out.println(" Public Key format  : " + pubKey.getFormat());
		byte[] priKeyByte = priKey.getEncoded();
		byte[] pubKeyByte = pubKey.getEncoded();

		System.out.println("< Write key data >");
		writeKey(priKeyFile, priKeyByte);
		writeKey(pubKeyFile, pubKeyByte);

		// Read key data
		System.out.println("< Read key data >");
		getKeyData();
	}

	static void getKeyData() {
		try {
			byte[] secKey = readKey(secKeyFile);
			System.out.println("Secret Key : " + toHexStr(secKey));
			DESedeKeySpec secKeySpec = new DESedeKeySpec(secKey);
			SecretKeyFactory skeyFactory = SecretKeyFactory
					.getInstance(secKeyAlg);
			SecretKey skey = skeyFactory.generateSecret(secKeySpec);
			System.out.println("Secret Key format  : " + skey.getFormat());

			byte[] priKey = readKey(priKeyFile);
			PKCS8EncodedKeySpec priKeySpec = new PKCS8EncodedKeySpec(priKey);
			KeyFactory keyFactory = KeyFactory.getInstance(pubKeyAlg);
			PrivateKey rsaPriKey = keyFactory.generatePrivate(priKeySpec);
			System.out.println("Private Key format : " + rsaPriKey.getFormat());

			byte[] pubKey = readKey(pubKeyFile);
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubKey);
			keyFactory = KeyFactory.getInstance(pubKeyAlg);
			PublicKey rsaPubKey = keyFactory.generatePublic(pubKeySpec);
			System.out.println("Public Key format  : " + rsaPubKey.getFormat());
		} catch (Exception ex) {
			System.err.println(ex.toString());
			return;
		}
	}

	static void writeKey(String fname, byte key[]) {

		try {
			FileOutputStream out = new FileOutputStream(fname);
			try {
				out.write(key, 0, key.length);
			} catch (Exception e) {
				System.err.println(e.toString());
			} finally {
				out.close();
			}
		} catch (Exception ex) {
			System.err.println(ex.toString());
		}
	}

	static byte[] readKey(String fname) {
		int skLen;
		byte sk[] = new byte[0];

		try {
			FileInputStream in = new FileInputStream(fname);
			try {
				skLen = in.available();
				sk = new byte[skLen];
				in.read(sk, 0, skLen);
			} catch (Exception e) {
				System.err.println(e.toString());
				return new byte[0];
			} finally {
				in.close();
			}
		} catch (Exception ex) {
			System.err.println(ex.toString());
			return sk;
		}
		return sk;
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
