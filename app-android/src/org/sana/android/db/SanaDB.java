package org.sana.android.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * This class defines the URI and data fields of the content providers for each of the content types uploaded to the MDS. This class also includes a subclass of the SQLiteOpenHelper class to help create the database.
 * @author Sana Development Team
 */
public final class SanaDB {
    /**
     * The authority for the procedures provider.
     */
    public static final String PROCEDURE_AUTHORITY = "org.sana.provider.Procedure";
    
    /**
     * The authority for the saved procedures provider.
     */
    public static final String SAVED_PROCEDURE_AUTHORITY = 
    	"org.sana.provider.SavedProcedure";
    
    /**
     * The authority for the sound provider.
     */
    public static final String BINARY_AUTHORITY = "org.sana.provider.Binary";
    
    /**
     * The authority for the image provider.
     */
    public static final String IMAGE_AUTHORITY = "org.sana.provider.Image";
    
    /**
     * The authority for the sound provider.
     */
    public static final String SOUND_AUTHORITY = "org.sana.provider.Sound";
    
    /**
     * The authority for the notification provider.
     */
    public static final String NOTIFICATION_AUTHORITY = "org.sana.provider.Notification";
    
    /**
     * The authority for the patient provider.
     */
    public static final String PATIENT_AUTHORITY = "org.sana.provider.Patient";
    
    /**
     * The authority for the event provider.
     */
    public static final String EVENT_AUTHORITY = "org.sana.provider.Event";
    
    /**
     * The authority for the helpinfo provider.
     */
    public static final String EDUCATIONRESOURCE_AUTHORITY = 
    	"org.sana.provider.EducationResource";
    
    /**
     * Name of SQLite database the content is stored in.
     */
    public static final String DATABASE_NAME = "sana.db";
    
    /**
     * SQLite database version.
     * 
     * Versions:
     * 1 - 1.0 release
     * 2 - Development versions between 1.0 and 1.1
     * 3 - 1.1 Release
     * 4 - Development versions between 1.1and 1.2
     * ...
     */
    public static final int DATABASE_VERSION = 4; 
    
    /**
     * This class defines the URI and data fields for the content provider storing the procedure xml.
     * @author Sana Development Team
     */
    public static final class ProcedureSQLFormat implements BaseColumns {
        private ProcedureSQLFormat() {
        }

        /**
         * The content:// style URI for this content provider.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PROCEDURE_AUTHORITY + "/procedures");

        /**
         * The MIME type of CONTENT_URI providing a directory of procedures.
         */
        public static final String CONTENT_TYPE = 
        	"vnd.android.cursor.dir/org.sana.android.procedure";

        /**
         * The MIME type of CONTENT_URI subdirectory of a single procedure.
         */
        public static final String CONTENT_ITEM_TYPE = 
        	"vnd.android.cursor.item/org.sana.android.procedure";

        /**
         * The default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        /**
         * The title of the procedure.
         */
        public static final String TITLE = "title";
        
        /**
         * The author of the procedure.
         */
        public static final String AUTHOR = "author";
        
        /**
         * The unique ID of the procedure.
         */
        public static final String GUID = "guid";

        /**
         * The procedure XML.
         */
        public static final String PROCEDURE = "procedure";

        /**
         * The date the procedure was created.
         */
        public static final String CREATED_DATE = "created";

        /**
         * The date the procedure was last modified.
         */
        public static final String MODIFIED_DATE = "modified";
    }
    
    /**
     * This class defines the URI and data fields for the content provider 
     * storing the text for a completed procedure form, referred to as a saved 
     * procedure.
     * 
     * @author Sana Development Team
     */
    public static final class SavedProcedureSQLFormat implements BaseColumns {
        
        private SavedProcedureSQLFormat() {
        }
        
        /**
         * The content:// style URI for this content provider.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + SAVED_PROCEDURE_AUTHORITY + "/savedProcedures");

        /**
         * The MIME type of CONTENT_URI providing a directory of saved procedures.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.sana.savedProcedure";

        /**
         * The MIME type of CONTENT_URI subdirectory of a single saved procedure.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.sana.savedProcedure";

        /**
         * The default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        
        /**
         * Sort by descending created column
         */
        public static final String CREATED_SORT_ORDER = "created DESC";
        
        /**
         * Sort by ascending upload_queue column
         */
        public static final String QUEUE_SORT_ORDER = "upload_queue ASC";
        
        // COLUMNS
        
        /**
         * The guid of this procedure (randomly generated on insert).
         */
        public static final String GUID = "GUID";

        /**
         * A foreign key to the procedure used to create this saved procedure.
         */
        public static final String PROCEDURE_ID = "procedure_id";
        
        /**
         * The JSON data representing the state of this procedure.
         */
        public static final String PROCEDURE_STATE = "procedure";
        

    	/** The subject who data was collected about; i.e. the patient */
        public static final String SUBJECT = "_subject";
        
        /**
         * Status of data inputting. When finished, upload data to MDS.
         */
        public static final String FINISHED = "finished";
        
        // This procedure's text/state has been uploaded to the MDS
		// successfully. This doesn't mean its binaries have been -- only the
		// text.
        /**
         * Status of procedure upload to MDS. Does not indicate status of 
         * binary upload -- only the text
         */
        public static final String UPLOADED = "uploaded";
        
        // Status of the procedure in the upload queue
        // For use in SavedProcedureList to show each procedure's status
        // 0 - Was never put into queue, or "Not Uploaded"
        // 1 - Still in the queue waiting to be sent
        // 2 - Upload Successful - has been sent to the MDS, no longer in queue
        // 3 - Upload in progress
        // 4 - In the queue but waiting for connectivity to upload
        // 5 - Upload failed
        // 6 - Upload stalled - username/password incorrect
        /**
         * Status of the procedure in the upload queue<br>
         * <pre><blockquote>
         * 0 - Was never put into queue, or "Not Uploaded"
         * 1 - Still in the queue waiting to be sent
         * 2 - Upload Successful - has been sent to the MDS, no longer in queue
         * 3 - Upload in progress
         * 4 - In the queue but waiting for connectivity to upload
         * 5 - Upload failed
         * 6 - Upload stalled - username/password incorrect
         * </blockquote></pre>
         */
        public static final String UPLOAD_STATUS = "upload_queue_status";
        
        // Keeps track of the background upload queue
        // >=0 -- In queue, shows position in line
        // =-1 -- Not in queue (either never added or upload finished)
        /**
         * Keeps track of the background upload queue.
         * <pre><blockquote>
         * >=0 - In queue, shows position in line
         * -1 - Not in queue (either never added or upload finished)
         * </blockquote></pre>
         */
        public static final String UPLOAD_QUEUE = "upload_queue";

        /**
         * The date the procedure was created.
         */
        public static final String CREATED_DATE = "created";

        /**
         * The date the procedure was last modified.
         */
        public static final String MODIFIED_DATE = "modified";
    }

    /**
     * This class defines the URI and data fields for the content provider 
     * storing the metadata for a binary file collected during a procedure.
     * 
     * @author Sana Development Team
     */
    public static final class BinarySQLFormat implements BaseColumns {
        private BinarySQLFormat() {
        }

        /**
         * The content:// style URI for this content provider.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + BINARY_AUTHORITY + "/binaries" );

        /** The MIME type of CONTENT_URI providing a directory of binaries.*/
        public static final String CONTENT_TYPE = 
        	"vnd.android.cursor.dir/org.sana.binary";

        /** The MIME type of CONTENT_URI subdirectory for a single binary.*/
        public static final String CONTENT_ITEM_TYPE = 
        	"vnd.android.cursor.item/org.sana.binary";

        /** Default sort order.*/
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        
        // COLUMNS
        /**
         * The id uniquely identifying this element within the procedure it is
         * associated with. The id is unique in the context of a procedure xml.
         */
        public static final String ELEMENT_ID = "element_id";

        /**
         * The id of the saved procedure associated with the  file.
         */
        public static final String SAVED_PROCEDURE_ID = "procedure";
        
        /**
         * The number of bytes successfully uploaded to the MDS.
         */
        public static final String UPLOAD_PROGRESS = "upload_progress";
        
        /** Indicates whether the content file was completely uploaded. */
        public static final String UPLOADED = "uploaded";
        
        /** Date the file was created. */
        public static final String CREATED_DATE = "created";

        /** Date the file was last modified. */
        public static final String MODIFIED_DATE = "modified";

        /** 
         * Holds the content style uri for the item. What gets used to open a 
         * file stream.  
         */
        public static final String CONTENT = "_content";
        
        /** Mime type of the item. */
        public static final String MIME = "_mime";
        
        /** The absolute path on the file system */
        public static final String DATA = "_data";
        
        /** Text data associated with this observation  */
        public static final String TEXT = "_text";
        
        /** Flag whether this is a file or string data  */
        public static final String COMPLEX = "_complex";
        
    }
    
    /**
     * This class defines the URI and data fields for the content provider 
     * storing the metadata for an image collected during a procedure.
     * @author Sana Development Team
     */
    public static final class ImageSQLFormat implements BaseColumns {
        private ImageSQLFormat() {
        }

        /**
         * The content:// style URI for this content provider.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + IMAGE_AUTHORITY + "/images");

        /**
         * The MIME type of CONTENT_URI providing a directory of images.
         */
        public static final String CONTENT_TYPE = 
        	"vnd.android.cursor.dir/org.sana.savedProcedure";
        
        /**
         * The MIME type of CONTENT_URI subdirectory of a single image.
         */
        public static final String CONTENT_ITEM_TYPE = 
        	"vnd.android.cursor.item/org.sana.image";

        /**
         * Default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        // COLUMNS
        
        /**
         * The id of the saved procedure the image is associated with.
         */
        public static final String SAVED_PROCEDURE_ID = "procedure";
        
        /**
         * The id uniquely identifying this element in the procedure it is 
         * associated with. The id is unique in the context of a procedure xml.
         */
        public static final String ELEMENT_ID = "element_id";
        
        /**
         * The URI of the image file.
         */
        public static final String FILE_URI = "file_uri";
        
        // Is the file written completely to storage yet?
        /**
         * Indicates if the file is completely written to storage.
         */
        public static final String FILE_VALID = "file_valid";

        /**
         * The file size in bytes.
         */
        public static final String FILE_SIZE = "file_size";
        
        /**
         * The number of bytes successfully uploaded to the MDS.
         */
        public static final String UPLOAD_PROGRESS = "upload_progress";
        
        /**
         * Indicates whether or not the image is completely uploaded.
         */
        public static final String UPLOADED = "uploaded";

        /**
         * The date the image was created.
         */
        public static final String CREATED_DATE = "created";

        /**
         * The date the image was last modified.
         */
        public static final String MODIFIED_DATE = "modified";
    }

    /**
     * This class defines the URI and data fields for the content provider 
     * storing the metadata for a sound recording collected during a procedure.
     * @author Sana Development Team
     *
     */
    public static final class SoundSQLFormat implements BaseColumns {
        private SoundSQLFormat() {
        }

        /**
         * The content:// style URI for this content provider.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + SOUND_AUTHORITY + "/sounds");

        /**
         * The MIME type of CONTENT_URI providing a directory of sound files.
         */
        public static final String CONTENT_TYPE = 
        	"vnd.android.cursor.dir/org.sana.sound";

        /**
         * The MIME type of CONTENT_URI subdirectory for a single sound file.
         */
        public static final String CONTENT_ITEM_TYPE = 
        	"vnd.android.cursor.item/org.sana.sound";

        /**
         * Default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        
        // COLUMNS

        /**
         * The id uniquely identifying this sound element in the procedure it is
         * associated with. The id is unique in the context of a procedure xml.
         */
        public static final String ELEMENT_ID = "element_id";

        /**
         * The id of the saved procedure associated with the sound file.
         */
        public static final String SAVED_PROCEDURE_ID = "procedure";
        
        /**
         * The URI of the sound file.
         */
        public static final String FILE_URI = "file_uri";
        
        // Is the file written completely to storage yet?
//        public static final String FILE_VALID = "file_valid";
//
//        public static final String FILE_SIZE = "file_size";
//        
        /**
         * The number of bytes successfully uploaded to the MDS.
         */
        public static final String UPLOAD_PROGRESS = "upload_progress";
        
        /**
         * Indicates whether the sound file is completely uploaded.
         */
        public static final String UPLOADED = "uploaded";
        
        /**
         * Date the sound file was created.
         */
        public static final String CREATED_DATE = "created";

        /**
         * Date the sound file was last modified.
         */
        public static final String MODIFIED_DATE = "modified";
    }
    
    /**
     * This class defines the URI and data fields for the content provider 
     * storing notifications. Notifications are SMS messages sent to the 
     * Android phone. They may contain doctor recommendations or diagnoses.
     * @author Sana Development Team
     */
    public static final class NotificationSQLFormat implements BaseColumns {
        private NotificationSQLFormat() {
        }

        /**
         * The content:// style URI for this content provider.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + NOTIFICATION_AUTHORITY + "/notifications");

        /**
         * The MIME type of CONTENT_URI providing a directory of notifications.
         */
        public static final String CONTENT_TYPE = 
        	"vnd.android.cursor.dir/org.sana.notification";

        /**
         * The MIME type of CONTENT_URI subdirectory for a single notification.
         */
        public static final String CONTENT_ITEM_TYPE = 
        	"vnd.android.cursor.item/org.sana.notification";

        /**
         * Default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        
        // COLUMNS
        
        /**
         * The unique id of the notification.
         */
        public static final String NOTIFICATION_GUID = "notification_guid";

        /**
         * The procedure the notification is referring to.
         */
        public static final String PROCEDURE_ID = "procedure_id";

        /**
         * The id of the patient the notification is for.
         */
        public static final String PATIENT_ID = "patient_id";
        
        /**
         * The message body, containing part of the transmitted message. 
         * Messages may take more than one notification to transmit.
         */
        public static final String MESSAGE = "message";
        
        /**
         * The entire message, including header information specifying the 
         * patient and encounter the notification is associated with.
         */
        public static final String FULL_MESSAGE = "full_message";
        
        /**
         * Indicates whether the entire message is downloaded.
         */
        public static final String DOWNLOADED = "downloaded";
        
        /**
         * The date the notification was created.
         */
        public static final String CREATED_DATE = "created";

        /**
         * The date the notification was last modified.
         */
        public static final String MODIFIED_DATE = "modified";
    }
    
    /**
     * This class defines the URI and data fields for the content provider 
     * storing patient identification information. When patients enter their 
     * identification information, forms that patients have already completed 
     * will be pre-filled with their responses.
     * 
     * @author Sana Development Team
     *
     */
    public static final class PatientSQLFormat implements BaseColumns {
    	private PatientSQLFormat() {	
    	}
    	
    	/**
         * The content:// style URI for this content provider.
         */
    	public static final Uri CONTENT_URI = Uri.parse("content://"
    			+ PATIENT_AUTHORITY + "/patients");

    	/**
         * The MIME type of CONTENT_URI providing a directory of patients.
         */
    	public static final String CONTENT_TYPE = 
    		"vnd.android.cursor.dir/org.sana.patient";
    	
    	/**
         * The MIME type of CONTENT_URI subdirectory for a single patient.
         */
    	public static final String CONTENT_ITEM_TYPE = 
    		"vnd.android.cursor.item/org.sana.patient";
    	
    	/**
    	 * Default sort order.
    	 */
    	public static final String DEFAULT_SORT_ORDER = "patient_lastname ASC";
    	
    	//COLUMNS
    	
    	/**
    	 * The unique ID for a patient.
    	 */
    	public static final String PATIENT_ID = "patient_id";
    	
    	/**
    	 * The patient's date of birth.
    	 */
    	public static final String PATIENT_DOB = "patient_dob";
    	
    	/**
    	 * The patient's first name.
    	 */
    	public static final String PATIENT_FIRSTNAME = "patient_firstname";
    	
    	/**
    	 * The patient's last name.
    	 */
    	public static final String PATIENT_LASTNAME = "patient_lastname";
    	
    	/**
    	 * The patient's gender.
    	 */
    	public static final String PATIENT_GENDER = "patient_gender";
    	

    	/** An image of the patient */
    	public static final String IMAGE = "_data";
    	
    	/** The current registration state. */
    	public static final String STATE = "_state";
    	
    }
    
    /**
     * This class defines the URI and data fields for the content provider 
     * storing events that need to be tracked, such as crashes/exceptions, 
     * user actions, background process actions, and device actions.
     * @author Sana Development Team
     *
     */
    public static final class EventSQLFormat implements BaseColumns {
    	private EventSQLFormat() {
    		
    	}
    	
    	/**
         * The content:// style URI for this content provider.
         */
    	public static final Uri CONTENT_URI = Uri.parse(
    			"content://" + EVENT_AUTHORITY + "/events");
    	
    	/**
         * The MIME type of CONTENT_URI providing a directory of events.
         */
    	public static final String CONTENT_TYPE = 
    		"vnd.android.cursor.dir/org.sana.event";
    	
    	/**
         * The MIME type of a CONTENT_URI subdirectory of a single event. 
         */
    	public static final String CONTENT_ITEM_TYPE = 
    		"vnd.android.cursor.item/org.sana.event";
    	
    	/**
    	 * Default sort order.
    	 */
    	public static final String DEFAULT_SORT_ORDER = "modified DESC";
    	
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
    	
    	// COLUMNS
    	
    	/**
    	 * The type of the event. Choices are listed in enum EventType.
    	 */
    	public static final String EVENT_TYPE = "event_type";
    	
    	/**
    	 * The value of the event.
    	 */
    	public static final String EVENT_VALUE = "event_value";
    	
    	/**
    	 * Reference to the patient ID associated with the event.
    	 */
    	public static final String PATIENT_REFERENCE = "patient_reference";
    	
    	/**
    	 * Reference to the encounter ID associated with the event.
    	 */
    	public static final String ENCOUNTER_REFERENCE = "encounter_reference";
    	
    	/**
    	 * Reference to the user ID associated with the event.
    	 */
    	public static final String USER_REFERENCE = "user_reference";
    	
    	/**
    	 * Indicates if the event is uploaded.
    	 */
    	public static final String UPLOADED = "uploaded";
    	
    	/**
    	 * The date the event was created.
    	 */
    	public static final String CREATED_DATE = "created";

        /**
         * The date the event was last modified.
         */
        public static final String MODIFIED_DATE = "modified";
    	
    }
    
    /**
     * This class defines the URI and data fields for the content provider 
     * storing doctor information.
     * 
     * @author Sana Development Team
     *
     */
    public static final class DoctorGroupSQLFormat implements BaseColumns {
    	private DoctorGroupSQLFormat() {	
    	}
    	
    	/**
         * The content:// style URI for this content provider.
         */
    	//public static final Uri CONTENT_URI = Uri.parse("content://"
    		//	+ DOCTOR_GROUP_AUTHORITY + "/doctorGroups");
    	
    	/**
         * The MIME type of CONTENT_URI providing a directory of doctors.
         */
    	public static final String CONTENT_TYPE = 
    		"vnd.android.cursor.dir/org.sana.doctorGroup";
    	
    	/**
    	 * The MIME type of a CONTENT_URI subdirectory of a single doctor.
    	 */
    	public static final String CONTENT_ITEM_TYPE = 
    		"vnd.android.cursor.item/org.sana.doctorGroup";
    	
    	/**
    	 * Default sort order.
    	 */
    	public static final String DEFAULT_SORT_ORDER = "modified DESC";
    	
    	//COLUMNS
    	
    	/**
    	 * The doctor group id.
    	 */
    	public static final String DOCTOR_GROUP_ID = "doctor_group_id";
    	    	
    	/**
    	 * The doctor group name.
    	 */
    	public static final String DOCTOR_GROUP_NAME = "doctor_group_name";
    }
    
    /**
     * This class defines the URI and data fields for the content provider 
     * storing education resources.
     * @author Sana Development Team
     *
     */
    public static final class EducationResourceSQLFormat implements BaseColumns 
    {
        private EducationResourceSQLFormat() {
        }
        
        /**
         * The MIME type of CONTENT_URI providing a directory of education 
         * reources.
         */
    	public static final String CONTENT_TYPE = 
    								"vnd.android.cursor.dir/org.sana.info";
    	
    	
    	/**
    	 * The MIME type of a CONTENT_URI subdirectory of a single resource.
    	 */
    	public static final String CONTENT_ITEM_TYPE = 
    								"vnd.android.cursor.item/org.sana.info";

    	/**
         * The content:// style URI for this table
         */
    	public static final Uri CONTENT_URI = Uri.parse(
    					"content://" +EDUCATIONRESOURCE_AUTHORITY + "/info");
        
    }
    
    /**
     * This class helps open, create, and upgrade the database file.
     * @author Sana Development Team
     */
    public static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, SanaDB.DATABASE_NAME, null, SanaDB.DATABASE_VERSION);
        }

        /* (non-Javadoc)
         * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
         */
        /**
         * Creates a table for each content provider in the input database.
         * @param db The SQLite database where the tables are stored.
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            ProcedureProvider.onCreateDatabase(db);
            SavedProcedureProvider.onCreateDatabase(db);
            ImageProvider.onCreateDatabase(db);
            SoundProvider.onCreateDatabase(db);
            NotificationProvider.onCreateDatabase(db);
            PatientProvider.onCreateDatabase(db);
            EventProvider.onCreateDatabase(db);
            BinaryProvider.onCreateDatabase(db);
            ObservationProvider.onCreateDatabase(db);
            
        }

        /* (non-Javadoc)
         * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
         */
        /**
         * Upgrades the database version for each content provider in the input database.
         * @param db The SQLite database where the tables are stored.
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            ProcedureProvider.onUpgradeDatabase(db, oldVersion, newVersion);
            SavedProcedureProvider.onUpgradeDatabase(db, oldVersion, newVersion);
            BinaryProvider.onUpgradeDatabase(db, oldVersion, newVersion);
            ImageProvider.onUpgradeDatabase(db, oldVersion, newVersion);
            SoundProvider.onUpgradeDatabase(db, oldVersion, newVersion);
            NotificationProvider.onUpgradeDatabase(db, oldVersion, newVersion);
            PatientProvider.onUpgradeDatabase(db, oldVersion, newVersion);
            EventProvider.onUpgradeDatabase(db, oldVersion, newVersion);
            ObservationProvider.onUpgradeDatabase(db, oldVersion, newVersion);
        }
    }
    
}
