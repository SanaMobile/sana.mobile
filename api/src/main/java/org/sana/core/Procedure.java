/**
 * 
 */
package org.sana.core;

import org.sana.api.IProcedure;


/**
 * A set of instructions for collecting data.
 * 
 * @author Sana Development
 *
 */
public class Procedure extends Model implements IProcedure{
	public String author;
	public String version;
	public String description;
    public String src;
	public String title;
	
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

	/* (non-Javadoc)
	 * @see org.sana.core.IProcedure#getAuthor()
	 */
	@Override
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/* (non-Javadoc)
	 * @see org.sana.core.IProcedure#getVersion()
	 */
	@Override
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/* (non-Javadoc)
	 * @see org.sana.core.IProcedure#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/* (non-Javadoc)
	 * @see org.sana.core.IProcedure#getSrc()
	 */
	@Override
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
	
	public String getTitle(){
		return title;
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	
}