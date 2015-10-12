package org.sana.android.activity;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import org.sana.R;
import org.sana.android.app.Locales;
import org.sana.android.content.Intents;
import org.sana.android.content.Uris;
import org.sana.android.db.EventDAO;
import org.sana.android.db.ModelWrapper;
import org.sana.android.db.SanaDB.ImageSQLFormat;
import org.sana.android.fragment.EncounterListFragment;
import org.sana.android.net.MDSInterface;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Observations;
import org.sana.android.provider.Patients;
import org.sana.android.provider.Subjects;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Events.EventType;
import org.sana.android.service.BackgroundUploader;
import org.sana.android.service.QueueManager;
import org.sana.android.service.ServiceConnector;
import org.sana.android.service.ServiceListener;
import org.sana.android.util.SanaUtil;
import org.sana.api.IModel;
import org.sana.api.task.Status;
import org.sana.net.Response;
import org.sana.util.UUIDUtil;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.Toast;


/**
 * Displays a list of previous encounters and their status.
 *
 * @author Sana Development Team
 */
public class EncounterList extends BaseActivity implements
        EncounterListFragment.OnModelItemSelectedListener
{
	private static final String TAG = EncounterList.class.getSimpleName();
	private EncounterListFragment mListFragment;

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onStart()");
        super.onCreate(savedInstanceState);
        Locales.updateLocale(this, getString(R.string.force_locale));
        setContentView(R.layout.activity_encounterlist);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
        LocalBroadcastManager.getInstance(this.getApplicationContext()).unregisterReceiver(mReceiver);
    }

   @Override
    protected void onResume() {
        super.onResume();
    	Log.i(TAG, "onResume()");
        IntentFilter filter = new IntentFilter(Response.RESPONSE);
        try{
            filter.addDataType(Encounters.CONTENT_ITEM_TYPE);
        } catch (Exception e){}
        
        LocalBroadcastManager.getInstance(this.getApplicationContext()).registerReceiver(mReceiver, filter);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        Log.d(TAG, "onStart()");
        super.onAttachFragment(fragment);
        Locales.updateLocale(this, getString(R.string.force_locale));
        if (fragment.getClass() == EncounterListFragment.class) {
            mListFragment = (EncounterListFragment) fragment;
            mListFragment.setOnModelItemSelectedListener(this);
            //showProgressDialogFragment(null);
        }
    }


    @Override
    public void onModelItemSelected(long id) {
		Log.i(TAG, "onModelItemSelected() " + id);
        Bundle data = mListFragment.getSelectedData(id);
        Uri encounter = mListFragment.getItemUri(id);
        Boolean finished = mListFragment.isItemFinished(id);
		Log.d(TAG, "....finished=" + finished + ", uri=" + encounter);
        Intent intent = null;
        try{
			if(finished || !finished){
				intent = new Intent(Intent.ACTION_VIEW, encounter);
				startActivity(intent);
			} else {
				intent = new Intent(Intent.ACTION_EDIT, encounter);
				startActivity(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    /*
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
	private HashMap<Integer,Boolean> checkboxes =
            new HashMap<Integer,Boolean>();

    private ServiceConnector mConnector = new ServiceConnector();
    private BackgroundUploader mUploadService = null;
    SimpleDateFormat sdf = new SimpleDateFormat(IModel.DATE_FORMAT,Locale.US);
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd",Locale.US);
    /**
     * Listens for connections to the BackgroundUploader service
     * @author Sana Development Team
     *
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
	 *
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
        Log.i(TAG, "lookupProcedureName() " + procedure);
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
	/** Parses patient name from JSON String *
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

	/** Gets the upload status of an item in the queue *
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
			message = getString(R.string.general_upload_in_progress);
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
        Log.i(TAG, "getPatientNameFromTable() uuid value: " + uuid );
		String[] row = new String[4];
		final String[] projection = new String[] {
				Patients.Contract.PATIENT_ID,
				Patients.Contract.UUID,
				Patients.Contract.GIVEN_NAME,
				Patients.Contract.FAMILY_NAME };
		Uri uri = Subjects.CONTENT_URI;
		String selection = Patients.Contract.UUID + " = '?'";
		String[] selectionArgs = new String[] { uuid };
		// Handle any
		if(uuid.startsWith("content")){
			Log.w(TAG, "uuid value is a uri: " + uuid );
			uri = Uri.parse(uuid);
			selectionArgs = null;
			selection = null;
		} else {
			//selection = Patients.Contract.UUID + " = ?";
                        //uri = Uris.withAppendedUuid(Patients.CONTENT_URI,uuid);
                        uri = Uri.parse(Subjects.CONTENT_URI.toString() +"/" + uuid);
			selectionArgs = null;
			selection = null;
		}

		if(patientToName.containsKey(uuid)) {
			row = patientToName.get(uuid);
		} else {
                row = new String[4];
                Cursor cur2 = null;
                try{
                    cur2 = getContentResolver().query(uri,
                    projection,
                    null,
                    null, null);
                    if(cur2 != null && cur2.moveToFirst()){
                        for(int i=0; i < 4; i++){
                            row[i] = cur2.getString(i);
                        }
                    } else {
                        row[0] = uuid;
                    }
                    patientToName.put(uuid, row);
                } catch (Exception e){
                    Log.e(TAG, "error building name row");
                    e.printStackTrace();
                } finally {
                    if(cur2 != null) cur2.close();
                    else Log.e(TAG, "NULL cursor ");
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
	 *
	@Override
	public boolean setViewValue(View v, Cursor cur, int columnIndex) {
		try {
            Log.i(TAG, "setViewValue()" + v.getClass().getSimpleName());
            final int position = cur.getPosition();
            Log.d(TAG, "....position=" + position);
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
					Locales.updateLocale(this, getString(R.string.force_locale));
					int queueStatus = cur.getInt(cur.getColumnIndex(Encounters.Contract.UPLOAD_STATUS));
					int queuePosition = cur.getInt(cur.getColumnIndex(Encounters.Contract.UPLOAD_QUEUE));
					String message = getUploadStatus2(queueStatus,
							queuePosition + 1);
					//Log.d(TAG, "Setting upload status to : " + message);
					((TextView)v).setText(message);
					break;
                                //case 7:
                                //        String dateStr = cur.getString(cur.getColumnIndex(Encounters.Contract.MODIFIED));
                                //        Date date = sdf.parse(dateStr);
                                //        ((TextView)v).setText(df.format(date));
				}
			} else if (v instanceof  CheckBox){
                final CheckBox checkbox = (CheckBox)v;
                checkbox.setClickable(true);
                Boolean checked = checkboxes.get(position);
                if(checked != null){
                    checkbox.setChecked(checked);
                }
                checkbox.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v){
                        final CheckBox ckbx = (CheckBox) v;
                        ckbx.toggle();
                        boolean ckd = ckbx.isChecked();
                        Log.i(TAG,"CheckBox.onClick() position=" + position
                            +", checked="+ ckd);
                        //((CheckBox) v).setChecked(!ckd);
                        checkboxes.put(position, !ckd);
                    }
                });
            }
		} catch (Exception e) {
			Log.e(TAG, "Exception in setting the text in the list: " +
					e.toString() + ((cur != null)? cur.getPosition(): -1));
			e.printStackTrace();
		}
		return true;
	}
*
	/** {@inheritDoc} *
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
                                        Encounters.Contract.MODIFIED,
                                        Encounters.Contract.UPLOAD_STATUS },
	                new int[] { R.id.toptext, R.id.bottomtext,R.id.datetext,
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

	/** {@inheritDoc} *
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
        Log.i(TAG,"onListItemClick() position=" + position + ", id=" + id);
        Log.d(TAG, "....checkbox id="+R.id.icon);
		if(v.getId() == R.id.icon){
            //CheckBox checked = (Checkbox) view;
            Log.d(TAG,"....got a checkbox");
            return;
            //checked.setChecked(!checked.isChecked());
		}//l.setItemChecked(position, view.isChecked());

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
            //startActivity(new Intent(Intent.ACTION_EDIT, uri));

		}

	}

    @Override
	public boolean onLongClick(View v){
        Log.i(TAG,"onLongClick() " + v.getId());
        //Uri uri = ContentUris.withAppendedId(Encounters.CONTENT_URI, id);
        if(v.getId() != R.id.icon){
            String action = getIntent().getAction();
            if (Intent.ACTION_VIEW.equals(action)){
                //startActivity(new Intent(Intent.ACTION_VIEW, uri));
                return false;
            }
        } else {
            Log.d(TAG,"....Checkbox");
            //CheckBox checked = (Checkbox) v;
            return false;
        }
        return false;
	}
*/
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

    boolean selectAllToggle = false;
    /** {@inheritDoc} */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
		Log.i(TAG, "onOptionItemsSelected() " + item.getItemId());
        switch (item.getItemId()) {
        case SELECT_ALL:
            if(!selectAllToggle)
                mListFragment.selectAllProcedures();
            else
                mListFragment.unselectAllProcedures();
            selectAllToggle = !selectAllToggle;
            return true;
        case DELETE:
			mListFragment.deleteSelected();
            return true;
        case RESEND:
            resendSelected();
            return true;
        //case CANCEL_UPLOAD:
        //    cancelUploads();
        //    return true;
        }
        return false;
    }
	// gets selec
    private void resendSelected() {
		Log.i(TAG, "resendSelected()");
		int index = 0;
        List<Uri> uris = mListFragment.getSelected();
        for(Uri uri:uris){
            Intent intent = new Intent(Intents.ACTION_CREATE, uri);
            startService(intent);
            index++;
        }
		Log.i(TAG,"....sent: " + index);
    }

    private void resendSelectedFinished() {
		Log.i(TAG, "resendSelectedFinished()");
		int index = 0;
        List<Uri> uris = mListFragment.getSelectedFinished();
        for(Uri uri:uris){
            Intent intent = new Intent(Intents.ACTION_CREATE, uri);
            startService(intent);
            index++;
        }
		Log.i(TAG,"....sent: " + index);
    }

    private void cancelUploads(){

    }
    

    @Override
    protected void handleBroadcast(Intent data){
        Log.i(TAG,"handleBroadcast()");
        cancelProgressDialogFragment();
        String message = data.getStringExtra(Response.MESSAGE);
        Response.Code code = Response.Code.get(data.getIntExtra(Response.CODE,-1));
        switch(code){
            case CONTINUE:
                if(showProgressForeground())
                    showProgressDialogFragment(message);
                break;
            default:
                hideProgressDialogFragment();
                if(!TextUtils.isEmpty(message)){
                    Toast.makeText(this,message,Toast.LENGTH_LONG).show();
                }
        }
    }
/*
	boolean selectAllToggle = false;

	/** All checkboxes will be checked *
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
	 *
	private void unselectAllProcedures() {
        if(mListFragment != null){
            mListFrgament.unselectAllProcedures();
        }
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
	 *
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
			*
			unselectAllProcedures();
	}
*/
/*
	//Resend every checked item in the list.
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
		ListView view = getListView();
		//if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO)
		int count = getListAdapter().getCount();
		Log.i(TAG, "resendSelecected2() count=" + count);
		// iterate over list and resend checked
        for (int x = 0; x < count; x++) {
            boolean checked = getListView().isItemChecked(x);
            Log.w(TAG, "....ListView Item:"+ x+ ", checked : " + checked);
            Boolean ckd = (checkboxes.get(x) != null)? checkboxes.get(x): false;
            Log.d(TAG, "....checkboxes map: " + ckd);
            Uri encounter = ContentUris.withAppendedId(Encounters.CONTENT_URI, getListView().getItemIdAtPosition(x));
            Log.d(TAG, "Checking for encounter: " + encounter);
            View childView = view.getChildAt(x);
            if(childView != null){
                CheckBox checkbox = (CheckBox)childView.findViewById(R.id.icon);
                checked = checkbox.isChecked();
                Log.i(TAG, "....Checkbox checked: " + checkbox.isChecked() + ":" + checked);
            } else {
                Log.i(TAG, "resendSelected2(): NULL child of ListView");
            }
            if (checked || ckd) {
                Intent upload = new Intent("org.sana.intent.action.CREATE", encounter);
                //Cursor cur = c.getCursor();
                //String message = String.format("encounter: %s, procedure: %s, subject %d",
                //  cur.getString(2), cur.getString(3), cur.getInt(4));
                //StringBuilder sb = new StringBuilder();
                //DatabaseUtils.dumpCurrentRow(c.getCursor(), sb);
                startService(upload);
                String msg = "Sending dispatcher CREATE for: " + encounter;
                Log.i(TAG, msg);
                EventDAO.registerEvent(this, EventType.ENCOUNTER_SAVE_UPLOAD, msg);
            } else {
                Log.w(TAG, "Not selected: " + encounter);
            }
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
*/
}
