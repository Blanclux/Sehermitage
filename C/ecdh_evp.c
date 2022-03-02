/**
 * ecdh_evp.c
 *  ECDH Program for OpenSSL
 *  wrtten by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <openssl/evp.h>
#include <openssl/pem.h>
#include <openssl/ec.h>
#include <openssl/bio.h>
#include <openssl/bn.h>
#include <openssl/objects.h>
#include <openssl/rand.h>
#include <openssl/err.h>

static const char rnd_seed[] = "The quick brown fox jumps over the lazy dog";

EVP_PKEY*
get_peerkey(EVP_PKEY* keys) // it contains public + private
{
	int len = 0;
	unsigned char *buf = NULL, *p;
	const unsigned char *p2;
	EVP_PKEY* pkey;

	len = i2d_PUBKEY(keys, NULL); // find out required buffer length
	buf = (unsigned char*) OPENSSL_malloc(len); //allocate
	p = buf;
	len = i2d_PUBKEY(keys, &p);

	p2 = buf;
	pkey = d2i_PUBKEY(NULL, &p2, len);
	if (pkey == NULL) {
		fprintf(stderr, "d2i_PUBKEY failed\n");
	}
	OPENSSL_free(buf);

	return pkey;
}

static int
do_ecdh_evp(int nid, const char *text)
{
	EVP_PKEY_CTX *pctx1 = NULL, *kctx1 = NULL;
	EVP_PKEY_CTX *pctx2 = NULL, *kctx2 = NULL;
	EVP_PKEY_CTX *ctx1 = NULL, *ctx2 = NULL;
	EVP_PKEY *params1 = NULL, *params2 = NULL;
	EVP_PKEY *pkey1 = NULL, *pkey2 = NULL;
	EVP_PKEY *peerkey1 = NULL, *peerkey2 = NULL;
	unsigned char *skey1 = NULL, *skey2 = NULL;
	size_t skeyLen1, skeyLen2;
	int  rc = 1;
	size_t i;

	printf("< Diffie-Hellman key agreement (EC type) >\n");
	printf("  Elliptic Curev Paramater : %s\n\n", text);

	printf("< Part A (KeyGen) >\n");
	/* Create the context for parameter generation */
	pctx1 = EVP_PKEY_CTX_new_id(EVP_PKEY_EC, NULL);
	if (!pctx1)
	 	goto err;
	if (EVP_PKEY_paramgen_init(pctx1) <= 0)
		goto err;
	if (EVP_PKEY_CTX_set_ec_paramgen_curve_nid(pctx1, nid) <= 0)
		goto err;
	/* Create the parameter object params */
	if (EVP_PKEY_paramgen(pctx1, &params1) <= 0)
		goto err;

	/* Generate key */
	/* Create the context for the key generation */
	kctx1 = EVP_PKEY_CTX_new(params1, NULL);
	if (EVP_PKEY_keygen_init(kctx1) <= 0)
		goto err;
	if (EVP_PKEY_keygen(kctx1, &pkey1) <= 0) {
		fprintf(stderr, "EVP_PKEY_keygen error.\n");
		goto err;
	}
	PEM_write_PrivateKey(stdout, pkey1, NULL, NULL, 0, NULL, NULL);
	PEM_write_PUBKEY(stdout, pkey1);
	puts("\n");

	printf("< Part B (KeyGen) >\n");
	/* Create the context for parameter generation */
	pctx2 = EVP_PKEY_CTX_new_id(EVP_PKEY_EC, NULL);
	if (!pctx2)
	 	goto err;
	if (EVP_PKEY_paramgen_init(pctx2) <= 0)
		goto err;
	if (EVP_PKEY_CTX_set_ec_paramgen_curve_nid(pctx2, nid) <= 0)
		goto err;
	/* Create the parameter object params */
	if (EVP_PKEY_paramgen(pctx2, &params2) <= 0)
		goto err;

	/* Generate key */
	/* Create the context for the key generation */
	kctx2 = EVP_PKEY_CTX_new(params2, NULL);
	if (EVP_PKEY_keygen_init(kctx2) <= 0)
		goto err;
	if (EVP_PKEY_keygen(kctx2, &pkey2) <= 0) {
		fprintf(stderr, "EVP_PKEY_keygen error.\n");
		goto err;
	}
	PEM_write_PrivateKey(stdout, pkey2, NULL, NULL, 0, NULL, NULL);
	PEM_write_PUBKEY(stdout, pkey2);
	puts("\n");

	/* Get public key */
	peerkey1 = get_peerkey(pkey1);
	peerkey2 = get_peerkey(pkey2);

	printf("< Part A (KeyDerive) >\n");
	/* Create the context for the shared secret derivation */
	ctx1 = EVP_PKEY_CTX_new(pkey1, NULL);

	if (EVP_PKEY_derive_init(ctx1) <= 0) {
		fprintf(stderr, "EVP_PKEY_derive_init error\n");
		goto err;
	}

	if (EVP_PKEY_derive_set_peer(ctx1, peerkey2) <= 0) {
		fprintf(stderr, "EVP_PKEY_derive_set_peer erro\n");
		goto err;
	}

	/* Determine buffer length */
	if (EVP_PKEY_derive(ctx1, NULL, &skeyLen1) <= 0)
		goto err;
	skey1 = OPENSSL_malloc(skeyLen1);
	if (!skey1)
		goto err;
	if (EVP_PKEY_derive(ctx1, skey1, &skeyLen1) <= 0)
		goto err;

	printf("  Key 1 : ");
	for (i = 0; i < skeyLen1; i++) {
		printf("%02X", skey1[i]);
	}
	puts("\n");

	printf("< Part B (KeyDerive) >\n");
	/* Create the context for the shared secret derivation */
	ctx2 = EVP_PKEY_CTX_new(pkey2, NULL);

	if (EVP_PKEY_derive_init(ctx2) <= 0)
		goto err;
	if (EVP_PKEY_derive_set_peer(ctx2, peerkey1) <= 0)
		goto err;

	/* Determine buffer length */
	if (EVP_PKEY_derive(ctx2, NULL, &skeyLen2) <= 0)
		goto err;
	skey2 = OPENSSL_malloc(skeyLen2);
	if (!skey2)
		goto err;

	if (EVP_PKEY_derive(ctx2, skey2, &skeyLen2) <= 0)
		goto err;


	printf("  Key 2 : ");
	for (i = 0; i < skeyLen2; i++) {
		printf("%02X", skey2[i]);
	}
	puts("\n");

	if ((skeyLen1 != skeyLen2) || (memcmp(skey1, skey2, skeyLen1) != 0)) {
		fprintf(stderr, "Error in ECDH\n");
		rc = 1;
	} else {
		rc = 0;
	}

  err:
  	EVP_PKEY_CTX_free(pctx1);
  	EVP_PKEY_CTX_free(pctx2);
  	EVP_PKEY_CTX_free(kctx1);
  	EVP_PKEY_CTX_free(kctx2);
  	EVP_PKEY_CTX_free(ctx1);
  	EVP_PKEY_CTX_free(ctx2);
	EVP_PKEY_free(pkey1);
	EVP_PKEY_free(pkey2);
	EVP_PKEY_free(params1);
	EVP_PKEY_free(params2);
	EVP_PKEY_free(peerkey1);
	EVP_PKEY_free(peerkey2);

	if (skey1 != NULL)
		OPENSSL_free(skey1);
	if (skey2 != NULL)
		OPENSSL_free(skey2);
	return rc;
}

int
main(int argc, char *argv[])
{
	int rc = 1;
	int nid = 710;
	const char *name;

	if (argc > 2) {
		printf("usage: ecdh_evp [nid]\n");
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

	rc = do_ecdh_evp(nid, name);
	if (rc == 0) {
		printf("OK\n");
	} else {
		printf("NG\n");
	}

	CRYPTO_cleanup_all_ex_data();

	return rc;
}
