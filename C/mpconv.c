/**
 * mpconv.c
 *  Utility program for GNU MP
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
 
#include <stdlib.h>
#include <string.h>
#include <gmp.h>

void
mpz_mptoc(unsigned char *ch, unsigned int *n, mpz_ptr a)
{
	unsigned int  i, j, size;
	char *str;
	unsigned char tmp1, tmp2;

	size = (unsigned int)mpz_sizeinbase(a, 16);
	str = (char *)malloc(size + 1);
	mpz_get_str(str, 16, a);

	i = j = 0;
	tmp1 = 0;
	if (size % 2) {
		if (str[i] >= '0' && str[i] <= '9') {
			tmp1 = str[i++] - '0';
		} else if (str[i] >= 'A' && str[i] <= 'F') {
			tmp1 = str[i++] - 'A' + 0xA;
		} else if (str[i] >= 'a' && str[i] <= 'f') {
			tmp1 = str[i++] - 'a' + 0xA;
		}
		ch[j++] = tmp1 & 0xf;
	}
	for (; i < size; i += 2) {
		tmp1 = 0;
		tmp2 = 0;
		if (str[i] >= '0' && str[i] <= '9') {
			tmp1 = str[i] - '0';
		} else if (str[i] >= 'A' && str[i] <= 'F') {
			tmp1 = str[i] - 'A' + 0xA;
		} else if (str[i] >= 'a' && str[i] <= 'f') {
			tmp1 = str[i] - 'a' + 0xA;
		}
		if (i + 1 < size) {
			tmp1 <<= 4;
			if (str[i+1] >= '0' && str[i+1] <= '9') {
				tmp2 = str[i+1] - '0';
			} else if (str[i+1] >= 'A' && str[i+1] <= 'F') {
				tmp2 = str[i+1] - 'A' + 0xA;
			} else if (str[i+1] >= 'a' && str[i+1] <= 'f') {
				tmp2 = str[i+1] - 'a' + 0xA;
			}
		}
		ch[j++] = tmp1 | (tmp2 & 0xf);
	}
	*n = j;
	free(str);
}

void
mpz_mptouc(unsigned char *ch, unsigned int n, mpz_ptr a)
{
	unsigned char *p;
	unsigned int size;

	size = (unsigned int)mpz_sizeinbase(a, 16);
	p = (unsigned char *)malloc((size + 1) / 2);

	mpz_mptoc(p, &size, a);

	if (n <= size) {
		memcpy(ch, p, n);
	}
	else {
		memset(ch, 0, n - size);
		memcpy(ch + n - size, p, size);
	}
	free(p);
	return; 
}

void
mpz_ctomp(mpz_ptr a, unsigned char *ch, unsigned int n)
{
	unsigned int i;
	char *str;
	char num_to_text[] = "0123456789abcdef";

	str = (char *)malloc(2 * n + 1);

	for (i = 0; i < n; i++) {
		str[2*i]   = num_to_text[ch[i] >> 4];
		str[2*i+1] = num_to_text[ch[i] & 0xf];
	}
	str[2*n] = 0;
	mpz_set_str(a, str, 16);
}

int
mpz_bytelen(mpz_ptr a)
{
	int len, size;

	len = (unsigned int)mpz_sizeinbase(a, 16);
	size = len / 2;
	if (len % 2) {
		size++;
	}
	return size;
}

