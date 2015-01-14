/**
 * 
 */
package org.sana.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.sana.api.IEncounter;
import org.sana.api.IObservation;
import org.sana.api.IProcedure;
import org.sana.api.ISubject;


/**
 * An instance of Procedure execution where Observations were collected.
 * 
 * @author Sana Development
 *
 */
public class Encounter extends Model implements IEncounter{
	
	public Subject subject;
	public Procedure procedure;
	public Observer observer;
	public List<Observation> observations;
	
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

	/* (non-Javadoc)
	 * @see org.sana.core.IEncounter#getSubject()
	 */
	@Override
	public ISubject getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	/* (non-Javadoc)
	 * @see org.sana.core.IEncounter#getProcedure()
	 */
	@Override
	public IProcedure getProcedure() {
		return procedure;
	}

	/**
	 * @param procedure the procedure to set
	 */
	public void setProcedure(Procedure procedure) {
		this.procedure = procedure;
	}

	/* (non-Javadoc)
	 * @see org.sana.core.IEncounter#getObserver()
	 */
	@Override
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
	public void removeObservation(IObservation observation) {
		observations.remove(observation);
	}
	
}
