//!
//! Vernam encryption
//! 
//  Written by blanclux
//  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
use clap::{App, Arg};
use rand::Rng;
use std::fs::File;
use std::io::prelude::*;
use std::io::{BufReader, BufWriter};
use std::time::SystemTime;

/// Block length (64)
const BLEN: usize = 64;
/// Key buffer size (2048)
const KEYBUF_SIZE: usize = 2048;
/// Buffer size (51200)
const BUFFER_SIZE: usize = 51200;

/// memory block copy 
fn memcpy(to: &mut [u8], ofs1: usize, from: &[u8], ofs2: usize, length: usize) {
	to[ofs1..(length + ofs1)].clone_from_slice(&from[ofs2..(length + ofs2)]);
}

/// Print hex data
#[allow(dead_code)]
#[allow(clippy::needless_range_loop)]
fn hexdump(bytes: &[u8], len: usize) {
	let mut size = bytes.len();
	if len != 0 {
		size = len;
	}
	for i in 0..size {
		print!("{:02.x} ", bytes[i]);
		if (i + 1) % 16 == 0 {
			println!();
		}
	}
	println!();
}

/// Vernam object
pub struct Vernam {
	enc_mode: u8, // 0: encryption / 1: decryption
	key_mode: u8, // 0: key generation / 1: key file read
	buf: [u8; BLEN],
	datbuf: [u8; BUFFER_SIZE],
	keybuf: [u8; KEYBUF_SIZE],
	datlen: usize, // Length of input data
	keylen: usize, // Length of key
	seed: String,
	keyfp: Option<std::fs::File>,
	inpfp: Option<std::fs::File>,
	outfp: Option<std::fs::File>,
	kp: usize,
	bcp: usize,
	bep: usize,
}

impl Vernam {
	fn new() -> Vernam {
		Vernam {
			enc_mode: 0,
			key_mode: 0,
			buf: [0; BLEN],
			datbuf: [0; BUFFER_SIZE],
			keybuf: [0; KEYBUF_SIZE],
			datlen: 0,
			keylen: 0,
			seed: "default seed for key generation".to_string(),
			keyfp: None,
			inpfp: None,
			outfp: None,
			kp: 0,
			bcp: 0,
			bep: 0,
		}
	}

	/// Key generation from seed
	fn gen_key(&mut self, seeds: String) {
		let mut seed = [0; 32];
		let sb = seeds.as_bytes();
		let len = if sb.len() < 32 { sb.len() } else { 32 };

		seed[0..len].copy_from_slice(&sb[..len]);

		let mut rng: rand::rngs::StdRng = rand::SeedableRng::from_seed(seed);
		for x in &mut self.keybuf {
			*x = rng.gen();
		}
		self.keylen = KEYBUF_SIZE;
	}

	fn read_key(&mut self, key: &mut [u8]) {
		if self.kp + BLEN > self.keylen {
			self.kp = 0;
		}
		memcpy(key, 0, &self.keybuf, self.bcp, BLEN);
		self.kp += BLEN;
	}

	fn read_keyfile(&mut self) {
		let mut reader = BufReader::new(self.keyfp.as_ref().unwrap());
		self.keylen = reader.read(&mut self.keybuf).unwrap();
	}

	fn read_data(&mut self) -> usize {
		let mut len;
		if self.bcp == 0 {
			len = self.inpfp.as_ref().unwrap().read(&mut self.datbuf).unwrap();
			self.bep = len;
		} else {
			len = self.bep - self.bcp;
		}
		len = if len >= BLEN { BLEN } else { len };
		memcpy(&mut self.buf, 0, &self.datbuf, self.bcp, len);
		len
	}

	fn write_data(&mut self, data: &[u8]) {
		let mut writer = BufWriter::new(self.outfp.as_ref().unwrap());

		memcpy(&mut self.datbuf, self.bcp, data, 0, data.len());
		self.bcp += data.len();
		if self.bcp >= self.bep {
			let _len = writer.write(&self.datbuf[..self.bcp]).unwrap();
			self.bcp = 0;
		}
	}

	fn xor(&self, out: &mut [u8], inp1: &[u8], inp2: &[u8], n: usize) {
		for i in 0..n {
			out[i] = inp1[i] ^ inp2[i];
		}
	}

	fn encode(&mut self) {
		let mut key: [u8; BLEN] = [0; BLEN];
		let mut buf: [u8; BLEN] = [0; BLEN];
		let count = 1 + self.datlen / BUFFER_SIZE;

		//println!("length = {}, loop = {}", self.datlen, count);
		for _i in 0..count {
			loop {
				let count = self.read_data();
				if count == 0 {
					break;
				}
				self.read_key(&mut key);
				self.xor(&mut buf, &self.buf, &key, BLEN);
				if count < BLEN {
					let mut data = vec![0; count];
					memcpy(&mut data, 0, &buf, 0, count);
					self.write_data(&data);
				} else {
					self.write_data(&buf);
				}
			}
		}
	}
}

/// 'vernam main
fn main() {
	let app = App::new("Vernam")
		.version("1.0")
		.arg(
			Arg::with_name("file")
				.short("f")
				.long("file")
				.value_name("FILE")
				.help("Read key from file")
				.takes_value(true),
		)
		.arg(
			Arg::with_name("seed")
				.short("s")
				.long("seed")
				.value_name("SEED")
				.help("Seed for key generation (1 - 32 characters)")
				.takes_value(true),
		)
		.arg(
			Arg::with_name("mode")
				.short("m")
				.long("mode")
				.value_name("MODE")
				.possible_value("enc")
				.possible_value("dec")
				.default_value("enc")
				.help("Encryption mode"),
		)
		.arg(
			Arg::with_name("inFile")
				.help("Input file")
				.required(true)
				.index(1),
		)
		.arg(
			Arg::with_name("outFile")
				.help("Output file")
				.required(true)
				.index(2),
		);
	let matches = app.get_matches();

	println!("< Vernam cipher >");
	let s_time = SystemTime::now();
	let mut vcnt = Vernam::new();

	let mode = matches.value_of("mode").unwrap();
	vcnt.enc_mode = if mode == "enc" { 0 } else { 1 };
	println!(" Mode: {}", mode);

	// -f option
	if matches.is_present("file") {
		let key_file = matches.value_of("file").unwrap().to_string();
		println!(" Key file: {}", key_file);
		vcnt.key_mode = 1;
		vcnt.keyfp = Some(File::open(key_file).expect("File not found"));
		vcnt.read_keyfile();
		//hexdump(&self.keybuf, vcnt.keylen);
	}
	// -s option
	else if matches.is_present("seed") {
		vcnt.seed = matches.value_of("seed").unwrap().to_string();
		println!(" Seed: {}", &vcnt.seed);
		vcnt.gen_key(vcnt.seed.clone());
		//hexdump(&self.keybuf, vcnt.keylen);
	} else {
		println!(" Seed: {}", &vcnt.seed);
		vcnt.gen_key(vcnt.seed.clone());
	}
	println!(" Key size = {}", vcnt.keylen);
	let kmode = if vcnt.key_mode == 0 {
		"Key generation"
	} else {
		"Key from file"
	};
	println!(" Key mode: {}", kmode);

	// Input file
	let inp_file = matches.value_of("inFile").unwrap().to_string();
	println!(" Input  file: {}", inp_file);
	let attr = std::fs::metadata(&inp_file).unwrap();
	vcnt.datlen = attr.len() as usize;
	vcnt.inpfp = Some(File::open(inp_file).expect("File not found"));
	// Onput file
	let out_file = matches.value_of("outFile").unwrap().to_string();
	println!(" Output file: {}", out_file);
	vcnt.outfp = Some(File::create(out_file).expect("File create error"));

	// Vernam enc/dec
	vcnt.encode();

	let e_time = s_time.elapsed().expect("Clock may have gone backwards");
	println!(" Execution time: {:?}", e_time);
}
