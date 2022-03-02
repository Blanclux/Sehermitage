/**
 * ecdh.c
 *  DH Program for OpenSSL
 *  wrtten by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <openssl/ecdh.h>
#include <openssl/bio.h>
#include <openssl/bn.h>
#include <openssl/objects.h>
#include <openssl/rand.h>
#include <openssl/sha.h>
#include <openssl/err.h>

static const char rnd_seed[] = "The quick brown fox jumps over the lazy dog";

static const int KDF1_SHA1_len = 20;

static void *
KDF1_SHA1(const void *in, size_t inlen, void *out, size_t * outlen)
{
	if (*outlen < SHA_DIGEST_LENGTH) {
		return NULL;
	} else {
		*outlen = SHA_DIGEST_LENGTH;
	}
	return SHA1(in, inlen, out);
}

static int
do_ecdh(int nid, const char *text, BN_CTX * ctx, BIO * out)
{
	const EC_GROUP *group;
	EC_KEY *a = NULL;
	EC_KEY *b = NULL;
	BIGNUM *x_a = NULL, *y_a = NULL, *x_b = NULL, *y_b = NULL;
	char    buf[12];
	unsigned char *abuf = NULL, *bbuf = NULL;
	int     i, alen, blen, aout, bout, ret = 0;

	printf("< Diffie-Hellman key agreement (EC type) >\n");
	a = EC_KEY_new_by_curve_name(nid);
	b = EC_KEY_new_by_curve_name(nid);
	if (a == NULL || b == NULL) {
		goto err;
	}

	group = EC_KEY_get0_group(a);

	if ((x_a = BN_new()) == NULL) {
		goto err;
	}
	if ((y_a = BN_new()) == NULL) {
		goto err;
	}
	if ((x_b = BN_new()) == NULL) {
		goto err;
	}
	if ((y_b = BN_new()) == NULL) {
		goto err;
	}

	BIO_puts(out, " Elliptic Curve Parameter: ");
	BIO_puts(out, text);
	BIO_puts(out, "\n\n");

	printf("< Part A >\n");
	if (!EC_KEY_generate_key(a)) {
		goto err;
	}

	if (EC_METHOD_get_field_type(EC_GROUP_method_of(group)) ==
		NID_X9_62_prime_field) {
		if (!EC_POINT_get_affine_coordinates_GFp
			(group, EC_KEY_get0_public_key(a), x_a, y_a, ctx))
			goto err;
	}
	else {
		if (!EC_POINT_get_affine_coordinates_GF2m(group,
												  EC_KEY_get0_public_key
												  (a), x_a, y_a, ctx))
			goto err;
	}

	BIO_puts(out, "  Private Key 1 = ");
	BN_print(out, EC_KEY_get0_private_key(a));
	BIO_puts(out, "\n  Public  Key 1 = ");
	BN_print(out, x_a);
	BIO_puts(out, ",");
	BN_print(out, y_a);
	BIO_puts(out, "\n\n");

	printf("< Part B >\n");
	if (!EC_KEY_generate_key(b)) {
		goto err;
	}

	if (EC_METHOD_get_field_type(EC_GROUP_method_of(group)) ==
		NID_X9_62_prime_field) {
		if (!EC_POINT_get_affine_coordinates_GFp
			(group, EC_KEY_get0_public_key(b), x_b, y_b, ctx))
			goto err;
	}
	else {
		if (!EC_POINT_get_affine_coordinates_GF2m(group,
												  EC_KEY_get0_public_key
												  (b), x_b, y_b, ctx))
			goto err;
	}

	BIO_puts(out, "  Private Key 2 = ");
	BN_print(out, EC_KEY_get0_private_key(b));
	BIO_puts(out, "\n  Public  Key 2 = ");
	BN_print(out, x_b);
	BIO_puts(out, ",");
	BN_print(out, y_b);
	BIO_puts(out, "\n\n");

	printf("< Key agreement >\n");
	alen = KDF1_SHA1_len;
	abuf = (unsigned char *) OPENSSL_malloc(alen);
	aout = ECDH_compute_key(abuf, alen, EC_KEY_get0_public_key(b), a, KDF1_SHA1);
	printf(" Key length = %d (byte)\n", alen);

	BIO_puts(out, "  Key 1 = ");
	for (i = 0; i < aout; i++) {
		sprintf(buf, "%02X", abuf[i]);
		BIO_puts(out, buf);
	}
	BIO_puts(out, "\n");

	blen = KDF1_SHA1_len;
	bbuf = (unsigned char *) OPENSSL_malloc(blen);
	bout = ECDH_compute_key(bbuf, blen, EC_KEY_get0_public_key(a), b, KDF1_SHA1);

	BIO_puts(out, "  Key 2 = ");
	for (i = 0; i < bout; i++) {
		sprintf(buf, "%02X", bbuf[i]);
		BIO_puts(out, buf);
	}
	BIO_puts(out, "\n");
	if ((aout < 4) || (bout != aout) || (memcmp(abuf, bbuf, aout) != 0)) {
		fprintf(stderr, "Error in ECDH routines\n");
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
	if (x_a)
		BN_free(x_a);
	if (y_a)
		BN_free(y_a);
	if (x_b)
		BN_free(x_b);
	if (y_b)
		BN_free(y_b);
	if (b)
		EC_KEY_free(b);
	if (a)
		EC_KEY_free(a);
	return (ret);
}

int
main(int argc, char *argv[])
{
	BN_CTX *ctx = NULL;
	int     ret = 1;
	BIO    *out;
	int nid = 710;
	const char *name;

	if (argc > 2) {
		printf("usage: ecdh [nid]\n");
		return 1;
	}
	if (argc >= 2) {
		nid = atoi(argv[1]);
		name = OBJ_nid2sn(nid);
		if (name == NULL) {
			fprintf(stderr, "No such EC curve.\n");
			return 1;
		}
	} else {
		name = OBJ_nid2sn(nid);
	}
	
	RAND_seed(rnd_seed, sizeof rnd_seed);

	out = BIO_new(BIO_s_file());
	if (out == NULL) {
		return 1;
	}
	BIO_set_fp(out, stdout, BIO_NOCLOSE);

	if ((ctx = BN_CTX_new()) == NULL) {
		goto err;
	}

	ret = do_ecdh(nid, name, ctx, out);
	(ret == 0) ? puts("OK\n") : puts("NG\n");

  err:
	ERR_print_errors_fp(stderr);
	if (ctx) {
		BN_CTX_free(ctx);
	}
	BIO_free(out);
	CRYPTO_cleanup_all_ex_data();

	return ret;
}
