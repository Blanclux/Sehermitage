/*
 * HMacKey.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.crypto.key;

import javax.crypto.*;

/**
 * HMacKey
 */
public class HMacKey implements SecretKey {

	private static final long serialVersionUID = -5698558267043200935L;
	private byte[] tKey;

	/**
	 * Constructor
	 *
	 * @param key the Hmac key
	 */
	public HMacKey(byte[] key) {
		tKey = new byte[key.length];

		System.arraycopy(key, 0, tKey, 0, key.length);
	}

	 /**
	  * Returns the standard algorithm name for this key.
	  *
	  * @return the name of the algorithm associated with this key
	  */
	public String getAlgorithm() {
		return "HMacKey";
	}
	
	/**
	 * Returns the name of the primary encoding format of this key.
	 *
	 * @return "RAW"
	 */
	public String getFormat() {
		return "RAW";
	}

	/**
	 * Returns the key in its primary encoding format.
	 *
	 * @return the secret key (RAW Format)
	 */
	public byte[] getEncoded() {
		byte[] tmp = new byte[tKey.length];

		System.arraycopy(tKey, 0, tmp, 0, tKey.length);
		return tmp;
	}

}
