//!
//! Prime number generation program
//!
//  written by blanclux
//  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
//
extern crate num_bigint;
extern crate num_traits;

use num_bigint::{BigUint, RandBigInt};
use num_traits::{One, Zero};

const RABIN_P: u32 = 25;

const SMALL_PRIMES: [u32; 168] = [
	2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97,
	101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193,
	197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 277, 281, 283, 293, 307,
	311, 313, 317, 331, 337, 347, 349, 353, 359, 367, 373, 379, 383, 389, 397, 401, 409, 419, 421,
	431, 433, 439, 443, 449, 457, 461, 463, 467, 479, 487, 491, 499, 503, 509, 521, 523, 541, 547,
	557, 563, 569, 571, 577, 587, 593, 599, 601, 607, 613, 617, 619, 631, 641, 643, 647, 653, 659,
	661, 673, 677, 683, 691, 701, 709, 719, 727, 733, 739, 743, 751, 757, 761, 769, 773, 787, 797,
	809, 811, 821, 823, 827, 829, 839, 853, 857, 859, 863, 877, 881, 883, 887, 907, 911, 919, 929,
	937, 941, 947, 953, 967, 971, 977, 983, 991, 997,
];

/// Find the next prime number
pub fn find_prime(mut n: BigUint, ntests: u32) -> BigUint {
	// If the input is even, it should be made odd.
	if &n % 2u32 == BigUint::zero() {
		n += 1u32;
	}
	let two: BigUint = BigUint::from(2u32);
	while !is_prime(&n, ntests) {
		n += &two;
	}
	n
}

/// prime check (Rabin method)
pub fn is_prime(n: &BigUint, ntests: u32) -> bool {
	for i in SMALL_PRIMES.iter() {
		if n % i == BigUint::zero() {
			if n == &(BigUint::from(*i)) {
				return true;
			}
			return false;
		}
	}
	let (d, r) = decompose(n);
	let mut rng = rand::thread_rng();
	let two: BigUint = BigUint::from(2u32);
	for _ in 0..ntests {
		let a: BigUint = rng.gen_biguint_range(&two, &(n - 2u16));
		if trial_composite(n, &d, r, &a) {
			return false;
		}
	}
	true
}

#[allow(clippy::many_single_char_names)]
fn trial_composite(n: &BigUint, d: &BigUint, r: usize, a: &BigUint) -> bool {
	let mut x = a.modpow(d, n);
	if (x == BigUint::one()) || (x == (n - 1u32)) {
		return false;
	}
	let two = BigUint::from(2u32);
	for i in 0..(r - 1) {
		let e = d * (&two << i);
		x = a.modpow(&e, n);
		if n - 1u32 == x {
			return false;
		}
	}
	true
}

fn decompose(n: &BigUint) -> (BigUint, usize) {
	// Split number such that
	// n = d*2^r + 1
	let mut d = n - 1u32;
	let mut r: usize = 0;
	while (&d % 2u32).is_zero() {
		r += 1;
		d /= 2u32;
	}
	(d, r)
}

/// Random number in 0 < r < limit
pub fn random_max(limit: &BigUint) -> BigUint {
	let mut rng = rand::thread_rng();

	rng.gen_biguint_range(&BigUint::one(), limit)
}

/// Random number of specified bits length
pub fn random_bit(size: usize) -> BigUint {
	let mut limit = BigUint::one();
	limit = &limit << size;
	limit = &limit - 1u32;

	random_max(&limit)
}

/// Random prime generator
pub fn random_prime(bits: usize) -> BigUint {
	if bits == 1 {
		return BigUint::one();
	}

	let mut p = random_bit(bits);
	let mut s = BigUint::one();
	s <<= bits - 1; // Set MSB
	p |= s;
	p |= BigUint::one(); // Set odd number

	let dir = random_bit(32) & BigUint::one();
	while !is_prime(&p, RABIN_P) {
		if dir == BigUint::one() {
			p += 2u32;
			if p.bits() != bits as u64 {
				p = BigUint::one();
				p <<= bits - 1;
				p += 1u32; // 100.....1
			}
		} else {
			p -= 2u32;
			if p.bits() != bits as u64 {
				p = BigUint::one();
				p <<= bits;
				p -= 1u32; // 111.....1
			}
		}
	}
	p
}

/// Random prime generator (min < rand < max)
pub fn random_prime_range(max: &BigUint, min: &BigUint) -> BigUint {
	let mut p_max = max.clone();
	let mut p_min = min.clone();

	if p_max < p_min {
		return BigUint::from(2u32); // smallest prime 2
	}
	if p_max < BigUint::from(2u32) {
		return BigUint::from(2u32);
	}
	if p_max == p_min && (&p_max & BigUint::one() == BigUint::zero()) {
		return BigUint::from(2u32);
	}

	if &p_max & BigUint::one() == BigUint::zero() {
		// if p_max is even, +1
		p_max += 1u32;
	}
	if &p_min & BigUint::one() == BigUint::zero() {
		// if p_min is even, +1
		if p_min.is_zero() {
			p_min = BigUint::from(2u32);
		} else {
			p_min += 1u32;
		}
	}

	let mut c = (&p_max - &p_min) / BigUint::from(2u32); // c can't be smaller than 1
	let mut p;
	loop {
		p = random_max(&c);
		p += &p_min;
		p |= BigUint::one();
		if p <= p_max {
			break;
		}
	}

	// prime check
	let dir = random_bit(32) & BigUint::one();
	loop {
		let is_prime = is_prime(&p, RABIN_P);
		if is_prime {
			break;
		}
		c -= BigUint::one();
		if c.is_zero() {
			panic!("random_prime_range error!");
		}
		if dir == BigUint::one() {
			p += 2u32;
			if p > p_max {
				p = p_min.clone();
			}
		} else {
			p -= 2u32;
			if p < p_min || p.is_one() {
				p = p_max.clone();
			}
		}
	}
	p
}
