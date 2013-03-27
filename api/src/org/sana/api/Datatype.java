/**
 * 
 */
package org.sana.api;

/**
 * Allowed Concept data types correlating with the types recognized in the 
 * XML 1.1 specification.
 * 
 * @author Sana Development
 *
 */
public enum Datatype {
	/** Any character sequence. */
	STRING,
	/** Logical truth values. */
	BOOLEAN,
	/** Integer numeric data type. */
	INTEGER,
	/** Numeric type which can be represented as a decimal number */
	DECIMAL,
	/** Date value formatted as <code>DD-MM-YYYY</code> */
	DATE,
	/** Time value formatted as <code>HH:MM:SS</code> */
	TIME,
	/** Date and time value formatted as <code>DD-MM-YYYY HH:MM:SS</code>. */
	DATETIME,
	/** Qualified name for URI references. */
	QNAME;
	
	/** Case insensitive matching of a <code>String</code> to a Datatype
	 * 
	 * @param type The String to match
	 * @return
	 */
	public Datatype fromString(String type){
		return Datatype.valueOf(type.toUpperCase());
	}
}
