#!/usr/bin/env python
""" Enigma cipher

written by blanclux
This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
"""
import random
import copy


def main():
    # Set seed
    seed = int(input("Seed : "))
    # Text string
    plain = input('Text : ').lower()

    # encrypt
    enigma = Enigma(seed)
    enc = enigma.encode(plain)
    print("Encrypt :", end = " ")
    print(enc)

    # decrypt
    enigma = Enigma(seed)
    dec = enigma.encode(enc)
    print("Decrypt :", end = " ")
    print(dec)


class Enigma:
    def __init__(self, seed1, seed2 = 0, seed3 = 0):
        # set alphabet characters
        alphabet = [chr(i) for i in range(ord("a"), ord("z") + 1)]
        addch = [' ', '?', '.', ',']
        self.orig = alphabet + addch
        self.chNum = len(self.orig)
        self.chNum2 = self.chNum * self.chNum

        # make rotors
        self.rotor1 = self._make_rotor(seed1)
        self.rotor2 = self._make_rotor(seed2)
        self.rotor3 = self._make_rotor(seed3)
        self.reflect = self._make_rotor(seed1)
        # make a Plug board
        self.plug = self._make_plug()

    # Random rotor generation
    def _make_rotor(self, seed = 0):
        random.seed(seed)
        alphabet = copy.copy(self.orig)
        random.shuffle(alphabet)
        return alphabet

    # Plug board
    def _make_plug(self, seed = 0):
        random.seed(seed)
        plug = copy.copy(self.orig)
        replace = random.sample(range(self.chNum), 6)
        for index in range(0, 6, 2):
            plug[replace[index]], plug[replace[index + 1]] = (
                plug[replace[index + 1]],
                plug[replace[index]],
            )
        return plug

    # Rotate
    def _rotate(self, idx):
        self.rotor1.append(self.rotor1.pop(0))
        if idx % self.chNum == 0 and idx / self.chNum != 0:
            self.rotor2.append(self.rotor2.pop(0))
        if idx % self.chNum2 == 0 and idx / self.chNum2 != 0:
            self.rotor3.append(self.rotor3.pop(0))

    # Enigma process
    def encode(self, string):
        code_string = ""
        for idx, char in enumerate(string):
            code_string += self._encode_character(char)
            self._rotate(idx)

        return code_string

    # encode a character
    def _encode_character(self, ch):

        # Outward
        char = self.plug[self.orig.index(ch)]
        char = self.rotor1[self.orig.index(char)]
        char = self.rotor2[self.orig.index(char)]
        char = self.rotor3[self.orig.index(char)]

        # Reflector
        if self.reflect.index(char) % 2 == 0:
            char = self.reflect[self.reflect.index(char) + 1]
        else:
            char = self.reflect[self.reflect.index(char) - 1]

        # Return path
        char = self.orig[self.rotor3.index(char)]
        char = self.orig[self.rotor2.index(char)]
        char = self.orig[self.rotor1.index(char)]
        char = self.orig[self.plug.index(char)]

        return char


if __name__ == "__main__":
    main()
