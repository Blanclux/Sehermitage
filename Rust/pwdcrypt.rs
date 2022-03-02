/**
 * pwdcrypt.rs
 *  Password Cipher Program
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
extern crate crypto;

use std::env;
use std::fs::File;
use std::io::prelude::*;
use std::io::BufWriter;
use std::iter::repeat;

use crypto::aessafe::*;
use crypto::blockmodes::{CbcDecryptor, CbcEncryptor, PkcsPadding};
use crypto::buffer::BufferResult::{BufferOverflow, BufferUnderflow};
use crypto::buffer::{RefReadBuffer, RefWriteBuffer, WriteBuffer};
use crypto::symmetriccipher::{Decryptor, Encryptor};

use crypto::hmac::*;
use crypto::mac::Mac;
use crypto::pbkdf2;
use crypto::sha2::Sha256;

/* Count */
const COUNT: u32 = 8;
/* Salt */
const SALT: &[u8] = b"salt";

static IV: [u8; 16] = [
    0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88, 0x99, 0xAA, 0xBB, 0xCC, 0xDD, 0xEE, 0xFF,
];

static USAGE: &str = "usage: pwdcrypt {-e | -d} password inFile outFile\n";

#[allow(dead_code)]
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

    if args.len() != 5 {
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
    let passwd = &args[2].as_bytes();
    let infile = &args[3];
    let outfile = &args[4];

    let mut key = [0u8; 32];
    let mut hmac = Hmac::new(Sha256::new(), &passwd[..]);
    hmac.reset();
    pbkdf2::pbkdf2::<Hmac<Sha256>>(&mut hmac, SALT, COUNT, &mut key[..]);
    //hexdump(&key);

    if encode {
        //encrypt
        let mut fr = File::open(infile).expect("file not found");
        let mut data = String::new();
        fr.read_to_string(&mut data)
            .expect("something went wrong reading the file");
        let plain = data.as_bytes();

        let aes_enc = AesSafe256Encryptor::new(&key);
        let mut enc = CbcEncryptor::new(aes_enc, PkcsPadding, IV.to_vec());

        let mut buff_in = RefReadBuffer::new(plain);
        let mut encdata: Vec<u8> = repeat(0).take(plain.len() + 16).collect();
        let mut buff_out = RefWriteBuffer::new(&mut encdata);
        println!(" Encrypt the data from \"{}\".", infile);
        //hexdump(&plain);}

        match enc.encrypt(&mut buff_in, &mut buff_out, true) {
            Ok(BufferUnderflow) => {}
            Ok(BufferOverflow) => panic!("Encryption not completed"),
            Err(_) => panic!("Encrypt Error!"),
        }
        let enc_size = buff_out.position();
        println!(" Write data to \"{}\".", outfile);
        //hexdump(&encdata[..enc_size]);

        let mut fw = BufWriter::new(File::create(outfile).unwrap());
        fw.write_all(&encdata[..enc_size])
            .expect("something went wrong writing the file");
    } else {
        // decrypt
        // read the input file
        let mut fr = File::open(infile).unwrap();
        let mut buffer = Vec::new();
        let enc_size = fr.read_to_end(&mut buffer).unwrap();
        println!(" Decrypt the data from \"{}\".", infile);
        //hexdump(&buffer);

        let aes_dec = AesSafe256Decryptor::new(&key);
        let mut dec = CbcDecryptor::new(aes_dec, PkcsPadding, IV.to_vec());
        let mut decdata: Vec<u8> = repeat(0).take(enc_size).collect();
        let mut buff_in = RefReadBuffer::new(&buffer);
        let mut buff_out = RefWriteBuffer::new(&mut decdata);

        match dec.decrypt(&mut buff_in, &mut buff_out, true) {
            Ok(BufferUnderflow) => {}
            Ok(BufferOverflow) => panic!("Decryption not completed"),
            Err(_) => panic!("Decrypt Error!"),
        }
        let dec_size = buff_out.position();
        println!(" Write data to \"{}\".", outfile);
        //hexdump(&decdata[..dec_size]);

        // write the decrypted data
        let mut fw = BufWriter::new(File::create(outfile).unwrap());
        fw.write_all(&decdata[..dec_size])
            .expect("something went wrong writing the file");
    }
}
