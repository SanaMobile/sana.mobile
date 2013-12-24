package org.sana.core;

import org.sana.api.IDevice;


/**
 * Physical object which executes a Procedure for data collection and 
 * transmission.
 * 
 * @author Sana Development
 *
 */
public class Device extends Model implements IDevice{
	
	public String name; 
		
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

	/* (non-Javadoc)
	 * @see org.sana.core.IDevice#getName()
	 */
	@Override
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