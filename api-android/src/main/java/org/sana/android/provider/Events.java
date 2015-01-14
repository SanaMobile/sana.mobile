/**
 * Copyright (c) 2013, Sana
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sana nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL Sana BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.sana.android.provider;

import org.sana.core.Event;

import android.net.Uri;

/**
 * Metadata and contract for events in the database.
 * 
 * @author Sana Development Team
 */
public final class Events{
	/** The authority for the events. */
	public static final String AUTHORITY = "org.sana.provider";
	
	/** The content:// style URI for this content provider. */
	public static final Uri CONTENT_URI = Uri.parse(
			"content://" + AUTHORITY + "/core/event");
	
	/** The MIME type for a directory of events. */
	public static final String CONTENT_TYPE = 
		"vnd.android.cursor.dir/org.sana.event";
	
	/** The MIME type of a single event. */
	public static final String CONTENT_ITEM_TYPE = 
		"vnd.android.cursor.item/org.sana.event";
	
	/** Default sort order. */
	public static final String DEFAULT_SORT_ORDER = "modified DESC";

	private Events() {}
	
	/*
	 * This content provider is for recording random events we want to keep 
	 * track of in the app. These could be:
	 * 
	 * - Exceptions or crashes
	 * - User actions (for performance measurements)
	 * - Background process actions (failed uploads, successful uploads, 
	 * credential results)
	 * - Device actions (battery updates, GPS locations, network signal 
	 * strength, network status changes)
	 * 
	 * So this could just be type + data. Also it could include some basic 
	 * references like 
	 * - Patient reference
	 * - Encounter reference
	 * - Procedure reference
	 */
	/**
	 * A list of the types of events that are tracked. This includes 
	 * exceptions, user actions, background process actions, and mobile 
	 * device actions.
	 * @author Sana Development Team
	 *
	 */
	public enum EventType {
		// Run-time exceptions
		/**
		 * Run-time exception
		 */
		EXCEPTION,
		
		/**
		 * Run-time out of memory exception. 
		 */
		OUT_OF_MEMORY,
		
		// Encounter events
		
		// ProcedureRunner activity started (onCreate)
		/**
		 * ProcedureRunner activity started.
		 */
		ENCOUNTER_ACTIVITY_START_OR_RESUME, // confirmed
		
		// Called when we the loading task is started
		/**
		 * The loading task started.
		 */
		ENCOUNTER_LOAD_STARTED, // confirmed
		
		/**
		 * The loading task finished successfully. The UI should now be 
		 * present.
		 */
		ENCOUNTER_LOAD_FINISHED, // confirmed
		// Called when the load task failed to load a procedure
		/**
		 * The load task failed to load a procedure.
		 */
		ENCOUNTER_LOAD_FAILED, // confirmed
		
		/** A saved encounter was loaded. */
		ENCOUNTER_LOAD_SAVED, // confirmed
		
		/**
		 * A new encounter was loaded.
		 */
		ENCOUNTER_LOAD_NEW_ENCOUNTER, 
		
		/**
		 * The application warm-booted (orientation change, return from 
		 * background).
		 */
		ENCOUNTER_LOAD_HOTLOAD, // confirmed
		
		/**
		 * The patient lookup service started.
		 */
		ENCOUNTER_LOOKUP_PATIENT_START, // confirmed
		
		/**
		 * The patient was successfully found by the lookup service.
		 */
		ENCOUNTER_LOOKUP_PATIENT_SUCCESS, // confirmed
		
		/**
		 * The lookup service failed to find the patient.
		 */
		ENCOUNTER_LOOKUP_PATIENT_FAILED, // confirmed
		
		// When the user saved and quit 
		/**
		 * The user saved and quit the application.
		 */
		ENCOUNTER_SAVE_QUIT, // confirmed
		
		/**
		 * The user added the procedure to the upload queue and exited.
		 */
		ENCOUNTER_SAVE_UPLOAD, // confirmed
		
		/**
		 * The user discarded the form and exited.
		 */
		ENCOUNTER_DISCARD, // confirmed
		
		/**
		 * The user exited by some other action such as hitting Back on the 
		 * first page.
		 */
		ENCOUNTER_EXIT_NO_SAVE, // confirmed
		
		/** The user jumped to a question. */
		ENCOUNTER_JUMP_TO_QUESTION, // confirmed
		
		/**
		 * The user advanced to the next question.
		 */
		ENCOUNTER_NEXT_PAGE, // confirmed
		
		/**
		 * The user went back to the previous page.
		 */
		ENCOUNTER_PREVIOUS_PAGE, // confirmed
		
		/**
		 * The page validation failed.
		 */
		ENCOUNTER_PAGE_VALIDATION_FAILED, // confirmed
		
		
		
		// MDS Events
		/**
		 * The upload of an encounter to the MDS started. An encounter 
		 * includes answers to procedure questions and any media files.
		 */
		MDS_UPLOAD_START,
		/**
		 * The upload of procedure answers to the MDS started.
		 */
		MDS_UPLOAD_PROCEDURE_START,
		/**
		 * The upload of procedure answers to the MDS finished.
		 */
		MDS_UPLOAD_PROCEDURE_FINISH,
		/**
		 * The upload of procedure answers to the MDS failed.
		 */
		MDS_UPLOAD_PROCEDURE_FAILED,
		/**
		 * The upload of a binary file to the MDS started.
		 */
		MDS_UPLOAD_BINARY_START,
		/**
		 * The upload of a binary file to the MDS finished.
		 */
		MDS_UPLOAD_BINARY_FINISH,
		/**
		 * The upload of a binary file to the MDS failed.
		 */
		MDS_UPLOAD_BINARY_FAILED,
		/**
		 * The upload of a binary packet to the MDS started.
		 */
		MDS_UPLOAD_BINARY_CHUNK_START,
		/**
		 * The upload of a binary packet to the MDS finished.
		 */
		MDS_UPLOAD_BINARY_CHUNK_FINISH,
		/**
		 * The upload of a binary packet to the MDS failed.
		 */
		MDS_UPLOAD_BINARY_CHUNK_FAILED,
		/**
		 * The upload of an encounter to the MDS failed.
		 */
		MDS_UPLOAD_FAILED,
		/**
		 * The upload of an encounter to the MDS succeeded. All files have 
		 * been successfully uploaded (procedure answers and any related 
		 * media files).
		 */
		MDS_UPLOAD_SUCCESS,
		/**
		 * The MDS has validated the user credentials (OpenMRS username and 
		 * password).
		 */
		MDS_CREDENTIALS_VALIDATED,
		/**
		 * Syncing started. Syncing refers to user download of data from 
		 * the permanent data store through the mds. One example is 
		 * downloading of patient IDs to store on the mobile device.
		 */
		MDS_SYNC_START,
		/**
		 * Syncing failed.
		 */
		MDS_SYNC_FAILED,
		/**
		 * Syncing finished.
		 */
		MDS_SYNC_FINISH,
		
		// Network Events
		/**
		 * The network request timed out.
		 */
		NET_REQUEST_TIMEOUT,
		/**
		 * The radio status changed.
		 */
		NET_RADIO_STATUS_CHANGE,
		/**
		 * The radio signal strength changed.
		 */
		NET_RADIO_SIGNAL_STRENGTH,
		/**
		 * The radio transfer rate changed.
		 */
		NET_TRANSFER_RATE,
		
		// Phone Events
		/**
		 * The phone battery level is low.
		 */
		PHONE_BATTERY_LEVEL,
		/**
		 * The phone recorded a gps location.
		 */
		PHONE_GPS_LOCATION,
		/**
		 * The phone CPU load is too high.
		 */
		PHONE_CPU_LOAD,
		/**
		 * The phone memory usage is too high.
		 */
		PHONE_MEMORY_USAGE,
		
		/**
		 * An unspecified event.
		 */
		UNSPECIFIED
	}
	
	/**
	 * Contract for the Event table in the database.
	 * 
	 * @author Sana Development
	 *
	 */
	public static interface Contract extends BaseContract<Event>{

		/** The type of the event. {@link org.sana..android.provider.Events.EventType EventType}*/
		public static final String EVENT_TYPE = "event_type";
		/** The value of the event. */
		public static final String EVENT_VALUE = "event_value";
		/** Reference to the subject of the event. */
		public static final String SUBJECT = "subject";
		/** Reference to an encounter associated with the event. */
		public static final String ENCOUNTER = "encounter";
		/** Reference to the user ID associated with the event. */
		public static final String OBSERVER = "observer";
		/** Indicates if the event is uploaded. */
		public static final String UPLOADED = "uploaded";
		
	}

}