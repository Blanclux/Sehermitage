/**
 * randgen_aes.c
 *  Random data generation program
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <string.h>
#include <time.h>
#include <openssl/evp.h>

void randgen(unsigned char *rnd, int rndLen, char *seed, int seedLen);

#define BLEN	16

unsigned char seed[16] = {
	0x01, 0x23, 0x45, 0x67, 0x89, 0xab, 0xcd, 0xef,
	0xfe, 0xdc, 0xba, 0x98, 0x76, 0x54, 0x32, 0x10
};
unsigned char key[16] = {
	0x01, 0x23, 0x45, 0x67, 0x89, 0xab, 0xcd, 0xef,
	0xfe, 0xdc, 0xba, 0x98, 0x76, 0x54, 0x32, 0x10
};

void
AES_128(unsigned char *key, unsigned char *data, unsigned char *ciphertext)
{
	int     res;
	EVP_CIPHER_CTX ctx;
	int     outl = 0;
	int     block_size;

	res = EVP_EncryptInit(&ctx, EVP_aes_128_ecb(), key, NULL);
	if (!res) {
		fprintf(stderr, "EVP_EncryptInit() error\n");
		return;
	}
	EVP_CIPHER_CTX_set_padding(&ctx, 0); // disable padding

	block_size = EVP_CIPHER_CTX_block_size(&ctx);
	outl = block_size;
	res = EVP_EncryptUpdate(&ctx, ciphertext, &outl, data, block_size);
	if (!res) {
		fprintf(stderr, "EVP_EncryptUpdate() error\n");
		return;
	}

	res = EVP_EncryptFinal(&ctx, ciphertext, &outl);
	if (!res || outl != 0) {
		fprintf(stderr, "EVP_EncryptFinal() error\n");
		return;
	}
	EVP_CIPHER_CTX_cleanup(&ctx);
}

void
getrand(unsigned char *rnd, unsigned char *t)
{
	unsigned char tmp1[16], tmp2[16];
	int     i;

	/* X9.17 Key Generation */
	AES_128(key, t, tmp1);
	for (i = 0; i < 16; i++) {
		tmp2[i] = tmp1[i] ^ seed[i];
	}
	AES_128(key, tmp2, rnd);

	for (i = 0; i < 16; i++) {
		tmp2[i] = tmp1[i] ^ rnd[i];
	}
	AES_128(key, tmp2, seed);
}

void
randgen(unsigned char *rnd, int rndLen, char *seed, int seedLen)
{
	unsigned char rtmp[BLEN];
	unsigned char stmp[BLEN];
	int i;
	int count, rest;
	int sLen;
	unsigned char *p;

	count = rndLen / BLEN;
	rest  = rndLen % BLEN;
	p = rnd;

	if (seedLen >= BLEN) {
		sLen = BLEN;
	} else {
		sLen = seedLen;
	}
	memcpy(stmp, seed, sLen);
	for (i = sLen; i < BLEN; i++) {
		stmp[i] = i;
	}

	for (i = 0; i < count; i++) {
		getrand(rtmp, stmp);
		memcpy(p, rtmp, BLEN);
		p = p + BLEN;
	}
	if (rest > 0) {
		getrand(rtmp, stmp);
		memcpy(p, rtmp, rest);
	}
}

