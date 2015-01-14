package org.sana.android.service;

import java.util.PriorityQueue;

import org.sana.android.provider.Encounters;
import org.sana.android.provider.Patients;
import org.sana.android.provider.Procedures;
import org.sana.android.net.MDSInterface;
import org.sana.android.net.MDSInterface2;
import org.sana.android.util.SanaUtil;

import android.app.Application;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Background service to upload pending cases when data service is available.
 * This class will try to upload pending cases when a connection is available.
 * 
 * @author Sana Development Team
 */
public class BackgroundUploader extends Service {
	
	private static final String TAG = BackgroundUploader.class.getSimpleName();
	
	/**
	 * Available states of authorization status.
	 * 
	 * @author Sana Development Team
	 *
	 */
	public enum CredentialStatus {
		UNKNOWN,
		VALID,
		INVALID
	}
	
	private PriorityQueue<Uri> queue = null;

	private CredentialStatus credentialStatus = CredentialStatus.VALID;
	//private CheckCredentialsTask checkCredentialsTask = null;
	
	/**
	 * Provides a Binder to the BackgoundUploader Service.
	 * 
	 * @author Sana Development Team
	 *
	 */
	public class LocalBinder extends Binder {
		public BackgroundUploader getService() {
			return BackgroundUploader.this;
		}
	}
	private final IBinder mBinder = new LocalBinder();

	/**
	 * DataConnectionListener is a listener that waits for a data connection.
	 * Once a data connection is available to the phone, it notifies the
	 * BackgroundUploader that it should process its queue of cases and update
	 * their status.
	 */
	private class DataConnectionListener extends PhoneStateListener {
		@Override
		public void onDataConnectionStateChanged(int state) {
			if (state == TelephonyManager.DATA_CONNECTED) {
				Log.i("TAG", "Data is now connected");			
			} else {
				Log.i("TAG", "Data is now disconnected");
			}
			// Tell the BackgroundUploader the data connection state changed.
			BackgroundUploader.this.onConnectionChanged();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.v(TAG, "onCreate()");
		try {
			queue = QueueManager.initQueue(this);
			
			// Try to process the upload queue. Will check credentials if necessary.
			processUploadQueue();
		} catch (Exception e) {
			Log.e(TAG, "Exception creating background uploading service: "
					+ e.toString());
			e.printStackTrace();
		}
		TelephonyManager telephony = (TelephonyManager)getSystemService(
				Application.TELEPHONY_SERVICE);
		if (telephony != null) {
			telephony.listen(new DataConnectionListener(), 
					PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
		}
	}
	
	@Override
	public void onStart(Intent data, int startId) {
		Log.v(TAG, "onStart() intent " + data + " start ID: " + startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "onDestroy()");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	private static int getUploadStatusForCredentialStatus(
			CredentialStatus credentialStatus)
	{
		int status;
		switch (credentialStatus) {
		case INVALID:
			status = QueueManager.UPLOAD_STATUS_CREDENTIALS_INVALID;
			break;
		case UNKNOWN:
		case VALID:
		default:
			status = QueueManager.UPLOAD_STATUS_WAITING;
			break;
		}
		return status;
	}

	/**
	 * Update queue status for items in the queue
	 */
	private boolean updateQueueStatusAndCheckConnection() {
		try {
			boolean hasConnection = SanaUtil.checkConnection(this);
			
			if (hasConnection) {
				try {
					Log.i(TAG, "Credential status: " + credentialStatus);
					int status = getUploadStatusForCredentialStatus(
							credentialStatus);
					QueueManager.setProceduresUploadStatus(this, queue, status);
				} catch (Exception e) {
					Log.e(TAG, "Exception updating upload status in database: "
							+ e.toString());
				}
				return true;
			} else {
				try {
					// Signify procedures waiting for connectivity to upload
					QueueManager.setProceduresUploadStatus(this, queue, 
							QueueManager.UPLOAD_NO_CONNECTIVITY);
				} catch (Exception e) {
					Log.e(TAG, "Exception updating upload status in database: "
							+ e.toString());
				}
				return false;
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception in checkConnection(): " + e.toString());
			return false;
		}
	}
	
	public void addProcedureToQueue(Uri procedureUri) {
		
		if (QueueManager.isInQueue(queue, procedureUri)) {
			Log.i(TAG, "Procedure " + procedureUri + " is already in the queue."
					+"Skipping add request.");
			return;
		}
		
		Log.i(TAG, "Adding " + procedureUri + " to the upload queue.");
		QueueManager.addToQueue(this, queue, procedureUri);
		Log.i(TAG, "Queue is now: " + queue.toString());
		
		int status = getUploadStatusForCredentialStatus(credentialStatus);
		QueueManager.setProcedureUploadStatus(getApplicationContext(), 
				procedureUri, status);
		
		// Start the upload process if possible. Does its work in an AsyncTask
		processUploadQueue();
	}

	//Only check credentials with openMRS when username or password have changed 
	// in settings
	public void onCredentialsChanged(boolean credentials) {
		credentialStatus = credentials ? CredentialStatus.VALID : 
			CredentialStatus.INVALID;
		Log.i(TAG, "Setting credential status to " + credentialStatus);

		// Now that the credentials have changed, try to run through the queue.
		processUploadQueue();
	}
	
	public void onConnectionChanged() {
		// Since the connection status changed, try to run through the queue.
		processUploadQueue();
	}
	
	class UploadResult {
		public UploadResult(Uri procedure, boolean uploaded, String message) {
			this.procedure = procedure;
			this.uploaded = uploaded;
			this.message = message;
		}
		Uri procedure;
		boolean uploaded;
		String message;
	}
	
	AsyncTask<Void, UploadResult, Void> uploadTask = null; 

	private void processUploadQueue() {
		Log.i(TAG, "processUploadQueue()");
		
		// check if there are pending transfers in the database
		// if so, then spawn a thread to upload the first one
		boolean credentialsValid = CredentialStatus.VALID.equals(
				credentialStatus);
		boolean connectionAvailable = updateQueueStatusAndCheckConnection();
		if (!queue.isEmpty() && connectionAvailable) {
			Log.i(TAG, "Queue not empty and connection is available, so " +
					"spawning upload worker.");
			new AsyncTask<Void, UploadResult, Void>() {

				@Override 
				protected void onProgressUpdate(UploadResult... results) {
					for (UploadResult result : results) {
						if (result.uploaded) {
							onUploadSuccess(result.procedure); 
						} else {
							onUploadFailure(result.procedure, result.message);
						}
					}
				}
				
				@Override
				protected Void doInBackground(Void... params) {
					while (!queue.isEmpty() && updateQueueStatusAndCheckConnection()) {
						Uri procedure = queue.element();
						Log.i(TAG,"Uploading procedure " + procedure);
						
						try {
							// Signify procedure upload in progress
							QueueManager.setProcedureUploadStatus(
									BackgroundUploader.this, procedure, 
									QueueManager.UPLOAD_STATUS_IN_PROGRESS);
							
							boolean uploadResult = 
								MDSInterface2.postProcedureToDjangoServer(
										procedure, BackgroundUploader.this);	
							
							if (uploadResult) {
								// Remove the procedure from the queue after it 
								// has been successfully uploaded
								QueueManager.removeFromQueue(
										BackgroundUploader.this, queue, 
										procedure, 
										QueueManager.UPLOAD_STATUS_SUCCESS); 
							} else {
								// Remove the procedure from the queue so it 
								// does not keep trying to upload
								QueueManager.removeFromQueue(
										BackgroundUploader.this, 
										queue, procedure, 
										QueueManager.UPLOAD_STATUS_FAILURE); 
							}
							
							UploadResult result = new UploadResult(procedure, 
									uploadResult, "");
							publishProgress(result);
						} catch (OutOfMemoryError e) {
							Log.e(TAG, "While uploading procedure, " +
									"got Out of Memory error.");
							e.printStackTrace();
							UploadResult result = new UploadResult(procedure, 
									false, "Out of Memory");
							publishProgress(result);
						} catch (Exception e) {
							Log.e(TAG, "While uploading procedure + " + 
									procedure + " got exception: " 
									+ e.toString());
							e.printStackTrace();
							
							UploadResult result = new UploadResult(procedure, 
									false, "");
							publishProgress(result);
						}
					}
					
					return null;
				}
				
			}.execute();
		} else {
			Log.i(TAG, "Either queue is empty or connection is not available, " +
					"so not spawning upload worker.");
		}
	}
	
	private String getProcedureTitle(Uri procedure) {
		Cursor cursor = null;
		String procedureTitle = "Unknown Procedure";
		try {
			cursor = getContentResolver().query(procedure, new String [] { 
					Encounters.Contract._ID, 
					Encounters.Contract.PROCEDURE, 
					Encounters.Contract.STATE }, null, null,null);        
			cursor.moveToFirst();
			long savedProcedureId = cursor.getLong(cursor.getColumnIndex(
					Encounters.Contract._ID));
			long procedureId = cursor.getLong(cursor.getColumnIndex(
					Encounters.Contract.PROCEDURE));
			cursor.close();

			Uri procedureUri = ContentUris.withAppendedId(
					Procedures.CONTENT_URI, procedureId);;
			cursor = getContentResolver().query(procedureUri, new String[] { 
					Procedures.Contract.TITLE }, null, null, null);
			cursor.moveToFirst();
			procedureTitle = cursor.getString(cursor.getColumnIndex(
					Procedures.Contract.TITLE));
			
		} catch (Exception e) {
			Log.e(TAG, "Failed to get procedure title for procedure " 
						+ procedure + ". " + e.toString());
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return procedureTitle;
	}

	private void onUploadSuccess(Uri procedure) {
		Log.i(TAG, "onUploadSuccess for " + procedure);

		String procedureTitle = getProcedureTitle(procedure); 
		String patientId = ""; // TODO
		String msg = "Successfully sent " + procedureTitle + " for patient " 
						+ patientId + "\n";

		//String msg = "Successfully sent " + procedureTitle + " procedure\nwith ID = " + savedProcedureId;
		//String msg = "Successfully sent procedure\nwith ID = " + savedProcedureId;
		
		int sizeOfQueue = queue.size();
		if (sizeOfQueue != 0) {
			msg += "\nThere are still " + sizeOfQueue+"\ncases to be uploaded.";
		}
		else {
			msg += "\nAll cases are done uploading.";
		}
		Toast toast = Toast.makeText(getApplicationContext(), msg, 
				Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	private void onUploadFailure(Uri procedure, String message) {
		Log.i(TAG, "onUploadFailure for " + procedure);

		String procedureTitle = getProcedureTitle(procedure);
		String patientId = ""; // TODO
		String msg = "Upload of " + procedureTitle + " for patient " 
							+ patientId + " failed.\n";
		
		msg += message;

		//String msg = "Successfully sent " + procedureTitle + " procedure\nwith ID = " + savedProcedureId;
		//String msg = "Successfully sent procedure\nwith ID = " + savedProcedureId;
		
		int sizeOfQueue = queue.size();
		if (sizeOfQueue != 0) {
			msg += "\nThere are still " + sizeOfQueue + "\ncases to be uploaded.";
		}
		else {
		}
		Toast toast = Toast.makeText(getApplicationContext(), msg, 
									Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
		
	}
	
	/**
	 * Dispatch a POST request to create a new record in the backend server.
	 * 
	 * @param uri A content style uri
	 * @throws IllegalArgumentException if the Uri scheme is not content
	 * @throws UnsupportedOperationException if the content type is not supported for this 
	 * 			operation.
	 */
	public void create(Uri uri){
		String type = getContentResolver().getType(uri);
		if(uri.getScheme().compareTo("content") != 0)
			throw new IllegalArgumentException("Must be content style uri: " + uri);
		if (type.compareTo(Patients.CONTENT_TYPE) == 0){
			
		} else if (type.compareTo(Encounters.CONTENT_ITEM_TYPE) == 0){
			addProcedureToQueue(uri);
		} else {
			throw new UnsupportedOperationException("POST support not available for " + type);
		}
	}
	

	/**
	 * Dispatch a GET request to fetch a record from the backend server
	 * 
	 * @param uri A content style uri
	 * @throws IllegalArgumentException if the Uri scheme is not content
	 * @throws UnsupportedOperationException if the content type is not supported for this 
	 * 			operation.
	 */
	public void read(Uri uri){
		String type = getContentResolver().getType(uri);
		if(uri.getScheme().compareTo("content") != 0)
			throw new IllegalArgumentException("Must be content style uri: " + uri);
		throw new UnsupportedOperationException("POST support not available for " + type);
		
	}
	
	/**
	 * Dispatch a PUT request to update a record on the backend server.
	 * 
	 * @param uri A content style uri
	 * @throws IllegalArgumentException if the Uri scheme is not content
	 * @throws UnsupportedOperationException if the content type is not supported for this 
	 * 			operation.
	 */
	public void update(Uri uri){
		String type = getContentResolver().getType(uri);
		if(uri.getScheme().compareTo("content") != 0)
			throw new IllegalArgumentException("Must be content style uri: " + uri);
		throw new UnsupportedOperationException("POST support not available for " + type);
		
	}
	

	/**
	 * Dispatch a DELETE request to remove a record on the backend server.
	 * 
	 * @param uri A content style uri
	 * @throws IllegalArgumentException if the Uri scheme is not content
	 * @throws UnsupportedOperationException if the content type is not supported for this 
	 * 			operation.
	 */
	public void delete(Uri uri){
		String type = getContentResolver().getType(uri);
		if(uri.getScheme().compareTo("content") != 0)
			throw new IllegalArgumentException("Must be content style uri: " + uri);
		throw new UnsupportedOperationException("POST support not available for " + type);
	}
}