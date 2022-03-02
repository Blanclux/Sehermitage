/**
 * EME3.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.crypto.encode;

import java.security.*;
import java.security.spec.*;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import Blanclux.crypto.*;
import Blanclux.util.*;

/**
 * EME3 Message Encode
 */
public class EME3 extends MessageEncode {

	/** Key derive function */
	private MessageEncode kdf;

	/** MaskGeneration function */
	private MessageEncode mgf;

	/** Random generation function */
	private SecureRandom rng;

	/** SecretCipher object */
	private Cipher cipher;

	/** SecretCipher class name */
	private String cName;

	/** AlgoritmParameterSpec */
	private AlgorithmParameterSpec pspec;

	/** Randomized hash value */
	private byte[] hash;

	/** Message representative */
	private byte[] seed;

	/** Encrypted message */
	private byte[] code;

	/** Seed length */
	private int seedLen;

	/** Output length of MGF */
	private int outLen;

	/** Secret Cipher Key Length */
	private int keyLen;

	/** Provider Name */
	private String provider;

	/** Algorithm Name */
	private String algorithm;

	public EME3() {
		super("EME3");
	}

	/**
	 * Initialize
	 *
	 * @param hash the MessageDigest object
	 * @param mgf MessageEncode object
	 * @param kdf the MessageEncode object (KDF function)
	 * @param cipher the Cipher object
	 * @param spec the AlgorithmParameterSpec object
	 * @param random the SecureRandom object
	 * @param outLen the output length
	 * @param oLen the oLen
	 */
	public void init(MessageDigest hash, MessageEncode mgf, MessageEncode kdf, 
					 Cipher cipher, AlgorithmParameterSpec spec, 
					 SecureRandom random, int outLen, int oLen) {
		this.digest = hash;
		this.mgf = mgf;
		this.kdf = kdf;
		this.cipher = cipher;
		this.pspec = spec;

		if (random == null) {
			rng = new SecureRandom();
		} else {
			rng = random;
		}

		this.outLen = outLen / 8;
		keyLen = oLen / 8;
		provider = cipher.getProvider().getName();
		cName = cipher.getAlgorithm();
		algorithm = cName.substring(0, cName.indexOf('/'));

		System.out.println("Provider    : " + provider);
		System.out.println("Algorithm : " + algorithm);
		System.out.println("Cipher Class: " + cName);
	}

	/**
	 * Encode
	 * <p>
	 * @param src the source data
	 * @return the encoded data
	 */
	public byte[] encode(byte[] src) {

		return encode(src, encLen);
	}

	/**
	 * Decode
	 * <p>
	 * @param enc the encoded data
	 * @return the decoded data
	 */
	public byte[] decode(byte[] enc) {

		return decode(enc, code);
	}

	/**
	 * EME3 encode
	 *
	 * @param msg the message data
	 * @param emLen the encoded length
	 * @return the encoded data
	 */
	public byte[] encode(byte[] msg, int emLen)
				  throws IllegalStateException {
		// Seed
		seedLen = (emLen - 1) / 8;
		seed = new byte[seedLen];
		rng.nextBytes(seed);
		// KDF
		kdf.init(keyLen * 8);
		byte[] param = (byte[]) parameter;

		if (param != null) {
			kdf.setParameter(param);
		}

		byte[] key = kdf.encode(seed);

		System.out.println("key length = " + keyLen);
		System.out.println("encode key : " + ByteArray.toHexStr(key));
		// C = Encrypt(M)
		// set secret key
		try {
			SecretKey seckey = new SecretKeySpec(key, algorithm);

			cipher.init(Cipher.ENCRYPT_MODE, seckey, pspec);
			code = cipher.doFinal(msg);
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}

		// M || Seed || C || P
		int paramLen = 0;

		if (param != null) {
			paramLen = param.length;
		}

		byte[] db = new byte[msg.length + seedLen + code.length + paramLen];
		int offset = 0;

		System.arraycopy(msg, 0, db, 0, msg.length);
		offset = msg.length;
		System.arraycopy(seed, 0, db, offset, seed.length);
		offset += seed.length;
		System.arraycopy(code, 0, db, offset, code.length);
		offset += code.length;

		if (param != null) {
			System.arraycopy(param, 0, db, offset, paramLen);
		}

		// H = Hash(DB)
		digest.reset();
		byte[] hd = digest.digest(db);

		// MGF
		mgf.init(outLen);
		hash = mgf.encode(hd);
		byte[] enc = new byte[seedLen];

		System.arraycopy(seed, 0, enc, 0, seedLen);

		return enc;
	}

	/**
	 * EME3 decode
	 *
	 * @param enc the encoded data
	 * @param emsg the message
	 * @return the decoded data
	 */
	public byte[] decode(byte[] enc, byte[] emsg)
				  throws IllegalStateException {
		seedLen = enc.length;
		// KDF
		encLen = enc.length;
		kdf.init(keyLen * 8);
		byte[] param = (byte[]) parameter;

		if (param != null) {
			kdf.setParameter(param);
		}

		byte[] key = kdf.encode(enc);

		System.out.println("decode key : " + ByteArray.toHexStr(key));
		// M = Decrypt(C)
		// set secret key
		try {
			SecretKey seckey = new SecretKeySpec(key, algorithm);

			// decryption
			cipher.init(Cipher.DECRYPT_MODE, seckey, pspec);
			code = cipher.doFinal(emsg);
		} catch (Exception e) {

			return null;
		}

		// M || R || C || P
		int paramLen = 0;

		if (param != null) {
			paramLen = param.length;
		}

		byte[] db = new byte[emsg.length + seedLen + code.length + paramLen];
		int offset = 0;

		System.arraycopy(code, 0, db, 0, code.length);
		offset = code.length;
		System.arraycopy(enc, 0, db, offset, enc.length);
		offset += enc.length;
		System.arraycopy(emsg, 0, db, offset, emsg.length);
		offset += emsg.length;

		if (param != null) {
			System.arraycopy(param, 0, db, offset, paramLen);
		}

		// H = Hash(DB)
		digest.reset();
		byte[] hd = digest.digest(db);

		// MGF
		mgf.init(outLen);
		hash = mgf.encode(hd);
		byte[] dec = new byte[code.length];

		System.arraycopy(code, 0, dec, 0, code.length);

		return dec;
	}

	public byte[] getHashValue() {
		byte[] tmp = new byte[hash.length];

		System.arraycopy(hash, 0, tmp, 0, tmp.length);
		return tmp;
	}

	public byte[] getEncryptMessage() {
		byte[] tmp = new byte[code.length];

		System.arraycopy(code, 0, tmp, 0, tmp.length);
		return tmp;
	}

	public void setEncryptMessage(byte[] emsg) {
		code = new byte[emsg.length];
		System.arraycopy(emsg, 0, code, 0, emsg.length);
	}
}
