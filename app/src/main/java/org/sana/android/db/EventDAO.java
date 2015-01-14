package org.sana.android.db;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.sana.android.provider.Events;
import org.sana.android.provider.Events.Contract;
import org.sana.android.provider.Events.EventType;


import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

/**
 * Utility class for interacting with the EventProvider.
 * 
 * @author Sana Development Team
 *
 */
public class EventDAO {
	private static final String TAG = EventDAO.class.getName();

	/**
	 * Inserts a new event.
	 * 
	 * @param c the current context.
	 * @param type the event type.
	 * @param value the event value
	 * @param encounterRef an encounter id.
	 * @param patientRef a subject id.
	 * @param userRef the user.
	 */
	public static void registerEvent(Context c, EventType type, String value, 
			String encounterRef, String patientRef, String userRef) 
	{
		Log.i(TAG, "Event " + type + " Value: '" + value + "' Encounter: '" 
				+ encounterRef + "' Patient: '" + patientRef + "' User: '" 
				+ userRef + "'");
		ContentValues cv = new ContentValues();
		cv.put(Contract.EVENT_TYPE, type.toString());
		cv.put(Contract.EVENT_VALUE, value);
		
		if (encounterRef != null)
			cv.put(Contract.ENCOUNTER, encounterRef);
		if (patientRef != null)
			cv.put(Contract.SUBJECT, patientRef);
		if (userRef != null)
			cv.put(Contract.OBSERVER, userRef);
		
		c.getContentResolver().insert(Events.CONTENT_URI, cv);
	}
	
	/**
	 * inserts a new event with no encounter, subject, or user references.
	 * @param c the current context.
	 * @param type the event type.
	 * @param value the event value
	 */
	public static void registerEvent(Context c, EventType type, String value) {
		registerEvent(c, type, value, "", "", "");
	}
	
	/**
	 * Inserts a new event with only the type defined.
	 * @param c the current context.
	 * @param type the event type.
	 */
	public static void registerEvent(Context c, EventType type) {
		registerEvent(c, type, "", "", "", "");
	}
	
	/** Utility for printing the stack trace */
	public static String getStackTrace(Throwable throwable) {
	    Writer writer = new StringWriter();
	    PrintWriter printWriter = new PrintWriter(writer);
	    throwable.printStackTrace(printWriter);
	    return writer.toString();
	}

	/**
	 * Logs and registers an exception.
	 * 
	 * @param c the current context.
	 * @param e  the exception.
	 */
	public static void logException(Context c, Throwable e) {
		logException(c, e, "", "", "");
	}
	
	/**
	 * Logs and registers an exception.
	 * 
	 * @param c the current context.
	 * @param e  the exception.
	 * @param encounterRef an encounter id.
	 * @param patientRef a subject id.
	 * @param userRef the user.
	 */
	public static void logException(Context c, Throwable e, String encounterRef,
			String patientRef, String userRef) 
	{
		String stackTrace = getStackTrace(e);
		
		EventType et = EventType.EXCEPTION;
		if (e instanceof OutOfMemoryError) {
			et = EventType.OUT_OF_MEMORY;
		}
		registerEvent(c, et, stackTrace, encounterRef, patientRef, userRef);
	}
}
