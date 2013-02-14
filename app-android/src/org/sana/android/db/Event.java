package org.sana.android.db;

/** 
 * A class representing an event as defined in core Sana API.
 * 
 * @author Sana Development Team
 *
 */
public class Event {
	public String event_type;
	public String event_value;
	public long event_time;
	public String encounter_reference;
	public String patient_reference;
	public String user_reference;
}
