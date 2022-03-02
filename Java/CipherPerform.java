/**
 * CipherPerform.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.tools;

import java.util.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;

import Blanclux.util.Stopw;

/**
 * SecretCipher performance test
 */
public class CipherPerform {

	private static String provider = "SunJCE";

	private static String algorithm = "AES";
	private static String opmode = "/CBC/PKCS5Padding";
	private static int keyLength = 128;

	private static int count = 25;
	private static int loop = 20;
	private static int srcLen = 1024 * 1024;

	private static Cipher cipher = null;
	private static KeyGenerator keyGen;
	private static AlgorithmParameterSpec params;

	private static byte ivdata[];
	private static byte[] data;
	private static byte[] encr;
	private static byte[] decr;

	private static int encLen;
	private static int decLen;

	/**
	 * Usage : provider [alg [keyLength [dataLength [count [loop]]]]]
	 */
	public static void main(String[] args) {

		if (args.length < 1 || args.length > 6) {
			System.out
					.println("Usage : provider [alg [keyLength [dataLength [count [loop]]]]]");
			System.exit(-1);
		}
		provider = args[0];
		if (args.length >= 2) {
			algorithm = args[1];
		}
		if (args.length >= 3) {
			keyLength = Integer.parseInt(args[2]);
		}
		if (args.length >= 4) {
			srcLen = Integer.parseInt(args[3]);
		}
		if (args.length >= 5) {
			count = Integer.parseInt(args[4]);
		}
		if (args.length == 6) {
			loop = Integer.parseInt(args[5]);
		}
		opmode = algorithm + opmode;
		if (algorithm.equals("RC4")) {
			opmode = algorithm;
		}

		System.out.print("*** Cipher Performance Test [");
		System.out.println(Calendar.getInstance(TimeZone.getDefault())
				.getTime() + "] ***");

		if (args.length == 1 && args[0].compareTo("?") == 0) {
			Provider[] p = Security.getProviders();
			for (int i = 0; i < p.length; i++) {
				System.out.println("[ " + p[i].getName() + " ]");
				System.out.println(p[i].getInfo());
			}
			System.out.println("");
			return;
		}

		System.out.println("Provider   : " + provider);
		System.out.println("Algorithm  : " + algorithm);
		System.out.println("Key Length : " + keyLength);
		System.out.println("Data Length: " + srcLen + "\n");

		System.out.println("Outer Loop : " + count);
		System.out.println("Inner Loop : " + loop + "\n");

		data = new byte[srcLen];

		try {
			keyGen = KeyGenerator.getInstance(algorithm, provider);
			cipher = Cipher.getInstance(opmode, provider);

			doTest1();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}

	private static boolean doTest1() {
		try {
			SecretKey seckey = null;
			Random rnd = new Random();

			byte[] seed = new byte[16];
			rnd.nextBytes(seed);

			SecureRandom rand = new SecureRandom(seed);
			rand.nextBytes(seed);

			if (algorithm.equals("DES") || algorithm.equals("DESede")
					|| algorithm.equals("RC5")) {
				ivdata = new byte[8];
				rand.nextBytes(ivdata);
				params = new IvParameterSpec(ivdata);
			} else if (algorithm.equals("AES") || algorithm.equals("RC6")) {
				ivdata = new byte[16];
				rand.nextBytes(ivdata);
				params = new IvParameterSpec(ivdata);
			}
			keyGen.init(keyLength, new SecureRandom(seed));
			seckey = keyGen.generateKey();

			cipher.init(Cipher.ENCRYPT_MODE, seckey, params);
			encLen = cipher.getOutputSize(srcLen);

			cipher.init(Cipher.DECRYPT_MODE, seckey, params);
			decLen = cipher.getOutputSize(encLen);
			encr = new byte[encLen];
			decr = new byte[decLen];

			Stopw sw = new Stopw(3);

			for (int j = 0; j < count; j++) {
				System.out.print(".");

				sw.start(0);
				for (int i = 0; i < loop; i++) {
					keyGen.init(keyLength, new SecureRandom(seed));
					seckey = keyGen.generateKey();
				}
				sw.stop(0);

				if (algorithm.equals("DES") || algorithm.equals("DESede")
						|| algorithm.equals("RC5")) {
					rand.nextBytes(ivdata);
					params = new IvParameterSpec(ivdata);
				} else if (algorithm.equals("AES") || algorithm.equals("RC6")) {
					rand.nextBytes(ivdata);
					params = new IvParameterSpec(ivdata);
				}
				rand.nextBytes(data);

				sw.start(1);
				for (int i = 0; i < loop; i++) {
					cipher.init(Cipher.ENCRYPT_MODE, seckey, params);
					cipher.doFinal(data, 0, srcLen, encr);
				}
				sw.stop(1);

				sw.start(2);
				for (int i = 0; i < loop; i++) {
					cipher.init(Cipher.DECRYPT_MODE, seckey, params);
					decLen = cipher.doFinal(encr, 0, encLen, decr);
				}
				sw.stop(2);
			}

			System.out.println("\nKey generation Time: "
					+ sw.getAverage(0, count * loop) + " (msec)");
			System.out.println("Encryption Time: " + (float) sw.getTime(1)
					/ (float) (count * loop) + " (msec)");
			System.out.println("Encryption Performance: "
					+ ((float) (srcLen * count * loop) / sw.getTime(1))
					+ " (KB/sec)");
			System.out.println("Decryption Time: " + (float) sw.getTime(2)
					/ (float) (count * loop) + " (msec)");
			System.out.println("Decryption Performance: "
					+ ((float) (encr.length * count * loop) / sw.getTime(2))
					+ " (KB/sec)\n");
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
}
