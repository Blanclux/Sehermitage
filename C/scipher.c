/**
 * scipher.c
 *  Secret Cipher Test Program for OpenSSL
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <stdio.h>
#include <string.h>
#include <sys/stat.h>

#include <openssl/evp.h>
#ifndef OPENSSL_NO_ENGINE
#include <openssl/engine.h>
#endif

/* Select cipher algorithm and key. */
static int cno = 0;

struct {
	char   *cipher;
	unsigned int keyLen;
	unsigned int ivLen;
	unsigned char key[32];
	unsigned char iv[16];
} ckey[] = {
	{
		"AES-128-CBC", 16, 16, {
		0x2b, 0x7e, 0x15, 0x16, 0x28, 0xae, 0xd2, 0xa6,
		0xab, 0xf7, 0x15, 0x88, 0x09, 0xcf, 0x4f, 0x3c}, {
		0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77,
		0x88, 0x99, 0xAA, 0xBB, 0xCC, 0xDD, 0xEE, 0xFF}
	}, {
		"AES-192-CBC", 24, 16, {
		0x8e, 0x73, 0xb0, 0xf7, 0xda, 0x0e, 0x64, 0x52,
		0xc8, 0x10, 0xf3, 0x2b, 0x80, 0x90, 0x79, 0xe5,
		0x62, 0xf8, 0xea, 0xd2, 0x52, 0x2c, 0x6b, 0x7b}, {
		0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77,
		0x88, 0x99, 0xAA, 0xBB, 0xCC, 0xDD, 0xEE, 0xFF}
	}, {
		"AES-256-CBC", 32, 16, {
		0x60, 0x3d, 0xeb, 0x10, 0x15, 0xca, 0x71, 0xbe,
		0x2b, 0x73, 0xae, 0xf0, 0x85, 0x7d, 0x77, 0x81,
		0x1f, 0x35, 0x2c, 0x07, 0x3b, 0x61, 0x08, 0xd7,
		0x2d, 0x98, 0x10, 0xa3, 0x09, 0x14, 0xdf, 0xf4}, {
		0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77,
		0x88, 0x99, 0xAA, 0xBB, 0xCC, 0xDD, 0xEE, 0xFF}
	}
};

#ifdef DEBUG
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
#endif

/**
 * Write octet data to file
 */
int
write_file(char *fname, unsigned char *data, int len)
{
	FILE   *fp;

	if (data == NULL || fname == NULL) {
		return 1;
	}
	if ((fp = fopen(fname, "wb")) == NULL) {
		return 1;
	}
	if (fwrite(data, len, 1, fp) < 1) {
		fclose(fp);
		return 1;
	}
	fclose(fp);

	return 0;
}

/**
 * Read octet data from file
 */
unsigned char *
read_file(int *len, char *fname)
{
	FILE   *fp;
	struct stat sbuf;
	unsigned char *data;

	if (len == NULL || fname == NULL) {
		return NULL;
	}
	if (stat(fname, &sbuf) == -1) {
		return NULL;
	}
	*len = (int) sbuf.st_size;
	data = (unsigned char *) malloc(*len);
	if (!data) {
		return NULL;
	}
	if ((fp = fopen(fname, "rb")) == NULL) {
		return NULL;
	}
	if (fread(data, *len, 1, fp) < 1) {
		fclose(fp);
		return NULL;
	}
	fclose(fp);

	return data;
}

static int
do_cipher(const EVP_CIPHER * cipher, const unsigned char *key, int keyLen,
		  const unsigned char *iv, const unsigned char *in, int inLen,
		  unsigned char *out, int *outLen, int encdec)
{
	EVP_CIPHER_CTX ctx;
	int     out1, out2;

	if (keyLen != cipher->key_len) {
		fprintf(stderr, "Key length doesn't match, got %d expected %d.\n",
				keyLen, cipher->key_len);
		return 5;
	}
	EVP_CIPHER_CTX_init(&ctx);

	if (encdec == 1) {			// Encrypt
		if (!EVP_EncryptInit_ex(&ctx, cipher, NULL, key, iv)) {
			fprintf(stderr, "EncryptInit failed\n");
			return 6;
		}
		EVP_CIPHER_CTX_set_padding(&ctx, 1);

		if (!EVP_EncryptUpdate(&ctx, out, &out1, in, inLen)) {
			fprintf(stderr, "Encrypt failed\n");
			return 7;
		}
		if (!EVP_EncryptFinal_ex(&ctx, out + out1, &out2)) {
			fprintf(stderr, "EncryptFinal failed\n");
			return 8;
		}
		*outLen = out1 + out2;
	} else {					// Decrypt
		if (!EVP_DecryptInit_ex(&ctx, cipher, NULL, key, iv)) {
			fprintf(stderr, "DecryptInit failed\n");
			return 11;
		}
		EVP_CIPHER_CTX_set_padding(&ctx, 1);

		if (!EVP_DecryptUpdate(&ctx, out, &out1, in, inLen)) {
			fprintf(stderr, "Decrypt failed\n");
			return 12;
		}
		if (!EVP_DecryptFinal_ex(&ctx, out + out1, &out2)) {
			fprintf(stderr, "DecryptFinal failed\n");
			return 13;
		}
		*outLen = out1 + out2;
	}
	EVP_CIPHER_CTX_cleanup(&ctx);

	return 0;
}

int
main(int argc, char **argv)
{
	char   *inFile, *outFile;
	char   *cipherAlg;
	int     encdec = 1;
	int     keyLen, ivLen;
	int     inLen, outLen;
	unsigned char *iv, *key;
	unsigned char *intext, *outtext;
	const EVP_CIPHER *cipher;

	if (argc != 4) {
		fprintf(stderr, "usage: %s {-e | -d} inFile  outFile\n", argv[0]);
		exit(1);
	}

	/* Load up the software EVP_CIPHER definitions */
	OpenSSL_add_all_ciphers();
#ifndef OPENSSL_NO_ENGINE
	/* Load all compiled-in ENGINEs */
	ENGINE_load_builtin_engines();
	/* Register all available ENGINE implementations of ciphers. */
	ENGINE_register_all_ciphers();
#endif

	if (strcmp(argv[1], "-e") == 0) {
		encdec = 1;
	} else if (strcmp(argv[1], "-d") == 0) {
		encdec = 0;
	}
	inFile = argv[2];
	outFile = argv[3];

	cipherAlg = ckey[cno].cipher;
	keyLen = ckey[cno].keyLen;
	ivLen = ckey[cno].ivLen;
	key = ckey[cno].key;
	iv = ckey[cno].iv;

	intext = read_file(&inLen, inFile);
	outtext = malloc(inLen + ivLen);

	cipher = EVP_get_cipherbyname(cipherAlg);
	if (!cipher) {
		exit(2);
	}

	printf("Cipher algorithm: %s%s\n", EVP_CIPHER_name(cipher),
		   (encdec == 1 ? "(encrypt)" : "(decrypt)"));
#ifdef DEBUG
	printHex("Key", key, keyLen);
	printHex("IV", iv, ivLen);
#endif

	if (do_cipher(cipher, key, keyLen, iv, intext, inLen,
				  outtext, &outLen, encdec)) {
		exit(3);
	}
	if (write_file(outFile, outtext, outLen) != 0) {
		fprintf(stderr, "write file error!\n");
	}
	printf("In data  : %d bytes\n", inLen);
	printf("Out data : %d bytes\n", outLen);

	free(intext);
	free(outtext);

#ifndef OPENSSL_NO_ENGINE
	ENGINE_cleanup();
#endif
	EVP_cleanup();
	CRYPTO_cleanup_all_ex_data();

	return 0;
}
