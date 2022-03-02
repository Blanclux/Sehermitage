#!/usr/bin/env python
""" Substitution cipher

written by blanclux
This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
"""
import argparse

alpha = "abcdefghijklmnopqrstuvwxyz"
key = [
     "KWJHUBVGTXLZIDQYSMFRNCPAEO",
     "WJHUBVGTXLZIDQYSMFRNCPAEOK"
]

def main():
    parser = argparse.ArgumentParser(description="Substitution cipher")
    parser.add_argument("text", help="Text string")
    parser.add_argument("key", help="Key No.")
    parser.add_argument(
        "mode",
        choices=["enc", "dec"],
        nargs="?",
        default="enc",
        help="mode (encrypt/decrypt)",
    )
    args = parser.parse_args()

    text = args.text
    keyNo = int(args.key)

    if keyNo < 1 or keyNo > len(key):
        print("Key No. error")
        return
    if args.mode == "enc":
        print("<Encrypt>")
        print(encrypt(text, key[keyNo - 1]))
    else:
        print("<Decrypt>")
        print(decrypt(text, key[keyNo - 1]))

# Encryption
def encrypt(plain, key):
    cipher = ""

    for p in plain:
        if p in alpha:
            loc = alpha.index(p)
            cipher += key[loc]
        else:
            cipher += p

    return cipher

# Decryption
def decrypt(cipher, key):
    plain = ""

    for c in cipher:
        if c in key:
            loc = key.index(c)
            plain += alpha[loc]
        else:
            plain += c

    return plain


if __name__ == "__main__":
    main()

