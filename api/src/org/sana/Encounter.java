/**
 * 
 */
package org.sana;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An instance of Procedure execution where Observations were collected.
 * 
 * @author Sana Development
 *
 */
public class Encounter implements Unique{
	
	private String uuid;
	private Subject subject;
	private Procedure procedure;
	private Observer observer;
	private List<Observation> observations;
	
	/** Default Constructor */
	public Encounter(){
		this.observations = new ArrayList<Observation>();
	}
	
	/**
	 * Instantiates a new Encounter with a specified id.
	 * 
	 * @param uuid The UUID of the instance
	 */
	public Encounter(String uuid){
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
	 * @return the subject
	 */
	public Subject getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	/**
	 * @return the procedure
	 */
	public Procedure getProcedure() {
		return procedure;
	}

	/**
	 * @param procedure the procedure to set
	 */
	public void setProcedure(Procedure procedure) {
		this.procedure = procedure;
	}

	/**
	 * @return the observer
	 */
	public Observer getObserver() {
		return observer;
	}

	/**
	 * @param observer the observer to set
	 */
	public void setObserver(Observer observer) {
		this.observer = observer;
	}

	/**
	 * Returns the observations associated with this encounter;
	 * 
	 * @return the observations
	 */
	public List<Observation> getObservations() {
		return observations;
	}

	/**
	 * Removes and sets the observations for this encounter. 
	 * {@link #addObservations(Collection)} should generally be used instead of
	 * this method.
	 * 
	 * @param observations the observations to set
	 */
	public void setObservations(Collection<Observation> observations) {
		this.observations = new ArrayList<Observation>(observations);
	}

	/**
	 * Sets the observations for this encounter.
	 * 
	 * @param observations the observations to set
	 */
	public void addObservation(Observation observation) {
		observations.add(observation);
	}
	
	/**
	 * Sets the observations for this encounter.
	 * 
	 * @param observations the observations to set
	 */
	public void addObservations(Collection<Observation> observations) {
		observations.addAll(observations);
	}
	
	/**
	 * Sets the observations for this encounter.
	 * 
	 * @param observations the observations to set
	 */
	public void removeObservation(Observation observation) {
		observations.remove(observation);
	}
	
}
