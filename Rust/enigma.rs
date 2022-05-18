//!
//! Enigma cipher
//! 
// Written by blanclux
// This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
use rand::prelude::*;
use std::env;
use std::io;
use std::io::Write;

/// Read one line
fn read<T: std::str::FromStr>() -> T {
    let mut s = String::new();
    std::io::stdin().read_line(&mut s).ok();
    s.trim().parse().ok().unwrap()
}

/// 'enigma' main
fn main() {
    let args: Vec<String> = env::args().collect();
    let (seed1, seed2, seed3): (u64, u64, u64);
    let plain;

    if args.len() == 3 {
        // usage: enigma text seed
        plain = args[1].clone();
        seed1 = args[2].parse().unwrap();
        seed2 = seed1 * 2;
        seed3 = seed1 % 2;
    } else {
        // Input text string
        print!("Text: ");
        io::stdout().flush().unwrap();
        plain = read();
        // Input seed
        print!("Seed 1: ");
        io::stdout().flush().unwrap();
        seed1 = read::<u64>();
        print!("Seed 2: ");
        io::stdout().flush().unwrap();
        seed2 = read::<u64>();
        print!("Seed 3: ");
        io::stdout().flush().unwrap();
        seed3 = read::<u64>();
    }

    // encrypt
    let mut enigma = Enigma::new();
    enigma.init(seed1, seed2, seed3);
    let enc = enigma.encode(plain);
    println!("Encrypt : {}", enc);

    // decrypt
    let mut enigma = Enigma::new();
    enigma.init(seed1, seed2, seed3);
    let dec = enigma.encode(enc);
    println!("Decrypt : {}", dec);
}

/// Random rotor generation
fn make_rotor(rotor: &mut Vec<char>, seed: u64) {
    let mut rng = StdRng::seed_from_u64(seed);

    rotor.shuffle(&mut rng);
}

/// Plug board
fn make_plug(plug: &mut [char], seed: u64) {
    let mut rng = StdRng::seed_from_u64(seed);

    let size = plug.len();
    let mut num: Vec<usize> = Vec::new();
    for i in 0..size {
        num.push(i);
    }
    let replace: Vec<usize> = num.choose_multiple(&mut rng, 6).cloned().collect();
    for index in (0..6).step_by(2) {
        plug.swap(replace[index as usize], replace[index + 1]);
    }
}

/// Finds an index to match the character
fn find_index(ary: &[char], ch: char) -> usize {
    ary.iter().position(|&x| x == ch).unwrap()
}

/// Enigma object
#[derive(Clone, PartialEq)]
pub struct Enigma {
    orig: Vec<char>,
    ch_num: usize,
    ch_num2: usize,
    rotor1: Vec<char>,
    rotor2: Vec<char>,
    rotor3: Vec<char>,
    reflect: Vec<char>,
    plug: Vec<char>,
}

impl Enigma {
    pub fn new() -> Enigma {
        // set alphabet characters
        let alphabet: Vec<char> = "abcdefghijklmnopqrstuvwxyz".chars().collect();
        let add_ch = vec![' ', '?', '.', ','];
        let orig = [alphabet, add_ch].concat();

        let ch_num = orig.len();
        let ch_num2 = ch_num * ch_num;
        let rotor1 = orig.clone();
        let rotor2 = orig.clone();
        let rotor3 = orig.clone();
        let reflect = orig.clone();
        let plug = orig.clone();

        Enigma {
            orig,
            ch_num,
            ch_num2,
            // make rotors
            rotor1,
            rotor2,
            rotor3,
            reflect,
            // make a Plug board
            plug,
        }
    }

    /// Initialization 
    pub fn init(&mut self, seed1: u64, seed2: u64, seed3: u64) {
        // make rotors
        make_rotor(&mut self.rotor1, seed1);
        make_rotor(&mut self.rotor2, seed2);
        make_rotor(&mut self.rotor3, seed3);
        make_rotor(&mut self.reflect, seed1 + seed2);
        // make a Plug board
        make_plug(&mut self.plug, seed1 + seed3);
    }

    /// Rotate
    fn rotate(&mut self, idx: usize) {
        self.rotor1.rotate_right(1);
        if idx % self.ch_num == 0 && idx / self.ch_num != 0 {
            self.rotor2.rotate_right(1);
        }
        if idx % self.ch_num2 == 0 && idx / self.ch_num2 != 0 {
            self.rotor3.rotate_right(1);
        }
    }

    /// Enigma process
    pub fn encode(&mut self, string: String) -> String {
        let mut code_string = "".to_string();
        for (idx, ch) in string.chars().enumerate() {
            code_string += &self.encode_character(ch);
            self.rotate(idx);
        }

        code_string
    }

    /// Encode a character
    fn encode_character(&mut self, ch: char) -> String {
        // Outward
        let mut chr = self.plug[find_index(&self.orig, ch)];
        chr = self.rotor1[find_index(&self.orig, chr)];
        chr = self.rotor2[find_index(&self.orig, chr)];
        chr = self.rotor3[find_index(&self.orig, chr)];

        // Reflector
        if find_index(&self.reflect, chr) % 2 == 0 {
            chr = self.reflect[find_index(&self.reflect, chr) + 1];
        } else {
            chr = self.reflect[find_index(&self.reflect, chr) - 1];
        }
        // Return path
        chr = self.orig[find_index(&self.rotor3, chr)];
        chr = self.orig[find_index(&self.rotor2, chr)];
        chr = self.orig[find_index(&self.rotor1, chr)];
        chr = self.orig[find_index(&self.plug, chr)];

        chr.to_string()
    }
}

impl Default for Enigma {
    fn default() -> Self {
        Enigma::new()
    }
}
