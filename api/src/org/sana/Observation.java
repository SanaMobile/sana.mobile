package org.sana;

import java.io.File;

/**
 * An instance of data collected about a Subject during the execution of a 
 * Procedure on a Subject.
 * 
 * @author Sana Development
 *
 */
public class Observation implements Unique {
	
	private String uuid;
	private String id;
	private Encounter encounter;
	private Concept concept;
	private String value_complex;
	private String valueText; 
	
	/** Default Constructor */
	public Observation(){}
	
	/**
	 * Instantiates a new Observation with a specified id.
	 * 
	 * @param uuid The UUID of the instance
	 */
	public Observation(String uuid){
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
	 * Gets the unique id within the Encounter.
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the encounter
	 */
	public Encounter getEncounter() {
		return encounter;
	}
	/**
	 * @param encounter the encounter to set
	 */
	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}
	/**
	 * @return the concept
	 */
	public Concept getConcept() {
		return concept;
	}
	/**
	 * @param concept the concept to set
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	/**
	 * Returns either a File or String depending 
	 * @return the value
	 */
	public Object getValue() {
		if(isComplex()){
			return getValue_complex();
		} else {
			return getValue_text();
		}
	}
	/**
	 * This sets the internal value stored as either a File or String depending
	 * on whether the result of {@link Observation#isComplex()} is true. <br/>
	 * <br/>
	 * Note: if {@link Observation#isComplex()} is <code>true</code> and a 
	 * String instance is passed, it will be interpreted as a path.
	 * 
	 * @param value the value to set as either a File or String
	 */
	public void setValue(Object value) {
		if(isComplex()){
			setValue_text(null);
			if(value instanceof String){
				setValue_complex(value.toString());
			} else if(value instanceof File){
				setValue_complex(((File) value).getAbsolutePath());
			}
		} else {
			setValue_complex(null);
			setValue_text(String.valueOf(value));
		}
	}
	/**
	 * @return the value_complex
	 */
	public String getValue_complex() {
		return value_complex;
	}
	/**
	 * @param value_complex the value_complex to set
	 */
	public void setValue_complex(String valueComplex) {
		this.value_complex = valueComplex;
	}
	/**
	 * @return the valueText
	 */
	public String getValue_text() {
		return valueText;
	}
	/**
	 * @param valueText the valueText to set
	 */
	public void setValue_text(String valueText) {
		this.valueText = valueText;
	}
	
	/**
	 * Returns whether this instance's stored valued should be treated as either
	 * a File or String based on the mimetype of the Concept. 
	 * @return
	 */
	public boolean isComplex(){
		//TODO make this more robust.
		return !concept.getMediatype().equals("text/plain");
	}
}
