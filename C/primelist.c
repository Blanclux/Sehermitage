/**
 * primelist.c
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <stdio.h>
#include <stdlib.h>

char   *isprime;

/* Sieve of Eratosthenes */
void
eratosthenes(int n)
{
	int     i, k;

	isprime[0] = 0;
	isprime[1] = 0;

	for (i = 2; i < n; i++) {
		isprime[i] = 1;
	}

	for (k = 2; k * k < n; k++) {
		if (isprime[k]) {
			for (i = k * 2; i < n; i += k) {
				isprime[i] = 0;
			}
		}
	}
}

/*
 * Prime List Generation
 */
int
main(int argc, char *argv[])
{
	int     n = 10000;
	int     i, j = 0;
	int     count = 0;

	if (argc == 2) {
		n = atoi(argv[1]);
	}
	printf("< List of Prime Numbers (by Sieve of Eratosthenes) >\n");
	printf("  Maximum Number : %d\n", n);

	isprime = malloc(n);

	eratosthenes(n);

	for (i = 2; i < n; i++) {
		if (isprime[i]) {
			printf("%3d ", i);
			j++;
			count++;
		}
		if (j != 0 && j % 10 == 0) {
			printf("\n");
			j = 0;
		}
	}
	printf("\n# %d primes.\n", count);

	free(isprime);
	return 0;
}
