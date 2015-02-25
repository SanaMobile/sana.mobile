package org.sana.android.task;

import org.sana.android.db.PatientInfo;
import org.sana.android.net.MDSInterface;
import org.sana.android.util.SanaUtil;
import org.sana.android.util.UserDatabase;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * A task for looking up patient information.
 * 
 * @author Sana Development Team
 *
 */
public class PatientLookupTask extends AsyncTask<String, Void, PatientInfo> {
	private static final String TAG = PatientLookupTask.class.getSimpleName();
	
	private Context mContext;
	private PatientLookupListener listener = null;

	public String patientId;

	/**
	 * A new patient look up task.
	 * @param c the Context to operate in
	 */
	public PatientLookupTask(Context c) {
		mContext = c;
	}
	
	/**
	 * Sets the listener for look up events.
	 * @param listener the new listener
	 */
	public void setPatientLookupListener(PatientLookupListener listener) {
		this.listener = listener;
	}
	
	/** {@inheritDoc} */
	@Override
	protected PatientInfo doInBackground(String... params) {
		patientId = params[0];
		
		Log.i(TAG, "Looking up patient record for " + patientId);
		
		PatientInfo pi = null;
		try {
			if (SanaUtil.checkConnection(mContext)) {
				String mdsPatientInfo = MDSInterface.getUserInfo(mContext,
						patientId);
                Log.d(TAG, "NET Result" + mdsPatientInfo);
				pi = UserDatabase.getPatientFromMDSRecord(patientId, 
						mdsPatientInfo);
				Log.i(TAG, "Acquired patient record from MDS");
			}
		} catch (Exception e) {
			Log.e(TAG, "Could not get patient record from MDS: " 
					+ e.toString());
			e.printStackTrace();
		}
		
		try {
			if (pi == null) {
				pi = UserDatabase.getPatientFromLocalDatabase(mContext, 
						patientId);
				Log.i(TAG, "Acquired patient record from local Patient cache.");
			}
		} catch (Exception e) {
			Log.e(TAG, "Could not get patient record from local database: " 
					+ e.toString());
			e.printStackTrace();
		}
		
		if (pi == null) {
			pi = new PatientInfo();
			pi.setPatientIdentifier(patientId);
			pi.setConfirmed(false);
		}
		
		return pi;
	}

	/** {@inheritDoc} */
	@Override
	protected void onPostExecute(PatientInfo pi) {
		if (listener != null && pi != null) {
			if (!pi.isConfirmed()) {
				listener.onPatientLookupFailure(pi.getPatientIdentifier());
			} else {
				listener.onPatientLookupSuccess(pi);
			}
		}
	}

}
