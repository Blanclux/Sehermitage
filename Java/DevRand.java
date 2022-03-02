/**
 * DevRandom.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.crypto.prng;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;

/**
 * SecureRandomSpi that gets it's random bytes from the /dev/urandom PRNG
 * on systems that support it.
 */
public final class DevRand extends SecureRandomSpi {
	private static final long serialVersionUID = 1L;

	/** Name of the PRNG file. */
	private static final String RANDOM_DEV_NAME = "/dev/urandom";

	/** File representing the randomness device. */
	private static final File RANDOM_DEV = new File(RANDOM_DEV_NAME);

	/**
	 * Randomness file input stream. A setting of null denotes that
	 * this Spi is not available.
	 */
	private static FileInputStream randomStream = null;

	/** Number of one bits in a four-bit nibble */
	private static final int[] ONE_COUNT = {
		0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4
	};

	/**
	 * Open /dev/urandom and check whether the first 2500 bytes (20000 bits)
	 * look random. If they don't look random, disable this Spi.
	 */
	static {
		try {
			randomStream = new FileInputStream(RANDOM_DEV);

			byte[] test_bytes = new byte[2500];
			getRandomBytes(test_bytes);

			if (!looksRandom(test_bytes)) {
				System.out.println("Output of " + RANDOM_DEV_NAME
						+ " doesn't look random, "
						+ "this may indicate a serious security problem!");

				randomStream.close();
				randomStream = null;
			}
		} catch (IOException e) {
			randomStream = null;
		}
	}

    /**
     * Fill the given array with random bytes read from randomStream.
     */
    private static void getRandomBytes(byte[] bytes) throws IOException {
		int count;
		int offset = 0;
		int todo   = bytes.length;

		while (todo > 0) {
			synchronized (randomStream) {
				if ((count = randomStream.read(bytes, offset, todo)) == -1) {
					throw new IOException("EOF");
				}
			}
			offset += count;
			todo   -= count;
		}
	}

	/**
	 * Construct a new Spi that uses /dev/urandom as PRNG.
	 */
	public DevRand() {
		if (randomStream == null) {
            throw new InternalError("randomStream == null");
		}
	}

	/**
	 * Sets the seed data
	 */
	protected void engineSetSeed(byte[] seed) {
		// do nothing
	}

	/**
	 * Fill the given array with random bytes read from /dev/urandom.
	 */
	protected void engineNextBytes(byte[] bytes) {
		try {
			getRandomBytes(bytes);
		} catch (IOException e) {
			throw new RuntimeException(
							"Cannot read from randomness device: " + e);
		}
	}

	/**
	 * Return a seed.
	 */
	protected byte[] engineGenerateSeed(int numBytes) {
		byte[] seed = new byte[numBytes];

		engineNextBytes(seed);

		return seed;
	}

	/**
	 * Whether this Spi is available (/dev/urandom readable and looking
	 * random).
	 */
	public static boolean isAvailable() {
		return randomStream != null;
	}

	/**
	 * Couple of statistical tests for verifying (P)RNG output as defined in 
	 * FIPS 140-2.
	 *
	 * @param  data 2500 bytes (20,000 bits) of data to be tested
	 * @return true if the data 'looks random'
	 */
	public static boolean looksRandom(byte[] data) {
		return testMonobit(data) && testPoker(data);
	}

	/**
	 * Mono bit test as defined in FIPS 140-2.
	 *
	 * @param  data 2500 bytes (20,000 bits) of data to be tested
	 * @return true if the data passes the test, false otherwise
	 */
	public static boolean testMonobit(byte[] data) {
		if (data.length != 2500) {
			throw new IllegalArgumentException("2500 bytes expected");
		}

		int total = 0;
		for (int i = 0; i < 2500; i++) {
			int hi = ONE_COUNT[(data[i] >>> 4) & 0xF];
			int lo = ONE_COUNT[(data[i]) & 0xF];
			total += hi + lo;
		}

		return (9725 < total) && (total < 10275);
	}

	/**
	 * Poker test as defined in FIPS 140-2.
	 *
	 * @param  data 2500 bytes (20,000 bits) of data to be tested
	 * @return true if the data passes the test, false otherwise
	 */    
	public static boolean testPoker(byte[] data) {
		if (data.length != 2500) {
			throw new IllegalArgumentException("2500 bytes expected");
		}

		int[] b = new int[16];

		for (int i = 0; i < data.length; i++) {
			b[(data[i]) & 0xF]++;
			b[(data[i] >>> 4) & 0xF]++;
		}

		int sigma = 0;
		for (int i = 0; i < 16; i++) {
			sigma += b[i] * b[i];
		}

		float res = (16.0f * sigma) / 5000.0f - 5000.0f;

		return (2.16f < res) && (res < 46.17f);
	}
}
