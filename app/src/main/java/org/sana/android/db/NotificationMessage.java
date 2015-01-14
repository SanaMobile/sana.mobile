package org.sana.android.db;

import java.util.HashMap;
import java.util.Map;

/**
 * A class representing a notification. May be multi-part.
 * 
 * @author Sana Development Team
 */
public class NotificationMessage {
	/** 
	 * A new empty notification
	 */
	public NotificationMessage() {
		receivedMessages = 0;
		totalMessages = 0;
		messages = new HashMap<Integer,String>();
	}
	public int receivedMessages;
	public int totalMessages;
	public Map<Integer, String> messages; 
}
