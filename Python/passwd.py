#!/usr/bin/env python
""" Password manager

written by Blanclux
This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
"""

import sys, string, random
import shelve, re
import pyperclip as pclip
import getpass
import base64
from Crypto.Hash import SHA256
from Crypto.Random import get_random_bytes
from Crypto.Protocol.KDF import PBKDF2
import Crypto.Cipher.AES as AES
import Crypto.Util.Padding as PAD

# available password symbol
symbol = "[!-/:-@[-`{-~]+"
# Command list
cmd_list = ["get", "add", "del", "gen", "edit", "list", "disp", "quit"]

class Entry:
    def __init__(self):
        self.user = ""
        self.pwd = ""
        self.url = ""
        self.note = ""


class PasswordMng:
    def __init__(self):
        self._salt = "salt"
        self._keysize = 16
        self._count = 1000
        self.secret_key = b"0123456789abcdef"
        self.pwd_db = shelve.open("passwd_db")

    def check_masterpwd(self):
        pwd = getpass.getpass("Password: ")
        hash = SHA256.new()
        hash.update(pwd.encode())
        # print(hash.hexdigest())
        if (
            hash.hexdigest() 
            # Rewrite the following line with the value of SHA256(pwd) -- currently pwd = "security"
            == "5d2d3ceb7abe552344276d47d36a8175b7aeb250a9bf0bf00e850cd23ecf2e43"
        ):
            pass
        else:
            sys.exit("Error: invalid password")
        self.secret_key = PBKDF2(pwd, self._salt, self._keysize, self._count)

    # Encrypt
    def encrypt(self, plain):
        IV = get_random_bytes(AES.block_size)
        data = PAD.pad(plain.encode('ascii'), 16, 'pkcs7')
        cipher = AES.new(self.secret_key, AES.MODE_CBC, IV)
        return base64.b64encode(IV + cipher.encrypt(data))

    # Decrypt
    def decrypt(self, encode):
        encode = base64.b64decode(encode)
        IV = encode[: AES.block_size]
        cipher = AES.new(self.secret_key, AES.MODE_CBC, IV)
        data = cipher.decrypt(encode[AES.block_size :])
        plain = PAD.unpad(data, 16, 'pkcs7')
        return plain.decode('ascii')

    def get(self, id):
        if id == '':
            id = input("ID: ")
        if id in self.pwd_db.keys():
            print(id + " is registered.")
            ent = self.pwd_db[id]
            print("user: ", ent.user)
            pwd = self.decrypt(ent.pwd)
            pclip.copy(pwd)
            print("Password was copied to clipboard.")
        else:
            print(id + " is not registered. ")

    def delete(self, id):
        if id == '':
            id = input("ID: ")
        if id in self.pwd_db.keys():
            del self.pwd_db[id]
            print(id + " is removed.")
        else:
            print(id + " is not registered.")

    def add(self):
        ent = Entry()
        print("# Add entry")
        id = input("ID: ")
        ent.user = input("User: ")
        pwd = input("Password: ")
        ent.pwd = self.encrypt(pwd)
        ent.url = input("URL: ")
        ent.note = input("Note: ")
        self.pwd_db[id] = ent

    def edit(self, id):
        ent = Entry()
        print("# Edit entry")
        if id == '':
            id = input("ID* ")
        if id in self.pwd_db.keys():
            ent.user = self.pwd_db[id].user
            print("User (", ent.user, end=" ): ")
            user = input()
            if user != '':
                ent.user = user

            ent.pwd = self.pwd_db[id].pwd
            pwd = input("Password: ")
            if pwd != '':
                ent.pwd = self.encrypt(pwd)

            ent.url = self.pwd_db[id].url
            print("URL (", ent.url, end=" ): ")
            url = input()
            if url != '':
                ent.url = url

            ent.note = self.pwd_db[id].note
            print("Note (", ent.note, end=" ): ")
            note = input()
            if note != '':
                ent.note = note
            self.pwd_db[id] = ent
        else:
            print(id + " is not registered.")

    def gen(self):
        print("# Generate password & entry")
        ent = Entry()
        id = input("ID: ")
        ent.user = input("User: ")
        print("Password generation\nInput password char length", end=": ")
        character_num = int(input())
        while True:
            str = string.ascii_letters + string.digits + symbol
            pw_lst = random.sample(str, character_num)
            pw = "".join(pw_lst)
            if self.strength_test(pw):
                pclip.copy(pw)
                ent.pwd = self.encrypt(pw)
                self.pwd_db[id] = ent
                print("Password is registerd and copied. ")
                break

    def list(self):
        id_list = list(self.pwd_db.keys())
        id_list.sort(key=str.lower)
        print(" ID list ".center(20, "-"))
        for id in id_list:
            print(id)
        print("\n")

    def disp(self, id):
        print("# Entry", id)
        if id == '':
            id = input("ID: ")
        if id in self.pwd_db.keys():
            print("User: ", self.pwd_db[id].user)
            print("Password: ", self.pwd_db[id].pwd)
            print("URL: ", self.pwd_db[id].url)
            print("Note: ", self.pwd_db[id].note)
        else:
            print(id + " is not registered.")

    def strength_test(self, pw):
        test_s = re.search(re.compile(r"^[a-z]"), pw) # start ch: [a-z]
        test_A = re.search(re.compile(r"[A-Z]+"), pw)
        test_a = re.search(re.compile(r"[a-z]+"), pw)
        test_0 = re.search(re.compile(r"[0-9]+"), pw)
        test_symbol = re.search(re.compile(symbol), pw) 
        if test_A and test_a and test_0 and test_symbol and test_s:
            return True
        else:
            return False

def do_command(cmd): 
    if cmd == "get":
        id = input("ID: ")
        mypwd.get(id)
    elif cmd == "add":
        mypwd.add()
    elif cmd == "del":
        id = input("ID: ")
        mypwd.delete(id)
    elif cmd == "gen":
        mypwd.gen()
    elif cmd == "edit":
        id = input("ID: ")
        mypwd.edit(id)
    elif cmd == "list":
        mypwd.list()
    elif cmd == "disp":
        id = input("ID: ")
        mypwd.disp(id)
    else:
        print("Invalid command")
 
def command_loop(mypwd):
    print("Enter command: add / del / edit / gen / get / list / disp /quit");
    while True:
        cmd = input("> ")
        if not cmd in cmd_list:
            print("Error: Invalid command")
            continue
        if cmd == "quit":
            sys.exit()

        do_command(cmd)

# Main routine
mypwd = PasswordMng()
# Password check
mypwd.check_masterpwd()
# Execute command  
command_loop(mypwd)
