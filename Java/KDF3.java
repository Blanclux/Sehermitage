/**
 * KDF3.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.crypto.encode;

import java.security.*;

import Blanclux.crypto.*;

/**
 * KDF3 Key Derivation Class
 */
public class KDF3 extends MessageEncode {

	/** Parameter */
	protected byte[] param;

	/**
	 * Constructor
	 */
	public KDF3() {
		super("KDF3");

		try {
			digest = MessageDigest.getInstance("SHA1");
			digestLen = digest.getDigestLength();
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
	}

	/**
	 * Key derivation
	 *
	 * @param info the seed information
	 * @return the generated key
	 */
	public byte[] encode(byte[] info) {
		int zLen = info.length;
		byte[] hashValue;
		byte[] cNum;
		byte[] tmp;

		param = (byte[]) parameter;

		if (param != null) {
			tmp = new byte[zLen + 4 + param.length];
		} else {
			tmp = new byte[zLen + 4];
		}

		int count = (encLen + digestLen - 1) / digestLen;
		int outLen = (encLen + 7) / 8;
		byte[] data = new byte[count * digestLen];
		int off = 0;

		for (int i = 0; i < count; i++) {
			// CB
			cNum = int2Byte(i);
			// CB || ZB || PB
			System.arraycopy(cNum, 0, tmp, 0, 4);
			System.arraycopy(info, 0, tmp, 4, zLen);

			if (parameter != null) {
				System.arraycopy(param, 0, tmp, zLen + 4, param.length);
			}

			// HB = Hash(CB || ZB || PB)
			digest.reset();
			hashValue = digest.digest(tmp);
			// MB = MB || HB
			System.arraycopy(hashValue, 0, data, off, digestLen);
			off += digestLen;
		}

		byte[] key = new byte[outLen];
		System.arraycopy(data, 0, key, 0, outLen);

		return key;
	}

	public byte[] decode(byte[] enc) {
		return null;
	}
}
