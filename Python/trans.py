#!/usr/bin/env python
""" Transposition cipher

written by blanclux
This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
"""
import argparse

# Key table
key = [[3, 5, 2, 1, 4], [2, 4, 1, 5, 3], [5, 4, 3, 2, 1], [1, 2, 3, 4, 5]]
keyLen = len(key[0])

def main():
    parser = argparse.ArgumentParser(description="Transposition cipher")
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

    print("< Transposition cipher >")
    print(" Key block :", keyLen)
    print(" Key       :", key[keyNo - 1])
    
    if keyNo < 1 or keyNo > len(key):
        print("Key No. error")
        return
    if args.mode == "enc":
        print("< Encrypt >")
        print(" Plain text :", text)
        print(" Encrypted  :", end=" ")
        print(encrypt(text, key[keyNo - 1]))
    else:
        print("< Decrypt >")
        print(" Encrypted :", text)
        print(" Decrypted :", end=" ")
        print(decrypt(text, key[keyNo - 1]))


# Encryption
def encrypt(plain, key):
    cipher = ""

    stap = 0
    endp = keyLen

    while stap < len(plain):
        block = plain[stap:endp]

        if len(block) != keyLen:
            cipher += block
        else:
            for loc in key:
                cipher += block[loc - 1]

        stap += keyLen
        endp += keyLen

    return cipher


# Decryption
def decrypt(cipher, key):
    plain = ""

    stap = 0
    endp = keyLen

    while stap < len(cipher):
        block = cipher[stap:endp]

        if len(block) != keyLen:
            plain += block
        else:
            for no in range(1, keyLen + 1):
                loc = key.index(no)
                plain += block[loc]

        stap += keyLen
        endp += keyLen

    return plain


if __name__ == "__main__":
    main()
