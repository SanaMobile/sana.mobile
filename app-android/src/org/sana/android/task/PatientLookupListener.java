package org.sana.android.task;

import org.sana.android.db.PatientInfo;

/**
 * Callback methods when listening for patient lookup events.
 * 
 * @author Sana Development Team
 *
 */
public interface PatientLookupListener {
	/**
	 * Declares an action to take on look up success
	 * 
	 * @param pi the patient info to look up
	 */
	void onPatientLookupSuccess(PatientInfo pi);
	
	/**
	 * Declares an action to take on patient look up success.
	 * 
	 * @param patientIdenfifier A patient identifier
	 */
	void onPatientLookupFailure(String patientIdenfifier);
}
