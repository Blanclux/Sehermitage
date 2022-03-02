/**
 * Prime.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.math;

import java.security.SecureRandom;

/**
 * Prime class
 */
public class Prime {
	// comparison results
	static final int LESS = -1;
	static final int EQUAL = 0;
	static final int GREATER = 1;
	// useful MPInt constants
	public static final MPInt ZERO = MPInt.valueOf(0);
	public static final MPInt ONE = MPInt.valueOf(1);
	public static final MPInt TWO = MPInt.valueOf(2);
	private static final int RABIN_P = 20;
	static final int MP_PSMAX = 172;
	private MPInt primeNo;
	private SecureRandom rng;
	// Small Prime Table
	private static final int[] P_SMALL = {
		2,
		3, 5, 7, 11, 13, 17, 19, 23, 29, 31,
		37, 41, 43, 47, 53, 59, 61, 67, 71, 73,
		79, 83, 89, 97, 101, 103, 107, 109, 113, 127,
		131, 137, 139, 149, 151, 157, 163, 167, 173, 179,
		181, 191, 193, 197, 199, 211, 223, 227, 229, 233,
		239, 241, 251, 257, 263, 269, 271, 277, 281, 283,
		293, 307, 311, 313, 317, 331, 337, 347, 349, 353,
		359, 367, 373, 379, 383, 389, 397, 401, 409, 419,
		421, 431, 433, 439, 443, 449, 457, 461, 463, 467,
		479, 487, 491, 499, 503, 509, 521, 523, 541, 547,
		557, 563, 569, 571, 577, 587, 593, 599, 601, 607,
		613, 617, 619, 631, 641, 643, 647, 653, 659, 661,
		673, 677, 683, 691, 701, 709, 719, 727, 733, 739,
		743, 751, 757, 761, 769, 773, 787, 797, 809, 811,
		821, 823, 827, 829, 839, 853, 857, 859, 863, 877,
		881, 883, 887, 907, 911, 919, 929, 937, 941, 947,
		953, 967, 971, 977, 983, 991, 997, 1009, 1013, 1019,
		1021
	};

	/**
	 * Constructor
	 * @param rng  the random number generator
	 */
	public Prime(SecureRandom rng) {
		primeNo = TWO;
		this.rng = rng;
	}

	/**
	 * Constructor
	 *
	 * @param bits the bit length of the prime number
	 * @param rng  the random number generator
	 */
	public Prime(int bits, SecureRandom rng) {
		this.rng = rng;
		genPrimeRandom(bits);
	}

	/**
	 * Constructor
	 *
	 * @param pmin the maximum prime number
	 * @param pmax the minimum prime number
	 * @param rng  the random number generator
	 */
	public Prime(MPInt pmin, MPInt pmax, SecureRandom rng) {
		primeNo = TWO;
		this.rng = rng;
		genPrimeRandom(pmax, pmin);
	}

	/**
	 * Get prime number
	 *
	 * @return the prime number
	 */
	public MPInt getPrime() {

		return primeNo;
	}

	/**
	 * Get small prime number
	 *
	 * @return the small prime number
	 */
	public static MPInt getSmallPrime(int i) {
		if (i >= MP_PSMAX) {
			return null;
		}
		return MPInt.valueOf(P_SMALL[i]);
	}

	/**
	 * Check prime
	 *
	 * @param n MPInt
	 * @return true: n is a prime number / false: n is not prime
	 */
	public static boolean isPrime(MPInt n) {
		int cmp = n.compareTo(TWO);

		if (cmp == LESS) {
			return false;
		} else if (cmp == EQUAL) {
			return true;
		}

		if (MPInt.isEven(n)) {
			return false;
		}

		if (issPrime(n)) {
			return true;
		}

		if (hassFact(n)) {
			return false;
		}

		return n.isProbablePrime(RABIN_P); 	// Rabin testing
	}

	/** MPInt is small prime ?
	 */
	private static boolean issPrime(MPInt n) {

		for (int i = 0; i < P_SMALL.length; i++) {

			if (n.compareTo(MPInt.valueOf(P_SMALL[i])) == 0) {

				return true;
			}
		}

		return false;
	}

	/** check if MPInt has small prime factor
	 */
	private static boolean hassFact(MPInt n) {

		if (MPInt.isEven(n)) {
			return true;
		}

		if (issPrime(n)) {
			return false;
		}

		if (MPInt.isMult5(n)) {
			return true;
		}

		if (MPInt.isMult3(n)) {
			return true;
		}

		MPInt r = ONE;
		int i = 2;

		while (i < MP_PSMAX) {
			r = n.remainder(MPInt.valueOf(P_SMALL[i++]));

			if (r.compareTo(ZERO) == 0) {
				break;
			}
		}

		return (r.compareTo(ZERO) == 0) ? true : false;
	}

	/** Find the smallest prime factor.
	 *  Return the index of the smallest factor in P_SMALL if it exists.
	 *  If MPInt n itself is a small prime, -1 is returned.
	 */
	public int findsFact(MPInt n) {

		if (MPInt.isEven(n)) {
			return 0;
		}

		if (MPInt.isMult3(n)) {
			return 1;
		}

		if (MPInt.isMult5(n)) {
			return 2;
		}

		MPInt r = ONE;
		int i = 2;

		while (++i < MP_PSMAX) {
			r = n.remainder(MPInt.valueOf(P_SMALL[i]));

			if (r.compareTo(ZERO) == 0) {
				break;
			}
		}

		if (r.compareTo(ZERO) == 0) {

			if (n.compareTo(MPInt.valueOf(P_SMALL[i])) == 0) {
				return -1; // MPInt itself is a small prime.
			} else {
				return i;
			}
		} else {
			return -1;
		}
	}

	/**
	 * Generation of random prime number
	 *
	 * @param bits  the bit length of the prime number
	 *
	 * @return true : success / false : failure
	 */
	public boolean genPrimeRandom(int bits) {
		int d = 2;
		int dir;

		if (bits <= 1) {
			return false;
		}

		dir = rng.nextInt() & 1;
		MPInt limit = ZERO;

		limit = limit.setBit(bits);
		limit = limit.subtract(ONE);
		MPInt p = MPInt.random(rng, limit);

		p = p.setBit(bits - 1);
		p = p.setBit(0);

		while (!isPrime(p)) {

			if (dir != 0) {
				p = p.add(MPInt.valueOf(d));

				if (p.testBit(bits)) {
					p = ONE;
					p = p.setBit(bits - 1);
				}
			} else {
				p = p.subtract(MPInt.valueOf(d));

				if (!p.testBit(bits - 1)) {
					p = MPInt.psetBit(p, bits - 1, 0); // no need to clear
				}
			}
		}

		primeNo = p;

		return true;
	}

	/**
	 * Generation of random prime number
	 *
	 * @param pmax the maximum prime number
	 * @param pmin the minimum prime number
	 *
	 * @return true : success / false : failure
	 */
	public boolean genPrimeRandom(MPInt pmax, MPInt pmin)
						   throws ArithmeticException {
		MPInt p;
		boolean isprime = false;
		int dir;
		int mxf = 0;
		int mnf = 0;

		if (pmax.compareTo(pmin) <= 0) {
			return false;
		}
		if (pmax.compareTo(TWO) <= 0) {
			return false;
		}
		if (pmax.compareTo(pmin) == 0 && MPInt.isEven(pmax)) {
			return false;
		}
		if (MPInt.isEven(pmax)) { // if pmax is even, +1
			mxf = 1;
			pmax = pmax.add(ONE);
		}

		if (MPInt.isEven(pmin)) { // if pmin is even, +1
			if (MPInt.isZero(pmin)) {
				mnf = 2;
				pmin = TWO;
			} else {
				mnf = 1;
				pmin = pmin.add(ONE);
			}
		}

		MPInt c = pmax.subtract(pmin); // counter init.

		// the counter will be halved after generating random iv of p
		do { // random number generation
			p = MPInt.random(rng, c);
			p = p.add(pmin);
			p = p.setBit(0);
		} while (pmax.compareTo(p) <= 0);

		c = c.shiftRight(1); // c can't be smaller than 1
		dir = rng.nextInt() & 1; // choose direction randomly

		do { // prime check
			isprime = isPrime(p);

			if (isprime) {
				break;
			}

			c = c.subtract(ONE);
			if (!(dir == 0)) {
				p = p.add(TWO);

				if (p.compareTo(pmax) >= 0) { // pmax is not within a's domain
					p = pmin;
				}
			} else {
				p = p.subtract(TWO);

				if ((p.compareTo(pmin) < 0) || MPInt.isOne(p)) {
					p = pmax;
				}
			}
		} while (!MPInt.isZero(c));

		if (!(mxf == 0)) {
			pmax = pmax.subtract(ONE);
		}
		if (!(mnf == 0)) {
			pmin = pmin.subtract(MPInt.valueOf(mnf));
		}

		primeNo = p;

		return isprime;
	}

	/**
	 * a and b are co-prime ?
	 *
	 * @param a MPInt
	 * @param b MPInt
	 * @return true: a and b are coprime / false: other
	 */
	public static boolean iscoPrime(MPInt a, MPInt b) {

		if (a.compareTo(ZERO) == 0 || b.compareTo(ZERO) == 0) {
			return false;
		}

		if (a.compareTo(ONE) == 0 || b.compareTo(ONE) == 0) {
			return true;
		}

		if (a.compareTo(b) == 0) {
			return false;
		}

		MPInt gcd = a.gcd(b);

		if (gcd.compareTo(ONE) == 0) {
			return true;
		} else {
			return false;
		}
	}
}
