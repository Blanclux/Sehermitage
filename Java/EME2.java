/**
 * EME2.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.crypto.encode;

import java.security.*;

import Blanclux.crypto.*;
import Blanclux.util.*;

/**
 * P1363 Message-encoding method EME2
 */
public class EME2 extends MessageEncode {
	/** Random generation function */
	private SecureRandom rng;
	/** Seed length */
	private int seedLen = 16;
	/** Option parameter length */
	private int paramLen = 0;

	/** Encoded or Decoded Message */
	private byte[] em;
	/** Digest of message data */
	private byte[] hash;

	public EME2() {
		super("EME2");
	}

	/**
	 * Encode
	 * <p>
	 * @param src the source data
	 *
	 * @return encoded data
	 */
	public byte[] encode(byte[] src) {
		return encodeEME2(src, (encLen + 7) / 8);
	}

	/**
	 * Decode
	 * <p>
	 * @param src the source data
	 *
	 * @return the decoded data
	 */
	public byte[] decode(byte[] enc) {
		return decodeEME2(enc);
	}

	/**
	 * Initialize EME2 object.
	 *
	 * @param hash MessageDigest object
	 * @param random Random generator object
	 * @param seedLen length of random seed
	 * @param emLen max length of message
	 */
	public void init(MessageDigest hash, SecureRandom random, int seedLen) {
		digest = hash;
		if (random == null) {
			rng = new SecureRandom();
		} else {
			rng = random;
		}
		digestLen = digest.getDigestLength();
		if (seedLen > 0) {
			this.seedLen = seedLen;
		} else {
			this.seedLen = digestLen;
		}
	}

	/**
	 * EME2 encoding operation
	 *
	 * @param src the source data
	 * @param emLen the encoded message length
	 * @return the encoded data
	 */
	protected byte[] encodeEME2(byte[] src, int emLen) {
		int msgLen = src.length;
		int sLen = emLen - seedLen - 1 - msgLen;
		if (sLen < 0) {
			return null;			// Message too long
		}
		byte[] param = null;
		if (parameter != null) {
			param = (byte[]) parameter;
			paramLen = param.length;
		}

		// S = 0x00 .... 0x00
		byte[] db = new byte[sLen + 1];
		// M' = S || 01 || M
		db[sLen] = (byte) 0x01;
		db = ByteArray.concat(db, src);
		// EM = M' || seed
		byte[] seed = new byte[seedLen];
		rng.nextBytes(seed);
		db = ByteArray.concat(db, seed);
		em = new byte[db.length];
		System.arraycopy(db, 0, em, 0, db.length);
		// DB = EM || P
		if (paramLen > 0) {
			ByteArray.concat(db, param);
		}

		digest.reset();
		hash = digest.digest(db);

		return em;
	}

	/**
	 * EME2 decoding operation
	 *
	 * @param enc the encoded data
	 * @return the decoded data
	 */
	protected byte[] decodeEME2(byte[] enc) {
		int emLen = enc.length;
		// EM = S || 01 || M || seed
		if (emLen < seedLen + digestLen + 1) {
			return null;			// Decoding error
		}

		int off;
		for (off = 0; off < emLen - seedLen; off++) {
			if (enc[off] != 0) {
				break;
			}
		}
		if (enc[off] != 0x01 || off >= emLen - seedLen - 1) {
			return null;
		}

		int msgLen = emLen - off - seedLen - 1;
		byte[] msg = new byte[msgLen];
		System.arraycopy(enc, off + 1, msg, 0, msgLen);
		em = new byte[msgLen];
		System.arraycopy(msg, 0, em, 0, msgLen);

		byte[] db = new byte[enc.length];
		System.arraycopy(enc, 0, db, 0, enc.length);
		byte[] param = null;
		if (parameter != null) {
			param = (byte[]) parameter;
			db = ByteArray.concat(db, param);
		}

		digest.reset();
		hash = digest.digest(db);

		return em;
	}

	public byte[] getMessage() {
		byte[] tmp = new byte[em.length];

		System.arraycopy(em, 0, tmp, 0, tmp.length);
		return tmp;
	}

	public byte[] getRandom() {
		byte[] tmp = new byte[hash.length];

		System.arraycopy(hash, 0, tmp, 0, tmp.length);
		return tmp;
	}
}
