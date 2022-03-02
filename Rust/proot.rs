/**
 * proot
 *  Primitive root generation program
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
extern crate num_bigint;
extern crate num_traits;
extern crate rand;

use super::prime;

use num_bigint::BigUint;
use num_traits::{One, Zero};
use rand::Rng;

use prime::{is_prime, random_prime, random_max, random_bit};
use super::super::crypto::sha256::*;

const RABIN_P: u32 = 25;
const OUTLEN: usize = 256; // SHA256

/**
 * Generation of the Probable Primes p and q Using an Approved Hash Function
 * (speified in FIPS 186-4 Appendix 1.1.2)
 */
#[allow(clippy::many_single_char_names)]
pub fn generate_pq(pb: usize, qb: usize, seedlen: usize) -> (bool, BigUint, BigUint) {
	let mut ctx = Sha256::new();
	let mut md: [u8; 32] = [0; 32];
	let mut seedb = vec![0; seedlen / 8].into_boxed_slice();
	let mut seedi: BigUint;
	let mut offset;
	let (mut ui, mut vi, mut wi, mut xi);
	let (mut p, mut q);

	// 1.Check that the (L, N) pair is in the list of acceptable (L, N pairs) (see Section 4.2). If the pair is not in the list, then return INVALID.
	if !((pb == 1024 && qb == 160)
		|| (pb == 2048 && qb == 224)
		|| (pb == 2048 && qb == 256)
		|| (pb == 3072 && qb == 256)) {
		return (false, BigUint::zero(), BigUint::zero());
	}
	// 2. If (seedlen < N), then return INVALID.
	if seedlen < qb {
		return (false, BigUint::zero(), BigUint::zero());
	}
	// 3. n = ceil(L ⁄ outlen) – 1. (outlen is the bit length of the hash function output block)
	let n = (pb + OUTLEN - 1) / OUTLEN - 1;
	// 4. b = L – 1 – (n ∗ outlen).
	let b = pb - 1 - (n * OUTLEN);

	'algorithm: loop {
		// 5. Get an arbitrary sequence of seedlen bits as the domain_parameter_seed.
		let mut rng = rand::thread_rng();
		for i in 0..seedlen / 8 {
			seedb[i] = rng.gen();
		}
		// 6. U = Hash (domain_parameter_seed) mod 2^(N–1).
		sha256_init(&mut ctx);
		sha256_update(&mut ctx, &seedb, seedb.len());
		sha256_final(&mut ctx, &mut md);
		seedi = BigUint::from_bytes_be(&md);
		ui = &seedi % (BigUint::one() << (qb - 1));
		// 7. q = 2^(N–1) + U + 1 – ( U mod 2).
		q = (BigUint::one() << (qb - 1)) + &ui + 1u32 - (&ui % 2u32);
		// 8. Test whether or not q is prime
		// 9. If q is not prime then go to step 5.
		if !is_prime(&q, RABIN_P) {
			continue 'algorithm;
		}
		// 10. offset = 1
		offset = 1;
		// 11. For counter = 0 to (4L – 1)
		for _counter in 0..(4 * pb) {
			// 11.1
			wi = BigUint::zero();
			for j in 0..(n + 1) {
				let ui = (&seedi + BigUint::from(offset + j)) % (BigUint::one() << seedlen);
				let uib = &ui.to_bytes_be();
				sha256_init(&mut ctx);
				sha256_update(&mut ctx, uib, uib.len());
				sha256_final(&mut ctx, &mut md);
				vi = BigUint::from_bytes_be(&md);
				// 11.2
				if j > 0 && j < n {
					vi <<= j * OUTLEN;
				} else if j == n {
					vi = (vi % (BigUint::one() << b)) << (n * OUTLEN);
				}
				wi += vi;
			}
			// 11.3. X = W + 2^(L-1)
			//  0 <= W < 2^(L-1), hence, 2^(L-1) <= X < 2^L
			xi = wi + (BigUint::one() << (pb - 1));
			// 11.4 c = X mod 2q.
			let ci = &xi % (2u32 * &q);
			// 11.5 p = X - (c - 1)
			p = &xi - (&ci - 1u32);
			// 11.6. If (p < 2^(L–1)), then go to step 11.9.
			if p >= (BigUint::one() << (pb - 1)) {
				// 11.7. Test whether or not p is prime.
				// 11.8. If p is determined to be prime, then return VALID.
				if is_prime(&p, RABIN_P) {
					return (true, p, q);
				}
			}
			// 11.9 offset = offset + n + 1.
			offset += n + 1;
		}
	}
}

/**
 * Unverifiable Generation of the Generator g
 * (speified in FIPS 186-4 Appendix 2.1)
 */
#[allow(clippy::many_single_char_names)]
pub fn generate_g(p: &BigUint, q: &BigUint) -> BigUint {
	// 1. Let e = (p - 1) / q
	let e = (p - 1u32) / q;
	// 2. Set h = any integer satisfying 1 < h < ( p – 1), such that h differs from any value previously tried.
	let mut h = random_max(p);

	let p_1 = p - 1u32;
	let mut g = BigUint::from(2u32);
	let dir = random_bit(32) & BigUint::one();
	while h < p_1 {
		// 3. Set g = h^e mod p
		g = h.modpow(&e, &p);
		// 4. If g = 1, go to step 2.
		if !g.is_one() {
			break;
		}
		if dir == BigUint::one() {
			h += 1u32;
		} else {
			h -= 1u32;
		}
	}
	// 5. return g.
	g
}

/**
 * Assurance of the Validity of the Generator
 * (speified in FIPS 186-4 Appendix 2.2)
 */
pub fn verify_g(p: &BigUint, q: &BigUint, g: &BigUint) -> bool {
	let two = BigUint::from(2u32);
	let p_1 = p - 1u32;

	if g < &two || g > &p_1 {
		return false;
	}
	let gv = g.modpow(&q, &p);
	gv == BigUint::one()
}

/**
 * Generation of the Probable Prime p and the Generator g.
 */
const BITLEN_MIN: usize = 128;
const MAX_LOOP: u32 = 20;

pub fn generate_pg(pb: usize) -> (bool, BigUint, BigUint) {
	let mut blen;
	let mut shift;
	let mut cloop;
	let (mut p, mut g);
	let (mut prm1, mut prm2);
	let (exprm1, exprm2, exprm3);
	let mut tmp;

	if pb < BITLEN_MIN {
		return (false, BigUint::zero(), BigUint::zero());
	}
	let hlen = pb / 2;

	// prime genaration such that phi(p) = p - 1 = prime1 * prime2
	'prime1: loop {
		cloop = 0;
		// prime1 genaration
		prm1 = random_prime(hlen);
		loop {
			// prime2 genaration
			prm2 = random_prime(hlen - 1);

			p = &prm1 * &prm2;
			blen = p.bits();

			shift = 0;
			while blen != pb {
				p <<= 1;
				blen = p.bits();
				shift += 1;
			}
			p += 1u32;
			// p - 1 = (prime1 * prime2 * 2^(2 * shift))

			if is_prime(&p, RABIN_P) {
				break 'prime1;
			}
			if cloop > MAX_LOOP {
				break;
			}
			cloop += 1;
		}
	}
	/* primitive root generation */
	// exprm1 = prm1 * 2^shift
	exprm1 = &prm1 << shift;
	// exprm2 = prm2 * 2^shift
	exprm2 = &prm2 << shift;
	// exprm3 = prm1 * prm2 * 2^(shift-1)
	if shift > 0 {
		exprm3 = (&prm1 * &prm2) << (shift - 1);
	} else {
		exprm3 = &prm1 * &prm2;
	}

	//g = BigUint::from(2u32);
	g = random_max(&p);
	loop {
		g += &BigUint::one();

		tmp = g.modpow(&exprm1, &p);
		if tmp.is_one() {
			continue;
		}
		tmp = g.modpow(&exprm2, &p);
		if tmp.is_one() {
			continue;
		}
		tmp = g.modpow(&exprm3, &p);
		if !tmp.is_one() {
			break;
		}
	}
	// verify g
	let gv = g.modpow(&(&p - BigUint::one()), &p);
	if gv != BigUint::one() {
		return (false, BigUint::zero(), BigUint::zero());
	}

	(true, p, g)
}
