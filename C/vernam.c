/**
 *  vernam.c
 *  Vernam encryption program
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <openssl/evp.h>

#define BLEN			16
#define BUFFER_SIZE		51200
#define KEYBUF_SIZE		51200

typedef unsigned long ULONG;

int     keymode = 0;			/* 0: key generation / 1: key file read */

void    getrand(unsigned char *rnd, unsigned char *t);
void    vernam(void);
void    xor(unsigned char *in, unsigned char *out, int n);
void    init(void);
int     readData(void);
void    writeData(unsigned char *data, int len);
void    usage(char *prog);

unsigned char initseed[16] = {
	0x01, 0x23, 0x45, 0x67, 0x89, 0xab, 0xcd, 0xef,
	0xfe, 0xdc, 0xba, 0x98, 0x76, 0x54, 0x32, 0x10
};

unsigned char buf[16], rndkey[16];
unsigned char keybuf[KEYBUF_SIZE];

FILE   *fp1, *fp2;

ULONG   bufsize;
unsigned char *buffer, *blast, *bp, *kp;
int     size;

void
xor(unsigned char *in, unsigned char *out, int n)
{
	unsigned char *p = in, *q = out;

	do {
		*q++ ^= *p++;
	} while (--n);
}

void
readKey(unsigned char *key)
{
	if (kp - keybuf + 16 > size) {
		kp = keybuf;
	}
	memcpy(key, kp, 16);
	kp += 16;
}

void
vernam(void)
{
	int     count;
	unsigned char rndkey[16];

	while ((count = readData()) != 0) {
		if (keymode == 0) {
			getrand(rndkey, initseed);
		} else {
			readKey(rndkey);
		}

		xor(rndkey, buf, BLEN);

		if (count < BLEN) {
			writeData(buf, count);
		} else {
			writeData(buf, BLEN);
		}
	}
}

int
readData(void)
{
	int     len;

	if (bp == buffer) {
		len = fread(buffer, sizeof(char), bufsize, fp1);
		blast = buffer + len;
	} else {
		len = blast - bp;
	}
	len = (len >= BLEN) ? BLEN : len;
	memcpy(buf, bp, len);
	return len;
}

void
writeData(unsigned char *data, int dataLen)
{
	int     len;

	len = (dataLen <= 0) ? BLEN : dataLen;
	memcpy(bp, data, len);

	if ((bp += len) >= blast) {
		fwrite(buffer, sizeof(char), bp - buffer, fp2);
		bp = buffer;
	}
}

void
init(void)
{
	buffer = (unsigned char *) malloc(sizeof(char) * BUFFER_SIZE);
	if (buffer != NULL) {
		bp = buffer;
		bufsize = (BUFFER_SIZE / BLEN) * BLEN;
	} else {
		printf("Memory allocation error (%d KB)\n", BUFFER_SIZE / 1024);
		exit(1);
	}

	kp = keybuf;
}

void
usage(char *prog)
{
	fprintf(stderr,
			"Usage : %s [-fFile] [-sSeed] {-e|-d} inFile outFile\n", prog);
	fprintf(stderr, "   -f : Read key from a file\n");
	fprintf(stderr, "         File : key file name\n");
	fprintf(stderr, "   -s : Generate key\n");
	fprintf(stderr,
			"         Seed : 1 - 16 characters\n");
	fprintf(stderr, "   -e : Encryption mode\n");
	fprintf(stderr, "   -d : Decryption mode\n");
	fprintf(stderr, "  inFile  : source file (or 'stdin')\n");
	fprintf(stderr, "  outFile : destination file (or 'stdout')\n\n");
	exit(0);
}

int
main(int argc, char *argv[])
{
	int     k, kf = 0, kk = 0;
	int     deflag = -1;
	char   *file1, *file2, *p;
	unsigned char *q;

	if (argc < 2 || *argv[1] != '-') {
		usage(argv[0]);
	}
	k = 1;

	do {
		switch (*(argv[k] + 1)) {
		  case 'f':
			  kf = k;
			  keymode = 1;
			  break;
		  case 's':
			  keymode = 0;
			  kk = k;
			  break;
		  case 'd':
			  if (deflag == 0) {
				  usage(argv[0]);
			  }
			  deflag = 1;
			  break;
		  case 'e':
			  if (deflag == 1) {
				  usage(argv[0]);
			  }
			  deflag = 0;
			  break;
		  default:
			  usage(argv[0]);
		}
	} while (*argv[++k] == '-');

	if (deflag == -1 || k >= argc) {
		usage(argv[0]);
	}
	file1 = argv[k++];

	if (k >= argc) {
		usage(argv[0]);
	}
	file2 = argv[k];

	if (keymode == 0 && kk != 0) {
		p = argv[kk] + 2;
		q = initseed;
		k = 0;
		while (k++ < 16 && *p != '\0') {
			*q++ = *p++;
		}
		*q = '\0';
		printf("Initial seed: %s\n", initseed);
	} else if (kf != 0) {
		fp1 = fopen(argv[kf] + 2, "rb");
		if (fp1 == NULL) {
			fprintf(stderr, "Key file (%s) can't open.\n", argv[kf] + 2);
			return 1;
		}
		size = fread(keybuf, 1, KEYBUF_SIZE, fp1);
		printf("key size = %d\n", size);
		fclose(fp1);
	}

	/* Input */
	if (strcmp(file1, "stdin") == 0) {
		fp1 = stdin;
	} else {
		fp1 = fopen(file1, "rb");
		if (fp1 == NULL) {
			fprintf(stderr, "Source file (%s) can't open.\n", file1);
			return 1;
		}
	}

	/* Onput */
	if (strcmp(file2, "stdout") == 0) {
		fp2 = stdout;
	} else {
#if 0
		fp2 = fopen(file2, "rb");
		if (fp2 != NULL) {
			fprintf(stderr, "Destination file (%s) already exist.\n",
					file2);
			return 1;
		}
#endif
		fp2 = fopen(file2, "wb");
	}

	/* Initialize (memory allocation) */
	init();

	/* Vernam encryption */
	vernam();

	/* Write data to file */
	fwrite(buffer, sizeof(char), bp - buffer, fp2);

	fclose(fp1);
	fclose(fp2);
	free(buffer);
	return 0;
}
