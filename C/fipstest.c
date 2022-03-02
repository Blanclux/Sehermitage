/**
 * fipstest.c
 * FIPS PUB 140-2 Test Program
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

extern void randgen(unsigned char *rnd, int rndLen, char *seed, int seedLen);

#define SEC_NUM 67

#ifndef max
# define max(a, b) (((a) > (b)) ? (a) : (b))
#endif

static int run0[6], run1[6];
static int maxrun, maxrun0, maxrun1;

static int rmin[] = {2315, 1114, 527, 240, 103, 103};
static int rmax[] = {2685, 1386, 723, 384, 209, 209};

/*
 * Return the number of '1' bit
 */
static int
bit_count(unsigned char data[], unsigned int length)
{
	unsigned int i, j;
	int count = 0;

	for (i = 0; i < length; i++) {
		for (j = 0; j < 8; j++) {
			if (data[i] & (1 << j)) {
				count++;
			}
		}
	}
	return count;
}

static void
poker_count(int F[], unsigned char data[], unsigned int length)
{
	unsigned int i;
	unsigned int count1, count2;

	for (i = 0; i < length; i++) {
		count1 = data[i] & 0xf0;
		count1 >>= 4;
		count2 = data[i] & 0x0f;
		F[count1]++;
		F[count2]++;
	}
	return;
}

/*
 * Runs Count
 */
static void
run_count(unsigned char data[], unsigned int length)
{
	unsigned int i, j;
	int count0 = 0, count1 = 0;
	int cont;
	int cont0 = 0, cont1 = 0;
	int tmp;

	maxrun = maxrun0 = maxrun1;
	j = 0;
	for (i = 0; i < length; i++) {
		do {
			cont = 0;
			tmp =  data[i] & (1 << j);
			if ((cont1 == 0 && cont0 == 0) ||
				(cont1 && tmp) || (cont0 && !tmp)) {
				if (tmp) {	/* one bit run */
					while (data[i] & (1 << j)) {
						j++;
						count1++;
						if (j == 8) {
							cont = 1;
							cont1 = 1;
							j = 0;
							break;
						}
					}
				} else {	/* zero bit run */
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
				maxrun0 = max(maxrun0, count0);
				maxrun1 = max(maxrun1, count1);
				maxrun  = max(maxrun0, maxrun1);
				count0 = count1 = 0;
				cont0 = cont1 = 0;
			}
		} while (j != 8 && cont != 1);
	}
	return;
}

int
main(int argc , char *argv[])
{
	unsigned char ran[2500];
	int count, i;
	double x;
	int F[16];
	int total0 = 0, total1 = 0;

	puts("Statistical random number generator test (FIPS PUB 140-2)");

	char seed[16];

	if (argc >= 2) {
		strncpy(seed, argv[1], 16);
	} else {
		srand(time(NULL));
		for (i = 0; i < 16; i += 4) {
			*(int *)&seed[i] = rand();
		}
	}
	randgen(ran, 2500, seed, 16);

	/* The Monobit Test */
	count = bit_count(ran, 2500);
	puts("< Monobit Test ( 9,725 < X < 10,275 ) >");
	printf(" X = %d\n", count);
	if (count < 9725 || count > 10275) {
		puts(" ... Test NG ");
	} else {
		puts(" ... Test OK");
	}
	puts("");

	/* The Poker Test */
	for (i = 0; i < 16; i++) {
		F[i] = 0;
	}
	poker_count(F, ran, 2500);
	x = 0;
	for (i = 0; i < 16; i++) {
		x += (double)F[i] * (double)F[i];
	}
	x = (16.0 / 5000.0) * x - 5000.0;

	puts("< Poker Test ( 2.16 < X < 46.17 ) >");
	printf(" X = %8.3f\n", x);
	
	if (x < 2.16 || x > 46.17) {
		puts(" ... Test NG");
	} else {
		puts(" ... Test OK");
	}
	puts("");

	/* The Run Test */
	for (i = 0; i < 6; i++) {
		run0[i] = run1[i]= 0;
	}
	run_count(ran, 2500);
	puts("< Runs Test >");
	for (i = 0; i < 6; i++) {
		printf(" Length of Run: %d ", i + 1);
		printf(" (%d < Length of Run < %d)\n", rmin[i], rmax[i]);
		printf(" Zero runs = %d\n", run0[i]);
		printf(" One  runs = %d\n", run1[i]);
		if (run0[i] < rmin[i]  || run0[i] > rmax[i] ||
			run1[i] < rmin[i]  || run1[i] > rmax[i]) {
			puts(" ... Test NG ");
		} else {
			puts(" ... Test OK");
		}
	}
	for (i = 0; i < 6; i++) {
		total0 += (i + 1) * run0[i];
		total1 += (i + 1) * run1[i];
	}
	printf(" Total zero's bits = %d\n", total0);
	printf(" Total one's  bits = %d\n", total1);
	puts("");

	/* The Long Run Test */
	puts("< Long Run Test ( < 26 ) >");
	printf(" Long run (zero) = %d\n", maxrun0);
	printf(" Long run (one)  = %d\n", maxrun1);
	if (maxrun >= 26) {
		puts(" ... Test NG ");
	} else {
		puts(" ... Test OK");
	}
	
	return 0;
}
