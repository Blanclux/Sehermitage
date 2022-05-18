/**
 * scipher.rs
 * AES Cipher Program
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
extern crate crypto;

use std::env;
use std::fs::File;
use std::io::prelude::*;
use std::io::BufWriter;
use std::iter::repeat;

use crypto::symmetriccipher::{Decryptor, Encryptor};
use crypto::blockmodes::{CbcDecryptor, CbcEncryptor, PkcsPadding};
use crypto::aessafe::*;
use crypto::buffer::BufferResult::{BufferOverflow, BufferUnderflow};
use crypto::buffer::{RefReadBuffer, RefWriteBuffer, WriteBuffer};

struct Key {
    pub mode: &'static str,
    pub keylen: usize,
    pub ivlen: usize,
    pub key: &'static [u8],
    pub iv: &'static [u8],
}

const KEY_SEL: usize = 2;     // default: AES-256-CBC

static KEY_PARAM: [Key; 3] = [
    Key {
        mode: "AES-128-CBC",
        keylen: 16,
        ivlen: 16,
        key: &[
            0x2b, 0x7e, 0x15, 0x16, 0x28, 0xae, 0xd2, 0xa6, 0xab, 0xf7, 0x15, 0x88, 0x09, 0xcf,
            0x4f, 0x3c,
        ],
        iv: &[
            0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88, 0x99, 0xAA, 0xBB, 0xCC, 0xDD,
            0xEE, 0xFF,
        ],
    },
    Key {
        mode: "AES-192-CBC",
        keylen: 24,
        ivlen: 16,
        key: &[
            0x8e, 0x73, 0xb0, 0xf7, 0xda, 0x0e, 0x64, 0x52, 0xc8, 0x10, 0xf3, 0x2b, 0x80, 0x90,
            0x79, 0xe5, 0x62, 0xf8, 0xea, 0xd2, 0x52, 0x2c, 0x6b, 0x7b,
        ],
        iv: &[0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77],
    },
    Key {
        mode: "AES-256-CBC",
        keylen: 32,
        ivlen: 16,
        key: &[
            0x60, 0x3d, 0xeb, 0x10, 0x15, 0xca, 0x71, 0xbe, 0x2b, 0x73, 0xae, 0xf0, 0x85, 0x7d,
            0x77, 0x81, 0x1f, 0x35, 0x2c, 0x07, 0x3b, 0x61, 0x08, 0xd7, 0x2d, 0x98, 0x10, 0xa3,
            0x09, 0x14, 0xdf, 0xf4,
        ],
        iv: &[
            0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88, 0x99, 0xAA, 0xBB, 0xCC, 0xDD,
            0xEE, 0xFF,
        ],
    },
];

static USAGE: &str = "usage: scipher {-e | -d} inFile outFile
  {-e | -d}: -e: encrypt / -d: decrypt
  inFile : plain text file for -e / encrypted file for -d
  outFile: encrypted file for -e / decrypted file for -d\n";

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

#[allow(clippy::match_wild_err_arm)]
fn main() {
    let args: Vec<String> = env::args().collect();

    if args.len() != 4 {
        println!("{}", USAGE);
        return;
    }
    let encode: bool;
    if args[1] == "-e" {
        encode = true;
    } else if args[1] == "-d" {
        encode = false;
    } else {
        println!("{}", USAGE);
        return;
    }
    let infile = &args[2];
    let outfile = &args[3];

    println!("{}", KEY_PARAM[KEY_SEL].mode);
    let key = KEY_PARAM[KEY_SEL].key;
    let iv = KEY_PARAM[KEY_SEL].iv;
    println!("Key");
    hexdump(key);
    println!("IV");
    hexdump(iv);

    if encode { //encrypt
        let mut fr = File::open(infile).expect("file not found");
        let mut data = String::new();
        fr.read_to_string(&mut data)
            .expect("something went wrong reading the file");
        let plain = data.as_bytes();

        //let aes_enc = AesSafe128Encryptor::new(&key);
        //let aes_enc = AesSafe192Encryptor::new(&key);
        let aes_enc = AesSafe256Encryptor::new(key);
        let mut enc = CbcEncryptor::new(aes_enc, PkcsPadding, iv.to_vec());

        let mut buff_in = RefReadBuffer::new(plain);
        let mut encdata: Vec<u8> = repeat(0).take(plain.len() + 16).collect();
        let mut buff_out = RefWriteBuffer::new(&mut encdata);
        println!("read data from \"{}\":", infile);
        hexdump(plain);

        match enc.encrypt(&mut buff_in, &mut buff_out, true) {
            Ok(BufferUnderflow) => {}
            Ok(BufferOverflow) => panic!("Encryption not completed"),
            Err(_) => panic!("Error"),
        }
        let enc_size = buff_out.position();
        println!("write data to \"{}\":", outfile);
        hexdump(&encdata[..enc_size]);

        let mut fw = BufWriter::new(File::create(outfile).unwrap());
        fw.write_all(&encdata[..enc_size])
            .expect("something went wrong writing the file");

    } else {    // decrypt
        // read the input file
        let mut fr = File::open(infile).unwrap();
        let mut buffer = Vec::new();
        let enc_size = fr.read_to_end(&mut buffer).unwrap();
        println!("read data from \"{}\":", infile);
        hexdump(&buffer);

        //let aes_dec = AesSafe128Decryptor::new(&key);
        //let aes_dec = AesSafe192Decryptor::new(&key);
        let aes_dec = AesSafe256Decryptor::new(key);
        let mut dec = CbcDecryptor::new(aes_dec, PkcsPadding, iv.to_vec());
        let mut decdata: Vec<u8> = repeat(0).take(enc_size).collect();
        let mut buff_in = RefReadBuffer::new(&buffer);
        let mut buff_out = RefWriteBuffer::new(&mut decdata);

        match dec.decrypt(&mut buff_in, &mut buff_out, true) {
            Ok(BufferUnderflow) => {}
            Ok(BufferOverflow) => panic!("Decryption not completed"),
            Err(_) => panic!("Error"),
        }
        let dec_size = buff_out.position();
        println!("write data to \"{}\":", outfile);
        hexdump(&decdata[..dec_size]);

        // write the decrypted data
        let mut fw = BufWriter::new(File::create(outfile).unwrap());
        fw.write_all(&decdata[..dec_size])
            .expect("something went wrong writing the file");
    }
}
