#!/usr/bin/env python
""" Vigenere cipher

written by blanclux
This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
"""
import argparse

def main():
    parser = argparse.ArgumentParser(description='Vigenere cipher')
    parser.add_argument('text', help='text string')
    parser.add_argument('key', help='key string')
    parser.add_argument('mode', choices=['enc', 'dec'], nargs='?',default='enc', help="mode (encrypt/decrypt)")
    args = parser.parse_args()

    text = args.text
    key = args.key

    if args.mode == 'enc':
        print("<Encrypt>")
        print(encrypt(text, key))
    else:
        print("<Decrypt>")
        print(decrypt(text, key))

# Key expansion
def mkkey(string, key):
	keys = ""
	ls = len(string)
	lk = len(key)
	i = 0
	while i < int(ls / lk):
		keys += key
		i = i + 1
	keys += key[:ls % lk]
	return keys

# Encrypt
def encrypt(string, key):
    ciphertext = ""
    key = mkkey(string, key)
    key = key.upper()

    for s, k in zip(string, key):
        n = ord(k) - ord('A')
        if 'A' <= s <= 'Z':
            ciphertext += chr((ord(s) - ord('A') + n) % 26 + ord('A'))
        elif 'a' <= s <= 'z':
            ciphertext += chr((ord(s) - ord('a') + n) % 26 + ord('a'))
        elif '0' <= s <= '9':
            ciphertext += chr((ord(s) - ord('0') + n) % 10 + ord('0'))
        else:
            ciphertext += s

    return ciphertext

# Decrypt
def decrypt(string, key):
    plaintext = ""
    key = mkkey(string, key)
    key = key.upper()

    for s, k in zip(string, key):
        n = ord(k) - ord('A')
        if 'A' <= s <= 'Z':
            plaintext += chr((ord(s) - ord('A') - n) % 26 + ord('A'))
        elif 'a' <= s <= 'z':
            plaintext += chr((ord(s) - ord('a') - n) % 26 + ord('a'))
        elif '0' <= s <= '9':
            plaintext += chr((ord(s) - ord('0') - n) % 10 + ord('0'))
        else:
            plaintext += s

    return plaintext


if __name__ == '__main__':
	main()
