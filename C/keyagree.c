/**
 * keyagree.c
 *  DH Program for OpenSSL
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <openssl/dh.h>
#include <openssl/rand.h>
#include <openssl/err.h>

static const char rnd_seed[] = "The quick brown fox jumps over the lazy dog";

int
main(int argc, char *argv[])
{
	DH     *a;
	DH     *b = NULL;
	int     i, alen, blen, aout, bout, ck, ret = 1;
	int     keyLen = 64;
	char   *apri, *apub, *bpri, *bpub;
	unsigned char *abuf = NULL, *bbuf = NULL;

	if (argc > 2) {
		fprintf(stderr, "%s [bitLen]\n", argv[0]);
		return 1;
	}
	if (argc == 2) {
		keyLen = atoi(argv[1]);
	}

	printf("< Diffie-Hellman key agreement >\n");
	RAND_seed(rnd_seed, sizeof rnd_seed);

	if (((a = DH_new()) == NULL)
		|| !DH_generate_parameters_ex(a, keyLen, DH_GENERATOR_5, NULL)) {
		goto err;
	}

	if (!DH_check(a, &ck)) {
		goto err;
	}
	if (ck & DH_CHECK_P_NOT_PRIME) {
		fprintf(stderr, "p value is not prime\n");
	}
	if (ck & DH_CHECK_P_NOT_SAFE_PRIME) {
		fprintf(stderr, "p value is not a safe prime\n");
	}
	if (ck & DH_UNABLE_TO_CHECK_GENERATOR) {
		fprintf(stderr, "unable to check the generator value\n");
	}
	if (ck & DH_NOT_SUITABLE_GENERATOR) {
		fprintf(stderr, "the g value is not a generator\n");
	}

	printf("\n");
	DHparams_print_fp(stdout, a);
	printf("\n");

	b = DH_new();
	if (b == NULL) {
		goto err;
	}

	b->p = BN_dup(a->p);
	b->g = BN_dup(a->g);
	if ((b->p == NULL) || (b->g == NULL)) {
		goto err;
	}

	/* Set a to run with normal modexp and b to use constant time */
	a->flags &= ~DH_FLAG_NO_EXP_CONSTTIME;
	b->flags |= DH_FLAG_NO_EXP_CONSTTIME;

	/* A part */
	printf("< Part A >\n");
	if (!DH_generate_key(a)) {
		goto err;
	}
	apri = BN_bn2hex(a->priv_key);
	apub = BN_bn2hex(a->pub_key);
	printf("  Private key 1 = %s\n", apri);
	printf("  Public  key 1 = %s\n", apub);
	printf("\n");

	/* B part */
	printf("< Part B >\n");
	if (!DH_generate_key(b)) {
		goto err;
	}
	bpri = BN_bn2hex(b->priv_key);
	bpub = BN_bn2hex(b->pub_key);
	printf("  Private key 2 = %s\n", bpri);
	printf("  Public  key 2 = %s\n", bpub);
	printf("\n");

	/* A part */
	alen = DH_size(a);
	abuf = (unsigned char *) OPENSSL_malloc(alen);
	aout = DH_compute_key(abuf, b->pub_key, a);

	printf("< Key agreement >\n");
	printf(" Key length = %d (byte)\n", alen);
	printf("  Key 1 = ");
	for (i = 0; i < aout; i++) {
		printf("%02X", abuf[i]);
	}
	printf("\n");

	/* B part */
	blen = DH_size(b);
	bbuf = (unsigned char *) OPENSSL_malloc(blen);
	bout = DH_compute_key(bbuf, a->pub_key, b);

	printf("  Key 2 = ");
	for (i = 0; i < bout; i++) {
		printf("%02X", bbuf[i]);
	}
	printf("\n");
	if ((aout < 4) || (bout != aout) || (memcmp(abuf, bbuf, aout) != 0)) {
		fprintf(stderr, "Error in DH routines\n");
		ret = 1;
	} else {
		ret = 0;
	}
  err:
	ERR_print_errors_fp(stderr);

	if (abuf != NULL)
		OPENSSL_free(abuf);
	if (bbuf != NULL)
		OPENSSL_free(bbuf);
	if (b != NULL)
		DH_free(b);
	if (a != NULL)
		DH_free(a);

	if (apri != NULL)
		OPENSSL_free(apri);
	if (apub != NULL)
		OPENSSL_free(apub);
	if (bpri != NULL)
		OPENSSL_free(bpri);
	if (bpub != NULL)
		OPENSSL_free(bpub);

	return ret;
}
