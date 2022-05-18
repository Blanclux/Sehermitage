/**
 * rsa_gmp.c
 *  RSA Encrypt/Decrypt Program using GNU MP
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <stdio.h>
#include <string.h>
#include <gmp.h>

#define RSA_N_LEN	1024    /* RSA n key Length (bits) (must be even) */
#define RSA_E_LEN	5		/* RSA e key length (bits) ( > 2) */


/*
 * Minimum bitsize of RSA key p, q and n.
 */
#define RSA_MIN_P  49
#define RSA_MIN_Q  49
#define RSA_MIN_N  (RSA_MIN_P + RSA_MIN_Q)

/*
 * RSA_KEY_DFB defines the upper limit of bit difference between P and Q.
 */
#define RSA_KEY_DFB  4

/*
 * Macros for readability
 */
#define rsa_encrypt( c, m, e, n )      mpz_powm( (c), (m), (e), (n) )
#define rsa_decrypt( m, c, d, n )      mpz_powm( (m), (c), (d), (n) )

extern void mpz_random_prime(mpz_ptr, int);
extern void mpz_random_prime2(mpz_ptr, mpz_ptr, mpz_ptr);
extern void mpz_ctomp(mpz_ptr, unsigned char *, unsigned int);
extern void mpz_mptoc(unsigned char *, unsigned int *, mpz_ptr);


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
rsa_gen_pq(mpz_ptr n, mpz_ptr p, mpz_ptr q, int pb, int qb)
{
	int nb;
	mpz_t tp, tq, maxq, minq, tmp, tmp1;

	if (pb < RSA_MIN_P)
		return 1;
	if (qb < RSA_MIN_Q)
		return 1;

	mpz_init(tp);
	mpz_init(tq);
	mpz_init(maxq);
	mpz_init(minq);
	mpz_init(tmp);
	mpz_init(tmp1);

	nb = pb + qb;

	mpz_random_prime(tp, pb);	/* generate p */

	/* set maxq */
	mpz_set_ui(tmp, 0);
	mpz_setbit(tmp, nb);
	mpz_sub_ui(tmp, tmp, 1);	/* 2^nb - 1 */
	mpz_tdiv_q(maxq, tmp, tp);
	mpz_set_ui(tmp, 0);
	mpz_setbit(tmp, qb);		/* 2^qb */
	if (mpz_cmp(maxq, tmp) > 0) {
		mpz_set(maxq, tmp);			/* if maxq is larger than 2^qb, */
		mpz_sub_ui(maxq, maxq, 1);	/* let maxq 2^qb - 1 */
	}

	/* set minq */
	mpz_set_ui(tmp, 0);
	mpz_setbit(tmp, nb - 1);	/* 2 ^(nb-1) */
	mpz_tdiv_q(minq, tmp, tp);
	mpz_set_ui(tmp, 0);
	mpz_setbit(tmp, qb - 1);	/* 2 ^(qb-1) */
	if (mpz_cmp(minq, tmp) < 0) {	/* if minq is smaller than 2^(qb-1), */
		mpz_set(minq, tmp);			/* let minq 2^(qb-1) */
	}

	do {
		mpz_random_prime2(tq, maxq, minq);
		mpz_sub(tmp, tp, tq);
	} while (pb - mpz_sizeinbase(tmp, 2) > RSA_KEY_DFB);
	/* check if |p-q| is large enough */

	if (mpz_sgn(tmp) == -1 && pb == qb) {
		mpz_set(tmp1, tp);		/* if p is smaller than q and their bitsize is the same, */
		mpz_set(tp, tq);		/* exchange p and q. */
		mpz_set(tq, tmp1);		/* p should be larger than p generally */
	}

	mpz_mul(n, tp, tq);
	mpz_set(p, tp);
	mpz_set(q, tq);

	mpz_clear(tp);
	mpz_clear(tq);
	mpz_clear(maxq);
	mpz_clear(minq);
	mpz_clear(tmp);
	mpz_clear(tmp1);

	return 0;
}

int
rsa_gen_ed(mpz_ptr d, mpz_ptr e, int eb, mpz_ptr p, mpz_ptr q)
{
	mpz_t lm, p1, q1;
	int rv;

	if (eb < 2) {
		return 1;
	}

	mpz_init(lm);
	mpz_init_set(p1, p);
	mpz_sub_ui(p1, p1, 1);
	mpz_init_set(q1, q);
	mpz_sub_ui(q1, q1, 1);

	mpz_lcm(lm, p1, q1);		/* lm = lcm(p-1, q-1) */

	do {
		mpz_random_prime(e, eb);
		rv = mpz_invert(d, e, lm);	/* d = 1/e mod lm  */
	} while (!rv);

	if (mpz_sgn(d) == -1) {
		mpz_add(d, d, lm);
	}

	mpz_clear(lm);
	mpz_clear(p1);
	mpz_clear(q1);

	return 0;
}

int
main(int argc, char *argv[])
{
	unsigned int  i;
	char   *text = "The quick brown fox jumps over the lazy dog";
	unsigned char *data;
	unsigned int  dataLen, encLen, decLen;
	unsigned char enc[1024], dec[1024];

	mpz_t p, q, d, n, e;
	mpz_t c, m, mm;
	int pb, qb, eb;

	if (argc > 2) {
		fprintf(stderr, "%s plainText\n", argv[0]);
		return 1;
	}
	if (argc == 1) {
		data = (unsigned char *) text;
		dataLen =(unsigned int)strlen(text);
	} else {
		data = (unsigned char *) argv[1];
		dataLen = (unsigned int)strlen(argv[1]);
	}

	/* generate private key & public key */
	printf("< RSA Key Generation >\n");

	pb = qb = RSA_N_LEN / 2;
	eb = RSA_E_LEN;

	mpz_init(p);
	mpz_init(q);
	mpz_init(d);
	mpz_init(n);
	mpz_init(e);

	rsa_gen_pq(n, p, q, pb, qb);
	rsa_gen_ed(d, e, eb, p, q);

	printf("p = "); mpz_out_str(stdout, 16, p); puts("");
	printf("q = "); mpz_out_str(stdout, 16, q); puts("");
	printf("n = "); mpz_out_str(stdout, 16, n); puts("");
	printf("e = "); mpz_out_str(stdout, 16, e); puts("");
	printf("d = "); mpz_out_str(stdout, 16, d); puts("");

	/* Encrypt */
	mpz_init(c);
	mpz_init(m);

	mpz_ctomp(m, data, dataLen);
	rsa_encrypt(c, m, e, n);
	mpz_mptoc(enc, &encLen, c);

	printf("\n< RSA Encrypt/Decrypt >\n");
	printHex("PLAIN", data, dataLen);
	printHex("ENCRYPT", enc, encLen);

	/* Decrypt */
	mpz_init(mm);
	rsa_decrypt(mm, c, d, n);
	mpz_mptoc(dec, &decLen, mm);


	printHex("DECRYPT", dec, decLen);
	if (dataLen != decLen) {
		printf("Decrypt NG\n");
		return 1;
	}
	for (i = 0; i < decLen; i++) {
		if (data[i] != dec[i]) {
			printf("Decrypt NG\n");
			return 1;
		}
	}
	printf("Decrypt OK\n");

	mpz_clear(p);
	mpz_clear(q);
	mpz_clear(d);
	mpz_clear(n);
	mpz_clear(e);

	mpz_clear(c);
	mpz_clear(m);
	mpz_clear(mm);

	return 0;
}

