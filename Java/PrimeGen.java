/**
 * PrimeGen.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.math;

import Blanclux.util.Stopw;

import java.security.SecureRandom;

/**
 * Prime Generation
 */
class PrimeGen {
	private static SecureRandom rng;

	public static void main(String args[]) {

		int bitLen;
		MPInt p;

		if (args.length == 0) {
			System.out.println("PrimeGen bitLength");
			System.exit(1);
		}
		bitLen = Integer.valueOf(args[0]).intValue();

		if (rng == null) {
			rng = new SecureRandom();
		}
		Stopw sw = new Stopw();
		sw.start();

		Prime pn = new Prime(bitLen, rng); // generate prime
		sw.stop();

		System.out.print("Prime generation time = " + sw.getTime() + " msec\n");

		p = pn.getPrime();
		System.out.println("prime = " + p.toString(10) + " (" + p.bitLength()
				+ "bits)");

		return;
	}

}
