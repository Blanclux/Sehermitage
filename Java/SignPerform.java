/**
 * SignPerform.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.tools;

import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;
import java.util.*;

import Blanclux.util.*;

/**
 * Signature performance test
 */
public class SignPerform {

	private static String provider = "SunJSSE";
	private static String keyAlg = "RSA";
	private static String signAlg = "SHA1withRSA";
	private static int keyLength = 1024;
	private static int srcLength = 1024;
	private static int count = 10;
	private static int loop = 25;
	private static int kloop = 1;

	private static Signature sign = null;
	private static KeyPairGenerator keyGen;
	private static KeyPair kpair;
	private static String provider2;

	/**
	 * Usage : provider [keyAlg [signAlg [keyLength [dataLength [count
	 * [loop]]]]]]
	 */
	public static void main(String[] args) {
		Stopw sw = new Stopw();

		if (args.length < 1 || args.length > 6) {
			System.out.println("Usage : provider [keyAlg [signAlg [keyLength [dataLength [count [loop]]]]]]");
			System.exit(-1);
		}
		provider = args[0];
		if (args.length >= 2) {
			keyAlg = args[1];
		}
		if (args.length >= 3) {
			signAlg = args[2];
		}
		if (args.length >= 4) {
			keyLength = Integer.parseInt(args[2]);
		}
		if (args.length >= 5) {
			srcLength = Integer.parseInt(args[4]);
		}
		if (args.length >= 6) {
			count = Integer.parseInt(args[5]);
		}
		if (args.length == 7) {
			loop = Integer.parseInt(args[6]);
		}

		provider2 = provider;
		if (keyAlg.equals("DSA")) {
			provider2 = "SUN";
		}

		System.out.print("*** Signature Performance Test [");
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

		System.out.println("Provider : " + provider);
		System.out.println("Algorithm: " + signAlg);
		System.out.println("Keygen Provider : " + provider2 + "\n");

		System.out.println("Outer Loop : " + count);
		System.out.println("Inner Loop : " + loop + "\n");

		AlgorithmParameterSpec params = null;

		byte[] seed = new byte[16];
		Random rand = new Random();
		rand.nextBytes(seed);

		try {
			keyGen = KeyPairGenerator.getInstance(keyAlg, provider2);
			sw.start();
			sign = Signature.getInstance(signAlg, provider);
			sw.stop();
			System.out.println("Signature getInstance Time: " + sw.getTime()
					+ " [msec]\n");

			if (keyAlg.equals("RSA")) {
				keyGen.initialize(keyLength, new SecureRandom(seed));
			} else if (keyAlg.equals("DSA")) {
				AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator
						.getInstance("DSA", provider2);
				paramGen.init(keyLength, new SecureRandom(seed));
				System.out.println("# Generate parameter");
				AlgorithmParameters dsaParams = paramGen.generateParameters();
				params = dsaParams.getParameterSpec(DSAParameterSpec.class);
				((DSAKeyPairGenerator) keyGen).initialize((DSAParams) params,
						new SecureRandom(seed));

				keyGen.initialize(keyLength, new SecureRandom(seed));
			} else {
				System.err.println("Illegal algorithm name");
				System.exit(1);
			}

			doTest();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}

	private static boolean doTest() {
		try {
			byte[] seed = new byte[16];
			Random rand = new Random();
			rand.nextBytes(seed);
			Stopw sw = new Stopw(3);

			keyGen.initialize(keyLength, new SecureRandom(seed));

			rand.nextBytes(seed);
			SecureRandom rng = new SecureRandom(seed);

			System.out.println("# Key genration");
			System.out.println(" Key Length : " + keyLength);
			System.out.println("# Signature generation");
			System.out.println(" Data Length: " + srcLength + "\n");

			for (int i = 0; i < count; i++) {
				sw.start(0);
				for (int j = 0; j < kloop; j++) {
					kpair = keyGen.generateKeyPair();
				}
				sw.stop(0);

				if (srcLength <= 0) {
					srcLength = (int) (rng.nextDouble() * 127 + 1);
				}
				byte[] data = new byte[srcLength];
				rng.nextBytes(data);
				byte[] signature = null;
				PrivateKey prikey = kpair.getPrivate();
				PublicKey pubkey = kpair.getPublic();

				sw.start(1);
				for (int j = 0; j < loop; j++) {
					sign.initSign(prikey, rng);
					sign.update(data);
					signature = sign.sign();
				}
				sw.stop(1);

				sw.start(2);
				for (int j = 0; j < loop; j++) {
					sign.initVerify(pubkey);
					sign.update(data);
					sign.verify(signature);
				}
				sw.stop(2);
			}

			System.out.println(" Key Generation Time: "
					+ sw.getAverage(0, count * kloop) + " [msec]");

			System.out.println(" Signature Time: "
					+ sw.getAverage(1, count * loop) + " [msec]");
			System.out.println(" Signature Performance: "
					+ ((float) (srcLength * count * loop) / sw.getTime(1))
					+ " [KB/ses]");

			System.out.println(" Verification Time: "
					+ sw.getAverage(2, count * loop) + " [msec]");
			System.out.println(" Verification Performance: "
					+ ((float) (srcLength * count * loop) / sw.getTime(2))
					+ " [KB/sec]\n");
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

}
