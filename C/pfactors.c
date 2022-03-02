/**
 * pfactors.c
 *  Prime factorization program
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <stdio.h>
#include <stdlib.h>

#define FACTORS_SIZE 64

typedef struct fact_list {
	unsigned int fact;
	unsigned int ex;
} FACT;

int
prime_factorize(unsigned int n, FACT factor[])
{
	int count = 0;
	unsigned int prime = 2;
	unsigned int e = 0;
	unsigned int m;

	if (n < 4) {
		factor[count].fact = n;
		factor[count].ex = 1;
		return 1;
	}

	for (m = n; prime * prime <= m; prime++) {
		while (m % prime == 0) {
			if (count >= FACTORS_SIZE) {
				return 0;
			}
			if (e == 0) {
				factor[count].fact = prime;
			}
			factor[count].ex = ++e;
			m /= prime;
		}
		if (e > 0) {
			count++;
			e = 0;
		}
	}

	if (m != 1) {
		if (count >= FACTORS_SIZE) {
			return 0;
		}
		factor[count].fact = m;
		factor[count].ex = 1;
		count++;
	}
	factor[count + 1].ex = 0;

	return count;
}

void
print_factors(FACT factor[], int size)
{
	int  i;

	if (size > 0) {
		if (factor[0].ex == 1) {
			printf("%u", factor[0].fact);
		} else {
			printf("%u^%u", factor[0].fact, factor[0].ex);
		}
	}

	for (i = 1; i < size; i++) {
		if (factor[i].ex == 1) {
			printf(" * %u", factor[i].fact);
		} else {
			printf(" * %u^%u", factor[i].fact, factor[i].ex);
		}
		if (factor[i + 1].ex == 0) {
			break;
		}
	}
	putchar('\n');
}

int
main(int argc, char *argv[])
{
	FACT factor[FACTORS_SIZE];
	unsigned int n, count;

	if (argc == 1 || argc > 2) {
		printf("usage: pfactors number\n");
		exit(1);
	}

	n = atoi(argv[1]);

	printf("< Prime factorization >\n");
	count = prime_factorize(n, factor);
	printf("%d = ", n);
	print_factors(factor, count);

	return 0;
}
