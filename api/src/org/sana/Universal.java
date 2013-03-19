/**
 * 
 */
package org.sana;

/**
 * A declaration that an implementing class provides a universally unique
 * identifier, UUID.
 * 
 * @author Sana Development
 *
 */
public interface Universal {

	/** The regular expression for validating uuid Strings */ 
	public static final String UUID_REGEX = 
		"[a-f0-9]{8}-[a-f0-9]{4}-[1-5][a-f0-9]{3}-[89ab][a-f0-9]{3}-[a-f0-9]{12}";
	
	/** A universally unique identifier */
	public static final String UUID = "uuid";
	
	/** Returns a universally unique identifier */
	String getUuid();
	
	/** 
	 * Sets the instance's universally unique identifier
	 * 
	 * @param uuid the new UUID
	 * @throws IllegalArgumentException if the format of the argument does not 
	 * 	conform to {@link #UUID_REGEX}
	 */
	void setUuid(String uuid) throws IllegalArgumentException;
}
