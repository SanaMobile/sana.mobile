package org.sana.android.activity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.sana.R;
import org.sana.android.app.Locales;
import org.sana.android.content.Uris;
import org.sana.android.db.EventDAO;
import org.sana.android.db.ModelWrapper;
import org.sana.android.db.SanaDB.ImageSQLFormat;
import org.sana.android.net.MDSInterface;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Observations;
import org.sana.android.provider.Patients;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Events.EventType;
import org.sana.android.service.BackgroundUploader;
import org.sana.android.service.QueueManager;
import org.sana.android.service.ServiceConnector;
import org.sana.android.service.ServiceListener;
import org.sana.android.util.SanaUtil;
import org.sana.util.UUIDUtil;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


/**
 * Displays a list of previous encounters and their status.
 * 
 * @author Sana Development Team
 */
public class EncounterList extends ListActivity implements 
	SimpleCursorAdapter.ViewBinder
{
	private static final String TAG = EncounterList.class.getSimpleName();
	private static final String[] PROJECTION = { 
		    Encounters.Contract._ID,
			Encounters.Contract.UUID, 
			Encounters.Contract.PROCEDURE, 
			Encounters.Contract.SUBJECT, 
			Encounters.Contract.STATE, 
			Encounters.Contract.UPLOAD_STATUS,
			Encounters.Contract.UPLOAD_QUEUE,
			Encounters.Contract.MODIFIED };
	private HashMap<Integer, String> procedureToName = 
		new HashMap<Integer, String>();
	private HashMap<String, String[]> patientToName = 
			new HashMap<String, String[]>();

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
		
		Cursor cur2 = getContentResolver().query(Procedures.CONTENT_URI,
				new String[] { Procedures.Contract.TITLE },
				Procedures.Contract._ID + " = ?",
				new String[] { Integer.toString(procedureId) }, null);
		cur2.moveToFirst();
		String title = cur2.getString(0);
		cur2.close();
		
		procedureToName.put(procedureId, title);
		
		return title;
	}
	
	private String lookupProcedureName(Uri procedure){

		Cursor cur2 = getContentResolver().query(procedure,
				new String[] {
					Procedures.Contract._ID,
					Procedures.Contract.TITLE },
				null, null, null);
		cur2.moveToFirst();
		int id = cur2.getInt(0);
		String title = cur2.getString(1);
		cur2.close();
		procedureToName.put(id, title);
		
		return title;
	}
	
	/** Parses patient name from JSON String */
	@Deprecated
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
	@Deprecated
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
		//Log.i(TAG, "Message being set as the status of the procedure: " 
		//		+ message);
		return message;
	}
	
	private String getUploadStatus2(int queueStatus, int queuePosition) {
		String message = "";
		if (queueStatus == 0) 
			message = getString(R.string.not_uploaded);
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
			message = getString(R.string.upload_success);
		else if (queueStatus == QueueManager.UPLOAD_STATUS_IN_PROGRESS) 
			message = "Upload in progress";
		else if (queueStatus == QueueManager.UPLOAD_NO_CONNECTIVITY) 
			message = "Upload stalled - Waiting for connectivity";
		else if (queueStatus == QueueManager.UPLOAD_STATUS_FAILURE) 
			message = getString(R.string.upload_fail);
		else if (queueStatus == QueueManager.UPLOAD_STATUS_CREDENTIALS_INVALID) 
			message = "Upload stalled - username/password incorrect";
		else Log.i(TAG, "Not a valid number stored in database.");
		//Log.i(TAG, "Message being set as the status of the procedure: " 
		//		+ message);
		return message;
	}
	
	private String getPatientNameFromTable(String uuid) {
		String[] row = new String[4];
		final String[] projection = new String[] { 
				Patients.Contract.PATIENT_ID, 
				Patients.Contract.UUID,
				Patients.Contract.GIVEN_NAME,
				Patients.Contract.FAMILY_NAME };
		Uri uri = Patients.CONTENT_URI;
		String selection = Patients.Contract._ID + " = ?";
		String[] selectionArgs = new String[] { uuid };
		// Handle any 
		if(uuid.startsWith("content")){
			Log.w(TAG, "uuid value is a uri: " + uuid ); 
			uri = Uri.parse(uuid);
			selectionArgs = null;
			selection = null;
		} else {
			selection = Patients.Contract.UUID + " = ?";
			Log.d(TAG, "Patient uuid value: " + uuid ); 
		}
		
		if(patientToName.containsKey(uuid)) {
			row = patientToName.get(uuid);
		} else {
			row = new String[4];
			try{
				Cursor cur2 = getContentResolver().query(uri,
					projection,
					selection,
					selectionArgs, null);
				if(cur2 != null && cur2.moveToFirst()){
					for(int i=0; i < 4; i++){
						row[i] = cur2.getString(i);
					}
				}
				if(cur2 != null) cur2.close();
				patientToName.put(uuid, row);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	
		StringBuilder result = new StringBuilder(row[0]);
        if(!TextUtils.isEmpty(row[2])) {
        	result.append(" - ");
        	result.append(row[2]);
        	result.append(" ");
        	result.append(row[3]);
        }
        return result.toString();
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
					String procedureId = cur.getString(cur.getColumnIndex(Encounters.Contract.PROCEDURE));
					String procedureName = "UNKNOWN";
					Uri procedure;
					if(UUIDUtil.isValid(procedureId))
						procedure = Uris.withAppendedUuid(Procedures.CONTENT_URI, procedureId); 
					else
						procedure = ContentUris.withAppendedId(Procedures.CONTENT_URI, Long.parseLong(procedureId));
					
					procedureName = lookupProcedureName(procedure);
					((TextView)v).setText(procedureName);
					break;
				case 3:
					String patientUuid = cur.getString(cur.getColumnIndex(Encounters.Contract.SUBJECT));
					//Log.i(TAG, "Setting patient name for patient. " + patientUuid);
					String patientName = getPatientNameFromTable(patientUuid);
					((TextView)v).setText(patientName);
					break;
				case 5:
					Log.i(TAG, "Setting upload queue status in text view.");
					Locales.updateLocale(this, getString(R.string.force_locale));
					int queueStatus = cur.getInt(cur.getColumnIndex(Encounters.Contract.UPLOAD_STATUS));
					int queuePosition = cur.getInt(cur.getColumnIndex(Encounters.Contract.UPLOAD_QUEUE));
					String message = getUploadStatus2(queueStatus, 
							queuePosition + 1);
					//Log.d(TAG, "Setting upload status to : " + message);
					((TextView)v).setText(message);
					break;
				}
			}
		}
		catch (Exception e) {
			Log.e(TAG, "Exception in setting the text in the list: " + 
					e.toString() + ((cur != null)? cur.getPosition(): -1));
			e.printStackTrace();
		}
		return true;
	}
		
	/** {@inheritDoc} */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Log.d(TAG, "onCreate()");
		
		// Connect to the background uploader service
		try {
        	//mConnector.setServiceListener(
        	//		new BackgroundUploaderConnectionListener());
        	//mConnector.connect(this);
        }
        catch (Exception e) {
        	Log.e(TAG, "Exception starting background upload service: " 
        			+ e.toString());
        	e.printStackTrace();
        }
		
        Cursor cursor = managedQuery(Encounters.CONTENT_URI, 
        		PROJECTION, null, null, 
        		Encounters.DEFAULT_SORT_ORDER);
        try {
	        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,	                
	        		R.layout.row, cursor,
	                new String[] { Encounters.Contract.PROCEDURE, 
	        					   Encounters.Contract.SUBJECT, 
	        					   Encounters.Contract.UPLOAD_STATUS },
	                new int[] { R.id.toptext, R.id.bottomtext, 
	        					R.id.queue_status });
	        adapter.setViewBinder(this);
	        setListAdapter(adapter);
        }
        catch (Exception e) {
			e.printStackTrace();
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
	
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
		
		//CheckBox view = (CheckBox) v.findViewById(R.id.icon);
		//view.setChecked(!view.isChecked());
		//l.setItemChecked(position, view.isChecked());
        Uri uri = ContentUris.withAppendedId(Encounters.CONTENT_URI, id);
        Log.i(TAG, "procedure Uri in onListItemClick: " + uri);
        String action = getIntent().getAction();
        Log.i(TAG, "...action: " + action);
        if (Intent.ACTION_PICK.equals(action) || 
        		Intent.ACTION_GET_CONTENT.equals(action))
        {
            // The caller is waiting for us to return a note selected by
            // the user.  The have clicked on one, so return it now.
        	
        	//MDSInterface.logObservations(this, uri.toString());
            setResult(RESULT_OK, new Intent().setData(uri));
            finish();
        } else if (Intent.ACTION_VIEW.equals(action)){
        	startActivity(new Intent(Intent.ACTION_VIEW, uri));
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
		menu.add(0, SELECT_ALL, 0, getString(R.string.menu_select_all));
		menu.add(0, DELETE, 1, getString(R.string.menu_delete));
		menu.add(0, RESEND, 2, getString(R.string.menu_resend));
		//menu.add(0, CANCEL_UPLOAD, 2, "Cancel Upload");
		return true;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean onOptionsItemSelected(MenuItem item){

		switch (item.getItemId()) {
		case SELECT_ALL:
			if(!selectAllToggle) 
				selectAllProcedures();
			else
				unselectAllProcedures();
			selectAllToggle = !selectAllToggle;
			return true;
		case DELETE:
			deleteSelected();
			return true;
		case RESEND:
			resendSelected2();
			return true;
		/*case CANCEL_UPLOAD:
			cancelUploads();
			return true;*/
		}
		return false;
	}
	boolean selectAllToggle = false;
	
	/** All checkboxes will be checked */
	private void selectAllProcedures() {
			
			for (int x = 0; x < getListAdapter().getCount(); x++) {
				try {
					//getListView().setItemChecked(x, true);
					//boolean checked = getListView().isItemChecked(x);
					//Log.w(TAG, "....ListView Item:"+ x+ ", checked : " + checked);
				CheckBox checkbox = (CheckBox) getListView().getChildAt(x)
												.findViewById(R.id.icon);
				checkbox.setChecked(true);
				Log.i(TAG, "....Is checkbox checked? (Should be true): " 
						+ checkbox.isChecked());
				}
				catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "Exception in selectAll(): pos: " + x+" ," + e.getMessage());
				}
			}
	}
	
	/**
	 * Unselect all checked items in the list.
	 */
	private void unselectAllProcedures() {
		try {
			for (int x = 0; x < getListAdapter().getCount(); x++) {
				View v = getListView().getChildAt(x);
				//getListView().setItemChecked(x, false);
				CheckBox checkbox = (CheckBox) v.findViewById(R.id.icon);
				checkbox.setChecked(false);
				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Exception in unselectAll(): " + e.toString());
		}
	}
	
	/**
	 * Delete every checked item in the list.
	 */
	private void deleteSelected() {
		List<Long> ids = new LinkedList<Long>();
		List<String> uuids = new LinkedList<String>();
		ListAdapter adapter = getListAdapter();
			for (int x = 0; x < adapter.getCount(); x++) {
				try{
					CheckBox checkbox = (CheckBox) getListView().getChildAt(x)
												.findViewById(R.id.icon);
					Cursor c = (Cursor) adapter.getItem(x);
					if (checkbox.isChecked()) {
						long itemId = adapter.getItemId(x);
						uuids.add(c.getString(1));
						ids.add(itemId);
					}

				}
				catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "Exception in deleteSelected(): pos: " + x+" ," + e.getMessage());
				}
			}
			Log.w(TAG, "Delete count: " + ids.size());
			String idList = SanaUtil.formatPrimaryKeyList(ids);
			String uuidList = SanaUtil.formatPrimaryKeyList(uuids);
			Log.w(TAG, "Deleting: " + idList);
			// Now delete the ids
			getContentResolver().delete(Encounters.CONTENT_URI, 
					Encounters.Contract._ID + " IN " + idList, null);

			for(long id: ids){
				int deleted = getContentResolver().delete(ImageSQLFormat.CONTENT_URI, 
	        		ImageSQLFormat.ENCOUNTER_ID + " = ?", 
	        		new String[]{ String.valueOf(id) });
				Log.d(TAG, "Deleted n = " + deleted +" images");
				deleted = getContentResolver().delete(ImageSQLFormat.CONTENT_URI, 
	        		ImageSQLFormat.ENCOUNTER_ID + " = ?", 
	        		new String[]{ "encounter" });
				Log.d(TAG, "Deleted n = " + deleted +" bad images");
			}
			/*
			getContentResolver().delete(Observations.CONTENT_URI, 
	        		Observations.Contract.ENCOUNTER + " IN " + uuidList,null);
	        
	        getContentResolver().delete(ImageSQLFormat.CONTENT_URI, 
	        		ImageSQLFormat.ENCOUNTER_ID + " IN " + idList, null);
			/*
			for(long id: ids){
				int index = getListView().
				String uuid = ((Cursor)getListAdapter().get)).getString(columnIndex);
				getContentResolver().delete(Observations.CONTENT_URI, 
	        		Observations.Contract.ENCOUNTER + " = ?", 
	        		new String[]{ uuid });
	        	getContentResolver().delete(ImageSQLFormat.CONTENT_URI, 
	        		ImageSQLFormat.ENCOUNTER_ID + " = ?", 
	        		new String[]{ uuid });
	        
			}
			*/
			unselectAllProcedures();
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
					//boolean checked = getListView().isItemChecked(x);
					//Log.w(TAG, "ListView Item:"+ x+ ", checked : " + checked);
					CheckBox checkbox = (CheckBox)view.getChildAt(x)
													.findViewById(R.id.icon);
					if (checkbox.isChecked()) {
						Uri procedure = ContentUris.withAppendedId(contentUri, 
								(Long) view.getItemIdAtPosition(x));
						Log.i(TAG, "Resending encounter: " + procedure);
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
	
	private void resendSelected2(){
		// use the uEncounter field to Post the data.
		long[] ids = null;
		//if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO)
		SimpleCursorAdapter c = (SimpleCursorAdapter) this.getListAdapter();
		for (int x = 0; x < c.getCount(); x++) {
			//boolean checked = getListView().isItemChecked(x);
			//Log.w(TAG, "ListView Item:"+ x+ ", checked : " + checked);
			Uri encounter = ContentUris.withAppendedId(Encounters.CONTENT_URI, getListView().getItemIdAtPosition(x));
			Intent upload = new Intent("org.sana.android.intent.action.CREATE", encounter);
			Cursor cur = c.getCursor();
			//String message = String.format("encounter: %s, procedure: %s, subject %d",
			//		cur.getString(2), cur.getString(3), cur.getInt(4));
			StringBuilder sb = new StringBuilder();
			//DatabaseUtils.dumpCurrentRow(c.getCursor(), sb);
			Log.w(TAG, sb.toString());
			startService(upload);
			
			String msg = "Sending dispatcher CREATE for: " + encounter;
			Log.w(TAG, msg);
	        EventDAO.registerEvent(this, EventType.ENCOUNTER_SAVE_UPLOAD, msg);
		}
	}
 
    
	
	//private static ContentResolver contentResolver;
	//If selected procedures are in queue or are currently being uploaded, cancel the upload
	private void cancelUploads() {
		try{
			for (int x = 0; x < getListAdapter().getCount(); x++) {
				CheckBox checkbox = (CheckBox) getListView().getChildAt(x).findViewById(R.id.icon);
				Uri procedure = ContentUris.withAppendedId(getIntent().getData(), (Long) getListView().getItemIdAtPosition(x));
				if (checkbox.isChecked()) {
					// TODO Make is cancel if its in the middle of uploading, not just taking it out of the queue
					//mUploadService.removeFromQueue(procedure);
				}
			}
			unselectAllProcedures();
		}
		catch (Exception e) {
			unselectAllProcedures();
			Log.i(TAG, "Exception in cancelSelected(): " + e.toString());
		}
	}

}
