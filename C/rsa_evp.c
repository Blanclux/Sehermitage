/**
 * rsa_evp.c
 *  Public Key Cipher (RSA) Test Program for OpenSSL
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <stdio.h>
#include <string.h>

#include <openssl/evp.h>
#include <openssl/rsa.h>

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

	int rc = 1;
	unsigned char *out, *out2, *in;
	size_t outlen, outlen2, inlen;
	unsigned char *sig;
	size_t siglen;
	char   *text = "The quick brown fox jumps over the lazy dog";

	if (argc > 2) {
		fprintf(stderr, "%s plainText\n", argv[0]);
		return 1;
	}
	if (argc == 1) {
		in = (unsigned char *) text;
		inlen = strlen(text);
	} else {
		in = (unsigned char *) argv[1];
		inlen = strlen(argv[1]);
	}

	/* Generate a 2048 bit RSA key */
	printf("< Key Generation Test >\n");
	ctx = EVP_PKEY_CTX_new_id(EVP_PKEY_RSA, NULL);
	if (!ctx) {
		rc = 0;		/* Error occurred */
		goto err;
	}
	if ((rc = EVP_PKEY_keygen_init(ctx)) <= 0) {
		goto err;
	}
	if ((rc = EVP_PKEY_CTX_set_rsa_keygen_bits(ctx, 2048)) <= 0) {
		goto err;
	}
	/* Generate key */
	if ((rc = EVP_PKEY_keygen(ctx, &pkey)) <= 0) {
		fprintf(stderr, "EVP_PKEY_keygen error.\n");
		goto err;
	}
	EVP_PKEY_CTX_free(ctx);

	/* Encrypt data using OAEP (for RSA keys) */
	printf("< Encrypt/Decrypt Test >\n");
	printHex("plain data", in, inlen);

	ctx = EVP_PKEY_CTX_new(pkey, NULL);
	if (!ctx) {
		rc = 0;		/* Error occurred */
		goto err;
	}

	if (EVP_PKEY_encrypt_init(ctx) <= 0) {
		rc = 0;		/* Error occurred */
		goto err;
	}
	if ((rc = EVP_PKEY_CTX_set_rsa_padding(ctx, RSA_PKCS1_OAEP_PADDING)) <= 0) {
		goto err;
	}
	/* Determine buffer length */
	if ((rc = EVP_PKEY_encrypt(ctx, NULL, &outlen, in, inlen)) <= 0) {
		goto err;
	}
	out = OPENSSL_malloc(outlen);
	if ((rc = EVP_PKEY_encrypt(ctx, out, &outlen, in, inlen)) <= 0) {
		fprintf(stderr, "EVP_PKEY_encrypt error.\n");
		goto err;
	}
	/* Encrypted data is outlen bytes written to buffer out */
	printHex("encrypted data", out, outlen);

	/* Decrypt data */
	if ((rc = EVP_PKEY_decrypt_init(ctx)) <= 0) {
		fprintf(stderr, "EVP_PKEY_decrypt error.\n");
		goto err;
	}
	if ((rc = EVP_PKEY_CTX_set_rsa_padding(ctx, RSA_PKCS1_OAEP_PADDING)) <= 0) {
		goto err;
	}
	/* Determine buffer length */
	if ((rc = EVP_PKEY_decrypt(ctx, NULL, &outlen2, out, outlen)) <= 0) {
		goto err;
	}
	out2 = OPENSSL_malloc(outlen2);

	if ((rc = EVP_PKEY_decrypt(ctx, out2, &outlen2, out, outlen)) <= 0) {
		fprintf(stderr, "EVP_PKEY_decrypt error.\n");
		goto err;
	}
	printHex("decrypted data", out2, outlen2);
	OPENSSL_free(out);
	OPENSSL_free(out2);

	/* Signature generation using RSA and SHA-256 */
	printf("< Sign/Verify Test >\n");

	EVP_PKEY_CTX_free(ctx);
	ctx = EVP_PKEY_CTX_new(pkey, NULL);
	if (!ctx) {
		rc = 0;		/* Error occurred */
		goto err;
	}
	if ((rc = EVP_PKEY_sign_init(ctx)) <= 0) {
		goto err;
	}
	if ((rc = EVP_PKEY_CTX_set_rsa_padding(ctx, RSA_PKCS1_PADDING)) <= 0) {
		goto err;
	}
	//if ((rc = EVP_PKEY_CTX_set_signature_md(ctx, EVP_sha256())) <= 0) {
	//	goto err;
	//}
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
	if ((rc = EVP_PKEY_CTX_set_rsa_padding(ctx, RSA_PKCS1_PADDING)) <= 0) {
		goto err;		/* Error occurred */
	}
	//if ((rc = EVP_PKEY_CTX_set_signature_md(ctx, EVP_sha256())) <= 0) {
	//	goto err;		/* Error occurred */
	//}
	/* Perform operation */
	rc = EVP_PKEY_verify(ctx, sig, siglen, in, inlen);
	if (rc != 1) {
		fprintf(stderr, "EVP_PKEY_verify error (RC = %d).\n", rc);
	} else  {
		printf("Verify OK\n");
	}
	
	OPENSSL_free(sig);
	EVP_PKEY_CTX_free(ctx);
	EVP_PKEY_free(pkey);

 err:
	if (rc != 1) {
		fprintf(stderr, "Error return (RC = %d).\n", rc);
		return 1;
	}

	return 0;
}

