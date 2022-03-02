/**
 * ElGamalPrivateKey.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.crypto.cipher;

import Blanclux.math.*;

import java.security.PrivateKey;

/**
 * ElGamalPrivateKey
 */
public class ElGamalPrivateKey implements PrivateKey {

	private static final long serialVersionUID = 1L;

	/** modulus */
	private MPInt p;

	/** generator */
	private MPInt g;

	/** public key */
	private MPInt y;

	/** private key */
	private MPInt x;

	/**
	 * ElGamalPrivateKey
	 */
	public ElGamalPrivateKey(MPInt modulus, MPInt generator,
							 MPInt publicKey, MPInt secretKey) {
		p = modulus;
		g = generator;
		y = publicKey;
		x = secretKey;
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
	 * Gets a private key
	 */
	public MPInt getPrivate() {
		return x;
	}

	/**
	 * Gets key information
	 */
	public String toString() {
		String out = "p = " + getModulus().toString(16) + "\n" + "g = "
				+ getGenerator().toString(16) + "\n" + "y = "
				+ getPublic().toString(16) + "\n" + "x = "
				+ getPrivate().toString(16) + "\n";

		return out;
	}
}
