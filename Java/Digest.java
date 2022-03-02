/**
 * Digest.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.tools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

//import java.security.SecureRandom;

/**
 * Message Digest
 */
class Digest {
	public static void main(String args[]) {
		String provider = "SUN";
		String data;
		byte hash[] = null;
		MessageDigest md = null;

		if (args.length != 2 && args.length != 3) {
			System.out.println("Args : algName data [Provider]");
			System.exit(-1);
		}
		if (args.length == 3) {
			provider = args[2];
		}
		data = args[1];

		try {
			md = MessageDigest.getInstance(args[0], provider);
		} catch (NoSuchAlgorithmException e) {
			System.err.println(e.toString());
			System.exit(1);
		} catch (NoSuchProviderException e) {
			System.err.println(e.toString());
			System.exit(2);
		}
		// System.out.println(md.toString());
		System.out.println("Provider : " + provider + "\n");
		System.out.println("Message Digest Algorithm : " + args[0]);

		// Initialize
		md.reset();
		// Update
		md.update(data.getBytes());
		// Digest
		hash = md.digest();

		System.out.println("Digest data for \"" + data + "\" : ");
		System.out.println(toHexStr(hash));
	}

	private static final char[] HEX_CHARS = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8',
		'9', 'A', 'B', 'C', 'D', 'E', 'F' };

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