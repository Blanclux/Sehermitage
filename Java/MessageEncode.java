/**
 * MessageEncode.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.crypto;

import java.security.*;

/**
 * MessageEncode Class
 */
public abstract class MessageEncode {

	/** Algorithm name */
	protected String algorithm;

	/** Parameter */
	protected Object parameter;

	/** Length of an encoded data (bits) */
	protected int encLen;

	/** Message digest */
	protected MessageDigest digest;

	/** Digest Length */
	protected int digestLen;

	/**
     * MessageEncode
     */
	public MessageEncode(String alg) {
		algorithm = alg;
	}

	public MessageEncode() {
	}

	/**
     * Returns the algorithm name.
     *
     * @return  the algorithm name
     */
	public String getAlgorithm() {

		return algorithm;
	}

	/**
     * Set the parameter.
     *
     * @param parameter the parameter
     */
	public void setParameter(Object parameter) {
		this.parameter = parameter;
	}

	/**
     * Returns the parameter.
     *
     * @return the parameter
     */
	public Object getParameter() {

		return parameter;
	}

	/**
     * Initialize (Set the encoded length)
     *
     * @param length the encoded length
     */
	public void init(int length) {
		encLen = length;
	}

	/**
     * Initialize (Set MessageDigest)
     *
     * @param digest MessageDigest
     */
	public void init(MessageDigest digest) {
		this.digest = digest;
		digest.reset();
		digestLen = digest.getDigestLength();
	}

	/**
     * Returns the encoded length.
     *
     * @return the encoded length
     */
	public int getEncodeLen() {

		return encLen;
	}

	/**
     * Returns the MessageDigest.
     *
     * @return the MessageDigest
     */
	public MessageDigest getMessageDigest() {

		return digest;
	}

	/**
     * Generates a MessageEncode object that implements the algorithm
     * requested.
     *
     * @param algorithm the standard string name of the algorithm.
     * @return the new MessageEncode object.
     * @exception NoSuchAlgorithmException if there is no such algorithm.
     */
	public static MessageEncode getInstance(String algorithm)
									 throws NoSuchAlgorithmException {
		try {
			Class<?> c = Class.forName("Blanclux.crypto.encode." + algorithm);

			return (MessageEncode) c.newInstance();
		} catch (ClassNotFoundException e) {
			throw new NoSuchAlgorithmException("Class Blanclux.crypto.encode."
						 + algorithm + " cannot be found.\n");
		} catch (InstantiationException e) {
			throw new NoSuchAlgorithmException("Class Blanclux.crypto.encode."
						 + algorithm + " cannot be instantiated.\n");
		} catch (IllegalAccessException e) {
			throw new NoSuchAlgorithmException("Class Blanclux.crypto.encode."
						 + algorithm + " cannot be accessed.\n");
		}
	}

	/**
     * Convert an integer to bytes
     *
     * @param x the integer
     * @return the converted byte data
     */
	protected byte[] int2Byte(int x) {
		byte[] out = new byte[4];

		out[3] = (byte) (x & 0xff);
		out[2] = (byte) ((x >>>= 8) & 0xff);
		out[1] = (byte) ((x >>>= 8) & 0xff);
		out[0] = (byte) ((x >>>= 8) & 0xff);

		return out;
	}

	/**
     * Encode process
     *
     * @param src the source data
     * @return the encode data
     */
	public abstract byte[] encode(byte[] src);

	/**
     * Decode process
     *
     * @param enc the encoded data
     * @return the decode data
     */
	public abstract byte[] decode(byte[] enc);
}