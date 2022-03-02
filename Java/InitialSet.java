/**
 * InitialSet.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.util;

import java.util.*;
import java.io.*;

/**
 * Initial file handling
 */
public class InitialSet {
	Properties props = new Properties();
	String initialFileName;

	/**
	 * Constructor
	 * 
	 * @param initialFile
	 *            the initial file
	 */
	public InitialSet(String initialFile) {
		initialFileName = initialFile;
	}

	/**
	 * Reads the initial file
	 */
	public int getProperties() {
		try {
			FileInputStream propsStreamIn = new FileInputStream(initialFileName);
			props.load(propsStreamIn);
			propsStreamIn.close();
			return 0;
		} catch (Exception e) {
			System.err.println("Can't find " + initialFileName);
			return 1;
		}
	}

	/**
	 * Writes the initial file
	 */
	public void setProperties() {
		try {
			FileOutputStream propsStreamOut = new FileOutputStream(
					initialFileName);
			props.store(propsStreamOut, "--- Initial Set File ---");
			propsStreamOut.close();
		} catch (Exception e) {
			System.err.println("Can't find " + initialFileName);
		}
	}

	/**
	 * Gets the property value
	 * 
	 * @param key
	 *            the keyword
	 * @param defaultValue
	 *            the default value
	 * @return the attribute value
	 */
	public String getProperty(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}

	/**
	 * Gets the property value
	 * 
	 * @param key
	 *            the keyword
	 * @return the attribute value
	 */
	public String getProperty(String key) {
		return props.getProperty(key);
	}

	/**
	 * Sets the property value
	 * 
	 * @param key
	 *            the keyword
	 * @param value
	 *            the attribute value
	 */
	public void setProperty(String key, String value) {
		props.setProperty(key, value);
	}

}
