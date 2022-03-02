/**
 * MPInt.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.math;

import java.math.BigInteger;
import java.security.SecureRandom;

public final class MPInt {
	// useful MPInt constants
	public static final MPInt ZERO = new MPInt(BigInteger.ZERO);
	public static final MPInt ONE = valueOf(1);
	public static final MPInt TWO = valueOf(2);

	public BigInteger bigInt;

	public MPInt(byte[] val) {
		bigInt = new BigInteger(val);
	}

	public MPInt(String val) {
		bigInt = new BigInteger(val);
	}

	public MPInt(int signum, byte[] magnitude) {
		bigInt = new BigInteger(signum, magnitude);
	}

	public MPInt(String val, int radix) {
		bigInt = new BigInteger(val, radix);
	}

	public MPInt(int numBits, SecureRandom rng) {
		bigInt = new BigInteger(numBits, rng);
	}

	public MPInt(int bitLength, int certainty, SecureRandom rng) {
		bigInt = new BigInteger(bitLength, certainty, rng);
	}

	public MPInt(BigInteger bigInt) {
		this.bigInt = bigInt;
	}

	public MPInt clone() {
		return new MPInt(bigInt);
	}

	public void setOne() {
		bigInt = BigInteger.ONE;
	}

	public void setZero() {
		bigInt = BigInteger.ZERO;
	}

	public boolean isZero() {
		return this.compareTo(ZERO) == 0;
	}

	public static MPInt valueOf(long val) {
		return new MPInt(BigInteger.valueOf(val));
	}

	public MPInt add(MPInt addend) {
		return new MPInt(bigInt.add(addend.bigInt));
	}

	public MPInt subtract(MPInt minuend) {
		return new MPInt(bigInt.subtract(minuend.bigInt));
	}

	public MPInt multiply(MPInt factor) {
		return new MPInt(bigInt.multiply(factor.bigInt));
	}

	public MPInt divide(MPInt divisor) {
		return new MPInt(bigInt.divide(divisor.bigInt));
	}

	public MPInt[] divideAndRemainder(MPInt divisor) {
		BigInteger[] dar = bigInt.divideAndRemainder(divisor.bigInt);
		return new MPInt[] { new MPInt(dar[0]), new MPInt(dar[1]) };
	}

	public MPInt remainder(MPInt divisor) {
		return new MPInt(bigInt.remainder(divisor.bigInt));
	}

	public MPInt pow(int exponent) {
		return new MPInt(bigInt.pow(exponent));
	}

	public MPInt gcd(MPInt val) {
		return new MPInt(bigInt.gcd(val.bigInt));
	}

	public MPInt abs() {
		return new MPInt(bigInt.abs());
	}

	public MPInt negate() {
		return new MPInt(bigInt.negate());
	}

	public int signum() {
		return bigInt.signum();
	}

	public MPInt mod(MPInt modulus) {
		return new MPInt(bigInt.mod(modulus.bigInt));
	}

	public MPInt modPow(MPInt exponent, MPInt modulus) {
		return new MPInt(bigInt.modPow(exponent.bigInt, modulus.bigInt));
	}

	public MPInt modInverse(MPInt modulus) {
		return new MPInt(bigInt.modInverse(modulus.bigInt));
	}

	public MPInt shiftLeft(int n) {
		return new MPInt(bigInt.shiftLeft(n));
	}

	public MPInt shiftRight(int n) {
		return new MPInt(bigInt.shiftRight(n));
	}

	public MPInt and(MPInt val) {
		return new MPInt(bigInt.and(val.bigInt));
	}

	public MPInt or(MPInt val) {
		return new MPInt(bigInt.or(val.bigInt));
	}

	public MPInt xor(MPInt val) {
		return new MPInt(bigInt.xor(val.bigInt));
	}

	public MPInt not() {
		return new MPInt(bigInt.not());
	}

	public MPInt andNot(MPInt val) {
		return new MPInt(bigInt.andNot(val.bigInt));
	}

	public boolean testBit(int n) {
		return bigInt.testBit(n);
	}

	public MPInt setBit(int n) {
		return new MPInt(bigInt.setBit(n));
	}

	public MPInt clearBit(int n) {
		return new MPInt(bigInt.clearBit(n));
	}

	public MPInt flipBit(int n) {
		return new MPInt(bigInt.flipBit(n));
	}

	public int getLowestSetBit() {
		return bigInt.getLowestSetBit();
	}

	public int bitLength() {
		return bigInt.bitLength();
	}

	public int bitCount() {
		return bigInt.bitCount();
	}

	public boolean isProbablePrime(int certainty) {
		return bigInt.isProbablePrime(certainty);
	}

	public int compareTo(MPInt other) {
		return bigInt.compareTo(other.bigInt);
	}

	public MPInt min(MPInt other) {
		return new MPInt(bigInt.min(other.bigInt));
	}

	public MPInt max(MPInt other) {
		return new MPInt(bigInt.max(other.bigInt));
	}

	public boolean equals(Object other) {
		if (!(other instanceof MPInt)) {
			return false;
		}
		return bigInt.equals(((MPInt) other).bigInt);
	}

	public int hashCode() {
		return bigInt.hashCode();
	}

	public String toString(int radix) {
		return bigInt.toString(radix);
	}

	public String toString() {
		return bigInt.toString();
	}

	public byte[] toByteArray() {
		return bigInt.toByteArray();
	}

	public int intValue() {
		return bigInt.intValue();
	}

	public long longValue() {
		return bigInt.longValue();
	}

	public float floatValue() {
		return bigInt.floatValue();
	}

	public double doubleValue() {
		return bigInt.doubleValue();
	}

	/**
	 * is the number zero?
	 * 
	 * @param n MPInt value
	 * @return true: n = 0 / false: n != 0
	 */
	public static boolean isZero(MPInt n) {

		return n.compareTo(ZERO) == 0;
	}

	/**
	 * is the number one?
	 * 
	 * @param n MPInt value
	 * @return true: n = 1 / false: n != 1
	 */
	public static boolean isOne(MPInt n) {

		return n.compareTo(ONE) == 0;
	}

	/**
	 * is the number even?
	 * 
	 * @param n MPInt value
	 * @return true: n is even / false: n is odd
	 */
	public static boolean isEven(MPInt n) {

		return n.testBit(0) ? false : true;
	}

	/**
	 * is the number odd?
	 * 
	 * @param n MPInt value
	 * @return true: n is odd / false: n id even
	 */
	public static boolean isOdd(MPInt n) {

		return n.testBit(0);
	}

	/**
	 * is the number multiple of 3
	 * 
	 * @param n MPInt value
	 * @return true if n is multiple of 3 else false
	 */
	public static boolean isMult3(MPInt n) {

		return (n.remainder(MPInt.valueOf(3)).compareTo(ZERO) == 0) ? true
				: false;
	}

	/**
	 * is the number multiple of 5
	 * 
	 * @param n MPInt value
	 * @return true if n is multiple of 5 else false
	 */
	public static boolean isMult5(MPInt n) {

		return (n.remainder(MPInt.valueOf(5)).compareTo(ZERO) == 0) ? true
				: false;
	}

	/**
	 * partial bit set
	 * set 1 from the sb bit position to the eb bit position<br>
	 * starting 0(sb > eb)<br>
	 * 
	 * @param n MPInt value
	 * @return the result value
	 */
	public static MPInt psetBit(MPInt n, int sb, int eb) {
		MPInt y = new MPInt(n.toByteArray());

		for (int i = sb; i >= eb; i--) {
			y = y.setBit(i);
		}

		return y;
	}

	/**
	 * shift right to LSB = 1
	 * 
	 * @param n MPInt value
	 * @return the shifted value
	 */
	public static MPInt stuff(MPInt n) {
		MPInt x = new MPInt(n.toByteArray());

		if (isZero(x) || isOdd(x)) {
			return ZERO;
		}

		int shift = 0;

		for (int i = 0; i < x.bitLength(); i++) {
			if (x.testBit(i) == false) {
				shift++;
			} else {
				break;
			}
		}
		x = x.shiftRight(shift);

		return x;
	}

	/**
	 * a + b mod n
	 * 
	 * @param a MPInt value
	 * @param b MPInt value
	 * @param n the modulus
	 * @return Returns a MPInt whose value is a + b (mod n).
	 */
	public static MPInt modadd(MPInt a, MPInt b, MPInt n) {
		MPInt c = a.add(b);

		if (n.compareTo(c) < 0) {
			c = c.subtract(n);
		}

		return c;
	}

	/**
	 * a - b mod n
	 * 
	 * @param a MPInt value
	 * @param b MPInt value
	 * @param n the modulus
	 * @return Returns a BigInteger whose value is a - b (mod n).
	 */
	public static MPInt modsub(MPInt a, MPInt b, MPInt n) {
		MPInt c;

		if (a.compareTo(b) < 0) {
			c = a.add(n);
			c = c.subtract(b);
		} else {
			c = a.subtract(b);
		}

		return c;
	}

	/**
	 * a * b mod n
	 * 
	 * @param a MPInt value
	 * @param b MPInt value
	 * @param n the modulus
	 * @return Returns a MPInt whose value is a * b (mod n).
	 */
	public static MPInt modmult(MPInt a, MPInt b, MPInt n) {

		if (MPInt.isZero(a) || MPInt.isZero(b)) {
			return ZERO;
		}

		return (a.multiply(b)).mod(n);
	}

	/**
	 * a ^ e
	 * 
	 * @param n MPInt value
	 * @param e long value
	 * @return (n^e)
	 */
	public static MPInt expu(MPInt n, long e) {
		long msk = 2;
		int blen = 1;

		if (e == 0) {
			return ONE;
		}

		if (isZero(n)) {
			return ZERO;
		}

		long et = e;

		while ((et >>>= 1) != 0) {
			blen++;
		}

		MPInt t = n;

		for (int i = 1; i < blen; i++) {
			t = t.multiply(t); // at = at^2

			if ((msk & e) != 0) {
				t = t.multiply(n); // at = at * n
			}

			msk <<= 1;
		}

		return t;
	}

	/**
	 * n^d mod pq (CRT)
	 * 
	 * @param n MPInt value
	 * @param dp d mod p-1
	 * @param dq d mod q-1
	 * @param p prime
	 * @param q prime
	 * @param qi 1/q mod p
	 * @return (n^d mod pq)
	 */
	public static MPInt modexpCrt(MPInt n, MPInt dp,
			MPInt dq, MPInt p, MPInt q, MPInt qi) {

		MPInt xp = n.mod(p).modPow(dp, p);
		MPInt xq = n.mod(q).modPow(dq, q);
		MPInt t = modsub(xp, xq, p);

		t = t.multiply(qi).mod(p);

		return t.multiply(q).add(xq);
	}

	/**
	 * floor(sqrt(n))
	 * 
	 * @param n MPInt value
	 * @return (floor(sqrt(n))
	 */
	public static MPInt sqrt(MPInt n) {
		MPInt sb;
		MPInt s;
		MPInt st;
		MPInt t1;
		MPInt t2;
		int b = n.bitLength() + 1;

		sb = MPInt.ONE;
		t1 = MPInt.ZERO;
		t2 = n;
		s = MPInt.ZERO;
		b >>= 1;

		while (b-- != 0) {
			st = s.add(sb.shiftLeft(b));
			t1 = st.multiply(st);

			if (t2.compareTo(t1) >= 0) {
				s = st;
			}

			if (t2.compareTo(t1) == 0) {
				break;
			}
		}

		return s;
	}

	/**
	 * Random
	 * 
	 * @param rng random number generator
	 * @param limit the limit value
	 * @return MPInt value (from 0 to limit-1)
	 */
	public static MPInt random(java.security.SecureRandom rng, MPInt limit) {

		if (limit.compareTo(ZERO) <= 0) {
			throw new ArithmeticException("Random limit must be positive");
		}

		int bitLen = limit.bitLength();
		MPInt rnd;

		do {
			rnd = new MPInt(bitLen, rng);
		} while (rnd.compareTo(limit) >= 0);

		return rnd;
	}

	/**
	 * LCM ( a, b )
	 */
	public static MPInt lcm(MPInt a, MPInt b) {
		MPInt l;
		MPInt s;
		MPInt sr;
		MPInt gd;
		int rv;

		if (MPInt.isZero(a) || MPInt.isZero(b)) {
			return ZERO;
		}

		rv = a.compareTo(b); // a == b

		if (rv == 0) {
			return new MPInt(a.toByteArray());
		} else if (rv > 0) { // a > b
			l = a;
			s = b;
		} else { // a < b
			l = b;
			s = a;
		}

		gd = s.gcd(l);
		sr = s.divide(gd);

		return sr.multiply(l);
	}

	/**
	 * x^2 = a (mod p)
	 * 
	 * @param a the MPInt a
	 * @param p the MPInt p
	 * @return a MPInt x such that x^2 = a mod p
	 */
	public static MPInt modsqrt(MPInt a, MPInt p) {

		MPInt v = null;

		if (a.compareTo(ZERO) < 0) {
			a = a.add(p);
		}
		if (a.equals(ZERO)) {
			return ZERO;
		}
		if (p.equals(TWO)) {
			return a;
		}

		// p = 3 mod 4
		if (p.testBit(0) && p.testBit(1)) {
			if (jacobi(a, p) == 1) { // a quadr. residue mod p
				v = p.add(ONE); // v = p+1
				v = v.shiftRight(2); // v = v/4
				return a.modPow(v, p); // return a^v mod p
				// return --> a^((p+1)/4) mod p
			}
			throw new ArithmeticException();
		}

		long t = 0;
		// initialization
		// compute k and s, where p = 2^s (2k+1) +1
		MPInt k = p.subtract(ONE); // k = p-1
		long s = 0;
		while (!k.testBit(0)) { // while k is even
			s++; // s = s+1
			k = k.shiftRight(1); // k = k/2
		}

		k = k.subtract(ONE); // k = k - 1
		k = k.shiftRight(1); // k = k/2

		// initial values
		MPInt r = a.modPow(k, p); // r = a^k mod p

		MPInt n = r.multiply(r).remainder(p); // n = r^2 % p
		n = n.multiply(a).remainder(p); // n = n * a % p
		r = r.multiply(a).remainder(p); // r = r * a %p

		if (n.equals(ONE)) {
			return r;
		}

		// non-quadratic residue
		MPInt z = TWO; // z = 2
		while (jacobi(z, p) == 1) {
			// while z quadratic residue
			z = z.add(ONE); // z = z + 1
		}

		v = k;
		v = v.multiply(TWO); // v = 2k
		v = v.add(ONE); // v = 2k + 1
		MPInt c = z.modPow(v, p); // c = z^v mod p

		while (n.compareTo(ONE) == 1) { // n > 1
			k = n; // k = n
			t = s; // t = s
			s = 0;

			while (!k.equals(ONE)) { // k != 1
				k = k.multiply(k).mod(p); // k = k^2 % p
				s++; // s = s + 1
			}

			t -= s; // t = t - s
			if (t == 0) {
				throw new ArithmeticException();
			}

			v = ONE;
			for (long i = 0; i < t - 1; i++) {
				v = v.shiftLeft(1); // v = 1 * 2^(t - 1)
			}
			c = c.modPow(v, p); // c = c^v mod p
			r = r.multiply(c).remainder(p); // r = r * c % p
			c = c.multiply(c).remainder(p); // c = c^2 % p
			n = n.multiply(c).mod(p); // n = n * c % p
		}
		return r;
	}

	// the jacobi function uses this lookup table
	private static final int[] jacobiTable = { 0, 1, 0, -1, 0, -1, 0, 1 };
	/**
	 * Computes the value of the Jacobi symbol (A|B).
	 * 
	 * @param A integer value
	 * @param B integer value
	 * @return value of the jacobi symbol (A|B)
	 */
	public static int jacobi(MPInt A, MPInt B) {
		MPInt a, b, v;
		long k = 1;

		// test trivial cases
		if (B.equals(ZERO)) {
			a = A.abs();
			return a.equals(ONE) ? 1 : 0;
		}
		if (!A.testBit(0) && !B.testBit(0)) {
			return 0;
		}

		a = A;
		b = B;
		if (b.signum() == -1) { // b < 0
			b = b.negate(); // b = -b
			if (a.signum() == -1) {
				k = -1;
			}
		}
		v = ZERO;
		while (!b.testBit(0)) {
			v = v.add(ONE); // v = v + 1
			b = b.divide(TWO); // b = b/2
		}

		if (v.testBit(0)) {
			k = k * jacobiTable[a.intValue() & 7];
		}
		if (a.signum() < 0) { // a < 0
			if (b.testBit(1)) {
				k = -k; // k = -k
			}
			a = a.negate(); // a = -a
		}

		// main loop
		while (a.signum() != 0) {
			v = ZERO;
			while (!a.testBit(0)) { // a is even
				v = v.add(ONE);
				a = a.divide(TWO);
			}
			if (v.testBit(0)) {
				k = k * jacobiTable[b.intValue() & 7];
			}
			if (a.compareTo(b) < 0) { // a < b
				// swap and correct intermediate result
				MPInt x = a;
				a = b;
				b = x;
				if (a.testBit(1) && b.testBit(1)) {
					k = -k;
				}
			}
			a = a.subtract(b);
		}

		return b.equals(ONE) ? (int) k : 0;
	}

	/**
	 * jacobi(a, b)
	 * 
	 * @param a the MPInt a
	 * @param p the MPInt p
	 * @return 0 / 1 / -1
	 */
	public static int jacobi2(MPInt a, MPInt p) {
		MPInt n;
		MPInt m;
		MPInt w;
		int x = 1;

		n = a;
		m = p;

		for (;;) {
			n = n.mod(m);

			if (isZero(n)) {
				return 0;
			}

			if (n.compareTo(m.subtract(ONE)) == 0) {
				if (m.mod(MPInt.valueOf(4)).intValue() == 3) {
					x = -x;
				}
				return x;
			}

			for (;;) {
				if (isOne(n)) {
					return x;
				}
				if (isOdd(n)) {
					break;
				}
				n = n.shiftRight(1);
				if (m.mod(MPInt.valueOf(8)).intValue() == 3
						|| m.mod(MPInt.valueOf(8)).intValue() == 5) {
					x = -x;
				}
			}

			if (n.mod(MPInt.valueOf(4)).intValue() == 3
					&& m.mod(MPInt.valueOf(4)).intValue() == 3) {
				x = -x;
			}

			w = n;
			n = m;
			m = w;
		}
	}

	public static boolean isPrime(int n) {
		String s = "";
		s += n;
		MPInt p = new MPInt(s);
		return p.isProbablePrime(25);
	}

	/**
	 * gcd(a, b) 
	 * Computes the greatest common divisor of the two specified
	 * integers.
	 * 
	 * @param u first parameter
	 * @param v second parameter
	 * 
	 * @return gcd(a, b)
	 */
	public static int gcd(int u, int v) {
		int a = u;
		int b = v;
		int r;

		if (a < b) {
			r = a;
			a = b;
			b = r;
		}
		r = a % b;
		while (r != 0) {
			a = b;
			b = r;
			r = a % b;
		}
		return b;
	}

	public static MPInt gcd(MPInt a, MPInt b) {
		MPInt[] ret = MPInt.exgcd(a, b);
		return ret[0];
	}
	
	/**
	 * exgcd(a, b) 
	 * Extended Eucrid algorithm Return gcd(a, b), and S and T such
	 * that g = as + bt.
	 */
	public static MPInt[] exgcd(MPInt a, MPInt b) {
		MPInt s0;
		MPInt s1;
		MPInt t;
		MPInt x;
		MPInt d0;
		MPInt d1;
		MPInt[] qr;
		MPInt[] ret = new MPInt[3];

		s0 = ONE;
		s1 = ZERO;
		d0 = a;
		d1 = b;

		while (!isZero(d1)) {
			qr = d0.divideAndRemainder(d1);
			d0 = d1;
			d1 = qr[1];
			x = s1.multiply(qr[0]);
			x = s0.subtract(x);
			s0 = s1;
			s1 = x;
		}

		x = s0.multiply(a);
		x = d0.subtract(x);

		if (isZero(b)) {
			t = ZERO;
		} else {
			t = x.divide(b);
		}

		ret[0] = d0;
		ret[1] = s0;
		ret[2] = t;

		return ret;
	}

	/**
	 * Determines the order of g modulo p, p prime and 1 < g < p. This algorithm
	 * is only efficient for small p.
	 * 
	 * @param g an integer with 1 < g < p
	 * @param p a prime
	 * 
	 * @return the order k of g (that is k is the smallest integer with g^k = 1
	 *         mod p
	 */
	public static int order(int g, int p) {
		int b = g;
		int j = 1;

		while (b != 1) {
			b *= g;
			b %= p;
			if (b < 0) {
				b += p;
			}
			j++;
		}
		return j;
	}

	/**
	 * Converts to a Byte String.
	 * 
	 * @param n the MPInt value
	 * @param len the length to be converted
	 */
	public static byte[] toByteArray(MPInt n, int len) {
		byte[] val = n.toByteArray();
		int cLen = val.length;

		if (cLen == len) {
			return val;
		}

		byte[] ret = new byte[len];

		if (cLen < len) {
			System.arraycopy(val, 0, ret, len - cLen, cLen);
		} else if (cLen > len) { // delete sign byte
			System.arraycopy(val, cLen - len, ret, 0, len);
		}

		return ret;
	}

	/**
	 * Converts to a Fixed Length Byte String.
	 * 
	 * @param x the MPInt value
	 * @param byteLen the length to be converted
	 */
	public static byte[] toFixedBytes(MPInt x, int byteLen) {

		if (x.signum() != 1) {
			throw new IllegalArgumentException("MPInt not positive.");
		}

		byte[] xb = x.toByteArray();
		int xLen = xb.length;

		/* check sign byte */
		int xOff = (xb[0] == 0) ? 1 : 0;
		xLen -= xOff;

		if (xLen > byteLen) {
			throw new IllegalArgumentException("MPInt too large.");
		}

		byte[] result = new byte[byteLen];
		int resOff = byteLen - xLen;

		System.arraycopy(xb, xOff, result, resOff, xLen);
		return result;
	}

	/**
	 * Converts an integer to an octet string according to P1363.
	 * 
	 * @param bi an integer
	 * @return an octet string representing the integer <i>bi</i>
	 */
	public static byte[] i2OSP(MPInt bi) {
		byte[] result = bi.abs().toByteArray();

		// check whether the array includes a sign bit
		if ((bi.bitLength() & 7) != 0) {
			return result;
		}
		// get rid of the sign bit (first byte)
		byte[] tmp = new byte[bi.bitLength() >> 3];
		System.arraycopy(result, 1, tmp, 0, tmp.length);
		return tmp;
	}

	/**
	 * Converts an octet string to an integer according to P1363.
	 * 
	 * @param os an octet string
	 * @return an integer representing the octet string <i>os</i>
	 */
	public static MPInt os2IP(byte[] os) {
		int length = os.length;
		byte[] val = new byte[length + 1];

		val[0] = 0;
		System.arraycopy(os, 0, val, 1, length);
		return new MPInt(val);
	}

	/**
	 * FE2IP
	 */
	public static MPInt fe2IP(int[] fe, int p) {
		MPInt ip = MPInt.ZERO;
		MPInt pi = MPInt.valueOf(p);
		// i = (fe[j] + i) * p
		int j;
		for (j = fe.length - 1; j > 0; j--) {
			ip = ip.add(MPInt.valueOf(fe[j]));
			ip = ip.multiply(pi);
		}
		// i = i + fe[0]
		ip = ip.add(MPInt.valueOf(fe[j]));

		return ip;
	}

	public static MPInt fe2IP(int[] fe, MPInt p) {
		MPInt iv = MPInt.ZERO;

		// iv = (fe[i] + iv) * p
		for (int i = fe.length - 1; i > 0; i--) {
			iv = iv.add(MPInt.valueOf(fe[i]));
			iv = iv.multiply(p);
		}
		// iv = iv + fe[0]
		iv = iv.add(MPInt.valueOf(fe[0]));

		return iv;
	}

	/**
	 * I2FEP
	 */
	public static int[] i2FEP(MPInt n, int p) {
		MPInt pi = MPInt.valueOf(p);
		MPInt[] qr = new MPInt[2];
		MPInt t;
		int M = n.toByteArray().length;
		int[] fe = new int[M];

		t = n;
		for (int i = 0; i < M; i++) {
			qr = t.divideAndRemainder(pi);
			fe[i] = qr[1].intValue();
			t = qr[0];
		}
		return fe;
	}

	public static int[] i2FEP(MPInt n, MPInt p, int m) {
		MPInt[] qr = new MPInt[2];
		int[] fe = new int[m];

		MPInt t = n;
		for (int i = 0; i < m; i++) {
			qr = t.divideAndRemainder(p);
			fe[i] = qr[1].intValue();
			t = qr[0];
		}
		return fe;
	}

}
