/**
 * eccalc.c
 *  EC Calculation Program using GNU MP
 *  wrtten by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <stdarg.h>
#include <gmp.h>

/*
 * mpz_t variable initializer
 */
void
vmpz_init(mpz_t * a, ...)
{
	va_list ap;
	mpz_t *t;

	va_start(ap, a);
	mpz_init(*a);
	while ((t = va_arg(ap, mpz_t *)) != (mpz_t *) NULL) {
		mpz_init(*t);
	}
	va_end(ap);
}

/*
 * mpz_t variable remover
 */
void
vmpz_clear(mpz_ptr a, ...)
{
	va_list ap;
	mpz_ptr t;

	va_start(ap, a);
	mpz_clear(a);
	while ((t = va_arg(ap, mpz_ptr)) != 0) {
		mpz_clear(t);
	}
	va_end(ap);
}

/* ECP_3D */
/*
 * P = 2 * P1
 */
#define EC_Dbl_3D(px,py,pz,p1x,p1y,p1z,a,p,e,f,h,s,w,tmp0,tmp1) \
	if (!mpz_cmp_ui(p1z, 0)) { \
		mpz_set(px, p1x); \
		mpz_set(py, p1y); \
		mpz_set(pz, p1z); \
	} \
	else{ \
		/* s, e, f, w, h */ \
		mpz_mul(s, p1y, p1z);		/* s = y1*z1 */ \
		mpz_mod(s, s, p); \
		mpz_mul(e, p1y, s);			/* e = y1*s */ \
		mpz_mod(e, e, p); \
		mpz_mul(f, p1x, e);			/* f = x1*e */ \
		mpz_mod(f, f, p); \
		mpz_mul(tmp0, p1x, p1x);	/* x1^2 */ \
		mpz_mod(tmp0, tmp0, p); \
		mpz_mul_ui(tmp0, tmp0, 3); \
		mpz_mod(tmp0, tmp0, p);		/* 3*x1^2 */ \
		mpz_mul(tmp1, p1z, p1z);	/* z1^2 */ \
		mpz_mod(tmp1, tmp1, p); \
		mpz_mul(tmp1, a, tmp1);		/* a*z1^2 */ \
		mpz_mod(tmp1, tmp1, p); \
		mpz_add(w, tmp0, tmp1);		/* w = 3*x1^2 + a*z1^2 */ \
		mpz_mod(w, w, p); \
		mpz_mul(tmp0, w, w);		/* w^2 */ \
		mpz_mod(tmp0, tmp0, p); \
		mpz_mul_2exp(f, f, 3);		/* 8*f */ \
		mpz_sub(h, tmp0, f);		/* h = w^2 - 8*f */ \
		mpz_mod(h, h, p); \
		/* x3 */ \
		mpz_mul(tmp0, s, h);		/* s*h */ \
		mpz_mod(tmp0, tmp0, p);	\
		mpz_mul_2exp(tmp0, tmp0, 1);	/* 2*s*h */ \
		mpz_mod(px, tmp0, p);		/* x3 = 2*s*h */ \
		/* y3 */ \
		mpz_tdiv_q_2exp(f, f, 1);	/* 4*f */ \
		mpz_sub(tmp0, f, h);		/* 4*f-h */ \
		mpz_mod(tmp0, tmp0, p);	\
		mpz_mul(tmp0, tmp0, w);		/* w*(4*f-h) */ \
		mpz_mod(tmp0, tmp0, p); \
		mpz_mul(tmp1, e, e);		/* e^2 */ \
		mpz_mod(tmp1, tmp1, p);	\
		mpz_mul_2exp(tmp1, tmp1, 3);	/* 8*e^2 */ \
		mpz_sub(py, tmp0, tmp1);	/* y3 = w*(4*f-h)-8*e^2 */ \
		mpz_mod(py, py, p); \
		/* z3 */ \
		mpz_pow_ui(tmp0, s, 3);			/* s^3 */ \
		mpz_mul_2exp(tmp0, tmp0, 3);	/* 8*s^3 */ \
		mpz_mod(pz, tmp0, p);		/* z3 = 8*s^3 */ \
	}

/*
 * P = P1 + P2
 */
#define EC_Add_3D(px,py,pz,p1x,p1y,p1z,p2x,p2y,p2z,p,u,v,t,a,b0,b1,b2,b3,b4,tmp0,tmp1) \
	if (!mpz_cmp_ui(p1z, 0)) { \
		mpz_set(px, p2x); \
		mpz_set(py, p2y); \
		mpz_set(pz, p2z); \
	} \
	else if (!mpz_cmp_ui(p2z, 0)){ \
		mpz_set(px, p1x); \
		mpz_set(py, p1y); \
		mpz_set(pz, p1z); \
	} \
	else { \
		mpz_mul(b0, p2x, p1z);		/* x2*z1 */ \
		mpz_mod(b0, b0, p); \
		mpz_mul(b1, p1x, p2z);		/* x1*z2 */ \
		mpz_mod(b1, b1, p); \
		mpz_mul(b2, p1y, p2z);		/* y1*z2 */ \
		mpz_mod(b2, b2, p); \
		mpz_mul(b3, p2y, p1z);		/* y2*z1 */ \
		mpz_mod(b3, b3, p); \
		/* v */ \
		mpz_sub(v, b0, b1);			/* v = x2*z1 - x1*z2 */ \
		mpz_mod(v, v, p); \
		if (mpz_cmp(b0, b1) == 0 && mpz_cmp(b2, b3) == 0 ) {\
			EC_Dbl_3D(px,py,pz,p1x,p1y,p1z,a,p,b0,b1,b2,b3,b4,tmp0,tmp1);\
		} else if (!mpz_cmp_ui(v, 0)) {\
			mpz_set_ui(px, 0);\
			mpz_set_ui(py, 0);\
			mpz_set_ui(pz, 0);\
		} else {\
			/* t */ \
			mpz_add(t, b0, b1);			/* t = x2*z1 + x1*z2 */ \
			mpz_mod(t, t, p); \
			/* u */ \
			mpz_sub(u, b3, b2);			/* u = y2*z1 - y1*z2 */ \
			mpz_mod(u, u, p); \
			mpz_mul(b3, v, v);			/* v^2 */ \
			mpz_mod(b3, b3, p); \
			mpz_mul(b4, b3, v);			/* v^3 */ \
			mpz_mod(b4, b4, p); \
			mpz_mul(b0, p1z, p2z);		/* z1*z2 */ \
			mpz_mod(b0, b0, p); \
			/* a */ \
			mpz_mul(tmp0, u, u);		/* u^2 */ \
			mpz_mod(tmp0, tmp0, p); \
			mpz_mul(tmp0, tmp0, b0);	/* u^2*z1*z2 */ \
			mpz_mod(tmp0, tmp0, p); \
			mpz_mul(tmp1, b3, t);		/* v^2*t */ \
			mpz_mod(tmp1, tmp1, p); \
			mpz_sub(a, tmp0, tmp1);		/* a = u^2*z1*z2 - v^2*t */ \
			mpz_mod(a, a, p); \
			/* x3 */ \
			mpz_mul(px, v, a);			/* x3 = v*a */ \
			mpz_mod(px, px, p); \
			/* z3 */ \
			mpz_mul(pz, b4, b0);		/* z3 = v^3*z1*z2 */ \
			mpz_mod(pz, pz, p); \
			/* y3 */ \
			mpz_mul(tmp0, b1, b3);		/* x1*z2*v^2 */ \
			mpz_mod(tmp0, tmp0, p); \
			mpz_sub(tmp0, tmp0, a);		/* x1*z2*v^2-a */ \
			mpz_mod(tmp0, tmp0, p); \
			mpz_mul(tmp0, tmp0, u);		/* (x1*z2*v^2-a)*u */ \
			mpz_mod(tmp0, tmp0, p); \
			mpz_mul(tmp1, b2, b4);		/* y1*z2*v^3 */ \
			mpz_mod(tmp1, tmp1, p); \
			mpz_sub(py, tmp0, tmp1);	/* y3 = (x1*z2*v^2-a)*u - y1*z2*v^3 */ \
			mpz_mod(py, py, p); \
		}\
	}

/*
 * P = P1 + P2
 */
int
ecpAdd_3D(mpz_t px, mpz_t py, mpz_t pz, mpz_t x1, mpz_t y1, mpz_t z1,
		  mpz_t x2, mpz_t y2, mpz_t z2, mpz_t a, mpz_t p)
{
	mpz_t  W1, W2, W3, W4, W5, W6, W7, W8, W9, W10, W11;

	vmpz_init(&W1, &W2, &W3, &W4, &W5, &W6, &W7, &W8, &W9, &W10,
			  &W11, (mpz_t *) 0);

	mpz_mul(W1, x1, z2);
	mpz_mod(W1, W1, p);

	mpz_mul(W2, x2, z1);
	mpz_mod(W2, W2, p);

	mpz_mul(W3, y1, z2);
	mpz_mod(W3, W3, p);

	mpz_mul(W4, y2, z1);
	mpz_mod(W4, W4, p);

	if (mpz_cmp(W1, W2) == 0 && mpz_cmp(W3, W4) == 0 &&
		mpz_cmp_ui(W1, 0) && mpz_cmp_ui(W2, 0)) {
		EC_Dbl_3D(px, py, pz, x1, y1, z1, a, p, W1, W2, W3, W4, W5, W6, W7);
	}
	else {
		EC_Add_3D(px, py, pz, x1, y1, z1, x2, y2, z2, p, W1, W2, W3, W4,
				  W5, W6, W7, W8, W9, W10, W11);
	}
	vmpz_clear(W1, W2, W3, W4, W5, W6, W7, W8, W9, W10, W11, (mpz_ptr) 0);

	return 0;
}

/*
 * P = k * P1
 */
int
ecpMul_3D(mpz_ptr px, mpz_ptr py, mpz_ptr pz, mpz_ptr k,
		  mpz_ptr x, mpz_ptr y, mpz_ptr z, mpz_ptr a, mpz_ptr p)
{
	mpz_t W1, W2, W3, W4, W5, W6, W7, W8, W9, W10, W11;
	int i;

	if (!mpz_cmp_ui(k, 0)) {
		mpz_set_ui(px, 0);
		mpz_set_ui(py, 0);
		mpz_set_ui(pz, 0);
		return 1;
	}

	vmpz_init(&W1, &W2, &W3, &W4, &W5, &W6, &W7, &W8, &W9, &W10, &W11,
			  (mpz_t *) 0);

	mpz_set(px, x);
	mpz_set(py, y);
	mpz_set(pz, z);

	for (i = mpz_sizeinbase(k, 2) - 2; i >= 0; i--) {
		EC_Dbl_3D(px, py, pz, px, py, pz, a, p, W1, W2, W3, W4,
				  W5, W6, W7);
		if (mpz_scan1(k, i) == (unsigned) i) {
			EC_Add_3D(px, py, pz, px, py, pz, x, y, z, p, W1, W2, W3,
					  W4, W5, W6, W7, W8, W9, W10, W11);
		}
	}

	vmpz_clear(W1, W2, W3, W4, W5, W6, W7, W8, W9, W10, W11, (mpz_ptr) 0);

	return 0;
}

/*
 * Convert 3D to 2D
 */
int
toAffine(mpz_ptr x, mpz_ptr y, mpz_ptr z, mpz_ptr p)
{
	if (!mpz_cmp_ui(z, 1)) {
		return 0;
	}
	/* x = x * z^(-1) , y = y * z^(-1), z = 1 */
	if (mpz_cmp_ui(z, 0)) {
		if (mpz_invert(z, z, p) == 0) {	/* Invert error */
			return 1;
		}
		mpz_mul(x, x, z);
		mpz_mod(x, x, p);
		mpz_mul(y, y, z);
		mpz_mod(y, y, p);
		mpz_set_ui(z, 1);
		return 0;
	}
	else {
		return 1;
	}
}


/* ECP_2D */
/*
 * P = 2 * P1
 */
#define EC_Dbl_2D(px, py, p1x, p1y, p, a, W1, W2, W3, W4) \
	mpz_mul(W1, p1x, p1x); \
	mpz_mod(W1, W1, p); \
	mpz_mul_ui(W2, W1, 3); \
	mpz_add(W1, W2, a); 	/* W1 = 3 * x1^2 + a */ \
	mpz_mod(W1, W1, p); \
	mpz_add(W2, p1y, p1y); \
	mpz_mod(W2, W2, p); \
	mpz_invert(W3, W2, p);	/* W3 = 1/(2 * y1) */ \
	mpz_mul(W2, W1, W3);	/* W2 = (3 * x1^2 + a)/(2 * y1) */ \
	mpz_mod(W2, W2, p); \
	/* x3 = (( 3 * x1^2 + a)/(2 * y1))^2 - 2 * x1 */ \
	mpz_add(W1, p1x, p1x); \
	mpz_mod(W1, W1, p); \
	mpz_mul(W3, W2, W2); \
	mpz_mod(W3, W3, p); \
	mpz_set(W4, p1x); \
	mpz_sub(px, W3, W1); \
	mpz_mod(px, px, p); \
	/* y3 = (( 3 * x1^2 + a)/(2 * y1))(x1 - x3) - y1 */ \
	mpz_sub(W1, W4, px); \
	mpz_mod(W1, W1, p); \
	mpz_mul(W3, W2, W1); \
	mpz_mod(W3, W3, p); \
	mpz_sub(py, W3, p1y); \
	mpz_mod(py, py, p);

/*
 * P = P1 + P2
 */
#define EC_Add_2D(px, py, p1x, p1y, p2x, p2y, p, a, W1, W2, W3, W4) \
	if (!mpz_cmp_ui(p1x, 0) && !mpz_cmp_ui(p1y, 0)) { \
		mpz_set(px, p2x); \
		mpz_set(py, p2y); \
		goto END; \
	} \
	else if (!mpz_cmp_ui(p2x, 0) && !mpz_cmp_ui(p2y, 0)) { \
		mpz_set(px, p1x); \
		mpz_set(py, p2y); \
		goto END; \
	} \
	else { \
		mpz_sub(W1, p2x, p1x);	/* W1 = x2 - x1 */ \
		mpz_mod(W1, W1, p); \
		mpz_sub(W2, p2y, p1y);	/* W2 = y2 - y1 */ \
		mpz_mod(W2, W2, p); \
		if (!mpz_cmp_ui(W1, 0)) { \
			if (!mpz_cmp_ui(W2, 0)) { \
				if (!mpz_cmp_ui(p1y, 0)) { \
					mpz_set_ui(px, 0); \
					mpz_set_ui(py, 0); \
					goto END; \
				} \
				else { \
					mpz_mul(W2, p1x, p1x); \
					mpz_mod(W2, W2, p); \
					mpz_mul_ui(W1, W2, 3); \
 					mpz_add(W2, W1, a);		/* W2 = 3 * x1^2 + a */ \
					mpz_mod(W2, W2, p); \
					mpz_add(W1, p1y, p1y);	/* W1 = 2 * y1 */ \
					mpz_mod(W1, W1, p); \
					mpz_invert(W3, W1, p);	/* W3 = 1/(2 * y1) */ \
					mpz_mul(W1, W2, W3);	/* W1 = (3 * x1^2 + a)/(2 * y1) */ \
					mpz_mod(W1, W1, p); \
				} \
			} \
			else { \
				mpz_set_ui(px, 0); \
				mpz_set_ui(py, 0); \
				goto END; \
			} \
		} \
		else { \
			mpz_invert(W3, W1, p); \
			mpz_mul(W1, W3, W2);	/* W1 = (y2 - y1)/(x2 - x1) */ \
			mpz_mod(W1, W1, p); \
		} \
	} \
	/* x3 = ((y2 - y1)/(x2 - x1))^2 - x1 - x2  */ \
	mpz_mul(W3, W1, W1); \
	mpz_mod(W3, W3, p); \
	mpz_add(W2, p1x, p2x); \
	mpz_mod(W2, W2, p); \
	mpz_set(W4, p1x); \
	mpz_sub(px, W3, W2); \
	mpz_mod(px, px, p); \
	/* y3 = ((y2 - y1)/(x2 - x1))(x1 - x3) - y1 */ \
	mpz_sub(W2, W4, px); \
	mpz_mod(W2, W2, p); \
	mpz_mul(W3, W1, W2); \
	mpz_mod(W3, W3, p); \
	mpz_sub(py, W3, p1y); \
	mpz_mod(py, py, p); \
 END:


/*
 * P = P1 + P2
 */
int
ecpAdd_2D(mpz_ptr px, mpz_ptr py, mpz_ptr pz, mpz_ptr p1x, mpz_ptr p1y, mpz_ptr p1z,
		  mpz_ptr p2x, mpz_ptr p2y, mpz_ptr p2z, mpz_ptr a, mpz_ptr p)
{
	mpz_t W1, W2, W3, W4;

	vmpz_init(&W1, &W2, &W3, &W4, (mpz_t *) 0);

	if (mpz_cmp(p1x, p2x) == 0 && mpz_cmp(p1y, p2y) == 0) {
		EC_Dbl_2D(px, py, p1x, p1y, p, a, W1, W2, W3, W4);
	}
	else {
		EC_Add_2D(px, py, p1x, p1y, p2x, p2y, p, a, W1, W2, W3, W4);
	}
	vmpz_clear(W1, W2, W3, W4, (mpz_ptr) 0);

	mpz_set_ui(pz, 1);

	return 0;
}

/*
 * P = k * P1
 */
int
ecpMul_2D(mpz_ptr px, mpz_ptr py, mpz_ptr pz, mpz_ptr k,
		  mpz_ptr p1x, mpz_ptr p1y, mpz_ptr p1z, mpz_ptr a, mpz_ptr p)
{
	mpz_t Gx, Gy, W1, W2, W3, W4;
	int i;

	if (!mpz_cmp_ui(k, 0)) {
		mpz_set_ui(px, 0);
		mpz_set_ui(py, 0);
		mpz_set_ui(pz, 1);
		return 1;
	}

	vmpz_init(&W1, &W2, &W3, &W4, &Gx, &Gy, (mpz_t *) 0);

	mpz_set(Gx, p1x);
	mpz_set(Gy, p1y);

	for (i = mpz_sizeinbase(k, 2) - 2; i >= 0; i--) {
		EC_Dbl_2D(Gx, Gy, Gx, Gy, p, a, W1, W2, W3, W4);
		if (mpz_scan1(k, i) == (unsigned) i) {
			//EC_Add_2D_nocheck(Gx, Gy, Gx, Gy, p1x, p1y, p, W1, W2, W3, W4);
			EC_Add_2D(Gx, Gy, Gx, Gy, p1x, p1y, p, a, W1, W2, W3, W4);
		}
	}
	mpz_set(px, Gx);
	mpz_set(py, Gy);
	mpz_set_ui(pz, 1);

	vmpz_clear(W1, W2, W3, W4, Gx, Gy, (mpz_ptr) 0);

	return 0;
}

/*
 * P = P1 + P2
 */
int
ecpAdd(mpz_ptr px, mpz_ptr py, mpz_ptr pz, mpz_ptr p1x, mpz_ptr p1y, mpz_ptr p1z,
	   mpz_ptr p2x, mpz_ptr p2y, mpz_ptr p2z, mpz_ptr a, mpz_ptr p)
{
#ifdef ECP_2D
	return ecpAdd_2D(px, py, pz, p1x, p1y, p1z, p2x, p2y, p2z, a, p);
#elif defined(ECP_3D)
	return ecpAdd_3D(px, py, pz, p1x, p1y, p1z, p2x, p2y, p2z, a, p);
#endif
}

/*
 * P = k * P1
 */
int
ecpMul(mpz_ptr px, mpz_ptr py, mpz_ptr pz, mpz_ptr k,
	   mpz_ptr p1x, mpz_ptr p1y, mpz_ptr p1z, mpz_ptr a, mpz_ptr p)
{
#ifdef ECP_2D
	return ecpMul_2D(px, py, pz, k, p1x, p1y, p1z, a, p);
#elif defined(ECP_3D)
	return ecpMul_3D(px, py, pz, k, p1x, p1y, p1z, a, p);
#endif
}
