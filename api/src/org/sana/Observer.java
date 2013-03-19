/**
 * 
 */
package org.sana;

/**
 * An entity that collects data.
 * 
 * @author Sana Development
 *
 */
public class Observer extends AbstractModel{

	private String uuid;
	private String username;
	private String password;
	private String role;
	
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

	/**
	 * Returns the username for an instance of this class.
	 * 
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username for an instance of this class. 
	 *
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Returns the password for an instance of this class.
	 * 
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password for an instance of this class. 
	 *
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Returns the role for an instance of this class.
	 * 
	 * @return the role
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Sets the role for an instance of this class. 
	 *
	 * @param role the role to set
	 */
	public void setRole(String role) {
		this.role = role;
	}
	
}
