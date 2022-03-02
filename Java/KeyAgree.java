/**
 * KeyAgree.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.tools;

import java.util.*;
import java.security.*;
import java.security.spec.*;

import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * KeyAgreement Test
 */
class KeyAgree {

	private static String provider = "SunJCE";
	static String algorithm = "DH";
	static int pLen = 1024;
	static int eLen = 256;

	public static void main(String args[]) {

		System.out.println("< KeyAgreement Test >");
		System.out.println(" Provider: " + provider);
		System.out.println(" Algorithm: " + algorithm);
		System.out.println(" P Length: " + pLen);
		System.out.println(" E Length: " + eLen);

		int ret = doTest();
		if (ret == 0) {
			System.out.println("Test: OK");
		} else {
			System.out.println("Test: NG");
		}
	}

	public static int doTest() {
		try {
			AlgorithmParameterSpec spec = null;
			KeyPairGenerator dhKey1;
			KeyPairGenerator dhKey2;

			// Generating parameters
			Random rand = new Random();
			byte[] seed = new byte[8];
			rand.nextBytes(seed);
			SecureRandom rng = new SecureRandom(seed);

			System.out.println("\n DH parameter generation");
			DHGenParameterSpec dhSpec = new DHGenParameterSpec(pLen, eLen);
			AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator
					.getInstance(algorithm, provider);
			paramGen.init(dhSpec, rng);
			AlgorithmParameters params = paramGen.generateParameters();

			spec = params.getParameterSpec(DHParameterSpec.class);
			System.out.println(" p : "
					+ ((DHParameterSpec) spec).getP().toString(16));
			System.out.println(" g : "
					+ ((DHParameterSpec) spec).getG().toString(16));
			System.out.println(" l : " + ((DHParameterSpec) spec).getL());

			byte[] pg = params.getEncoded();
			System.out.println(toHexStr(pg));
			AlgorithmParameters aparams = AlgorithmParameters.getInstance(
					algorithm, provider);
			aparams.init(pg);

			spec = aparams.getParameterSpec(DHParameterSpec.class);
			System.out.println(" p : "
					+ ((DHParameterSpec) spec).getP().toString(16));
			System.out.println(" g : "
					+ ((DHParameterSpec) spec).getG().toString(16));
			System.out.println(" l : " + ((DHParameterSpec) spec).getL());

			// Key pair generation (A)
			dhKey1 = KeyPairGenerator.getInstance(algorithm, provider);
			dhKey1.initialize(spec, rng);
			KeyPair kp1 = dhKey1.generateKeyPair();
			PrivateKey priKey1 = kp1.getPrivate();
			PublicKey pubKey1 = kp1.getPublic();

			// Key pair generation (B)
			dhKey2 = KeyPairGenerator.getInstance(algorithm, provider);
			dhKey2.initialize(spec, rng);
			KeyPair kp2 = dhKey2.generateKeyPair();
			PrivateKey priKey2 = kp2.getPrivate();
			PublicKey pubKey2 = kp2.getPublic();

			// Key Agreement
			/* Part A */
			System.out.println("< Part A >");
			KeyAgreement keyAgree1 = KeyAgreement.getInstance(algorithm,
					provider);
			keyAgree1.init(priKey1, spec, rng);
			keyAgree1.doPhase(pubKey2, true);

			byte[] key1 = keyAgree1.generateSecret();
			System.out.println("Shared Secret Key(A): ");
			System.out.println(" Key Length = " + key1.length);
			System.out.println(toHexStr(key1));

			keyAgree1.init(priKey1, spec, rng);
			keyAgree1.doPhase(pubKey2, true);

			SecretKey skey1 = keyAgree1.generateSecret("AES");
			byte[] cKey1 = skey1.getEncoded();
			System.out.println("AES Key(A): ");
			System.out.println(toHexStr(cKey1));

			/* Part B */
			System.out.println("< Part B >");
			KeyAgreement keyAgree2 = KeyAgreement.getInstance(algorithm,
					provider);
			keyAgree2.init(priKey2, spec, rng);
			keyAgree2.doPhase(pubKey1, true);
			byte[] key2 = keyAgree2.generateSecret();
			if (key2 == null) {
				System.err.println("Key Agreement Error !");
				return 1;
			}
			System.out.println("Shared Secret Key(B): ");
			System.out.println(" Key Length = " + key2.length);
			System.out.println(toHexStr(key2));

			keyAgree2.init(priKey2, spec, rng);
			keyAgree2.doPhase(pubKey1, true);
			SecretKey skey2 = keyAgree2.generateSecret("AES");
			byte[] cKey2 = skey2.getEncoded();
			System.out.println("AES Key(B): ");
			System.out.println(toHexStr(cKey2));

			for (int i = 0; i < key1.length; i++) {
				if (key1[i] != key2[i]) {
					System.err.println("Key Agreement Error !");
					return 1;
				}
			}
			for (int i = 0; i < cKey1.length; i++) {
				if (cKey1[i] != cKey2[i]) {
					System.err.println("Key Agreement Error !");
					return 1;
				}
			}
		} catch (Exception ex) {
			System.err.println(ex.toString());
			return 1;
		}
		return 0;
	}

	private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	static String toHexStr(byte data[]) {
		String res = "";

		for (int i = 0; i < data.length; i++) {
			if (i % 4 == 0) {
				res += " ";
			}
			res += (HEX_CHARS[(data[i] >>> 4) & 0x0f]);
			res += (HEX_CHARS[(data[i]) & 0x0f]);
			if (i % 32 == 31) {
				res += "\n";
			}
		}
		return res;
	}

}
