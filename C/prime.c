/**
 * prime.c
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <stdio.h>
#include <stdlib.h>
#include <math.h>

int
main(int argc, char *argv[])
{
	int     i, n;
	int		max;
	int     prime;

	if (argc != 2) {
		printf("usage: prime number\n");
		return (1);
	}
	n = atoi(argv[1]);

	i = 3;
	prime = 1;
	max = (int) sqrt((double) n);

	if (n != 2 && n % 2 == 0) {
		prime = 0;
	}

	while (i < max + 1 && prime == 1) {
		if (n % i == 0) {
			prime = 0;
		}
		i += 2;
	}
	if (prime == 1) {
		printf("%d is a prime number.\n", n);
	} else {
		printf("%d is a composite number.\n", n);
	}
	return 0;
}
