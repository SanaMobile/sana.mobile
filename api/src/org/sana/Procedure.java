/**
 * 
 */
package org.sana;

/**
 * A set of instructions for collecting data.
 * 
 * @author Sana Development
 *
 */
public class Procedure implements Unique{
	private String uuid;
	private String author;
	private String version;
    private String description;
	private String src;
	
	/** Default Constructor */
	public Procedure(){}

	/**
	 * Creates a new instance with a specified unique id.
	 * 
	 * @param uuid The UUID of the instance
	 */
	public Procedure(String uuid){
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
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the source uri or file path String. 
	 * @return the src
	 */
	public String getSrc() {
		return src;
	}
	
	/**
	 * Sets the source file uri or path.
	 * 
	 * @param src the src to set
	 */
	public void setSrc(String src) {
		this.src = src;
	}
	
}