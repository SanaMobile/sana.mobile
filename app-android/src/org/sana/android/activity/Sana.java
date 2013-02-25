package org.sana.android.activity;

import org.sana.R;
import org.sana.android.Constants;
import org.sana.android.activity.settings.Settings;
import org.sana.android.db.SanaDB.NotificationSQLFormat;
import org.sana.android.db.SanaDB.PatientSQLFormat;
import org.sana.android.db.SanaDB.ProcedureSQLFormat;
import org.sana.android.db.SanaDB.SavedProcedureSQLFormat;
import org.sana.android.media.EducationResource;
import org.sana.android.procedure.Procedure;
import org.sana.android.service.BackgroundUploader;
import org.sana.android.service.ServiceConnector;
import org.sana.android.service.ServiceListener;
import org.sana.android.task.CheckCredentialsTask;
import org.sana.android.task.MDSSyncTask;
import org.sana.android.task.ResetDatabaseTask;
import org.sana.android.task.ValidationListener;
import org.sana.android.util.SanaUtil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * Main Sana activity. When Sana is launched, this activity runs, allowing the 
 * user to either run a procedure, view notifications, or view pending 
 * transfers.
 * 
 * @author Sana Dev Team
 */
public class Sana extends SherlockActivity implements View.OnClickListener {
    public static final String TAG = Sana.class.getSimpleName();

    // Option menu codes
    private static final int OPTION_RELOAD_DATABASE = 0;
    private static final int OPTION_SETTINGS = 1;
	private static final int OPTION_SYNC = 2;
	
    // Activity request codes
	/** Intent request code for picking a procedure */
    public static final int PICK_PROCEDURE = 0;
    
    /** Intent request code for picking a saved procedure */
    public static final int PICK_SAVEDPROCEDURE = 1;
    
    /** Intent request code for picking a notification */
    public static final int PICK_NOTIFICATION = 2;
    
    /** Intent request code to start running a procedure */
    public static final int RUN_PROCEDURE = 3;
    
    /** Intent request code to resume running a saved procedure*/
    public static final int RESUME_PROCEDURE = 4;
    
    /** INtent request code to view settings */
    public static final int SETTINGS = 6;
    
    /** Intent request code for creating a new patient. */
    public static final int NEW_PATIENT = 7;
    
    /** Intent request code for viewing all patients. */
    public static final int PICK_PATIENT = 8;
    
    //Alert dialog codes
    private static final int DIALOG_INCORRECT_PASSWORD = 0;
	private static final int DIALOG_NO_CONNECTIVITY = 1;
	private static final int DIALOG_NO_PHONE_NAME = 2;
    
    private ServiceConnector mConnector = new ServiceConnector();
    private BackgroundUploader mUploadService = null;
    private CheckCredentialsTask mCredentialsTask;
    private ResetDatabaseTask mResetDatabaseTask;
    private MDSSyncTask mSyncTask;
    
    // State 
    private Bundle mSavedState;
    static final String STATE_CHECK_CREDENTIALS = "_credentials";
    static final String STATE_MDS_SYNC = "_mdssync";
    static final String STATE_RESET_DB = "_resetdb";
    
    /**
     * Background listener for taking action when network service is available
     * 
     * @author Sana Development Team
     *
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
     * Background listener to signal that credentials have been validated with
     * the permanent data store.
     * 
     * @author Sana Development Team
     */
    private class CredentialsValidatedListener implements ValidationListener {
    	/**
         * Called when CheckCredentialsTask completes
         */
    	public void onValidationComplete(int validationResult) {
    		switch(validationResult){
    		case(CheckCredentialsTask.CREDENTIALS_NO_CONNECTION):
    			Log.i(TAG, "Cannot validate EMR credentials -"+
					"no network connectivity!");
    			if(!isFinishing())
    				showDialog(DIALOG_NO_CONNECTIVITY);
    			break;
    		case(CheckCredentialsTask.CREDENTIALS_INVALID):
    			Log.i(TAG, "Could not validate EMR username/password");
				if (mUploadService != null) {
					mUploadService.onCredentialsChanged(false);
				}
    			break;
    		case(CheckCredentialsTask.CREDENTIALS_VALID):
    			Log.i(TAG, "Username/Password for EMR correct");
				if (mUploadService != null) {
					mUploadService.onCredentialsChanged(true);
				}
    			break;
    		}
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
					+ e.getMessage());
			e.printStackTrace();
		}
	}

	/** {@inheritDoc} */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.main);
        
        View viewPatients = findViewById(R.id.moca_main_view_patients);
        viewPatients.setOnClickListener(this);
        
        View openProcedure = findViewById(R.id.moca_main_procedure);
        openProcedure.setOnClickListener(this);

        View viewTransfers = findViewById(R.id.moca_main_transfers);
        viewTransfers.setOnClickListener(this);

        View viewNotifications = findViewById(R.id.moca_main_notifications);
        viewNotifications.setOnClickListener(this);
        // Create a connection to the background upload service. 
        // This starts the service when the app starts.
        try {
        	mConnector.setServiceListener(
        			new BackgroundUploaderConnectionListener());
        	mConnector.connect(this);
        }
        catch (Exception e) {
        	Log.e(TAG, "Exception starting background upload service: " 
        			+ e.getMessage());
        	e.printStackTrace();
        }
    }
    
    void init(){

        SharedPreferences preferences = 
        		PreferenceManager.getDefaultSharedPreferences(this);
        if(!preferences.getBoolean(Constants.DB_INIT,false)){
        	doClearDatabase();
            // Make sure directory structure is in place on external drive
            EducationResource.intializeDevice();
            Procedure.intializeDevice();
            preferences.edit().putBoolean(Constants.DB_INIT, true).commit();
        }
    }
    
    /** 
     * Starts Activity for viewing all patients 
     * 
     * @param intentWithProcedureInfo Intent to use for selecting a patient 
     * (optional). This Intent should contain information about the procedure
     * to start for the selected patient (via Bundle extra).
     */
    private void pickPatient(Intent intentWithProcedureInfo) {
        if (intentWithProcedureInfo == null) {
            intentWithProcedureInfo = new Intent();
        }
        intentWithProcedureInfo.setAction(Intent.ACTION_PICK);
        intentWithProcedureInfo.setType(PatientSQLFormat.CONTENT_TYPE);
        intentWithProcedureInfo.setData(PatientSQLFormat.CONTENT_URI);
        startActivityForResult(intentWithProcedureInfo, PICK_PATIENT);
    }
    
    /** 
     * Activates selecting a procedure and to start a new encounter 
     * 
     * @param intentWithPatientId Optional Intent for selecting a Procedure.
     * This Intent should contain the patient (patient ID) to run the 
     * Procedure for.
     */
    private void pickProcedure(Intent intentWithPatientId) {
        if (intentWithPatientId == null) {
            intentWithPatientId = new Intent();
        }
        intentWithPatientId.setAction(Intent.ACTION_PICK);
        intentWithPatientId.setType(ProcedureSQLFormat.CONTENT_TYPE);
        intentWithPatientId.setData(ProcedureSQLFormat.CONTENT_URI);
        startActivityForResult(intentWithPatientId, PICK_PROCEDURE);
    }
    
    /** Starts Activity for selecting and then viewing a previous encounter */
    private void pickSavedProcedure() {
    	Intent i = new Intent(Intent.ACTION_PICK);
    	i.setType(SavedProcedureSQLFormat.CONTENT_TYPE);
    	i.setData(SavedProcedureSQLFormat.CONTENT_URI);
    	startActivityForResult(i, PICK_SAVEDPROCEDURE);
    }

    /** Starts Activity for selecting and then viewing notifications */
    private void pickNotification() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType(NotificationSQLFormat.CONTENT_TYPE);
        i.setData(NotificationSQLFormat.CONTENT_URI);
        startActivityForResult(i, PICK_NOTIFICATION);
    }
    
	/** {@inheritDoc} */
    @Override
    public void onClick(View arg0) {
		Log.d(TAG, "Button: " +arg0.getId());
		switch (arg0.getId()) {
		// buttons on the main screen
        case R.id.moca_main_view_patients:
            pickPatient(null);
            break;
		case R.id.moca_main_procedure:
			pickProcedure(null);
			break;
		case R.id.moca_main_transfers:
			pickSavedProcedure();
			break;
		case R.id.moca_main_notifications:
			pickNotification();
			break;
		}
	}
    
    /** {@inheritDoc} */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, 
    		Intent data) 
    {
    	SanaUtil.logActivityResult(TAG, requestCode, resultCode);
        switch (resultCode) {
        case RESULT_CANCELED:
        	if(requestCode == RUN_PROCEDURE) {
        		pickProcedure(null);
        	} else if(requestCode == RESUME_PROCEDURE) {
        		pickSavedProcedure();
        	} else if(requestCode == SETTINGS) {
        		//Check to make sure there is a phone number entered, 
        		// otherwise will not connect to MDS
        		String phoneNum = PreferenceManager
        							.getDefaultSharedPreferences(this)
        							.getString(Constants.PREFERENCE_PHONE_NAME, 
        										null);
        		Log.d(TAG, "phoneNum from preferences is: " + phoneNum);
        		if (TextUtils.isEmpty(phoneNum)) {
        			Log.d(TAG, "No phone number entered - showing dialog now");
        			if(!isFinishing())
        				showDialog(DIALOG_NO_PHONE_NAME);
        		}
        		// Attempt to validate the credentials changed in the settings.
        		if(mCredentialsTask != null) 
        			if(mCredentialsTask.getStatus() == Status.FINISHED)
        			{
        				mCredentialsTask = new CheckCredentialsTask();
        				mCredentialsTask.setValidationListener(
        								new CredentialsValidatedListener());
        				mCredentialsTask.execute(this);
        			}
        	}
            break;
        case RESULT_OK:
        	Uri uri = null;
        	if(data != null) {
        		uri = data.getData();
        	}
        	if(requestCode == PICK_PROCEDURE) {
        		assert(uri != null);
        		long patientId = data.getLongExtra(PatientsList.EXTRA_PATIENT_ID, PatientsList.INVALID_PATIENT_ID);
        		if (patientId == PatientsList.INVALID_PATIENT_ID) {
        		    pickPatient(data);
        		} else {
        		    doPerformProcedureForPatient(uri, patientId);
        		}
        	} else if(requestCode == PICK_SAVEDPROCEDURE) {
        		assert(uri != null);
        		doResumeProcedure(uri);
        	} else if(requestCode == PICK_NOTIFICATION) {
        		assert(uri != null);
        		doShowNotification(uri);
        	} else if (requestCode == RUN_PROCEDURE || 
        				requestCode == RESUME_PROCEDURE) {
        		pickSavedProcedure();
        	} else if (requestCode == PICK_PATIENT) {
        	    long patientId = data.getLongExtra(PatientsList.EXTRA_PATIENT_ID, PatientsList.INVALID_PATIENT_ID);
        	    assert(patientId != PatientsList.INVALID_PATIENT_ID);
        	    Uri procedureUri = data.getParcelableExtra(ProceduresList.EXTRA_PROCEDURE_URI);
        	    if (procedureUri == null) {
        	        pickProcedure(data);
        	    } else {
        	        doPerformProcedureForPatient(procedureUri, patientId);
        	    }
        	}
            break;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_INCORRECT_PASSWORD:
        	return new AlertDialog.Builder(this)
        	.setTitle("Error!")
            .setMessage(getString(R.string.dialog_incorrect_credentials))
            .setPositiveButton(getString(R.string.general_change_settings), 
            		new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	// Dismiss dialog and return to settings                	
                	Intent i = new Intent(Intent.ACTION_PICK);
                    i.setClass(Sana.this, Settings.class);
                    startActivityForResult(i, SETTINGS);
                	setResult(RESULT_OK, null);
                	dialog.dismiss();
                }
            })
            .setCancelable(true)
            .setNegativeButton(getString(R.string.general_cancel), 
            		new OnClickListener() 
            {
				public void onClick(DialogInterface dialog, int whichButton) {
					setResult(RESULT_CANCELED, null);
                	dialog.dismiss();
				}
            })
            .create();
        case DIALOG_NO_CONNECTIVITY:
        	return new AlertDialog.Builder(this)
        	.setTitle(getString(R.string.general_error))
            .setMessage(getString(R.string.dialog_no_network))
            .setPositiveButton(getString(R.string.general_ok), 
            		new DialogInterface.OnClickListener() 
            {
                public void onClick(DialogInterface dialog, int whichButton) {
                	// Dismiss dialog and return to settings
                	setResult(RESULT_OK, null);
                	dialog.dismiss();
                }
            })
            .create();
        case DIALOG_NO_PHONE_NAME:
        	return new AlertDialog.Builder(this)
        	.setTitle(getString(R.string.general_error))
            .setMessage(getString(R.string.dialog_no_phone_name))
            .setPositiveButton(getString(R.string.general_change_settings), 
            		new DialogInterface.OnClickListener() 
            {
                public void onClick(DialogInterface dialog, int whichButton) {
                	// Dismiss dialog and return to settings                	
                	Intent i = new Intent(Intent.ACTION_PICK);
                    i.setClass(Sana.this, Settings.class);
                    startActivityForResult(i, SETTINGS);
                	setResult(RESULT_OK, null);
                	dialog.dismiss();
                }
            })
            .setCancelable(true)
            .setNegativeButton(getString(R.string.general_cancel), 
            		new OnClickListener() 
            {
				public void onClick(DialogInterface dialog, int whichButton) {
					setResult(RESULT_CANCELED, null);
                	dialog.dismiss();
				}
            })
            .create();
        }
        return null;
    }

    /**
     * Starts Activity for viewing a Notification.
     * 
     * @param uri The notification to view.
     */
    private void doShowNotification(Uri uri) {
    	try {
    		Intent i = new Intent(Intent.ACTION_VIEW, uri);
    		startActivity(i);
    	} catch(Exception e) {
    		Log.e(TAG, "While showing notification " + uri 
    				+ " an exception occured: " + e.toString());
    	}
    }
    
    /**
     * Starts Activity for resuming a saved procedure
     * 
     * @param uri The saved procedure to restart
     */
    private void doResumeProcedure(Uri uri) {
    	try {
    		Intent i = new Intent(Intent.ACTION_VIEW, uri);
    		i.putExtra("savedProcedureUri", uri.toString());
    		startActivityForResult(i, RESUME_PROCEDURE);
    	} catch(Exception e) {
    		Log.e(TAG, "While resuming procedure " 
    				+ uri + " an exception occured: " + e.toString());
    	}
    }
    
    /**
     * Starts an Activity for running a new Procedure
     * 
     * @param uri The Procedure to run
     */
    private void doPerformProcedureForPatient(final Uri uri, long patientId) {
        Log.i(TAG, "doPerformProcedure uri=" + uri.toString());
        try {
        	Intent i = new Intent(Intent.ACTION_VIEW, uri);
        	i.putExtra(PatientsList.EXTRA_PATIENT_ID, patientId);
    		startActivityForResult(i, RUN_PROCEDURE);
        } catch (Exception e) {
            SanaUtil.errorAlert(this, e.toString());
            Log.e(TAG, "While running procedure " + uri 
            		+ " an exception occured: " + e.toString());
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
        menu.add(0, OPTION_RELOAD_DATABASE, 0, 
        		getString(R.string.menu_reload_db));
        menu.add(0, OPTION_SETTINGS, 1, getString(R.string.menu_settings));
		menu.add(0, OPTION_SYNC, 2, getString(R.string.menu_sync));
        return true;
    }
    
    /** Executes a task to clear out the database */
    private void doClearDatabase() {
    	// TODO: context leak
    	if(mResetDatabaseTask!= null && mResetDatabaseTask.getStatus() != Status.FINISHED)
    			return;
    	mResetDatabaseTask = 
    			(ResetDatabaseTask) new ResetDatabaseTask(this).execute(this);
    }
    
    /** Syncs the Patient database with MDS */
    private void doUpdatePatientDatabase() {
    	if(mSyncTask != null && mSyncTask.getStatus() != Status.FINISHED)
    		return;
    	mSyncTask = (MDSSyncTask) new MDSSyncTask(this).execute(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
        case OPTION_RELOAD_DATABASE:
        	// TODO: Dialog leak
        	AlertDialog.Builder bldr = new AlertDialog.Builder(this);
        	AlertDialog dialog = bldr.create();
        	dialog.setMessage(getString(R.string.dialog_no_reload_db_warn));
        	dialog.setCancelable(true);
        	dialog.setButton("Yes", new OnClickListener() {
        		public void onClick(DialogInterface i, int v) {
        			doClearDatabase();
        		}
        	});
        	dialog.setButton2(getString(R.string.general_no), 
        			(OnClickListener)null);
        	if(!isFinishing())
        		dialog.show();
            return true;
        case OPTION_SETTINGS:
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setClass(this, Settings.class);
            startActivityForResult(i, SETTINGS);
            return true;
        case OPTION_SYNC:
        	doUpdatePatientDatabase();
			return true;
        }    
        return false;
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveLocalTaskState(outState);
        mSavedState = outState;
    }
    
    private void saveLocalTaskState(Bundle outState){
    	final CheckCredentialsTask task = mCredentialsTask;
        if (task != null && task.getStatus() != Status.FINISHED) {
        	task.cancel(true);
        	outState.putBoolean(STATE_CHECK_CREDENTIALS, true);
        }
    	final MDSSyncTask mTask = mSyncTask;
        if (mTask != null && mTask.getStatus() != Status.FINISHED) {
        	mTask.cancel(true);
        	outState.putBoolean(STATE_MDS_SYNC, true);
        }
    	final ResetDatabaseTask rTask = mResetDatabaseTask;
        if (rTask != null && rTask.getStatus() != Status.FINISHED) {
        	rTask.cancel(true);
        	outState.putBoolean(STATE_RESET_DB, true);
        }
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreLocalTaskState(savedInstanceState);
        mSavedState = null;
    }
    
    /** Restores any tasks running on this thread */
    private void restoreLocalTaskState(Bundle savedInstanceState){
    	if (savedInstanceState.getBoolean(STATE_CHECK_CREDENTIALS)){
			final CheckCredentialsTask task = new CheckCredentialsTask();
			task.setValidationListener(
						new CredentialsValidatedListener());
			mCredentialsTask = (CheckCredentialsTask) task.execute(this);
    	}
    	if (savedInstanceState.getBoolean(STATE_MDS_SYNC))
    		mSyncTask = (MDSSyncTask) new MDSSyncTask(this).execute(this);
    	if (savedInstanceState.getBoolean(STATE_RESET_DB))
    		mResetDatabaseTask = 
    			(ResetDatabaseTask) new ResetDatabaseTask(this).execute(this);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (mSavedState != null) restoreLocalTaskState(mSavedState);
        init();
    }
}