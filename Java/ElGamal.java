/**
 * ElGamal.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.crypto.cipher;

import Blanclux.math.*;

import java.security.*;

import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * ElGamal
 */
public class ElGamal {

	/** modulus p */
	private MPInt p;

	/** generator g */
	private MPInt g;

	/** public key
	 *  y = g^a mod p
	 */
	private MPInt y;

	/** private key */
	private MPInt x;

	/** block size of cipher (bytes) */
	private int blockSize;

	/** block size of cipher text (bytes) */
	private int cipherBlockSize;

	/** random generator */
	private SecureRandom rng = null;

	/** Cipher object */
	private Cipher cipher;
	/** secret cipher algorithm */
	private static String algorithm = "AES";
	/** cipher mode of operation */
	private static String opmode = "AES/CBC/PKCS5Padding";
	/** key length */
	private static int keyLength = 128;
	/** IV */
	private static byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0,
							    0, 0, 0, 0, 0, 0, 0, 0};

	/**
	 * Constructor
	 */
	public ElGamal() {
		try {
			cipher = Cipher.getInstance(opmode);
		} catch (NoSuchAlgorithmException e) {
			System.err.println("ElGamal: " + e.toString());
		} catch (NoSuchPaddingException e) {
			System.err.println("ElGamal: " + e.toString());
		}
	}

	/**
	 * Gets a block length
	 *  pLen / 8 
	 */
	public int getBlockSize() {
		return blockSize;
	}

	/**
	 * Gets a cipher block length
	 *  ((pLen + 7) / 8) * 2
	 */
	public int getCipherBlockSize() {
		return cipherBlockSize;
	}

	/**
	 * Encrypt initialization
	 *
	 * @param key ElGamalPublicKey
	 * @param random random generator
	 */
	public void initEncrypt(Key key, SecureRandom random)
		throws InvalidKeyException {

		if (key instanceof ElGamalPublicKey) {
			ElGamalPublicKey publicKey = (ElGamalPublicKey) key;
			p = publicKey.getModulus();
			g = publicKey.getGenerator();
			y = publicKey.getPublic();
			blockSize = p.bitLength() / 8;
			cipherBlockSize = ((p.bitLength() + 7) / 8) * 2;
			rng = random;
		} else {
			throw new InvalidKeyException("Key is not a ElGamal Public Key");
		}
	}

	/**
	 * Decrypt initialization
	 *
	 * @param key ElGamalPrivateKey
	 */
	public void initDecrypt(Key key) throws InvalidKeyException {

		if (key instanceof ElGamalPrivateKey) {
			ElGamalPrivateKey privateKey = (ElGamalPrivateKey) key;
			p = privateKey.getModulus();
			g = privateKey.getGenerator();
			x = privateKey.getPrivate();
			blockSize = p.bitLength() / 8;
			cipherBlockSize = ((p.bitLength() + 7) / 8) * 2;
		} else {
			throw new InvalidKeyException("Key is not a ElGamal Private Key");
		}
	}

	/**
	 * Decrypt initialization
	 *
	 * @param pi prime p
	 * @param gi generator g
	 */
	public void initDecrypt(MPInt pi, MPInt gi) {

			this.p = pi;
			this.g = gi;

			blockSize = pi.bitLength() / 8;
			cipherBlockSize = ((pi.bitLength() + 7) / 8) * 2;
	}
	
	/**
	 * Encryption
	 * 
	 * @param in the plain text area
	 * @param inOffset the input offset
	 * @param inLen the plain text length
	 * @param out the cipher text area (allocated)
	 * @param outOffset the output offset
	 */
	public void encrypt(byte[] in, int inOffset, int inLen, byte[] out,
			int outOffset) {

		int len;
		if (inLen > blockSize) {
			len = blockSize;
		} else {
			len = inLen;
		}

		// Byte to MPInt
		byte[] mB = new byte[len];
		System.arraycopy(in, inOffset, mB, 0, len);
		MPInt m = new MPInt(1, mB);

		// Generate random number r (0 < r < p - 1)
		MPInt r;
		MPInt p_1 = p.subtract(MPInt.ONE);

		do {
			r = new MPInt(p.bitLength() - 1, rng);
		} while ((r.compareTo(p_1)) >= 0);

		// cipher text C = (C1, C2)
		MPInt c1 = g.modPow(r, p);
		MPInt c2 = (m.multiply(y.modPow(r, p))).mod(p);

		// Output C
		byte[] c1B = MPInt.toFixedBytes(c1, cipherBlockSize / 2);
		byte[] c2B = MPInt.toFixedBytes(c2, cipherBlockSize / 2);
		System.arraycopy(c1B, 0, out, outOffset + cipherBlockSize / 2
				- c1B.length, c1B.length);
		System.arraycopy(c2B, 0, out, outOffset + cipherBlockSize - c2B.length,
				c2B.length);
	}

	/**
	 * Decryption
	 *
	 * @param in the cipher text (C1, C2)
	 * @param inOffset the input offset
	 * @param out the decrypted text (allocated)
	 * @param outOffset the output offset
	 * @return the decrypted text length
	 */
	public int decrypt(byte[] in, int inOffset, byte[] out, int outOffset) {

		// Byte to MPInt
		byte[] c1B = new byte[cipherBlockSize / 2];
		System.arraycopy(in, inOffset, c1B, 0, cipherBlockSize / 2);
		MPInt c1 = new MPInt(1, c1B);

		byte[] c2B = new byte[cipherBlockSize / 2];
		System.arraycopy(in, inOffset + cipherBlockSize / 2, c2B, 0,
				cipherBlockSize / 2);
		MPInt c2 = new MPInt(1, c2B);

		// Decrypt
		MPInt m = c2.multiply(c1.modPow(x.negate(), p)).mod(p);

		// Output M
		byte[] mB = MPInt.toByteArray(m);
		System.arraycopy(mB, 0, out, outOffset,	mB.length);
		return mB.length;
	}

	/**
	 * Hybrid encryption
	 *
	 * @param src  the plain text
	 * @param len  the plain text length (byte)
	 * @return the encrypted data
	 */
	public byte[] encrypt_hybrid(byte[] src, int len) {
		int keyLen = keyLength / 8;

		if (src == null || src.length < len) {
			return null;
		}

		try {
			KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
		
			IvParameterSpec params = new IvParameterSpec(iv);

			keyGen.init(keyLength, rng);
			SecretKey seckey = keyGen.generateKey();
			byte[] key = seckey.getEncoded();

			cipher.init(Cipher.ENCRYPT_MODE, seckey, params);
			byte[] c1 = cipher.doFinal(src, 0, len);

			byte[] c2 = new byte[cipherBlockSize];
			// padding
			byte[] in = new byte[keyLen + 1];
			in[0] = (byte) 0x80;
			System.arraycopy(key, 0, in, 1, keyLen);
			encrypt(in, 0, in.length, c2, 0);

			byte[] enc = new byte[c1.length + c2.length];
			System.arraycopy(c1, 0, enc, 0, c1.length);
			System.arraycopy(c2, 0, enc, c1.length, c2.length);
			return enc;
		} catch (InvalidKeyException e) {
			System.err.println("ElGamal/decrypt_hybrid error: " + e.toString());
			return null;
		} catch (NoSuchAlgorithmException e) {
			System.err.println("ElGamal/decrypt_hybrid error: " + e.toString());
			return null;
		} catch (IllegalStateException e) {
			System.err.println("ElGamal/decrypt_hybrid error: " + e.toString());
			return null;
		} catch (IllegalBlockSizeException e) {
			System.err.println("ElGamal/decrypt_hybrid error: " + e.toString());
			return null;
		} catch (BadPaddingException e) {
			System.err.println("ElGamal/decrypt_hybrid error: " + e.toString());
			return null;
		} catch (InvalidAlgorithmParameterException e) {
			System.err.println("ElGamal/decrypt_hybrid error: " + e.toString());
			return null;
		}
	}

	/**
	 * Hybrid decryption
	 * 
	 * @param src  the encrypted data
	 * @param len  the encrypted data length (byte)
	 * @return the decrypted data
	 */
	public byte[] decrypt_hybrid(byte[] src, int len) {
		int keyLen = keyLength / 8;

		if (src == null || src.length < len
			|| len < (cipherBlockSize + keyLen)) {
			return null;
		}

		try {
			byte[] c2 = new byte[cipherBlockSize];
			System.arraycopy(src, len - cipherBlockSize, c2, 0,
							 cipherBlockSize);

			byte[] out = new byte[keyLen + 1];
			decrypt(c2, 0, out, 0);
			// unpadding
			byte[] key = new byte[keyLen];
			System.arraycopy(out, 1, key, 0, keyLen);

			SecretKey seckey = new SecretKeySpec(key, algorithm);

			IvParameterSpec params = new IvParameterSpec(iv);

			cipher.init(Cipher.DECRYPT_MODE, seckey, params);
		
			byte[] dest = cipher.doFinal(src, 0, len - cipherBlockSize);

			return dest;
		} catch (InvalidKeyException e) {
			System.err.println("ElGamal/decrypt_hybrid error: " + e.toString());
			return null;
		} catch (InvalidAlgorithmParameterException e) {
			System.err.println("ElGamal/decrypt_hybrid error: " + e.toString());
			return null;
		} catch (IllegalStateException e) {
			System.err.println("ElGamal/decrypt_hybrid error: " + e.toString());
			return null;
		} catch (IllegalBlockSizeException e) {
			System.err.println("ElGamal/decrypt_hybrid error: " + e.toString());
			return null;
		} catch (BadPaddingException e) {
			System.err.println("ElGamal/decrypt_hybrid error: " + e.toString());
			return null;
		}
	}
	
	/**
	 * Encrypt (multiple block)
	 *
	 * @param src  the plain text
	 * @param len  the plain text length (byte)
	 * @return the encrypted data
	 */
	public byte[] encrypt(byte[] src, int len) {
		byte pad = (byte) 0x80;

		if (src == null || src.length < len) {
			return null;
		}

		int block  = len / (blockSize - 2);
		int remain = len % (blockSize - 2);
		if (remain != 0) {
			block++;
		}
		byte[] in  = new byte[blockSize - 1];
		byte[] out = new byte[cipherBlockSize];

		byte[] dest = new byte[block * cipherBlockSize];

		for (int i = 0; i < block; i++) {
			int bsize = blockSize - 2;
			if (i == block - 1 && remain != 0) {
				bsize = remain;
			}
			in[0] = pad;
			System.arraycopy(src, i * (blockSize - 2), in, 1, bsize);
			encrypt(in, 0, bsize + 1, out, 0);
			System.arraycopy(out, 0, dest, i * cipherBlockSize,
							 cipherBlockSize);
		}
		return dest;
	}

	/**
	 * Decrypt (multiple block)
	 *
	 * @param src  the encrypted data
	 * @param len  the encrypted data length (byte)
	 * @return the decrypted data
	 */
	public byte[] decrypt(byte[] src, int len) {
		if (src == null || src.length < len) {
			return null;
		}
		if (len % cipherBlockSize != 0) {
			return null;
		}
		int block = len / cipherBlockSize;
		int bSize = cipherBlockSize / 2;

		byte[] in  = new byte[cipherBlockSize];
		byte[] out = new byte[bSize - 1];

		byte[] temp = new byte[block * bSize];

		int outLen;
		int decLen = 0;

		for (int i = 0; i < block; i++) {
			System.arraycopy(src, i * cipherBlockSize, in, 0, cipherBlockSize);
			outLen = decrypt(in, 0, out, 0);
			System.arraycopy(out, 1, temp, decLen, outLen - 1);
			decLen += outLen - 1;
		}

		byte[] dest = new byte[decLen];
		System.arraycopy(temp, 0, dest, 0, decLen);
		return dest;
	}

}
