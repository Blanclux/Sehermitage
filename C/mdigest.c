/**
 * mdigest.c
 *  Message Digest Test Program for OpenSSL
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

/* Select digest algorithm */
static int dno = 1;

static char *digestAlg[] = {
	"MD5", 
	"SHA1",
	"SHA256"
};

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
do_digest(const EVP_MD * md,
		  const unsigned char *in, int inLen,
		  unsigned char *out, unsigned int *outLen)
{
	EVP_MD_CTX ctx;
	unsigned int mdLen;

	EVP_MD_CTX_init(&ctx);
	if (!EVP_DigestInit_ex(&ctx, md, NULL)) {
		fprintf(stderr, "DigestInit failed\n");
		return 10;
	}
	if (!EVP_DigestUpdate(&ctx, in, inLen)) {
		fprintf(stderr, "DigestUpdate failed\n");
		return 11;
	}
	if (!EVP_DigestFinal_ex(&ctx, out, &mdLen)) {
		fprintf(stderr, "DigestFinal failed\n");
		return 12;
	}
	*outLen = mdLen;

	EVP_MD_CTX_cleanup(&ctx);

	return 0;
}

int
main(int argc, char **argv)
{
	char   *inFile, *outFile;
	int     inLen;
	unsigned int     outLen;
	unsigned char *in, *out;
	const EVP_MD *md;

	if (argc != 3) {
		fprintf(stderr, "usage: %s inFile  outFile\n", argv[0]);
		exit(1);
	}

	/* Load up the software EVP_MD definitions */
	OpenSSL_add_all_digests();
#ifndef OPENSSL_NO_ENGINE
	/* Load all compiled-in ENGINEs */
	ENGINE_load_builtin_engines();
	/* Register all available ENGINE implementations of digests. */
	ENGINE_register_all_digests();
#endif

	inFile = argv[1];
	outFile = argv[2];

	in = read_file(&inLen, inFile);
	inLen -= 1;		// remove EOF
	outLen = EVP_MAX_MD_SIZE;
	out = malloc(outLen);

	md = EVP_get_digestbyname(digestAlg[dno]);
	if (!md) {
		fprintf(stderr, "Can't find %s\n", digestAlg[dno]);
		exit(2);
	}

	printf("Digest Algorithm: %s\n", EVP_MD_name(md));
	printHex("In data", in, inLen);

	if (do_digest(md, in, inLen, out, &outLen)) {
		fprintf(stderr, "Digest error: %s\n", digestAlg[dno]);
		exit(3);
	}
	printf("Digest length = %d\n", outLen);
	printHex("Digest data", out, outLen);
	if (write_file(outFile, out, outLen) != 0) {
		fprintf(stderr, "write file error!\n");
	}

	free(in);
	free(out);
#ifndef OPENSSL_NO_ENGINE
	ENGINE_cleanup();
#endif
	EVP_cleanup();
	CRYPTO_cleanup_all_ex_data();

	return 0;
}
