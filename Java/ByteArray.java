/**
 * ByteArray.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.util;

import java.io.*;

/**
 * Class used to represent an array of bytes as an Object.
 */
public class ByteArray {
	private byte bytes[];
	private int offset = 0;
	private static final int BITS_PER_CHAR = 8;

	/** Create a ByteArray with the default offset. */
	public ByteArray() {
		this(512);
	}

	/** Create a ByteArray with a specific default offset. */
	public ByteArray(int offset) {
		bytes = new byte[offset];
	}

	/** Create a ByteArray from a String. */
	public ByteArray(String s) {
		this(s.length());
		append(s);
	}

	/** Create a ByteArray from an array of bytes. */
	public ByteArray(byte b[]) {
		this(b.length);
		append(b);
	}


	/**
	 * Get data
	 */
	/** Return the bytes. */
	public byte[] getBytes() {
		return bytes.clone();
	}

	/** Copy the bytes. */
	public byte[] toBytes() {
		byte[] res = new byte[offset];

		System.arraycopy(bytes, 0, res, 0, offset);
		return res;
	}

	/** Return the i-th byte. */
	public byte get(int i) {
		if (i < offset) {
			return bytes[i];
		}
		return (byte) 0;
	}

	/** Return the number of bytes. */
	public int length() {
		return offset;
	}


	/**
	 * Append data
	 */
	/** Append a byte. */
	public void append(byte ch) {
		if (offset == bytes.length) {
			byte tmpbytes[] = bytes;
			bytes = new byte[tmpbytes.length * 2];
			System.arraycopy(tmpbytes, 0, bytes, 0, offset);
		}
		bytes[offset++] = ch;
	}

	/** Append a integer value. */
	public void append(int x) {
		byte[] tmp = itoByte(x);
		append(tmp);
	}

	/** Append a ByteArray. */
	public void append(ByteArray b) {
		if (bytes.length - offset < b.length()) {
			byte tmpbytes[] = bytes;
			bytes = new byte[tmpbytes.length + b.length()];
			System.arraycopy(tmpbytes, 0, bytes, 0, offset);
		}
		System.arraycopy(b.bytes, 0, bytes, offset, b.length());
		offset += b.length();
	}

	/** Append an array of bytes. */
	public void append(byte b[]) {
		if (b == null || b.length == 0) {
			return;
		}
		if (bytes.length - offset < b.length) {
			byte tmpbytes[] = bytes;
			bytes = new byte[tmpbytes.length + b.length];
			System.arraycopy(tmpbytes, 0, bytes, 0, offset);
		}
		System.arraycopy(b, 0, bytes, offset, b.length);
		offset += b.length;
	}

	public void append(byte b[], int size) {
		if (bytes.length - offset < size) {
			byte tmpbytes[] = bytes;
			bytes = new byte[tmpbytes.length + size];
			System.arraycopy(tmpbytes, 0, bytes, 0, offset);
		}
		System.arraycopy(b, 0, bytes, offset, size);
		offset += size;
	}

	public void append(byte b[], int start, int size) {
		if (start > b.length || start < 0) {
			start = 0;
		}
		if (bytes.length - offset < size) {
			byte tmpbytes[] = bytes;
			bytes = new byte[tmpbytes.length + size];
			System.arraycopy(tmpbytes, 0, bytes, 0, offset);
		}
		System.arraycopy(b, start, bytes, offset, size);
		offset += size;
	}

	/** Append a String. */
	public void append(String s) {
		append(s.getBytes());
	}


	/**
	 * Concatenate, Split, Clear
	 */
	public static byte[] concat(byte[] src1, int src1Off, int src1Len,
			byte[] src2, int src2Off, int src2Len) {
		byte[] buf = new byte[src1Len + src2Len];
		System.arraycopy(src1, src1Off, buf, 0, src1Len);
		System.arraycopy(src2, src2Off, buf, src1Len, src2Len);
		return buf;
	}

	public static byte[] concat(byte[] src1, byte[] src2) {
		return concat(src1, 0, src1.length, src2, 0, src2.length);
	}

	/** Split c to a(size) and b(cLen - size) */
	public static void split(ByteArray a, ByteArray b, byte[] c, int size) {
		int len;
		if (size >= c.length) {
			len = c.length;
		} else {
			len = size;
		}
		if (a == null) {
			b.clear();
			b.append(c);
			return;
		}
		a.clear();
		a.append(c, len);
		if (b == null) {
			return;
		}
		b.clear();
		b.append(c, len, c.length - len);
		return;
	}

	public void chop(int i) {
		offset -= i;
		if (offset < 0)	{
			offset = 0;
		}
	}

	public void clear() {
		offset = 0;
	}


	/**
	 * Compare
	 */
	public boolean equals(ByteArray b) {
		if (offset != b.length()) {
			return false;
		}
		for (int i = 0; i < offset; i++) {
			if (bytes[i] != b.get(i)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isEqual(byte[] a, byte[] b) {
		if (a.length != b.length) {
			return false;
		}
		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Compares two non-null byte arrays.
	 * Returns true if they are identical, false otherwise.
	 */
	public static boolean isEqual(byte[] a, int aOff,
				byte[] b, int bOff, int len) {
		for (int i = 0; i < len; i++) {
			if (a[aOff + i] != b[bOff + i]) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Exclusive OR
	 */
	public static byte[] xor(byte[] a, byte[] b, int len) {
		byte[] c = new byte[len];
		for (int i = 0; i < len; i++) {
			c[i] = (byte) (a[i] ^ b[i]);
		}
		return c;
	}

	public static byte[] xor(byte[] a, byte[] b) {
		int size = (a.length >= b.length) ? a.length : b.length;
		int sel = (a.length >= b.length) ? 1 : 0;

		byte[] c = new byte[size];
		if (sel == 1) {
			System.arraycopy(a, 0, c, 0, size);
			for (int i = 0; i < b.length; i++) {
				c[i] = (byte) (c[i] ^ b[i]);
			}
		} else {
			System.arraycopy(b, 0, c, 0, size);
			for (int i = 0; i < a.length; i++) {
				c[i] = (byte) (c[i] ^ a[i]);
			}
		}
		return c;
	}

	/**
	 * Reverse bit order 
	 */
	public static byte[] reverseBit(byte[] b) {
		byte[] rB = new byte[b.length];
		int bb;

		for (int i = b.length - 1; i >= 0; i--) { 
			if (b[i] < 0) {
				bb = b[i] & 255;
			} else {
				bb = b[i];
			}
			int k = b.length - i - 1;

			rB[k] = 0;
			for (int j = 0; j < 8; j++) {
				if ((bb & (1 << j)) != 0) {
					rB[k] |= 1;
				}
				if (j != 7) {
					rB[k] <<= 1;
				}
			}
		}
		return rB;
	}


	/**
	 * Convert to String.
	 */
	public String toString() {
		return new String(bytes, 0, offset);
	}

	public static void printHex(byte b) {
		if ((b & 0xff) < 0x10)
			System.out.print("0");

		System.out.print(Long.toString((b) & 0xff, 16));
	}

	/**
	 * converts a byte-array to the corresponding hexstring
	 *
	 * @param  bytes  the byte-array to be converted
     * @return the corresponding hexstring
     */
	public static String toHexStrFmt(byte[] bytes) {
		StringBuffer res = new StringBuffer();

		for (int i = 0; i < bytes.length; i++) {
			if (i % 4 == 0) {
				res.append(" ");
			}
			res.append(HEX_CHARS[(bytes[i] >>> 4) & 0x0f]);
			res.append(HEX_CHARS[(bytes[i]      ) & 0x0f]);
			if (i % 32 == 31) {
				res.append("\n");
			}
		}
		return res.toString();
	}

	public String toHexStrFmt() {
		return toHexStr(toBytes());
	}

	public static String toHexStr(byte[] bytes) {
		String res = "";

		for (int i = 0; i < bytes.length; i++) {
			res += (HEX_CHARS[(bytes[i] >>> 4) & 0x0f]);
			res += (HEX_CHARS[(bytes[i]      ) & 0x0f]);
		}
		return res;
	}

	public String toHexStr() {
		return toHexStr(toBytes());
	}

	private static final char[] HEX_CHARS = {
		'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
	};

	/**
	 * converts a string containing hexadecimal characters to a byte-array
	 *
	 * @param  s  hexstring
     * @return a byte array with the corresponding value
	 */
	public static byte[] strtoByte(String s) {
		byte [] res;
		char c;

		for (int i = 0; i < s.length(); i++) {
			c = s.charAt(i);
			if (!(((c >= '0') && (c <= '9')) ||
				  ((c >= 'a') && (c <= 'f')) ||
				  ((c >= 'A') && (c <= 'F')))) {
				s = s.substring(0, i) + s.substring(i + 1, s.length());
				i--;
			}
		}
		int qlen = s.length() / 2;
		int rlen = s.length() % 2;
		if (rlen != 0) {
			qlen++;
		}
		res = new byte[qlen];
		int pos = 0;
		if (rlen != 0) {
			res[0] = (byte)Integer.parseInt("0" + s.substring(0, 1), 16);
			pos = 1;
		} else {
			res[0] = (byte)Integer.parseInt(s.substring(0, 2), 16);
		}
		for (int i = 1; i < qlen; i++) {
			res[i] = (byte)Integer.parseInt(s.substring(2 * i - pos,
														(2 * i - pos) + 2), 16);
		}
		return res;
	}

	/**
	 * Convert integer to byte array
	 * (Big-endian format)
	 */
	public static byte[] itoByte(int a, int blen) {
		int i, elen;

		if (blen < 1) {
			return new byte[0];
		}
		byte[] b = new byte[blen];

		elen = blen - 4;
		if (elen > 0) {
			for (i = 0; i < elen; i++) {
				b[i] = 0;
			}
		} else {
			elen = 0;
		}
		for (i = blen - elen - 1; i >= 0; i--) {
			b[blen - i - 1] = (byte) ((a >>> (i * BITS_PER_CHAR))
				   % (1 << BITS_PER_CHAR));
		}
		return b;
	}
    
	public static byte[] itoByte(int x) {
		byte[] out = new byte[4];

		out[3] = (byte) (x & 0xff);
		out[2] = (byte) ((x >>>= 8) & 0xff);
		out[1] = (byte) ((x >>>= 8) & 0xff);
		out[0] = (byte) ((x >>>= 8) & 0xff);
		return out;
	}
	
	/**
	 * Convert integer to byte array (String format)
	 * (Big-endian format)
	 */
	public static byte[] itoByteStr(int num) {
		int idx[] = {1000, 100, 10, 1};
		byte buf[] = new byte[4];

		for (int i = 0; i < 4; i++) {
			buf[i] = (byte) (num / idx[i] + 0x30);
			num = num - (num / idx[i]) * idx[i];
		}
		return buf;
	}

	/**
	 * Convert byte array to integer
	 * (Big-endian format)
	 */
	public static int bytetoI(byte[] b, int blen) {
		int size = 0;
		int bch;

		if (b == null || blen < 1) {
			return -1;
		}
		for (int i = 0; i < blen; i++) {
			bch = b[i];
			if (bch < 0) {
			   bch = b[i] + 256;
			}
			size = (size << 8) | bch;
		}
		return size;
	}

	/**
	 * Read & Write
	 */
	public void writeTo(OutputStream out) throws IOException {
		out.write(bytes, 0, offset);
	}

	public static byte[] readData(String fname) {
		int dLen;
		byte data[] = new byte[0];

		try {
			FileInputStream in = new FileInputStream(fname);
			try {
				dLen = in.available();
				data = new byte[dLen];
				in.read(data, 0, dLen);
			} catch (Exception e) {
				System.err.println(e.toString());
				return data;
			} finally {
				in.close();
			}
		} catch (Exception ex) {
			System.err.println(ex.toString());
			return data;
		}
		return data;
	}

	public static boolean writeData(String fname, byte data[]) {

		try {
			FileOutputStream out = new FileOutputStream(fname);
			try {
				out.write(data, 0, data.length);
			} catch (Exception e) {
				System.err.println(e.toString());
				return false;
			} finally {
				out.close();
			}
		} catch (Exception ex) {
			System.err.println(ex.toString());
			return false;
		}
		return true;
	}

}
