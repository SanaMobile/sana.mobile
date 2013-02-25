package org.sana;

/**
 * Physical object which executes a Procedure for data collection and 
 * transmission.
 * 
 * @author Sana Development
 *
 */
public class Device implements Unique{
	
	private String uuid;
	private String name; 
		
	/** Default Constructor */
	public Device(){}

	/**
	 * Creates a new instance with a specified unique id.
	 * 
	 * @param uuid The UUID of the instance
	 */
	public Device(String uuid){
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

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


}