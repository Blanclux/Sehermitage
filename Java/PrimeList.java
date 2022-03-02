/**
 * PrimeList.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.math;

// Prime List Generation
public class PrimeList {
	static boolean[] isprime;

	public static void main(String[] args) {
		int n = 10000;

		if (args.length != 0) {
			try {
				n = Integer.parseInt(args[0]);
			} catch (Exception e) {
				throw new IllegalArgumentException("Illegal number");
			}
		}
		System.out
				.println("< List of Prime Numbers (by Sieve of Eratosthenes) >");
		System.out.println("  Maximum Number : " + n + "\n");

		isprime = new boolean[n];

		Eratosthenes(n);

		int j = 0;
		int count = 0;
		for (int i = 2; i < n; i++) {
			if (isprime[i]) {
				System.out.print(i + " ");
				j++;
				count++;
			}
			if (j != 0 && j % 10 == 0) {
				System.out.println("");
				j = 0;
			}
		}
		System.out.println("\n# " + count + " primes");
	}

	// Sieve of Eratosthenes
	public static void Eratosthenes(int n) {
		isprime[0] = false;
		isprime[1] = false;

		for (int i = 2; i < n; i++) {
			isprime[i] = true;
		}

		for (int k = 2; k * k < n; k++) {
			if (isprime[k]) {
				for (int i = k * 2; i < n; i += k) {
					isprime[i] = false;
				}
			}
		}
	}
}
