package org.sana.android.net;

/**
 * A representation of a notification sent from the MDS. This may be one segment
 * of a multi-part message.
 * 
 * @author Sana Development Team
 *
 */
public class MDSNotification {
	/** The MDS's ID for this notification. */
	public String n;
	
	/** The saved procedure UUID to which this notification refers. */
	public String c;
	
	/** The patient identifier. */ 
	public String p;
	
	/** This notification's count -- formatted like this: 
	 * <br/>
	 * <code>(?P<this_message>\d+)/(?P<total_messages>\d+)</code> 
	 */
	public String d;
}
