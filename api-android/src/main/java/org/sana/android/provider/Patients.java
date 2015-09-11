package org.sana.android.provider;

import android.net.Uri;

import org.sana.core.Patient;

/**
 * This class defines the URI and data fields for the content provider 
 * storing patient identification information. When patients enter their 
 * identification information, forms that patients have already completed 
 * will be pre-filled with their responses.
 * 
 * @author Sana Development Team
 *
 */
public final class Patients{
	
	public static final String AUTHORITY = "org.sana.provider";

	public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

	/**
     * The MIME type of CONTENT_URI providing a directory of patients.
     */
	public static final String CONTENT_TYPE = 
		"vnd.android.cursor.dir/org.sana.patient";
	
	/** The content type of {@link #CONTENT_URI} for a single instance. */
	public static final String CONTENT_ITEM_TYPE = 
		"vnd.android.cursor.item/org.sana.patient";

	/**
     * The content:// style URI for this content provider.
     */
	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/core/patient");
	
	/** Default sort order. */
	public static final String DEFAULT_SORT_ORDER = Contract.FAMILY_NAME + "  ASC";
	
	/** Sort patients by first name. */
	public static final String GIVEN_NAME_SORT_ORDER = Contract.GIVEN_NAME + " ASC";
	
    private Patients() {}

    /**
     * Projections for the Patient table.
     * @author Sana Development
     */
    public static interface Projection extends BaseProjection<Patient> {
    	
    	/** Projection to obtain ID and name of the patient. */
    	public static final String[] DISPLAY_NAME = new String[] {
    		Contract._ID, Contract.GIVEN_NAME, Contract.FAMILY_NAME, Contract.IMAGE,
    		Contract.PATIENT_ID
    	};
    }
    
	/**
	 * Contract for the Patient table in the database.
	 * 
	 * @author Sana Development
	 *
	 */
    public static interface Contract extends BaseContract<Patient>{
    	
    	//COLUMNS
		/** Unique id attribute within the encounter */
		public static final String UUID = "uuid";
		
    	/** The unique ID for a patient. */
    	public static final String PATIENT_ID = "system_id";
    	
    	/**
    	 * The patient's date of birth.
    	 */
    	public static final String DOB = "dob";
    	
    	/**
    	 * The patient's first name.
    	 */
    	public static final String GIVEN_NAME = "given_name";
    	
    	/**
    	 * The patient's last name.
    	 */
    	public static final String FAMILY_NAME = "family_name";
    	
    	/**
    	 * The patient's gender.
    	 */
    	public static final String GENDER = "gender";
    	

    	/** An image of the patient */
    	public static final String IMAGE = "image";
    	
    	/** The current registration state. */
        // Status of the patient
        // For use in PatientRunnerFragment to show each patient's status
        // 0 - Was never put into queue, or "Not Uploaded"
        // 1 - Still in the queue waiting to be sent
        // 2 - Upload Successful - has been sent to the MDS, no longer in queue
        // 3 - Upload in progress
        // 4 - In the queue but waiting for connectivity to upload
        // 5 - Upload failed
        // 6 - Upload stalled - username/password incorrect
    	public static final String STATE = "_state";
    	
    	/** A location code for the patient */
    	public static final String LOCATION = "location";

        public static final String CONFIRMED = "confirmed";

        public static final String DOB_ESTIMATED = "dob_estimated";

    	public static final String ADDRESS_ONE = "address_one";
    	public static final String ADDRESS_TWO = "address_two";
    	public static final String ADDRESS_THREE = "address_three";
    	public static final String ADDRESS_FOUR = "address_four";
    	
    	public static final String CONTACT_ONE = "contact_one";
    	public static final String CONTACT_TWO = "contact_two";
    	public static final String CONTACT_THREE = "contact_three";
    	public static final String CONTACT_FOUR = "contact_four";
    	
	}

}
