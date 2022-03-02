/**
 * EME1.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.crypto.encode;

import java.security.*;

import Blanclux.crypto.*;
import Blanclux.util.*;

/**
 * P1363 Message-encoding method EME1
 */
public class EME1 extends MessageEncode {
	/** MGF function */
	private MGF1 mgf;
	/** Random generation function */
	private SecureRandom rng;
	/** Seed length */
	private int seedLen;

	public EME1() {
		super("EME1");
	}

	/**
	 * Encode
	 * <p>
	 * @param src the source data
	 *
	 * @return the encoded data
	 */
	public byte[] encode(byte[] src) {
		return encodeEME1(src, (encLen + 7) / 8);
	}

	/**
	 * Decode
	 * <p>
	 * @param src the source data
	 *
	 * @return the decoded data
	 */
	public byte[] decode(byte[] enc) {
		return decodeEME1(enc);
	}

	/**
	 * Initialize EME1 object.
	 *
	 * @param hash MessageDigest object
	 * @param mg MaskGenerator object
	 * @param random Random generator object
	 */
	public void init(MessageDigest hash, MessageEncode mg,
			SecureRandom random) {
		digest = hash;
		mgf = (MGF1) mg;
		if (random == null) {
			rng = new SecureRandom();
		} else {
			rng = random;
		}
		digestLen = digest.getDigestLength();
		seedLen = digestLen;
	}

	/**
	 * EME1 Encoding operation
	 *
	 * @param src the source data
	 * @param emLen the encoded message length
	 * @return the encoded data
	 */
	protected byte[] encodeEME1(byte[] src, int emLen) {
		byte[] db, tmp, hash, seed, maskSeed;

		seedLen = digestLen;
		int msgLen = src.length;
		int psLen = emLen - digestLen - seedLen - 1 - msgLen;
		if (psLen < 0) {
			return null;			// Message too long
		}

		// S = 0x00 .... 0x00
		tmp = new byte[psLen + 1];
		// M' = S || 01 || M
		tmp[psLen] = (byte) 0x01;
		tmp = ByteArray.concat(tmp, src);

		// DB = cHash || M'
		digest.reset();
		db = digest.digest((byte[]) parameter);
		db = ByteArray.concat(db, tmp);

		seed = new byte[seedLen];
		rng.nextBytes(seed);

		// dbMask = MGF(seed, emLen - seedLen)
		mgf.init((emLen - seedLen) * 8);
		maskSeed = mgf.encode(seed);

		// maskedDB = DB ^ dbMask
		for (int i = 0; i < emLen - seedLen; i++) {
			db[i] = (byte) (db[i] ^ maskSeed[i]);
		}

		// seedMask = MGF(maskedDB, seedLen)
		mgf.init(seedLen * 8);
		hash = mgf.encode(db);

		// maskedSeed = seed ^ seedMask
		for (int i = 0; i < seedLen; i++) {
			seed[i] = (byte) (seed[i] ^ hash[i]);
		}

		// EM = maskedSeed || maskedDB. 
		byte[] enc = new byte[seedLen];
		System.arraycopy(seed, 0, enc, 0, seedLen);
		enc = ByteArray.concat(enc, db);
		return enc;
	}

	/**
	 * EME1 Decoding operation
	 *
	 * @param enc the encoded data
	 * @return the decoded data
	 */
	protected byte[] decodeEME1(byte[] enc) {
		int dbLen;
		byte[] maskSeed, maskDB, db, seed;
		byte[] hash, hash2;

		seedLen = digestLen;
		encLen = enc.length;
		if (encLen < seedLen + digestLen + 1) {
			return null;			// Decoding error
		}

		maskSeed = new byte[seedLen];
		System.arraycopy(enc, 0, maskSeed, 0, seedLen);
		maskDB = new byte[encLen - seedLen];
		System.arraycopy(enc, seedLen, maskDB, 0, encLen - seedLen);

		// seedMask = MGF(maskedDB, seedLen)
		mgf.init(seedLen * 8);
		seed = mgf.encode(maskDB);

		// seed = maskedSeed ^ seedMask
		for (int i = 0; i < seedLen; i++) {
			seed[i] = (byte) (seed[i] ^ maskSeed[i]);
		}

		// dbMask = MGF(seed, encLen - seedLen)
		mgf.init((encLen - seedLen) * 8);
		db = mgf.encode(seed);

		// DB = DB ^ maskedDB
		for (int i = 0; i < encLen - seedLen; i++) {
			db[i] = (byte) (db[i] ^ maskDB[i]);
		}

		digest.reset();
		hash = digest.digest((byte[]) parameter);

		hash2 = new byte[digestLen];
		System.arraycopy(db, 0, hash2, 0, digestLen);

		if (!ByteArray.isEqual(hash, hash2)) {
			return null;
		}

		// Exist Separate Code(0x01) ?
		dbLen = db.length;
		int off;
		for (off = digestLen; off < dbLen; off++) {
			if (db[off] != 0) {
				break;
			}
		}
		if (off == dbLen || db[off] != 0x01) {
			return null;
		}
		off++;
		int msgLen = dbLen - off;
		byte[] bM = new byte[msgLen];
		for (int i = 0; i < msgLen; i++) {
			bM[i] = db[off++];
		}

		return bM;
	}

}
