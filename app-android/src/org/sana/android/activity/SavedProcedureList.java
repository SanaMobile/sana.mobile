package org.sana.android.activity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.sana.R;
import org.sana.android.db.SanaDB.ProcedureSQLFormat;
import org.sana.android.db.SanaDB.SavedProcedureSQLFormat;
import org.sana.android.service.BackgroundUploader;
import org.sana.android.service.QueueManager;
import org.sana.android.service.ServiceConnector;
import org.sana.android.service.ServiceListener;
import org.sana.android.util.SanaUtil;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * Displays a list of previous encounters and their status.
 * 
 * @author Sana Development Team
 */
public class SavedProcedureList extends SherlockListActivity implements 
	SimpleCursorAdapter.ViewBinder 
{
	private static final String TAG = SavedProcedureList.class.getSimpleName();
	private static final String[] PROJECTION = { SavedProcedureSQLFormat._ID,
			SavedProcedureSQLFormat.GUID, 
			SavedProcedureSQLFormat.PROCEDURE_ID, 
			SavedProcedureSQLFormat.PROCEDURE_STATE, 
			SavedProcedureSQLFormat.UPLOAD_STATUS,
			SavedProcedureSQLFormat.UPLOAD_QUEUE };
	private HashMap<Integer, String> procedureToName = 
		new HashMap<Integer, String>();

    private ServiceConnector mConnector = new ServiceConnector();
    private BackgroundUploader mUploadService = null;

    /**
     * Listens for connections to the BackgroundUploader service
     * @author Sana Development Team
     */
    private class BackgroundUploaderConnectionListener implements 
    	ServiceListener<BackgroundUploader> 
    {
    	
		public void onConnect(BackgroundUploader uploadService) {
			Log.i(TAG, "onServiceConnected");
			mUploadService = uploadService;
		}
		
		public void onDisconnect(BackgroundUploader uploadService) {
			Log.i(TAG, "onServiceDisconnected");
			mUploadService = null;
		}
    }	
	
	/**
	 * Lookup procedure name in the database by primary key with memoization.
	 * @return the procedure name for procedureId 
	 */
	private String lookupProcedureName(int procedureId) {
		
		if(procedureToName.containsKey(procedureId)) {
			return procedureToName.get(procedureId);
		}
		
		Cursor cur2 = getContentResolver().query(ProcedureSQLFormat.CONTENT_URI,
				new String[] { ProcedureSQLFormat.TITLE },
				ProcedureSQLFormat._ID + " = ?",
				new String[] { Integer.toString(procedureId) }, null);
		cur2.moveToFirst();
		String title = cur2.getString(0);
		cur2.close();
		
		procedureToName.put(procedureId, title);
		
		return title;
	}
	
	/** Parses patient name from JSON String */
	private String getPatientNameFromData(String jsonData) {
        String patientId = "";
        String patientFirst = "";
        String patientLast = "";
        
        try {
    		JSONTokener tokener = new JSONTokener(jsonData);
            JSONObject answersDict = new JSONObject(tokener);
            if (answersDict.has("patientId"))
            	patientId = (String)answersDict.get("patientId");
            if (answersDict.has("newPatientFirstName"))
            	patientFirst = (String)answersDict.get("newPatientFirstName");
            if (answersDict.has("patientFirstName") && "".equals(patientFirst))
            	patientFirst = (String)answersDict.get("patientFirstName");
            if (answersDict.has("newPatientLastName"))
            	patientLast = (String)answersDict.get("newPatientLastName");
            if (answersDict.has("patientLastName") && "".equals(patientLast))
            	patientLast = (String)answersDict.get("patientLastName");
        } catch(JSONException e) {
        	
        }
        
        StringBuilder result = new StringBuilder(patientId);
        if(!patientFirst.equals("")) {
        	result.append(" - ");
        	result.append(patientFirst);
        	result.append(" ");
        	result.append(patientLast);
        }
        return result.toString();
	}
	
	/** Gets the upload status of an item in the queue */
	private String getUploadStatus(int queueStatus, int queuePosition) {
		String message = "";
		if (queueStatus == 0 || queueStatus == -1) message = "Not Uploaded";
		else if (queueStatus == QueueManager.UPLOAD_STATUS_WAITING) {
			message = "Waiting in the queue to be uploaded, " + queuePosition;
			if (queuePosition == -1) 
				message = "Waiting in the queue to be uploaded";
			else if (queuePosition == 1) 
				message += "st in line";
			else if (queuePosition == 2) 
				message += "nd in line";
			else if (queuePosition == 3) 
				message += "rd in line";
			else 
				message += "th in line";
		} else if (queueStatus == QueueManager.UPLOAD_STATUS_SUCCESS) 
			message = "Uploaded Successfully";
		else if (queueStatus == QueueManager.UPLOAD_STATUS_IN_PROGRESS) 
			message = "Upload in progress";
		else if (queueStatus == QueueManager.UPLOAD_NO_CONNECTIVITY) 
			message = "Upload stalled - Waiting for connectivity";
		else if (queueStatus == QueueManager.UPLOAD_STATUS_FAILURE) 
			message = "Upload failed";
		else if (queueStatus == QueueManager.UPLOAD_STATUS_CREDENTIALS_INVALID) 
			message = "Upload stalled - username/password incorrect";
		else Log.i(TAG, "Not a valid number stored in database.");
		Log.i(TAG, "Message being set as the status of the procedure: " 
				+ message);
		return message;
	}
	
	/**
	 * Binds the cursor columns to the text views as follows
	 * [ Procedure name, Patient name, Upload Status ]
	 * 
	 */
	@Override
	public boolean setViewValue(View v, Cursor cur, int columnIndex) {
		try {
			if (v instanceof TextView) {
				((TextView)v).setText(cur.getString(columnIndex));
				switch(columnIndex) {
				case 2:
					int procedureId = cur.getInt(columnIndex);
					String procedureName = lookupProcedureName(procedureId);
					((TextView)v).setText(procedureName);
					break;
				case 3:
					String jsonData = cur.getString(columnIndex);
					String patientName = getPatientNameFromData(jsonData);
					((TextView)v).setText(patientName);
					break;
				case 4:
					Log.i(TAG, "Setting upload queue status in text view.");
					int queueStatus = cur.getInt(columnIndex);
					int queuePosition = cur.getInt(5);
					String message = getUploadStatus(queueStatus, 
							queuePosition + 1);
					((TextView)v).setText(message);
					break;
				}
			}
		}
		catch (Exception e) {
			Log.e(TAG, "Exception in setting the text in the list: " + 
					e.toString());
		}
		return true;
	}
		
	/** {@inheritDoc} */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		// Connect to the background uploader service
		try {
        	mConnector.setServiceListener(
        			new BackgroundUploaderConnectionListener());
        	mConnector.connect(this);
        }
        catch (Exception e) {
        	Log.e(TAG, "Exception starting background upload service: " 
        			+ e.toString());
        	e.printStackTrace();
        }
		
        Cursor cursor = managedQuery(SavedProcedureSQLFormat.CONTENT_URI, 
        		PROJECTION, null, null, 
        		SavedProcedureSQLFormat.DEFAULT_SORT_ORDER);

        try {
	        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,	                
	        		R.layout.row, cursor,
	                new String[] { SavedProcedureSQLFormat.PROCEDURE_ID, 
	        					   SavedProcedureSQLFormat.PROCEDURE_STATE, 
	        					   SavedProcedureSQLFormat.UPLOAD_STATUS },
	                new int[] { R.id.toptext, R.id.bottomtext, 
	        					R.id.queue_status });
	        adapter.setViewBinder(this);
	        setListAdapter(adapter);
        }
        catch (Exception e) {
			Log.e(TAG, "Exception in creating SimpleCursorAdapter: " 
					+ e.toString());
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			mConnector.disconnect(this);
			mUploadService = null;
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "While disconnecting service got exception: " 
					+ e.toString());
			e.printStackTrace();
		}
	}
	
	/** {@inheritDoc} */
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
        Log.i(TAG, "procedure Uri in onListItemClick: " + uri);
        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || 
        		Intent.ACTION_GET_CONTENT.equals(action)) 
        {
            // The caller is waiting for us to return a note selected by
            // the user.  The have clicked on one, so return it now.
            setResult(RESULT_OK, new Intent().setData(uri));
            finish();
        } else {
            // Launch activity to view/edit the currently selected item
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
        }
    }
	
	public static final int SELECT_ALL = 0;
	public static final int SELECT_FAILED = 3;
	public static final int DELETE = 1;
	public static final int RESEND = 2;
	//public static final int CANCEL_UPLOAD = 2;

	/** Available options are to select all, delete, or resend */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, SELECT_ALL, 0, "Select All");
		menu.add(0, DELETE, 1, "Delete");
		menu.add(0, RESEND, 2, "Resend");
		//menu.add(0, CANCEL_UPLOAD, 2, "Cancel Upload");
		return true;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean onOptionsItemSelected(MenuItem item){

		switch (item.getItemId()) {
		case SELECT_ALL:
			selectAllProcedures();
			return true;
		case DELETE:
			deleteSelected();
			return true;
		case RESEND:
			resendSelected();
			return true;
		/*case CANCEL_UPLOAD:
			cancelUploads();
			return true;*/
		}
		return false;
	}
	
	/** All checkboxes will be checked */
	private void selectAllProcedures() {
		try {
			for (int x = 0; x < getListAdapter().getCount(); x++) {
				CheckBox checkbox = (CheckBox) getListView().getChildAt(x)
												.findViewById(R.id.icon);
				checkbox.setChecked(true);
				Log.i(TAG, "Is checkbox checked? (Should be true): " 
						+ checkbox.isChecked());
			}
		}
		catch (Exception e) {
			Log.i(TAG, "Exception in selectAll(): " + e.toString());
		}
	}
	
	/**
	 * Unselect all checked items in the list.
	 */
	private void unselectAllProcedures() {
		try {
			for (int x = 0; x < getListAdapter().getCount(); x++) {
				CheckBox checkbox = (CheckBox) getListView().getChildAt(x)
													.findViewById(R.id.icon);
				checkbox.setChecked(false);
			}
		}
		catch (Exception e) {
			Log.i(TAG, "Exception in unselectAll(): " + e.toString());
		}
	}
	
	/**
	 * Delete every checked item in the list.
	 */
	private void deleteSelected() {
		List<Long> ids = new LinkedList<Long>();
		ListAdapter adapter = getListAdapter();
		ListView view = getListView();
		try {
			for (int x = 0; x < adapter.getCount(); x++) {
				CheckBox checkbox = (CheckBox) view.getChildAt(x)
												.findViewById(R.id.icon);
				if (checkbox.isChecked()) {
					long itemId = adapter.getItemId(x);
					ids.add(itemId);
				}
			}
			unselectAllProcedures();
			String idList = SanaUtil.formatPrimaryKeyList(ids);

			// Now delete the ids
			getContentResolver().delete(SavedProcedureSQLFormat.CONTENT_URI, 
					SavedProcedureSQLFormat._ID + " IN " + idList, null); 
		}
		catch (Exception e) {
			unselectAllProcedures();
			Log.i(TAG, "Exception in deleteSelected(): " + e.toString());
		}
	}
	
	/**
	 * Resend every checked item in the list.
	 */
	private void resendSelected() {
		try {
			if (mUploadService != null) {
				ListAdapter adapter = getListAdapter();
				ListView view = getListView();
				Uri contentUri = getIntent().getData();
				for (int x = 0; x < adapter.getCount(); x++) {
					CheckBox checkbox = (CheckBox)view.getChildAt(x)
													.findViewById(R.id.icon);
					if (checkbox.isChecked()) {
						Uri procedure = ContentUris.withAppendedId(contentUri, 
								(Long) view.getItemIdAtPosition(x));
						Log.i(TAG, "Resending procedure: " + procedure);
						mUploadService.addProcedureToQueue(procedure);
					}
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "While resending selected procedures, got exception: " 
					+ e.toString());
			e.printStackTrace();
		}
	}
	
	//private static ContentResolver contentResolver;
	//If selected procedures are in queue or are currently being uploaded, cancel the upload
	/*private void cancelUploads() {
		try{
			for (int x = 0; x < getListAdapter().getCount(); x++) {
				CheckBox checkbox = (CheckBox) getListView().getChildAt(x).findViewById(R.id.icon);
				Uri procedure = ContentUris.withAppendedId(getIntent().getData(), (Long) getListView().getItemIdAtPosition(x));
				if (checkbox.isChecked()) {
					// TODO Make is cancel if its in the middle of uploading, not just taking it out of the queue
					BackgroundUploader.removeFromQueue(procedure);
				}
			}
			unselectAllProcedures();
		}
		catch (Exception e) {
			unselectAllProcedures();
			Log.i(TAG, "Exception in cancelSelected(): " + e.toString());
		}
	}*/
}
