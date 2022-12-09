//!
//! Password manager
//!
//  written by blanclux
//  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
//

use base64;
use chrono::prelude::*;
use serde::{Deserialize, Serialize};

use rand::Rng;
use regex::Regex;
use std::{
    collections::HashMap, env, fs::File, io::prelude::*, io::BufReader, io::Result, iter::repeat,
};

use crypto::{
    aessafe::*,
    blockmodes::{CbcDecryptor, CbcEncryptor, PkcsPadding},
    buffer::BufferResult::{BufferOverflow, BufferUnderflow},
    buffer::{RefReadBuffer, RefWriteBuffer, WriteBuffer},
    digest::Digest,
    hmac::*,
    mac::Mac,
    pbkdf2,
    sha2::Sha256,
    symmetriccipher::{Decryptor, Encryptor},
};

/// Count
const COUNT: u32 = 8;
/// Salt
const SALT: &[u8] = b"salt";
/// Initial vector
static IV: [u8; 16] = [
    0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88, 0x99, 0xAA, 0xBB, 0xCC, 0xDD, 0xEE, 0xFF,
];
// Password hash value
static PWDHASH: [u8; 32] = [
    // for "security"
    0x5d, 0x2d, 0x3c, 0xeb, 0x7a, 0xbe, 0x55, 0x23, 0x44, 0x27, 0x6d, 0x47, 0xd3, 0x6a, 0x81, 0x75,
    0xb7, 0xae, 0xb2, 0x50, 0xa9, 0xbf, 0x0b, 0xf0, 0x0e, 0x85, 0x0c, 0xd2, 0x3e, 0xcf, 0x2e, 0x43,
];
/// Available password characters
const CHARSET: &[u8] = b"ABCDEFGHIJKLMNOPQRSTUVWXYZ\
                         abcdefghijklmnopqrstuvwxyz\
                         0123456789)(*&^%$#@!~";

pub struct PwdMng {
    dir: String,
    jsonfile: String,
    secret_key: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
struct Passwd {
    id: String,
    passwd: String,
    date: String,
}

type PwdDB = HashMap<String, Passwd>;

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

/// read json file
fn read_file(jsonfile: &str) -> PwdDB {
    let file = File::open(jsonfile).unwrap();
    let reader = BufReader::new(file);

    let pwddb: PwdDB = serde_json::from_reader(reader).unwrap();
    return pwddb;
}

/// write json file
fn write_file(jsonfile: &str, pwddb: PwdDB) -> Result<()> {
    // serialized
    let serialized: String = serde_json::to_string(&pwddb).unwrap();

    // write
    let mut file = File::create(jsonfile.clone())?;
    file.write_all(serialized.as_bytes())?;
    Ok(())
}

/// Read one line

fn read<T: std::str::FromStr>() -> T {
    let mut s = String::new();
    std::io::stdin().read_line(&mut s).ok();
    s.trim().parse().ok().unwrap()
}

/// Read one line and splits with whitespace
fn read_vec<T: std::str::FromStr>() -> Vec<T> {
    let mut s = String::new();
    std::io::stdin().read_line(&mut s).ok();
    s.trim()
        .split_whitespace()
        .map(|e| e.parse().ok().unwrap())
        .collect()
}

/*
/// use termion;
fn read_passwd() -> String {
    let stdout = stdout();
    let mut stdout = stdout.lock();
    let stdin = stdin();
    let mut stdin = stdin.lock();

    stdout.write_all(b"Password: ").unwrap();
    stdout.flush().unwrap();

    let pass = stdin.read_passwd(&mut stdout);

    if let Ok(Some(pass)) = pass {
        stdout.write_all(pass.as_bytes()).unwrap();
        stdout.write_all(b"\n").unwrap();
    } else {
        stdout.write_all(b"Error\n").unwrap();
    }
}
*/

impl PwdMng {
    pub fn new(dir: String) -> Self {
        PwdMng {
            dir: dir.clone(),
            jsonfile: (dir.clone() + "/password.json").to_string(),
            secret_key: "".to_string(),
        }
    }

    pub fn new_file(dir: String, file: String) -> Self {
        PwdMng {
            dir: dir.clone(),
            jsonfile: dir + &file,
            secret_key: "".to_string(),
        }
    }

    /// Create a password database
    fn create(&self) -> bool {
        let pwddb = HashMap::new();

        let rv = write_file(&self.jsonfile, pwddb);
        match rv {
            Ok(_) => true,
            Err(e) => {
                println!("{}", e);
                false
            }
        }
    }

    /// Get databese file names
    pub fn get_dbfile(&self) -> (String, String) {
        (self.dir.clone(), self.jsonfile.clone())
    }

    /// Check password duplication
    fn check(&self, npwd: &str) -> bool {
        let mut found = false;
        let pwddb = read_file(&self.jsonfile);

        for (key, val) in pwddb.iter() {
            let cpwd = self.decrypt(&val.passwd);

            if npwd == &*cpwd {
                found = true;
                println!("Password {} is used already for {}.", npwd, key);
            }
        }
        if !found {
            println!("Not found.");
        }
        found
    }

    /// Password generation
    pub fn pwd_gen(&self) -> String {
        print!("Password length (default: 10) = ");
        std::io::stdout().flush().unwrap();
        let length: String = read();
        let mut pwd_len = length.parse().unwrap_or(10);
        if pwd_len < 4 {
            println!("Password length must be >= 4. Default value 10 is used.");
            pwd_len = 10;
        }

        let mut rng = rand::thread_rng();
        let mut password: String;
        loop {
            password = (0..pwd_len)
                .map(|_| {
                    let idx = rng.gen_range(0..CHARSET.len());
                    CHARSET[idx] as char
                })
                .collect();

            if self.pwd_check(&password) {
                break;
            }
        }
        password
    }

    #[allow(unused_assignments)]
    fn pwd_check(&self, pwd: &str) -> bool {
        let mut rv = false;

        let mut re = Regex::new(r"^[a-zA-Z]").unwrap(); // start with [a-zA-Z]
        let mut test = re.find(pwd);
        rv = match test {
            Some(_x) => true,
            None => return false,
        };
        re = Regex::new(r"[a-z]+").unwrap();
        test = re.find(pwd);
        rv = match test {
            Some(_x) => true,
            None => return false,
        };
        re = Regex::new(r"[A-Z]+").unwrap();
        test = re.find(pwd);
        rv = match test {
            Some(_x) => true,
            None => return false,
        };
        re = Regex::new(r"[0-9]+").unwrap();
        test = re.find(pwd);
        rv = match test {
            Some(_x) => true,
            None => return false,
        };
        re = Regex::new(r"[*&^%$#@!~]").unwrap();
        test = re.find(pwd);
        rv = match test {
            Some(_x) => true,
            None => false,
        };
        rv
    }

    /// Display password list
    fn list(&self) -> bool {
        let pwddb = read_file(&self.jsonfile);

        for (key, value) in pwddb.iter() {
            println!("{}:", key);
            print!("\t");
            print!("{}", value.id);
            print!("\t");
            print!("{}", value.passwd);
            print!("\t");
            println!("{}", value.date);
        }
        true
    }

    /// get from json file
    fn get(&self, key: &str) -> Option<Passwd> {
        let pwddb = read_file(&self.jsonfile);
        let pwd = pwddb.get(key);

        match pwd {
            Some(pwd) => {
                let decpwd = self.decrypt(&pwd.passwd);
                let pwd = pwd.clone();
                Some(Passwd {
                    id: pwd.id,
                    passwd: decpwd,
                    date: pwd.date,
                })
            }
            None => None,
        }
    }

    /// Add new password
    fn add(&self, key: &str, id: &str, value: &str) -> bool {
        let mut pwddb = read_file(&self.jsonfile);
        let date = format!("{}", Local::today().format("%Y.%m.%d"));

        let pwd = Passwd {
            id: id.to_string(),
            passwd: self.encrypt(value),
            date: date,
        };
        pwddb.insert(key.to_string(), pwd);

        let rv = write_file(&self.jsonfile, pwddb);
        match rv {
            Ok(_) => true,
            Err(e) => {
                eprintln!("{}", e);
                false
            }
        }
    }

    /// Delete entry
    fn del(&self, key: &str) -> bool {
        let mut pwddb = read_file(&self.jsonfile);

        let rv = pwddb.remove(&key.to_string());
        match rv {
            None => {
                eprintln!("Specified name not found!");
                return false;
            }
            _ => {
                let rv = write_file(&self.jsonfile, pwddb);
                match rv {
                    Ok(_) => true,
                    Err(e) => {
                        eprintln!("{}", e);
                        false
                    }
                }
            }
        }
    }

    /// Update entry
    fn update(&self, key: &str) -> bool {
        let mut pwddb = read_file(&self.jsonfile);
        let date = format!("{}", Local::today().format("%Y.%m.%d"));

        let pwd = pwddb.get(key);
        let cid: String;
        match pwd {
            Some(pwd) => {
                cid = pwd.id.clone();
                pwd.passwd.clone()
            }
            None => {
                eprintln!("ID \"{}\": Not found", key);
                return false;
            }
        };
        println!("ID: {}", cid);
        print!("New password: ");
        std::io::stdout().flush().unwrap();
        let npwd: String = read();
        if npwd.is_empty() {
            return true; // No operation
        }

        let pwd = Passwd {
            id: cid,
            passwd: self.encrypt(&npwd),
            date,
        };
        pwddb.insert(key.to_string(), pwd);

        let rv = write_file(&self.jsonfile, pwddb);
        match rv {
            Ok(_) => true,
            Err(e) => {
                eprintln!("{}", e);
                false
            }
        }
    }

    /// Encryption
    fn encrypt(&self, data: &str) -> String {
        let seckey = self.secret_key.as_bytes();
        let mut hmac = Hmac::new(Sha256::new(), seckey);
        hmac.reset();

        let mut key = [0u8; 32];
        pbkdf2::pbkdf2::<Hmac<Sha256>>(&mut hmac, SALT, COUNT, &mut key[..]);

        let plain = data.as_bytes();
        let buf_len = 16 * (plain.len() / 16 + 1);

        let aes_enc = AesSafe256Encryptor::new(&key);
        let mut enc = CbcEncryptor::new(aes_enc, PkcsPadding, IV.to_vec());

        let mut buff_in = RefReadBuffer::new(plain);
        let mut encdata: Vec<u8> = repeat(0).take(buf_len).collect();
        let mut buff_out = RefWriteBuffer::new(&mut encdata);

        match enc.encrypt(&mut buff_in, &mut buff_out, true) {
            Ok(BufferUnderflow) => {}
            Ok(BufferOverflow) => panic!("Encryption not completed"),
            Err(_) => panic!("Encrypt Error!"),
        }

        // Base64 encode
        let pwdtxt = base64::encode(encdata);
        pwdtxt
    }

    /// Decryption
    fn decrypt(&self, data: &str) -> String {
        // Base64 decode
        let encdata = base64::decode(data).unwrap();
        let enc_size = encdata.len();

        let mut key = [0u8; 32];
        let seckey = self.secret_key.as_bytes();
        let mut hmac = Hmac::new(Sha256::new(), seckey);
        hmac.reset();
        pbkdf2::pbkdf2::<Hmac<Sha256>>(&mut hmac, SALT, COUNT, &mut key[..]);

        let aes_dec = AesSafe256Decryptor::new(&key);
        let mut dec = CbcDecryptor::new(aes_dec, PkcsPadding, IV.to_vec());

        let mut decdata: Vec<u8> = repeat(0).take(enc_size).collect();
        let mut buff_in = RefReadBuffer::new(&encdata);
        let mut buff_out = RefWriteBuffer::new(&mut decdata);

        match dec.decrypt(&mut buff_in, &mut buff_out, true) {
            Ok(BufferUnderflow) => {}
            Ok(BufferOverflow) => panic!("Decryption not completed"),
            Err(_) => panic!("Decrypt Error!"),
        }
        let dec_size = buff_out.position();
        let txt = String::from_utf8(decdata[..dec_size].to_vec()).unwrap();
        txt
    }

    /// Check master password
    fn check_masterpass(&mut self) -> bool {
        print!("Password: ");
        std::io::stdout().flush().unwrap();
        let pwd: String = read();
        //        let pwd: String = read_passwd();

        let mut digest = vec![0; 32];
        let mut md = Sha256::new();

        md.reset();
        md.input(pwd.as_bytes());
        md.result(&mut digest);

        for i in 0..32 {
            if digest[i] != PWDHASH[i] {
                hexdump(&digest);
                return false;
            }
        }
        self.secret_key = pwd;
        true
    }
}

const USAGE: &str = "usage: command [key [id [password]]]
       command: {create|add|update|del|get|list|check|pwdgen|quit}\n";

const ERR_MSG: &str = "Command paramter error!
 parameter: name [id [password]]\n";

const CMD_LIST: [&str; 8] = [
    "create", "add", "update", "del", "get", "list", "check", "pwdgen",
];

fn main() {
    let args: Vec<String> = env::args().collect();
    let mut dir = ".".to_string();
    if args.len() == 2 {
        dir = args[1].clone();
    }

    let mut mypwd: PwdMng = PwdMng::new(dir);
    if !mypwd.check_masterpass() {
        println!("Password error!");
        return;
    };

    cmd_loop(&mypwd);
}

fn cmd_loop(mypwd: &PwdMng) {
    println!("Enter command: create / add / update / del / get / list / check / pwdgen /quit");
    loop {
        print!("> ");
        std::io::stdout().flush().unwrap();
        let cmd: Vec<String> = read_vec();
        if cmd.is_empty() {
            continue;
        }
        if cmd[0] == "quit" {
            return;
        }
        if !CMD_LIST.contains(&&(*cmd[0])) {
            println!("Error: Invalid command: {}", cmd[0]);
            println!("{}", USAGE);
            continue;
        }

        do_command(mypwd, cmd);
    }
}

fn do_command(mypwd: &PwdMng, cmd: Vec<String>) -> bool {
    let plen = cmd.len();
    let mut rv = false;

    if plen == 1 {
        if cmd[0] == "create" {
            rv = mypwd.create();
        } else if cmd[0] == "list" {
            rv = mypwd.list();
        } else if cmd[0] == "pwdgen" {
            let pwd = mypwd.pwd_gen();
            println!("=> {}", pwd);
            rv = true;
        }
    } else if plen == 2 {
        if cmd[0] == "get" {
            let pwd = mypwd.get(&cmd[1]);
            match pwd {
                None => {
                    println!("Not found \"{}\".", &cmd[1]);
                    return false;
                }
                Some(data) => {
                    println!("ID: {}", data.id);
                    println!("Password: {}", data.passwd);
                    println!("Date: {}", data.date);
                    rv = true;
                }
            }
        } else if cmd[0] == "update" {
            rv = mypwd.update(&cmd[1]);
        } else if cmd[0] == "del" {
            rv = mypwd.del(&cmd[1]);
        } else if cmd[0] == "check" {
            mypwd.check(&cmd[1]);
            rv = true;
        }
    } else if plen == 3 {
        if cmd[0] == "add" {
            let pwd = mypwd.pwd_gen();
            rv = mypwd.add(&cmd[1], &cmd[2], &pwd);
        }
    } else if plen == 4 {
        if cmd[0] == "add" {
            rv = mypwd.add(&cmd[1], &cmd[2], &cmd[3]);
        }
    } else {
        println!("{}", ERR_MSG);
    }
    if !rv {
        println!("{}", ERR_MSG);
    }
    rv
}
