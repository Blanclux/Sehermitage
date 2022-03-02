/**
 * ParamGen.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.tools;

import Blanclux.math.MPInt;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Diffie-Hellman parameter generation as defined in RFC-2631.
 */
public class ParamGen {

	/** Length of public modulus p. */
	private static int L = 1024;
	/** Length of private modulus and of q. */
	private static int m = 160;

	/** Parameter file name */
	private static String paramFile = "param.dat";

	private static final MPInt TWO = MPInt.valueOf(2L);

	/** The SHA-1 instance to use. */
	private MessageDigest md = null;

	private SecureRandom rnd = null;

	/**
	 * Constructor
	 */
	public ParamGen(int mi, int Li, SecureRandom rnd) {
		super();

		m = mi;
		L = Li;
		this.rnd = rnd;
		System.out.println("*** FFC(Finite field cryptography) parameter generation ***");
		System.out.println("p : " + L + " bits");
		System.out.println("q : " + m + " bits");

		try {
			md = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			System.err.println(e.toString());
			System.exit(1);
		}
	}

	public MPInt[] generateParameters() {
		int i, j, counter;
		byte[] u1, u2, v;
		byte[] seedBytes = new byte[m / 8];
		MPInt SEED, U, q, R, V, W, X, p, g;
		// Generate p and q, where q is of length m and p is of length L
		// 1. Set m' = m/160 where / represents integer division with rounding
		// upwards. I.e. 200/160 = 2.
		int m_ = (m + 159) / 160;
		// 2. Set L'= L/160
		int L_ = (L + 159) / 160;
		// 3. Set N'= L/1024
		int N_ = (L + 1023) / 1024;
		algorithm: while (true) {
			step4: while (true) {
				// 4. Select an arbitrary bit string SEED such that length of SEED >= m
				nextRandomBytes(seedBytes);
				SEED = new MPInt(1, seedBytes).setBit(m - 1).setBit(0);
				// 5. Set U = 0
				U = MPInt.ZERO;
				// 6. For i = 0 to m' - 1
				// U = U + (SHA1[SEED + i] XOR SHA1[(SEED + m' + i)) * 2^(160 * i)
				// Note that for m=160, this reduces to the algorithm of
				// [FIPS-186]
				// U = SHA1[SEED] XOR SHA1[(SEED+1) mod 2^160 ].
				for (i = 0; i < m_; i++) {
					u1 = SEED.add(MPInt.valueOf(i)).toByteArray();
					u2 = SEED.add(MPInt.valueOf(m_ + i)).toByteArray();
					md.update(u1, 0, u1.length);
					u1 = md.digest();
					md.update(u2, 0, u2.length);
					u2 = md.digest();
					for (j = 0; j < u1.length; j++) {
						u1[j] ^= u2[j];
					}
					U = U.add(new MPInt(1, u1).multiply(TWO.pow(160 * i)));
				}
				// 5. Form q from U by computing U mod (2^m) and setting the most
				// significant bit (the 2^(m-1) bit) and the least significant bit to
				// 1. In terms of boolean operations, q = U OR 2^(m-1) OR 1.
				// Note
				// that 2^(m-1) < q < 2^m
				q = U.setBit(m - 1).setBit(0);
				// 6. Use a robust primality algorithm to test whether q is prime.
				// 7. If q is not prime then go to 4.
				if (q.isProbablePrime(25)) {
					break step4;
				}
			}
			// 8. Let counter = 0
			counter = 0;
			while (true) {
				// 9. Set R = seed + 2*m' + (L' * counter)
				R = SEED.add(MPInt.valueOf(2 * m_)).add(
						MPInt.valueOf(L_ * counter));
				// 10. Set V = 0
				V = MPInt.ZERO;
				// 12. For i = 0 to L'-1 do: V = V + SHA1(R + i) * 2^(160 * i)
				for (i = 0; i < L_; i++) {
					R = R.add(MPInt.valueOf(i));
					v = R.toByteArray();
					md.update(v, 0, v.length);
					v = md.digest();
					V = V.add(new MPInt(1, v).multiply(TWO.pow(160 * i)));
				}
				// 13. Set W = V mod 2^L
				W = V.mod(TWO.pow(L));
				// 14. Set X = W OR 2^(L-1)
				// Note that 0 <= W < 2^(L-1) and hence X >= 2^(L-1)
				X = W.setBit(L - 1);
				// 15. Set p = X - (X mod (2*q)) + 1
				p = X.add(MPInt.ONE).subtract(X.mod(TWO.multiply(q)));
				// 16. If p > 2^(L-1) use a robust primality test to test
				// whether p is prime. Else go to 18.
				// 17. If p is prime output p, q, seed, counter and stop.
				if (p.isProbablePrime(25)) {
					break algorithm;
				}
				// 18. Set counter = counter + 1
				counter++;
				// 19. If counter < (4096 * N) then go to 8.
				// 20. Output "failure"
				if (counter >= 4096 * N_) {
					continue algorithm;
				}
			}
		}

		// compute g. from FIPS-186, Appendix 4:
		// 1. Generate p and q as specified in Appendix 2.
		// 2. Let e = (p - 1) / q
		MPInt e = p.subtract(MPInt.ONE).divide(q);
		MPInt h = TWO;
		MPInt p_minus_1 = p.subtract(MPInt.ONE);
		g = TWO;
		// 3. Set h = any integer, where 1 < h < p - 1 and h differs from any
		// value previously tried
		for (; h.compareTo(p_minus_1) < 0; h = h.add(MPInt.ONE)) {
			// 4. Set g = h**e mod p
			g = h.modPow(e, p);
			// 5. If g = 1, go to step 3
			if (!g.equals(MPInt.ONE)) {
				break;
			}
		}
		//System.out.println("p = " + p.toString(16));
		//System.out.println("q = " + q.toString(16));
		//System.out.println("g = " + g.toString(16));
		System.out.println("(p - 1) / q = " + e.toString(16));
		System.out.println("seed = " + SEED.toString(16));
		System.out.println("counter = " + counter);

		return new MPInt[] { p, q, g };
	}

	/**
	 * Fills the designated byte array with random data.
	 */
	private void nextRandomBytes(byte[] buffer) {
		if (rnd != null) {
			rnd.nextBytes(buffer);
		}
	}

	/**
	 * Write a parameter file
	 */
	static void writeParams(String fname, String data) {

		try {
			PrintStream out = new PrintStream(new FileOutputStream(fname));
			out.print(data);
			out.close();
		} catch (IOException ex) {
			System.err.println(ex.toString());
		}
	}

	/**
	 * Gets a string of parameter
	 */
	public String toParamString(MPInt[] param) {

		String out = "p = " + param[0].toString(16) + "\n"
			+ "q = " + param[1].toString(16) + "\n"
			+ "g = " + param[2].toString(16) + "\n";

		return out;
	}

	/**
	 * Parameter generation
	 */
	public static void main(String[] args) {

		if (args.length < 1 || args.length > 3) {
			System.out.println("usage: ParamGen pLen [qLen [paramFile]]\n");
			return;
		}

		L  = Integer.parseInt(args[0]);
		if (args.length >= 2) {
			m = Integer.parseInt(args[1]);
		}
		if (args.length == 3) {
			paramFile = args[2];
		}

		// Random number generation
		Random rnd = new Random();
		byte[] seed = new byte[32];
		rnd.nextBytes(seed);
		SecureRandom rng = new SecureRandom(seed);

		// Parameter generation
		ParamGen pg = new ParamGen(m, L, rng);
		MPInt[] param = pg.generateParameters();
		
		// Write a parameter file
		writeParams(paramFile, pg.toParamString(param));
	}

}
