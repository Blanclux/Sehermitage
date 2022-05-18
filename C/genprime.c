/**
 * genprime.c
 *  Prime Generation Program using GNU MP
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <gmp.h>

#define _RABIN_P	25

extern void mpz_random_max(mpz_ptr, mpz_ptr);
void mpz_random_bit(mpz_ptr, mp_size_t);


/*
 * Simple random prime generator
 */
void
mpz_random_prime(mpz_ptr p, int bits)
{
	int dir;
	mpz_t ran;
	gmp_randstate_t state;
	time_t timer;

	if (bits <= 1)
		return;

	gmp_randinit_default(state);
	time(&timer);
	gmp_randseed_ui(state, (unsigned long)timer);

	mpz_init(ran);
	mpz_urandomb(ran, state, 1);
	gmp_randclear(state);

	dir = (int)mpz_scan0(ran, 0);

	mpz_random_bit(p, bits);
	mpz_setbit(p, bits - 1);	/* Set MSB */
	mpz_setbit(p, 0);			/* Set odd number */

	while (!mpz_probab_prime_p(p, _RABIN_P)) {
		if (dir) {
			mpz_add_ui(p, p, 2);
			if (mpz_sizeinbase(p, 2) != (size_t)bits) {
				mpz_set_ui(p, 0);
				mpz_setbit(p, bits - 1);
				mpz_setbit(p, 0);		/* 100.....1 */
			}
		}
		else {
			mpz_sub_ui(p, p, 2);
			if (mpz_sizeinbase(p, 2) != (size_t)bits) {
				mpz_set_ui(p, 0);
				mpz_setbit(p, bits);
				mpz_sub_ui(p, p, 1);	/* 111.....1 */
			}
		}
	}
}

/*
 * Simple random prime generator
 */
void
mpz_random_prime2(mpz_ptr p, mpz_ptr pmax, mpz_ptr pmin)
{
	int dir, isprime;
	int mxf = 0, mnf = 0;
	mpz_t c, ran;
	gmp_randstate_t state;
	time_t timer;


	if (mpz_cmp(pmax, pmin) <= 0)
		return;
	if (mpz_cmp_ui(pmax, 2) <= 0)
    	return;
	if (mpz_cmp(pmax, pmin) == 0 && mpz_scan0(pmax, 0) == 0)
		return;

	isprime = 0;

	if (mpz_scan0(pmax, 0) == 0) {	/* if pmax is even, +1 */
		mxf = 1;
		mpz_add_ui(pmax, pmax, 1);
	}
	if (mpz_scan0(pmin, 0) == 0) {	/* if pmin is even, +1 */
		if (mpz_cmp_ui(pmin, 0) == 0) {
			mnf = 2;
			mpz_set_ui(pmin, 2);
		}
		else {
			mnf = 1;
			mpz_add_ui(pmin, pmin, 1);
		}
	}

	mpz_init(c);
	mpz_sub(c, pmax, pmin);
	do {
		mpz_random_max(p, c);
		mpz_add(p, p, pmin);
		mpz_setbit(p, 0);
	} while (mpz_cmp(pmax, p) <= 0);

	mpz_tdiv_q_2exp(c, c, 1);	/* c can't be smaller than 1 */

	mpz_init(ran);				/* choose direction randomly */

	gmp_randinit_default(state);
	time(&timer);
	gmp_randseed_ui(state, (unsigned long)timer);

	mpz_urandomb(ran, state, 1);
	gmp_randclear(state);

	dir = (int)mpz_scan0(ran, 0);

	do {		/* prime check */
		isprime = mpz_probab_prime_p(p, _RABIN_P);
		if (isprime)
			break;

		mpz_sub_ui(c, c, 1);
		if (dir) {
			mpz_add_ui(p, p, 2);
			if (mpz_cmp(p, pmax) >= 0) {
				mpz_set(p, pmin);
			}
		}
		else {
			mpz_sub_ui(p, p, 2);
			if ((mpz_cmp(p, pmin) < 0) || mpz_cmp_ui(p, 1) == 0) {
				mpz_set(p, pmax);
			}
		}
	} while (mpz_cmp_ui(c, 0) != 0);

	if (isprime) {
		if (mpz_cmp(p, pmin) < 0 || mpz_cmp(pmax, p) < 0) {
			fprintf(stderr, "Fatal Internal Error at mpz_random_prime2.\n");
			fprintf(stderr, "p    = ");
			mpz_out_str(stderr, 16, p);
			puts("");
			fprintf(stderr, "pmax = ");
			mpz_out_str(stderr, 16, pmax);
			puts("");
			fprintf(stderr, "pmin = ");
			mpz_out_str(stderr, 16, pmin);
			puts("");
			exit (-1);
		}
	}

	if (mxf)
		mpz_sub_ui(pmax, pmax, 1);
	if (mnf)
		mpz_sub_ui(pmin, pmin, mnf);

	mpz_clear(c);
}
