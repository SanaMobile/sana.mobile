/** 
 * 
 */
package org.sana;

/**
 * A functional unit of meaning. All data must be annotated by at least one
 * Concept. Furthermore, all Classes defined within the data model are 
 * essentially convenience wrappers for the concrete implementation of a 
 * Concept.
 * 
 * @author Sana Development
 *
 */
public class Concept implements Unique{
	
	/** A universally unique identifier */
	private String uuid;
	
	/** 
	 * A machine friendly short name or identifier. <code>name</code> values
	 * should be formatted as [A-Z](_?[A-Z])*? 
	 */
	private String name;
	
	/** Human-readable name */
	private String displayName;

	/** Longer human-readable, narrative description. */
	private String description;
	
	/** XML compliant data type. @see org.sana.Datatype */
	private String datatype;
	
	/** Mime type. Default should be text/plain.  */
	private String mediatype;
	
	/** A validation function string to enforce on associated values
	 *  @see Observation#setValue(Object)
	 */
	private String constraint;
	
	/** Default Constructor */
	public Concept(){}
	
	/**
	 * Creates a new instance with a specified unique id.
	 * 
	 * @param uuid The UUID of the instance
	 */
	public Concept(String uuid){
		this.uuid = uuid;
	}
	
	public String getUuid() {
		return uuid;
	}

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

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param display_name the display_name to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
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
	 * @return the datatype
	 */
	public String getDatatype() {
		return datatype;
	}

	/**
	 * @param datatype the datatype to set
	 */
	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	/**
	 * @return the mediatype
	 */
	public String getMediatype() {
		return mediatype;
	}

	/**
	 * @param mediatype the mediatype to set
	 */
	public void setMediatype(String mediatype) {
		this.mediatype = mediatype;
	}

	/**
	 * @return the constraint
	 */
	public String getConstraint() {
		return constraint;
	}

	/**
	 * @param constraint the constraint to set
	 */
	public void setConstraint(String constraint) {
		this.constraint = constraint;
	}

}
