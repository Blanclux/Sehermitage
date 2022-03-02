/**
 * ElGamalPublicKey.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.crypto.cipher;

import Blanclux.math.*;

import java.security.PublicKey;

/**
 * ElGamalPublicKey
 */
public class ElGamalPublicKey implements PublicKey {

	private static final long serialVersionUID = 1L;

	/** modulus */
	private MPInt p;

	/** generator */
	private MPInt g;

	/** public key */
	private MPInt y;

	/**
	 * ElGamalPublicKey
	 */
	public ElGamalPublicKey(MPInt modulus, MPInt generator, MPInt publicKey) {
		p = modulus;
		g = generator;
		y = publicKey;
	}

	/**
	 * Gets an algorithm name
	 */
	public String getAlgorithm() {
		return "ElGamal";
	}

	/**
	 * Gets an encoded key
	 */
	public byte[] getEncoded() {
		return toString().getBytes();
	}

	/**
	 * Gets an encoded format
	 */
	public String getFormat() {
		return "String";
	}

	/**
	 * Gets a generator
	 */
	public MPInt getGenerator() {
		return g;
	}

	/**
	 * Gets a modulus
	 */
	public MPInt getModulus() {
		return p;
	}

	/**
	 * Gets a public key
	 */
	public MPInt getPublic() {
		return y;
	}

	/**
	 * Gets key information
	 */
	public String toString() {
		String out = "p = " + getModulus().toString(16) + "\n" + "g = "
				+ getGenerator().toString(16) + "\n" + "y = "
				+ getPublic().toString(16) + "\n";
		return out;
	}
}
