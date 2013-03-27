package org.sana.android.task;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.sana.core.Event;
import org.sana.android.net.MDSInterface;
import org.sana.android.provider.Events;
import org.sana.android.provider.Events.Contract;
import org.sana.android.util.SanaUtil;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Task for synching with an MDS instance.
 * 
 * @author Sana Development Team
 *
 */
public class MDSSyncTask extends AsyncTask<Context, Void, Integer> {
	public static final String TAG = MDSSyncTask.class.getSimpleName();
	/** Indicates a connection could not be established for synching. */
	public static final Integer EMR_SYNC_NO_CONNECTION = 0;
	/** Indicates synching was successful. */
	public static final Integer EMR_SYNC_SUCCESS = 1;
	/** Indicates synching failed. */
	public static final Integer EMR_SYNC_FAILURE = 2;
	
	private ProgressDialog progressDialog;
	private Context mContext = null; // TODO context leak?
	
	/** 
	 * A new synchronization task.
	 * 
	 * @param c the COntext to synch with
	 */
	public MDSSyncTask(Context c) {
		mContext = c;
	}
	
	private boolean syncPatients(Context c) throws UnsupportedEncodingException {
		return MDSInterface.updatePatientDatabase(c, c.getContentResolver());
	}
	
	private boolean syncEvents(Context c) {
		Cursor cursor = null; 
		Log.i(TAG, "Syncing the event log to the MDS.");
		
		try {
			// Get all un-uploaded events.
			cursor = c.getContentResolver().query(Events.CONTENT_URI, 
					new String[] {  Contract._ID, 
								    Contract.CREATED, 
									Contract.EVENT_TYPE, 
									Contract.EVENT_VALUE, 
									Contract.ENCOUNTER, 
									Contract.SUBJECT, 
									Contract.OBSERVER }, 
					Contract.UPLOADED+"=?", new String[] { "0" }, null);
			int numEvents = cursor.getCount();
			
			if (numEvents == 0) {
				// Nothing to upload, quit.
				Log.i(TAG, "No unuploaded events. Skipping syncEvents.");
				return true;
			} else {
				Log.i(TAG, "There are " + numEvents + " unuploaded events.");
			}
			
			StringBuilder sb = new StringBuilder("(");
			List<Event> events = new ArrayList<Event>(numEvents);

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				
				Event e = new Event();
				e.event_time = cursor.getLong(cursor.getColumnIndex(
						Contract.CREATED));
				e.event_type = cursor.getString(cursor.getColumnIndex(
						Contract.EVENT_TYPE));
				e.event_value = cursor.getString(cursor.getColumnIndex(
						Contract.EVENT_VALUE));
				e.encounter = cursor.getString(cursor.getColumnIndex(
						Contract.ENCOUNTER));
				e.subject = cursor.getString(cursor.getColumnIndex(
						Contract.SUBJECT));
				e.observer = cursor.getString(cursor.getColumnIndex(
						Contract.OBSERVER));
				int id = cursor.getInt(cursor.getColumnIndex(
						Contract._ID));
				events.add(e);
				
				sb.append(id);
				if (!cursor.isLast()) {
					sb.append(",");
				}
				cursor.moveToNext();
			}
			sb.append(")");

			// Submit the events to the MDS
			boolean result = MDSInterface.submitEvents(c, events);
			
			// Set the uploaded events as uploaded in the database.
			if (result) {
				Log.i(TAG, "Successfully uploaded " + numEvents + " events.");
				ContentValues cv = new ContentValues();
				cv.put(Contract.UPLOADED, 1);
				int rowsUpdated = c.getContentResolver().update(
						Events.CONTENT_URI, cv, 
						Contract._ID +" in " + sb.toString(), null);
				if (rowsUpdated != numEvents) {
					Log.w(TAG, 
					"Didn't get as many rows updated as we thought we would.");
				}
			} 
			return result;
		} catch (Exception e) {
			Log.e(TAG, "While trying to submit the event log, got exception: "
					+ e.toString());
			e.printStackTrace();
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	protected Integer doInBackground(Context... params) {
		Log.i(TAG, "Executing EMRSyncTask");
		Context c = params[0];
		
		Integer result = EMR_SYNC_NO_CONNECTION; // TODO detect this case better
		try{
			if (SanaUtil.checkConnection(c)) {
				boolean patientSyncResult = syncPatients(c);
				boolean eventSyncResult = syncEvents(c);
			
				result = (patientSyncResult && eventSyncResult) ? 
						EMR_SYNC_SUCCESS : EMR_SYNC_FAILURE;  
			}
		} catch(Exception e){
			Log.e(TAG, "Could not sync. " + e.toString());
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	protected void onPreExecute() {
		Log.i(TAG, "About to execute EMRSyncTask");
		if (progressDialog != null) {
    		progressDialog.dismiss();
    		progressDialog = null;
    	}
		progressDialog = new ProgressDialog(mContext);
    	progressDialog.setMessage("Updating patient database cache");
    	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	progressDialog.show();	
    }

	/** {@inheritDoc} */
	@Override
	protected void onPostExecute(Integer result) {
		Log.i(TAG, "Completed EMRSyncTask");
		if (progressDialog != null) {
    		progressDialog.dismiss();
    		progressDialog = null;
    	}
	}
	
}
