/**
 * ec_elgamal.c
 *  EC ElGamal Program for OpenSSL
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */

#include <string.h>
#include <assert.h>

#include <openssl/ec.h>
#include <openssl/err.h>
#include <openssl/rand.h>
#include <openssl/obj_mac.h>
#include <openssl/evp.h>

#define DATA_LEN	20
#define EC_NAME	"prime192v1"
#define EC_NID	NID_X9_62_prime192v1

typedef unsigned char byte;

static void
printHex(const char *title, const unsigned char *s, int len) 
{
	int    n;
	printf("%s:", title);
	for (n = 0; n < len; ++n) {
		if ((n % 16) == 0) {
			printf("\n%04x", n);
		}
		printf(" %02x", s[n]);
	}
	printf("\n");
}

/**
 * Encryption
 */
int
elgamal_encrypt(byte **encData, byte *data, int dataLen, const EC_KEY *eckey) 
{
	BN_CTX *ctx = NULL;
	BIGNUM *r = NULL, *p = NULL, *m;
	EC_POINT *C1 = NULL, *C2 = NULL;
	EC_POINT *Tmp = NULL, *M;
	const EC_POINT *Pkey;
	const EC_GROUP *group;
	int    c1Len, c2Len;
	int    rv;

	if ((group = EC_KEY_get0_group(eckey)) == NULL) {
		return 0;
	}
	p = BN_new();
	ctx = BN_CTX_new();
	EC_GROUP_get_curve_GFp(group, p, NULL, NULL, ctx);
#ifdef DEBUG
	printf(" p = ");
	BN_print_fp(stdout, p);
	puts("");
#endif

	/* C1 = r*G */
	C1 = EC_POINT_new(group);

	/* generate random number r */ 
	r = BN_new();
	M = EC_POINT_new(group);
	m = BN_new();
	do {
		if (!BN_rand_range(r, p)) {
			return 0;
		}
	} while (BN_is_zero(r));
#ifdef DEBUG
	printf(" r = ");
	BN_print_fp(stdout, r);
	puts("");
#endif

	EC_POINT_mul(group, C1, r, NULL, NULL, ctx);

	/* C2 = r*P + M */ 
	/* M */
	BN_bin2bn(data, dataLen, m);
	rv = EC_POINT_set_compressed_coordinates_GFp(group, M, m, 1, ctx);
	if (!rv) {
		return 0;
	}

	C2 = EC_POINT_new(group);
	Tmp = EC_POINT_new(group);
	Pkey = EC_KEY_get0_public_key(eckey);
	EC_POINT_mul(group, Tmp, NULL, Pkey, r, ctx);
	EC_POINT_add(group, C2, Tmp, M, ctx);

	/* cipher text C = (C1, C2) */ 
	c1Len = EC_POINT_point2oct(group, C1, POINT_CONVERSION_COMPRESSED,
							   NULL, 0, ctx);
#ifdef DEBUG
	printf(" Point converted length (C1) = %d\n", c1Len);
#endif
	c2Len =	EC_POINT_point2oct(group, C2, POINT_CONVERSION_COMPRESSED,
							   NULL, 0, ctx);
#ifdef DEBUG
	printf(" Point converted length (C2) = %d\n", c1Len);
#endif
	*encData = OPENSSL_malloc(c1Len + c2Len);
	EC_POINT_point2oct(group, C1, POINT_CONVERSION_COMPRESSED,
							*encData, c1Len, ctx);
	EC_POINT_point2oct(group, C2, POINT_CONVERSION_COMPRESSED,
							*encData + c1Len, c2Len, ctx);

	BN_clear_free(p);
	BN_clear_free(r);
	BN_clear_free(m);
	EC_POINT_free(C1);
	EC_POINT_free(C2);
	EC_POINT_free(M);
	EC_POINT_free(Tmp);
	BN_CTX_free(ctx);

	return (c1Len + c2Len);
}

/**
 * Decryption
 */
int
elgamal_decrypt(byte **decData, byte *encData, int encLen, const EC_KEY *eckey) 
{
	int rv;
	const EC_GROUP *group;
	const BIGNUM *prvKey;
	BN_CTX *ctx;
	EC_POINT *C1 = NULL, *C2 = NULL;
	EC_POINT *M = NULL, *Tmp = NULL;

	group = EC_KEY_get0_group(eckey);
	prvKey = EC_KEY_get0_private_key(eckey);
#ifdef DEBUG
	printf(" prvKey = ");
	BN_print_fp(stdout, prvKey);
	puts("");
#endif
	C1 = EC_POINT_new(group);
	C2 = EC_POINT_new(group);
	ctx = BN_CTX_new();

	/* C1 */
#ifdef DEBUG
	printHex("C1", encData, encLen / 2);
#endif
	rv = EC_POINT_oct2point(group, C1, encData, encLen / 2, ctx);
	if (!rv) {
		fprintf(stderr, "EC_POINT_oct2point error (C1)\n");
		return 0;
	}

	/* C2 */
#ifdef DEBUG
	printHex("C2", encData + encLen / 2, encLen / 2);
#endif
	rv = EC_POINT_oct2point(group, C2, encData + encLen / 2, encLen / 2,
							ctx);
	if (!rv) {
		fprintf(stderr, "EC_POINT_oct2point error (C2)\n");
		return 0;
	}
	Tmp = EC_POINT_new(group);
	M = EC_POINT_new(group);

	/* M = C2 - x C1 */ 
	EC_POINT_mul(group, Tmp, NULL, C1, prvKey, ctx);
	EC_POINT_invert(group, Tmp, ctx);
	EC_POINT_add(group, M, C2, Tmp, ctx);

	/* Output M */ 
	rv = EC_POINT_point2oct(group, M, POINT_CONVERSION_COMPRESSED, NULL, 0,
							ctx);

#ifdef DEBUG
	printf(" Point converted length = %d\n", rv);
#endif
	*decData = OPENSSL_malloc(rv);
	EC_POINT_point2oct(group, M, POINT_CONVERSION_COMPRESSED, *decData,
					   rv, ctx);

	EC_POINT_free(C1);
	EC_POINT_free(C2);
	EC_POINT_free(M);
	EC_POINT_free(Tmp);
	BN_CTX_free(ctx);

	return rv;
}

/**
 * ElGamal Enc/Dec Test
 */
int
do_ecelgamal(int nid)
{
	EC_KEY *eckey = NULL;
	EC_GROUP *group;
	EC_POINT *M = NULL;
	BN_CTX *ctx = NULL;
	BIGNUM *m = NULL;
	int  rv = 1;
	int  degree;
	unsigned int  i;
	unsigned int encLen, decLen, dataLen = DATA_LEN;
	unsigned int Lf, padLen;
	byte data[256], *dp;
	byte *encData, *decData;

	printf("\n< Generate key >\n");
	if ((eckey = EC_KEY_new()) == NULL) {
		goto err;
	}
	group = EC_GROUP_new_by_curve_name(nid);
	if (group == NULL) {
		goto err;
	}
	if (EC_KEY_set_group(eckey, group) == 0) {
		goto err;
	}

	degree = EC_GROUP_get_degree(EC_KEY_get0_group(eckey));
	if (degree < 160) {
		fprintf(stderr, "Skip the curve %s (degree = %d)\n",
				 OBJ_nid2sn(nid), degree);
		goto err;
	}
	Lf = (degree + 7) / 8;
	printf(" Degree = %d\n", degree);
	printf(" Field length = %d\n", Lf);

	/* create key */ 
	if (!EC_KEY_generate_key(eckey)) {
		fprintf(stderr, "EC_KEY_generate_key failed.\n");
		goto err;
	}

	/* check key */ 
	if (!EC_KEY_check_key(eckey)) {
		fprintf(stderr, "EC_KEY_check_key failed.\n");
		goto err;
	}

	printf("\n< Generate Message (EC Point) >\n");
	/* message with some random data */
	if (dataLen - 1 > Lf) {
		fprintf(stderr, "ERROR: Data length error (> Field length)\n");
		goto err;
	}

	padLen = Lf - dataLen;
	ctx = BN_CTX_new();
	m = BN_new();
	M = EC_POINT_new(group);
	do {
		if (!RAND_pseudo_bytes(data, Lf)) {
			fprintf(stderr, "ERROR: unable to get random data\n");
			goto err;
		}
		/* M || 8000...00 */
		data[dataLen] = 0x80;
		for (i = 1; i <= padLen; i++) {
			data[dataLen + i] = 0x00;
		}
		BN_bin2bn(data, Lf, m);
#ifdef DEBUG
		printf(" m = ");
		BN_print_fp(stdout, m);
		puts("");
#endif
		rv = EC_POINT_set_compressed_coordinates_GFp(group, M, m, 1, ctx);
	} while (rv == 0);

	printHex("Data", data, dataLen);
#ifdef DEBUG
	printHex("Data(Padding)", data, Lf);
#endif
	EC_GROUP_free(group);

	/* encrypt */
	printf("\n< Encrypt >\n");
	encLen = elgamal_encrypt(&encData, data, Lf, eckey);
	if (!encLen) {
		printf("Encrypt error\n");
		return 1;
	}
	printHex("ENCRYPT", encData, encLen);
#ifdef DEBUG
	printf(" Encrypt length = %d\n", encLen);
#endif
	/* decrypt */
	printf("\n< Decrypt >\n");
	decLen = elgamal_decrypt(&decData, encData, encLen, eckey);
#ifdef DEBUG
	printf(" Decrypt length = %d\n", decLen);
#endif
	if (!decLen) {
		printf("Decrypt error\n");
		return 2;
	}
	dp = decData + 1;
	decLen--;
	printHex("DECRYPT", dp, decLen);

	/* Unpadding */
	padLen = 0;
	for (i = decLen - 1; i > 0; i--) {
		if (dp[i] == 0x00) {
			padLen++;
		}
		else if (dp[i] == 0x80) {
			padLen++;
			break;
		}
	}
	decLen -= padLen;
#ifdef DEBUG
	printHex("DECRYPT(No padding)", dp, decLen);
#endif
	if (dataLen != decLen) {
		printf("dataLen = %d, decLen = %d\n", dataLen, decLen);
		return 3;
	}
	for (i = 0; i < decLen; i++) {
		if (data[i] != dp[i]) {
			return 4;
		}
	}
	rv = 0;

 err:
	BN_clear_free(m);
	EC_POINT_free(M);
	EC_KEY_free(eckey);
	OPENSSL_free(encData);
	OPENSSL_free(decData);
	BN_CTX_free(ctx);

	return rv;
}

int
main(int argc, char *argv[])
{
	int	  rv;
	char  *name = NULL;

	if (argc == 2 && strcmp(argv[1], "-h") == 0) {
		printf("usage: ec_elgamal [EC curve name]\n");
		return 1;
	}
	name = EC_NAME;
	if (argc >= 2) {
		name = argv[1];
	}

	printf("< EC ElGamal Test >\n");
	printf(" EC Curve name : %s\n", name);
	rv = do_ecelgamal(OBJ_sn2nid(name));
	if (rv == 0) {
		printf("\nOK\n");
	} else {
		printf("\nNG (ret = %d)\n", rv);
	}
	return 0;
}
