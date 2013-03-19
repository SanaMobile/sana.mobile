package org.sana;

/** 
 * A class representing an event as defined in core Sana API.
 * 
 * @author Sana Development Team
 *
 */
public class Event extends AbstractModel{
	public String event_type;
	public String event_value;
	public long event_time;
	public String encounter;
	public String subject;
	public String observer;
}
