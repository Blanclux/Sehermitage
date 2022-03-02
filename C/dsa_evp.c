/**
 * dsa_evp.c
 *  Public Key Cipher (DSA) Test Program for OpenSSL
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <stdio.h>
#include <stdlib.h>

#include <openssl/evp.h>
#include <openssl/dsa.h>

static void
printHex(const char *title, const unsigned char *s, int len)
{
	int     n;

	printf("%s:", title);
	for (n = 0; n < len; ++n) {
		if ((n % 16) == 0) {
			printf("\n%04x", n);
		}
		printf(" %02x", s[n]);
	}
	printf("\n");
}

int
main(int argc, char **argv)
{
	EVP_PKEY_CTX *ctx;
	EVP_PKEY *pkey = NULL;
	EVP_PKEY *param = NULL;

	unsigned int i;
	int rc = 1;
	unsigned char *in, *sig;
	size_t inlen, siglen;
	int dataLen = 256;
	int paramLen = 1024;


	if (argc > 3) {
		fprintf(stderr, "usage: %s [[dataLen [keyLen]]\n", argv[0]);
		exit(1);
	}
	if (argc >= 2) {
		dataLen = atoi(argv[1]);
	}
	if (argc == 3) {
		paramLen = atoi(argv[2]);
	}

	/* Generate a DSA key */
	printf("*** Key Generation Test ***\n");
	ctx = EVP_PKEY_CTX_new_id(EVP_PKEY_DSA, NULL);
	if (!ctx) {
		rc = 0;		/* Error occurred */
		goto err;
	}
	/* Generate parameter */
	printf("< DSA Parameter Generation >\n");
	if ((rc = EVP_PKEY_paramgen_init(ctx)) <= 0) {
		goto err;
	}
	if ((rc = EVP_PKEY_CTX_set_dsa_paramgen_bits(ctx, paramLen)) <= 0) {
		goto err;
	}
	if ((rc = EVP_PKEY_paramgen(ctx, &param)) <= 0) {
		fprintf(stderr, "EVP_PKEY_paramgen error.\n");
		goto err;
	}
	EVP_PKEY_CTX_free(ctx);
	/* Generate key */
	printf("< DSA Key Generation >\n");
	ctx = EVP_PKEY_CTX_new(param, NULL);
	if ((rc = EVP_PKEY_keygen_init(ctx)) <= 0) {
		goto err;
	}
	if ((rc = EVP_PKEY_keygen(ctx, &pkey)) <= 0) {
		fprintf(stderr, "EVP_PKEY_keygen error.\n");
		goto err;
	}
	EVP_PKEY_CTX_free(ctx);

	/* Signature generation using DSA and SHA-256 */
	printf("< Sign/Verify Test >\n");
	inlen = dataLen;
	in = OPENSSL_malloc(inlen);
	srand((unsigned int)time(NULL));
	for (i = 0; i < inlen; i++) {
		in[i] = (unsigned char) rand();
	}
	printHex("text data", in, inlen);

	ctx = EVP_PKEY_CTX_new(pkey, NULL);
	if (!ctx) {
		rc = 0;		/* Error occurred */
		goto err;
	}
	if ((rc = EVP_PKEY_sign_init(ctx)) <= 0) {
		goto err;
	}
	if ((rc = EVP_PKEY_CTX_set_signature_md(ctx, EVP_sha256())) <= 0) {
		goto err;
	}
	/* Determine buffer length */
	if ((rc = EVP_PKEY_sign(ctx, NULL, &siglen, in, inlen)) <= 0) {
		goto err;
	}
	sig = OPENSSL_malloc(siglen);

	if ((rc = EVP_PKEY_sign(ctx, sig, &siglen, in, inlen)) <= 0) {
		fprintf(stderr, "EVP_PKEY_sign error.\n");
		goto err;		/* Error occurred */
	}
	/* Signature is siglen bytes written to buffer sig */
	printHex("signature data", sig, siglen);

	/* Signature verification */
	if ((rc = EVP_PKEY_verify_init(ctx)) <= 0) {
		fprintf(stderr, "EVP_PKEY_verify_init error.\n");
		goto err;		/* Error occurred */
	}
	if ((rc = EVP_PKEY_CTX_set_signature_md(ctx, EVP_sha256())) <= 0) {
		goto err;		/* Error occurred */
	}
	/* Perform operation */
	rc = EVP_PKEY_verify(ctx, sig, siglen, in, inlen);
	if (rc != 1) {
		fprintf(stderr, "EVP_PKEY_verify error (RC = %d).\n", rc);
	} else  {
		printf("Verify OK\n");
	}
	
	OPENSSL_free(in);
	OPENSSL_free(sig);
	EVP_PKEY_CTX_free(ctx);
	EVP_PKEY_free(param);
	EVP_PKEY_free(pkey);

 err:
	if (rc != 1) {
		fprintf(stderr, "Error return (RC = %d).\n", rc);
		return 1;
	}

	return 0;
}

