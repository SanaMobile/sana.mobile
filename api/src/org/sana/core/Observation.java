package org.sana.core;

import java.io.File;

import org.sana.api.IObservation;


/**
 * An instance of data collected about a Subject by executing an Instruction.
 * 
 * @author Sana Development
 *
 */
public class Observation extends Model implements IObservation {
	
	private String id;
	private String encounter;
	private String concept;
	private String value_complex;
	private String valueText;
	private boolean isComplex = false;
	
	/** Default Constructor */
	public Observation(){
		super();
	}
	
	/**
	 * Instantiates a new Observation with a specified id.
	 * 
	 * @param uuid The UUID of the instance
	 */
	public Observation(String uuid){
		super();
		this.uuid = uuid;
	}
	
	/* (non-Javadoc)
	 * @see org.sana.core.IObservation#getId()
	 */
	@Override
	public String getId() {
		return id;
	}
	
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/* (non-Javadoc)
	 * @see org.sana.core.IObservation#getEncounter()
	 */
	@Override
	public String getEncounter() {
		return encounter;
	}
	
	/**
	 * Sets the uuid of the encounter.
	 * 
	 * @param encounter the uuid to set
	 */
	public void setEncounter(String encounter) {
		this.encounter = encounter;
	}

	/**
	 * Sets the encounter uuid stored in this object from the uuid of an 
	 * {@link org.sana.core.Encounter} instance;
	 * @param encounter
	 */
	public void setEncounter(Encounter encounter) {
		this.encounter = encounter.getUuid();
	}

	/* (non-Javadoc)
	 * @see org.sana.core.IObservation#getConcept()
	 */
	@Override
	public String getConcept() {
		return concept;
	}
	/**
	 * @param concept the concept to set
	 */
	public void setConcept(String concept) {
		this.concept = concept;
	}
	
	/**
	 * Sets the value of whether this observation's stored value is complex 
	 * based on the value of {@link org.sana.core.Concept#isComplex()} and the 
	 * uuid of the concept this observation represents.
	 * 
	 * @param concept 
	 */
	public void setConcept(Concept concept){
		this.concept = concept.getUuid();
		setIsComplex(concept);
	}
	
	/**
	 * Convenience wrapper which returns either a File or String depending on 
	 * this object's Concept is complex. 
	 * 
	 * @return the value
	 */
	public Object getValue() {
		if(getIsComplex()){
			return getValue_complex();
		} else {
			return getValue_text();
		}
	}
	
	/**
	 * This sets the internal value stored as either a File or String depending
	 * on whether the result of {@link Observation#getIsComplex()} is true. <br/>
	 * <br/>
	 * Note: if {@link Observation#getIsComplex()} is <code>true</code> and a 
	 * String instance is passed, it will be interpreted as a path.
	 * 
	 * @param value the value to set as either a File or String
	 */
	public void setValue(Object value) {
		if(getIsComplex()){
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
	
	/* (non-Javadoc)
	 * @see org.sana.core.IObservation#getValue_complex()
	 */
	@Override
	public String getValue_complex() {
		return value_complex;
	}
	/**
	 * @param value_complex the value_complex to set
	 */
	public void setValue_complex(String valueComplex) {
		this.value_complex = valueComplex;
	}
	/* (non-Javadoc)
	 * @see org.sana.core.IObservation#getValue_text()
	 */
	@Override
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
	 * Returns whether this instance's stored valued represents a binary or text
	 * blob. 
	 * 
	 * @return
	 */
	public boolean getIsComplex(){
		return isComplex;
	}
	
	public void setIsComplex(Concept concept){
		isComplex = concept.isComplex();
	}
}
