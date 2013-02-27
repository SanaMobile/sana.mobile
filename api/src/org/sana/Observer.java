/**
 * 
 */
package org.sana;

/**
 * An entity that collects data about a Subject during an Encounter.
 * 
 * @author Sana Development
 *
 */
public class Observer implements Unique{

	private String uuid;
	private String username;
	private String password;
	
	/** Default Constructor */
	public Observer(){}
	
	/**
	 * Creates a new instance with a specified unique id.
	 * 
	 * @param uuid The UUID of the instance
	 */
	public Observer(String uuid){
		this.uuid = uuid;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
}
