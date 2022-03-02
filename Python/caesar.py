#!/usr/bin/env python
""" Caesar cipher

written by blanclux
This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
"""
import argparse

def main():
    parser = argparse.ArgumentParser(description='Caesar cipher')
    parser.add_argument('text', help='data string')
    parser.add_argument('key', type=int, help='key (integer)')
    parser.add_argument('mode', choices=['enc', 'dec', 'anl'], nargs='?',default='enc', help="mode")

    args = parser.parse_args()

    string = args.text
    key = args.key

    if args.mode == 'enc':
        print("<Encrypt>")
        print(encrypt(string, key))
    elif args.mode == 'dec':
        print("<Decrypt>")
        print(decrypt(string, key))
    else:
        print("Analyze")
        analyze(string, key)

# Encrypt
def encrypt(string, key):
    ciphertext = ""

    for ch in list(string):
        if 'A' <= ch <= 'Z':
            ciphertext += chr((ord(ch) - ord('A') + key) % 26 + ord('A'))
        elif 'a' <= ch <= 'z':
            ciphertext += chr((ord(ch) - ord('a') + key) % 26 + ord('a'))
        elif '0' <= ch <= '9':
            ciphertext += chr((ord(ch) - ord('0') + key) % 10 + ord('0'))
        else:
            ciphertext += ch

    return ciphertext

# Decrypt
def decrypt(string, key):
    return encrypt(string, -key)

# Analyze
def analyze(string, key):
    for k in range(1, 26):
        ptext = decrypt(string, k)
        print(str(k) + ":\t" + ptext)


if __name__ == '__main__':
    main()
