/**
 * MGF1.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.crypto.encode;

import java.security.*;
import Blanclux.crypto.*;


/**
 * MGF1 Mask Generation
 */
public class MGF1 extends MessageEncode {

	/**
	 * Constructor
	 */
	public MGF1() {
		algorithm = "MGF1";

		try {
			digest = MessageDigest.getInstance("SHA1");
			digestLen = digest.getDigestLength();
			digest.reset();
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
	}

	/**
	 * Mask generation
	 *
	 * @param msg the input data
	 * @return the mask data
	 */
	public byte[] encode(byte[] msg) {
		int hashLen = digest.getDigestLength();
		int outLen = (encLen + 7) / 8;
		int block = (outLen + hashLen - 1) / hashLen;
		byte[] bT = new byte[block * hashLen];
		byte[] mask = new byte[outLen];
		int off = 0;

		for (int i = 0; i < block; i++) {
			byte[] bC = int2Byte(i);
			byte[] bMC = new byte[msg.length + bC.length];

			System.arraycopy(msg, 0, bMC, 0, msg.length);
			System.arraycopy(bC, 0, bMC, msg.length, bC.length);

			digest.reset();
			byte[] bH = digest.digest(bMC);

			System.arraycopy(bH, 0, bT, off, hashLen);
			off += hashLen;
		}

		System.arraycopy(bT, 0, mask, 0, outLen);

		return mask;
	}

	public byte[] decode(byte[] enc) {
		return null;
	}
}
