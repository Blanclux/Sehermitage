/**
 * mdigest.rs
 * Message Digest Program
 *  wrtten by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
extern crate crypto;

use crypto::digest::Digest;
use crypto::sha1::*;
use crypto::sha2::*;
use std::env;
use std::time::SystemTime;

const DLEN: usize = 100 * 1024; // 100 KB
const COUNT: u32 = 1024;

static USAGE: &str = "usage: mdigest Message AlgID
   Message : Message String
   AlgID   : sha1 / sha256 / sha512\n";

fn hexdump(bytes: &[u8]) {
	println!("({} bytes)", bytes.len());
	for (i, b) in bytes.iter().enumerate() {
		print!("{:02.x} ", b);
		if (i + 1) % 16 == 0 {
			println!();
		}
	}
	println!();
}

fn main() {
	let args: Vec<String> = env::args().collect();

	if args.len() < 3 {
		println!("{}", USAGE);
		return;
	}
	let msg = &args[1];
	let alg = &args[2];

	let md: &dyn Digest;
	let (md_1, md_256, md_512);
	let mut digest: Vec<u8>;
	let mut alg_name = "SHA-256";

	match &alg[..] {
		"SHA1" | "sha1" => {
			alg_name = "SHA-1";
			md_1 = Sha1::new();
			md = &md_1 as &dyn Digest;
		}
		"SHA256" | "sha256" => {
			alg_name = "SHA-256";
			md_256 = Sha256::new();
			md = &md_256 as &dyn Digest;
		}
		"SHA512" | "sha512" => {
			alg_name = "SHA-512";
			md_512 = Sha512::new();
			md = &md_512 as &dyn Digest;
		}
		_ => {
			md_256 = Sha256::new();
			md = &md_256 as &dyn Digest;
		}
	}

	println!("\n> Digest algorithm : {}", alg_name);
	println!("> Digest size : {}", (*md).output_bits());
	println!("> Block size  : {}\n", (*md).block_size());
	let input = msg.as_bytes();
	print!("> Input: ");
	hexdump(input);
	println!();

	if alg_name == "SHA-256" {
		let mut md = Sha256::new();
		digest = vec![0; 32];
		md.reset();
		md.input(input);
		md.result(&mut digest);
	} else if alg_name == "SHA-512" {
		let mut md = Sha512::new();
		digest = vec![0; 64];
		md.reset();
		md.input(input);
		md.result(&mut digest);
	} else {
		let mut md = Sha1::new();
		digest = vec![0; 20];
		md.reset();
		md.input(input);
		md.result(&mut digest);
	}

	print!("> Digest: ");
	hexdump(&digest);

	println!("> Performance: ");
	let mut data: Box<[u8]> = Box::new([0; DLEN]);
	for i in 0..DLEN {
		data[i] = (i + 1) as u8;
	}
	if alg_name == "SHA-1" {
		// SHA-1
		let mut md = Sha1::new();
		digest = vec![0; 20];

		let s_time = SystemTime::now();
		md.reset();
		for _ in 0..COUNT {
			md.input(&data);
		}
		md.result(&mut digest);
		let e_time = s_time.elapsed().expect("Clock may have gone backwards");
		println!("SHA-1");
		println!(" time: {:?}", e_time);
		println!(" {} MiB/sec", 100.0 / e_time.as_secs_f32());
	} else if alg_name == "SHA-256" {
		// SHA-256
		let mut md = Sha256::new();
		digest = vec![0; 32];

		let s_time = SystemTime::now();
		md.reset();
		for _ in 0..COUNT {
			md.input(&data);
		}
		md.result(&mut digest);
		let e_time = s_time.elapsed().expect("Clock may have gone backwards");
		println!("SHA-256");
		println!(" time: {:?}", e_time);
		println!(" {} MiB/sec", 100.0 / e_time.as_secs_f32());
	} else {
		//SHA-512
		let mut md = Sha512::new();
		digest = vec![0; 64];

		let s_time = SystemTime::now();
		md.reset();
		for _ in 0..COUNT {
			md.input(&data);
		}
		md.result(&mut digest);
		let e_time = s_time.elapsed().expect("Clock may have gone backwards");
		println!("SHA-512");
		println!(" time: {:?}", e_time);
		println!(" {} MiB/sec", 100.0 / e_time.as_secs_f32());
	}
}
