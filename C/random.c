/**
 * random.c
 *  Random number generation using GNU MP
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */

#include <gmp.h>
#include <stdio.h>
#if defined(__GNUC__)
#include <sys/time.h>
#else
#include <time.h>
#endif

/*
 * Random number generation
 */
/** random number in 0 < r < limit */
#if defined(__GNUC__)
void
mpz_random_max (mpz_ptr r, const mpz_ptr limit)
{
	gmp_randstate_t state;
	struct timeval tv, tv2;

	gmp_randinit_default(state);

	gettimeofday(&tv2, NULL);
	do {
		gettimeofday(&tv, NULL);
	} while (tv.tv_usec == tv2.tv_usec);

	gmp_randseed_ui(state, tv.tv_usec);
	mpz_urandomm(r, state, limit);

	gmp_randclear(state);
}
#else
void
mpz_random_max (mpz_ptr r, const mpz_ptr limit)
{
	gmp_randstate_t state;
	time_t timer;

	gmp_randinit_default(state);

	time(&timer);

	gmp_randseed_ui(state, (unsigned long)timer);
	mpz_urandomm(r, state, limit);

	gmp_randclear(state);
}
#endif

/** random number of length bits */
void
mpz_random_bit (mpz_ptr r, const int bits)
{
	mpz_t   limit;

	mpz_init(limit);
	mpz_setbit(limit, bits);
	mpz_random_max(r, limit);

	mpz_clear(limit);
}

