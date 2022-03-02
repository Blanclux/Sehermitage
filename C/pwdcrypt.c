/**
 * pwdcrypt.c
 *  Password Cipher Program for OpenSSL
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>

#include <openssl/evp.h>

/* Iteration count */
#define ITERATION 8
/* Salt */
static unsigned char *saltValue = (unsigned char *)"salt";

#define ALGORITHM	"AES-128-CBC";
#define KEY_LEN 16
#define IV_LEN	16

static unsigned char key[32];
static unsigned char iv[16];

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
	unsigned char *out;
	char   *pwd;
	char   *inFile, *outFile;
	char   *cipherAlg;
	int     encdec = 1;
	int     keyLen, ivLen;
	int     inLen, outLen;
	unsigned char *intext, *outtext;
	const EVP_CIPHER *cipher;
	if (argc != 5) {
		fprintf(stderr, "usage: %s {-e | -d} passwd inFile  outFile\n",
				argv[0]);
		exit(1);
	}

	/* Load up the software EVP_CIPHER definitions */
	OpenSSL_add_all_ciphers();
	if (strcmp(argv[1], "-e") == 0) {
		encdec = 1;
	} else if (strcmp(argv[1], "-d") == 0) {
		encdec = 0;
	}
	pwd = argv[2];
	inFile = argv[3];
	outFile = argv[4];
	cipherAlg = ALGORITHM;
	keyLen = KEY_LEN;
	ivLen = IV_LEN;

	/* Set key anf iv from password */
	outLen = ivLen + keyLen;
	out = (unsigned char *) malloc(outLen);
	printf("Password: %s\n", pwd);
	printf("Itertion: %u\n", ITERATION);
	printHex("Salt", saltValue, sizeof(saltValue));
	if (PKCS5_PBKDF2_HMAC_SHA1(pwd, strlen(pwd), saltValue,
		sizeof(saltValue), ITERATION, outLen, out) != 0) {
#ifdef DEBUG
		printHex("KDF out", out, outLen);
#endif
	} else {
		fprintf(stderr, "PKCS5_PBKDF2_HMAC_SHA1 failed\n");
	}
	memcpy(key, out, keyLen);
	memcpy(iv, out + keyLen, ivLen);
	free(out);
	intext = read_file(&inLen, inFile);
	if (intext == NULL) {
		fprintf(stderr, "File %s not found.\n", inFile);
		exit(1);
	}

	outtext = malloc(inLen + ivLen);
	cipher = EVP_get_cipherbyname(cipherAlg);
	if (!cipher) {
		exit(2);
	}
	printf("Cipher algorithm: %s %s\n", EVP_CIPHER_name(cipher),
		   (encdec == 1 ? "(encrypt)" : "(decrypt)"));

#ifdef DEBUG
	printHex("Key", key, keyLen);
	printHex("IV", iv, ivLen);
#endif
	if (do_cipher(cipher, key, keyLen, iv, intext, inLen, outtext, &outLen,
				  encdec)) {
		exit(3);
	}
	if (write_file(outFile, outtext, outLen) != 0) {
		fprintf(stderr, "write file error!\n");
	}
	printf("In data  : %d bytes\n", inLen);
	printf("Out data : %d bytes\n", outLen);
	free(intext);
	free(outtext);
	EVP_cleanup();
	CRYPTO_cleanup_all_ex_data();

	return 0;
}
