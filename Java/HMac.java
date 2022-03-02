/*
 * HMac.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.crypto.mac;

import java.io.*;
import java.security.*;
import java.security.spec.*;

import javax.crypto.*;

import Blanclux.crypto.spec.*;

/**
 * HMAC Class
 * <p>
 * Implementation of HMAC.
 */
public class HMac {
//public class HMac extends MacSpi {
	private int digestSize = 20;
	private int blockSize  = 64;

	private MessageDigest digest;
	private ByteArrayOutputStream out;
	private byte[] data;
	private byte[] key;
	private int keyLen;

	/**
	 * HMAC
	 */
	public HMac() {
		out = new ByteArrayOutputStream();
	}

	/**
	 * Initializes the MAC with the given (secret) key and algorithm parameters
	 *
	 * @param key the (secret) key.
	 * @param params the algorithm parameters.
 	 */
//	protected void engineInit(Key key, AlgorithmParameterSpec params)
	public void init(Key key, AlgorithmParameterSpec params)
		throws InvalidKeyException, InvalidAlgorithmParameterException {
		HMacParameterSpec pspec = (HMacParameterSpec) params;

		String algorithm = pspec.getAlgorithm();
		try {
			digest = MessageDigest.getInstance(algorithm);
		} catch (Exception ex) {
			throw new InvalidAlgorithmParameterException();
		}

		if (key instanceof SecretKey) {
			this.key = key.getEncoded();
		} else {
			throw new InvalidKeyException();
		}
		keyLen = this.key.length;
		digestSize = digest.getDigestLength();
		digest.reset();
	}

	/**
	 * Completes the MAC computation and resets the MAC for further use.
	 *
	 * @return the MAC result
	 */
//	public byte[] engineDoFinal() {
	public byte[] doFinal() {
		byte[] buf = new byte[blockSize];

		data = out.toByteArray();

		if (keyLen > blockSize) {
				key = digest.digest(key);
				keyLen = digestSize;
		}

		// Inner Digest
		digest.reset();

		// Pad the key for inner digest
        for (int i = 0 ; i < keyLen ; ++i) {
			buf[i] = (byte) (key[i] ^ 0x36);
		}
        for (int i = keyLen ; i < blockSize ; ++i) {
			buf[i] = 0x36 ;
		}

		digest.update(buf);
		byte[] ihash = digest.digest(data);

		// Outer Digest
		digest.reset();

		// Pad the key for outer digest
		for (int i = 0 ; i < keyLen ; ++i) {
			buf[i] = (byte) (key[i] ^ 0x5C);
		}
		for (int i = keyLen ; i < blockSize ; ++i) {
			buf[i] = 0x5C;
		}

		digest.update(buf);
		return digest.digest(ihash);
	}

	/**
	 * Resets the MAC for further use, maintaining the secret key that
	 * the MAC was initialized with. 
	 */
//	protected void engineReset() {
	public void reset() {
		out.reset();
	}

	/**
	 * Returns the length of the MAC in bytes.
	 *
	 * @return the MAC length in bytes
	 */
//	protected int engineGetMacLength() {
	public int getMacLength() {
		return digestSize;
	}

	/**
	 * Processes the given byte.
	 *
	 * @param input the input byte to be processed
	 */
//	protected void engineUpdate(byte input) {
	public void update(byte input) {
		out.write(input);
	}

	/**
	 * Processes the first len bytes in input, starting at offset inclusive
	 *
	 * @param input  the input buffer
	 * @param offset the offset in input where the input starts
	 * @param len    the number of bytes to process
	 */
//	protected void engineUpdate(byte[] input, int offset, int len) {
	public void update(byte[] input, int offset, int len) {
		out.write(input, offset, len);
	}

}
