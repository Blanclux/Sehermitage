//!
//! Password hash program (bcrypt version)
//!
//  Written by blanclux
//  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
//
extern crate crypto;

use crypto::bcrypt::bcrypt;
use radix64::CRYPT;
use rand::Rng;
use std::{env, error, fmt};

fn hex_dump(bytes: &[u8]) {
    println!("({} bytes)", bytes.len());
    for (i, b) in bytes.iter().enumerate() {
        print!("{:02.x} ", b);
        if (i + 1) % 16 == 0 {
            println!();
        }
    }
    println!();
}

pub fn bcrypt_encode(passwd: &str, salt: &[u8], cost: u32) -> String {
    let mut hash = [0u8; 24];

    bcrypt(cost, salt, passwd.as_bytes(), &mut hash[..]);

    let mut result = "$2a$".to_string();
    // cost parameter
    result.push_str(&cost.to_string());
    result.push('$');
    // salt: Radix-64 encode (22 chars)
    result.push_str(&CRYPT.encode(&salt));
    result.push('$');
    // digest: Radix-64 encode (31 chars)
    result.push_str(&CRYPT.encode(&hash));
    result.push('$');

    result
}

/// Compares a password against the result of a `bcrypt_format`.
pub fn bcrypt_check(passwd: &str, hash: &str) -> Result<(), CheckError> {
    let mut iter = hash.split('$');

    // Check that there are no characters before the first "$"
    if iter.next() != Some("") {
        return Err(CheckError::InvalidFormat);
    }
    // Check the name
    if iter.next() != Some("2a") {
        return Err(CheckError::InvalidFormat);
    }
    // Check the cost parameter
    let cost: u32 = match iter.next() {
        Some(cost_str) => cost_str.parse::<u32>().unwrap(),
        None => return Err(CheckError::InvalidFormat),
    };
    // Salt
    let salt = match iter.next() {
        Some(salt_str) => match CRYPT.decode(salt_str) {
            Ok(salt) => salt,
            Err(_) => return Err(CheckError::InvalidFormat),
        },
        None => return Err(CheckError::InvalidFormat),
    };
    // Hashed value
    let hash = match iter.next() {
        Some(hash_str) => match CRYPT.decode(hash_str) {
            Ok(hash) => hash,
            Err(_) => return Err(CheckError::InvalidFormat),
        },
        None => return Err(CheckError::InvalidFormat),
    };
    // Make sure that the input ends with a "$"
    if iter.next() != Some("") {
        return Err(CheckError::InvalidFormat);
    }
    // Make sure there is no trailing data after the final "$"
    if iter.next() != None {
        return Err(CheckError::InvalidFormat);
    }

    let mut output = vec![0u8; 24];
    bcrypt(cost, &salt, passwd.as_bytes(), &mut output);
    if output == hash {
        Ok(())
    } else {
        Err(CheckError::HashMismatch)
    }
}

/// `bcrypt_check` error
#[derive(Debug, Copy, Clone, Eq, PartialEq)]
pub enum CheckError {
    // Password hash mismatch, e.g. due to the incorrect password.
    HashMismatch,
    // Invalid format of the hash string.
    InvalidFormat,
}

impl fmt::Display for CheckError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        f.write_str(match *self {
            CheckError::HashMismatch => "password hash mismatch",
            CheckError::InvalidFormat => "invalid `hashed_value` format",
        })
    }
}

impl error::Error for CheckError {
    fn description(&self) -> &str {
        match *self {
            CheckError::HashMismatch => "password hash mismatch",
            CheckError::InvalidFormat => "invalid `hashed_value` format",
        }
    }
}

static USAGE: &str = "usage: pwdhash Password Cost [Salt]\n";

fn main() {
    let args: Vec<String> = env::args().collect();
    if args.len() != 3 && args.len() != 4 {
        println!("{}", USAGE);
        return;
    }

    let passwd = &args[1];
    let cost: u32 = args[2].parse().unwrap();
    let mut salt = vec![0u8; 16];
    if args.len() == 4 {
        let in_salt = &args[3];
        for (i, x) in in_salt.as_bytes().iter().enumerate() {
            if i > 15 {
                break;
            }
            salt[i] = *x;
        }
    } else {
        let mut rng = rand::thread_rng();
        salt = rng.gen_iter::<u8>().take(16).collect();
    }

    let mut output = [0u8; 24];
    print!("Salt ");
    hex_dump(&salt);
    bcrypt(cost, &salt, passwd.as_bytes(), &mut output);
    print!("Digest ");
    hex_dump(&output);
    println!("\nEncoded string");
    let out = bcrypt_encode(passwd, &salt, cost);
    println!("{}", out);
    assert_eq!(bcrypt_check(passwd, &out), Ok(()));
    // Illegal password
    assert_ne!(bcrypt_check("passwd2", &out), Ok(()));
}
