/**
 * 
 */
package org.sana;

/**
 * Entity about whom data is collected. Most implementations will want to 
 * extend this class to include more useful annotations such as names, 
 * locations, and other fields which are typically static over the lifetime
 * of the Subject's interaction with the system.
 * 
 * @author Sana Development
 *
 */
public class Subject implements Unique{
	private String uuid;
		
	/** Default Constructor */
	public Subject(){}

	/**
	 * Creates a new instance with a specified unique id.
	 * 
	 * @param uuid The UUID of the instance
	 */
	public Subject(String uuid){
		this.uuid = uuid;
	}

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
