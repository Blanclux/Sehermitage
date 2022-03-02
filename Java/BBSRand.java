/**
 * BBSRand.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.crypto.prng;

import Blanclux.math.*;
import java.security.*;
import java.util.Random;

/**
 * Blum Blum Shub random generator
 */
public class BBSRand extends SecureRandomSpi {
//public class BBSRand extends SecureRandom {
	private static final long serialVersionUID = 1L;
	private static int sleepTime;
	private static final int TARGET_SPIN_COUNT = 55000;
	private static final int MIN_SPIN_COUNT = (6 * TARGET_SPIN_COUNT) / 10;
	private static final int MAX_SPIN_COUNT = 2 * TARGET_SPIN_COUNT;

	private static String ps = "c8d616a0e38facbe852ca2c88c04c8e2c3ceed578ab2cff4da7c5c1299b2f0b7fb621cad156f0c6918d58cc39f52da731c00441b6a79ce55de11266f3de88c8fb5589fec926f1f52dd8d2ea81f645b555e00547e144dda4718187bc0ed681c864c7937f7d71578af5dcb03dce2546ddbcfeb3db141c2000545a403590ae0e613";
	private static String qs = "d904e32b484421304a4d46ed3a91d0414eae490ccd596cb533a2d1abda0c857677cf6b56254492fbc09a04dc7759e18245b67fad35796dff287576f86b3dbbfb7924e254135486bd210539122746e94b94c04cd3ed8556180fe8e4a9caff10ee033b8588c1c4c57f93daec92225421967c3e53e14c832e3782aa17348bab18d1";

	private MPInt p;
	private MPInt q;
	private MPInt n;

	private MPInt s;

	/**
	 * Class constructor
	 */
	public BBSRand() {
		//generateParam(1024);
		p = new MPInt(ps, 16);
		q = new MPInt(qs, 16);

		n = p.multiply(q);
	}

	/**
	 * Generate a random number
	 * @param  length the number of random bytes to generate.
	 * @return the random byte array
	 */
	protected byte[] engineGenerateSeed(int numBytes) {
		byte[] ret = new byte[numBytes];

		setSleepTime();
		for (int i = 0; i < numBytes; i++) {
			ret[i] = (byte) genSeed();
		}

		return ret;
	}

	/**
	 * Reseeds this random object.
	 *
	 * @param seed the seed.
	 */
	protected void engineSetSeed(byte[] seed) {
		s = new MPInt(1, seed);

		while (Prime.iscoPrime(s, n) == false) {
			n = n.add(MPInt.ONE);
		}
		s = s.multiply(s).mod(n);
	}

	/**
	 * Generates a user-specified number of random bytes.
	 * @param bytes the array to be filled in with random bytes.
	 */
	protected void engineNextBytes(byte[] bytes) {
		byte[] rand;

		for (int i = 0; i < bytes.length; i++) {
			s = s.multiply(s).mod(n);
			rand = s.toByteArray();
			bytes[i] = rand[rand.length - 1];
		}
	}

	protected byte[] getState() {
		return s.toByteArray();
	}

	public void generateParam(int bitLen) {
		Random rnd = new Random();
		int seedLen = 16;
		byte[] seed = new byte[seedLen];
		rnd.nextBytes(seed);
		SecureRandom rng = new SecureRandom(seed);

		MPInt tmp;
		System.out.println("BBSRand generateParam");
		System.out.println("generate p");
		for (;;) {
			Prime pn = new Prime(bitLen, rng);	// generate p
			p = pn.getPrime();
			tmp = p.mod(MPInt.valueOf(4));
			if (tmp.intValue() == 3) {
				System.out.println("p = " + p.toString(16));
				break;
			}
		}
		System.out.println("generate q");
		for (;;) {
			Prime qn = new Prime(bitLen, rng);	// generate q
			q = qn.getPrime();
			tmp = q.mod(MPInt.valueOf(4));
			if (tmp.intValue() == 3) {
				System.out.println("q = " + q.toString(16));
				break;
			}
		}
		n = p.multiply(q);
	}

	/**
	 * Calculates a sleep time.
	 */
	private static void setSleepTime() {
		sleepTime = (1000 * TARGET_SPIN_COUNT) / genSeed(1000);
	}

	/**
	 * Generates a random seed
	 */
	private static synchronized int genSeed() {
		int candidate = genSeed(sleepTime);

		while (candidate < MIN_SPIN_COUNT) {
			setSleepTime();
			candidate = genSeed(sleepTime);
		}

		if (candidate > MAX_SPIN_COUNT) {
			setSleepTime();
		}

		return candidate;
	}

	/**
	 * Generate a random seed
	 */
	private static int genSeed(int sleepTime) {
		int counter = 0;

		Thread sleeper = new Sleeper(sleepTime);
		sleeper.start();

		while (sleeper.isAlive()) {
			counter++;
			Thread.yield();
		}

		return counter;
	}

}

/**
 * Sleeper Class
 * Sleeps for a designated period (in milliseconds)
 */
class Sleeper extends Thread {
	private int sleepTime;

	Sleeper(int sleepTime) {
		this.sleepTime = sleepTime;
	}

	final public void run() {
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
		}
	}
}
