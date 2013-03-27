package org.sana.core;

import java.io.File;

import org.sana.api.IEncounter;
import org.sana.api.IObservation;


/**
 * An instance of data collected about a Subject by executing an Instruction.
 * 
 * @author Sana Development
 *
 */
public class Observation extends Model implements IObservation {
	
	private String id;
	private IEncounter encounter;
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
	public IEncounter getEncounter() {
		return encounter;
	}
	/**
	 * @param encounter the encounter to set
	 */
	public void setEncounter(IEncounter encounter) {
		this.encounter = encounter;
	}
	/* (non-Javadoc)
	 * @see org.sana.core.IObservation#getConcept()
	 */
	@Override
	public Concept getConcept() {
		return concept;
	}
	/**
	 * @param concept the concept to set
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	/* (non-Javadoc)
	 * @see org.sana.core.IObservation#getValue()
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see org.sana.core.IObservation#isComplex()
	 */
	@Override
	public boolean isComplex(){
		//TODO make this more robust.
		return !concept.getMediatype().equals("text/plain");
	}
}
