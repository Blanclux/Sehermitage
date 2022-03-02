/*
 * RandFips.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.tools;

import java.security.*;
import java.util.*;

/**
 * FIPS PUB 140-2 Random Test
 */
class RandFips {

	static String algorithm = "SHA1PRNG";
	static final int SEC_NUM = 67;

	static int[] run0 = new int[6];
	static int[] run1 = new int[6];
	static int maxrun, maxrun0, maxrun1;

	// FIPS PUB 140-2
	static int rmin[] = { 2315, 1114, 527, 240, 103, 103 };
	static int rmax[] = { 2685, 1386, 723, 384, 209, 209 };

	static void distribution(byte data[], int Max, int value) {
		int middle;
		int left;
		int right;

		middle = Max / 2;
		left = middle - (Max / 8);
		right = middle + (Max / 8);

		if (value < left) {
			data[0]++;
		} else if (value > right) {
			data[SEC_NUM - 1]++;
		} else {
			data[value - left + 1]++;
		}

		return;
	}

	/**
	 * Return the bit difference between two data
	 */
	static int bitDiff(byte data1[], byte data2[], int length) {
		byte diff;
		int count = 0;

		for (int i = 0; i < length; i++) {
			diff = (byte) (data1[i] ^ data2[i]);
			for (int j = 0; j < 8; j++) {
				if ((diff & (1 << j)) != 0) {
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * Return the number of '1' bit
	 */
	static int bitCount(byte data[], int length) {
		int count = 0;

		for (int i = 0; i < length; i++) {
			for (int j = 0; j < 8; j++) {
				if ((data[i] & (1 << j)) != 0) {
					count++;
				}
			}
		}
		return count;
	}

	static void pokerCount(int F[], byte data[], int length) {
		int count1, count2;

		for (int i = 0; i < length; i++) {
			count1 = data[i] & 0xf0;
			count1 >>= 4;
			count2 = data[i] & 0x0f;
			F[count1] += 1;
			F[count2] += 1;
		}
		return;
	}

	/**
	 * Runs Count
	 */
	static void runCount(byte data[], int length) {
		int i, j;
		int count0 = 0, count1 = 0;
		int cont;
		int cont0 = 0, cont1 = 0;
		int tmp;

		maxrun = maxrun0 = maxrun1;
		j = 0;
		for (i = 0; i < length; i++) {
			do {
				cont = 0;
				tmp = data[i] & (1 << j);
				if ((cont1 == 0 && cont0 == 0) || (cont1 != 0 && tmp != 0)
						|| (cont0 != 0 && tmp == 0)) {
					if (tmp != 0) { /* one bit run */
						while ((data[i] & (1 << j)) != 0) {
							j++;
							count1++;
							if (j == 8) {
								cont = 1;
								cont1 = 1;
								j = 0;
								break;
							}
						}
					} else { /* zero bit run */
						while ((data[i] & (1 << j)) == 0) {
							j++;
							count0++;
							if (j == 8) {
								cont = 1;
								cont0 = 1;
								j = 0;
								break;
							}
						}
					}
				}
				if (cont == 0) {
					if (count0 > 0) {
						if (count0 < 6) {
							run0[count0 - 1]++;
						} else {
							run0[5]++;
						}
					} else {
						if (count1 < 6) {
							run1[count1 - 1]++;
						} else {
							run1[5]++;
						}
					}
					maxrun0 = (maxrun0 > count0) ? maxrun0 : count0;
					maxrun1 = (maxrun1 > count1) ? maxrun1 : count1;
					maxrun = (maxrun0 > maxrun1) ? maxrun0 : maxrun1;
					count0 = count1 = 0;
					cont0 = cont1 = 0;
				}
			} while (j != 8 && cont != 1);
		}
		return;
	}

	public static void main(String args[]) {
		byte[] ran = new byte[2500];
		int count, i;
		float x;
		int[] F = new int[16];
		int total0 = 0, total1 = 0;

		if (args.length == 1) {
			algorithm = args[0];
		}
		System.out.println("FIPS PUB 140-1");
		System.out.println("Statistical random number generator test");
		System.out.println("Algorithm : " + algorithm);

		Random rnd = new Random();
		int seedLen = 32;
		byte[] seed = new byte[seedLen];
		rnd.nextBytes(seed);

		SecureRandom rng = null;
		try {
			rng = SecureRandom.getInstance(algorithm);
			rng.setSeed(seed);
		} catch (Exception ex) {
			System.err.println(ex.toString());
			System.exit(1);
		}
		rng.nextBytes(ran);

		// The Mono bit Test
		count = bitCount(ran, 2500);
		System.out.println("< Monobit Test ( 9,725 < X < 10,275 ) >");
		System.out.println(" X = " + count);
		if (count < 9725 || count > 10275) {
			System.out.println(" ... Test NG ");
		} else {
			System.out.println(" ... Test OK");
		}
		System.out.println("");

		// The Poker Test
		for (i = 0; i < 16; i++) {
			F[i] = 0;
		}
		pokerCount(F, ran, 2500);
		x = 0;
		for (i = 0; i < 16; i++) {
			x += F[i] * F[i];
		}
		x = (float) (16.0 / 5000.0) * x - (float) 5000.0;

		System.out.println("< Poker Test ( 2.16 < X < 46.17 ) >");
		System.out.println(" X = " + x);

		if (x < 2.16 || x > 46.17) {
			System.out.println(" ... Test NG");
		} else {
			System.out.println(" ... Test OK");
		}
		System.out.println("");

		// The Run Test
		for (i = 0; i < 6; i++) {
			run0[i] = run1[i] = 0;
		}
		runCount(ran, 2500);
		System.out.println("< Runs Test >");
		for (i = 0; i < 6; i++) {
			System.out.println(" Length of Run: " + (i + 1));
			System.out.println(" (" + rmin[i] + " < Length of Run < " + rmax[i]
					+ " )\n");
			System.out.println(" Zero runs = " + run0[i]);
			System.out.println(" One  runs = " + run1[i]);
			if (run0[i] < rmin[i] || run0[i] > rmax[i] || run1[i] < rmin[i]
					|| run1[i] > rmax[i]) {
				System.out.println(" ... Test NG ");
			} else {
				System.out.println(" ... Test OK");
			}
		}
		for (i = 0; i < 6; i++) {
			total0 += (i + 1) * run0[i];
			total1 += (i + 1) * run1[i];
		}
		System.out.println(" Total zero's bits = " + total0);
		System.out.println(" Total one's  bits = " + total1);
		System.out.println("");

		// The Long Run Test
		System.out.println("< Long Run Test ( < 26 ) >");
		System.out.println(" Long run (zero) = " + maxrun0);
		System.out.println(" Long run (one)  = " + maxrun1);
		if (maxrun >= 26) {
			System.out.println(" ... Test NG ");
		} else {
			System.out.println(" ... Test OK");
		}
	}
}
