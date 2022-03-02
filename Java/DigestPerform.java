/**
 * DigestPerform.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.tools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;
//import java.security.SecureRandom;
import Blanclux.util.*;

/**
 * Message Digest Performance
 */
public class DigestPerform {
	private static String provider = "SUN";
	private static String algorithm = "SHA1";
	private static int dataSize = 1024*1024;
	private static int count = 1000;

	/**
	 * Usage : algName [dataLength [count [provider]]]
	 */
	public static void main(String args[]) {

		if (args.length < 1 || args.length > 4) {
			System.out.println("Usage : algName [dataLength(byte) [count [provider]]]");
			System.exit(-1);
		}
		algorithm = args[0];
		if (args.length >= 2) {
			dataSize = Integer.parseInt(args[1]);
		}
		if (args.length >= 3) {
			count = Integer.parseInt(args[2]);
		}
		if (args.length == 4) {
			provider = args[3];
		}

		System.out.print("*** Digest Performance Test [");
		System.out.println(Calendar.getInstance(
							TimeZone.getDefault()).getTime() + "] ***");

		System.out.println("Provider : " + provider);
		System.out.println("Algorithm: " + algorithm);
		System.out.println("Loop : " + count + "\n");

		doTest();
	}

	private static void doTest() {
		MessageDigest md = null;

		byte[] text = new byte[dataSize];

		Random ran = new Random();

		for (int i = 0; i < dataSize; i++) {
			int r = ran.nextInt();
			text[i++] = (byte)r;
			r >>= 8;
			text[i] = (byte)r;
		}

		Stopw sw = new Stopw(2);

		sw.start(1);
		try {
			md = MessageDigest.getInstance(algorithm, provider);
		}
		catch (NoSuchAlgorithmException e) {
			System.err.println(e.toString());
			return;
		} catch (NoSuchProviderException e) {
			System.err.println(e.toString());
			return;
		}
		sw.stop(1);

		sw.start();
		for (int i = 0; i < count; i++) {
			md.reset();
			md.update(text);
			md.digest();
		}
		sw.stop();

		System.out.println("Performance for " + dataSize + " Bytes data : ");

		System.out.println("Class Load Time = "
					+ (float)sw.getTime(1) + " msec");
		System.out.println("Execution Time = "
					+ (float)sw.getTime()/(float)count + " msec");
		System.out.println("Performance: "
					+ ((float)(dataSize*count)/sw.getTime())
					+ " (KB/sec)");
		System.out.println("Performance(total): "
				+ ((float)(dataSize*count)/(sw.getTime()+sw.getTime(1)))
				+ " (KB/sec)\n");
	}
}
