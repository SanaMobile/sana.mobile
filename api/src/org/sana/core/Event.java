package org.sana.core;

import org.sana.api.IEvent;


/** 
 * A class representing an event as defined in core Sana API.
 * 
 * @author Sana Development Team
 *
 */
public class Event extends Model implements IEvent{
	public String event_type;
	public String event_value;
	public long event_time;
	public String encounter;
	public String subject;
	public String observer;
	
	/* (non-Javadoc)
	 * @see org.sana.core.IEvent#getEvent_type()
	 */
	@Override
	public String getEvent_type() {
		return event_type;
	}
	/**
	 * Sets the event_type for an instance of this class. 
	 *
	 * @param event_type the event_type to set
	 */
	public void setEvent_type(String event_type) {
		this.event_type = event_type;
	}
	/* (non-Javadoc)
	 * @see org.sana.core.IEvent#getEvent_value()
	 */
	@Override
	public String getEvent_value() {
		return event_value;
	}
	/**
	 * Sets the event_value for an instance of this class. 
	 *
	 * @param event_value the event_value to set
	 */
	public void setEvent_value(String event_value) {
		this.event_value = event_value;
	}
	/* (non-Javadoc)
	 * @see org.sana.core.IEvent#getEvent_time()
	 */
	@Override
	public long getEvent_time() {
		return event_time;
	}
	/**
	 * Sets the event_time for an instance of this class. 
	 *
	 * @param event_time the event_time to set
	 */
	public void setEvent_time(long event_time) {
		this.event_time = event_time;
	}
	/* (non-Javadoc)
	 * @see org.sana.core.IEvent#getEncounter()
	 */
	@Override
	public String getEncounter() {
		return encounter;
	}
	/**
	 * Sets the encounter for an instance of this class. 
	 *
	 * @param encounter the encounter to set
	 */
	public void setEncounter(String encounter) {
		this.encounter = encounter;
	}
	/* (non-Javadoc)
	 * @see org.sana.core.IEvent#getSubject()
	 */
	@Override
	public String getSubject() {
		return subject;
	}
	/**
	 * Sets the subject for an instance of this class. 
	 *
	 * @param subject the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}
	/* (non-Javadoc)
	 * @see org.sana.core.IEvent#getObserver()
	 */
	@Override
	public String getObserver() {
		return observer;
	}
	/**
	 * Sets the observer for an instance of this class. 
	 *
	 * @param observer the observer to set
	 */
	public void setObserver(String observer) {
		this.observer = observer;
	}
}
