/*
 * HMacParameterSpec.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.crypto.spec;

import java.security.spec.*;


/**
 * HMacParameterSpec
 */
public class HMacParameterSpec
	implements AlgorithmParameterSpec {

	/** Message Digest algorithms */
	protected String algorithm;

	/**
     * Creates a new HMacParameterSpec
     * 
     * @param algorithm the Message Digest algorithm
     */
	public HMacParameterSpec(String algorithm) {
		this.algorithm = algorithm;
	}

	/**
     * Returns the MessageDigest algorithm
     *
     * @return the MessageDigest algorithm
     */
	public String getAlgorithm() {

		return algorithm;
	}
}