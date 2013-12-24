package org.sana.android.activity;

import org.sana.R;
import org.sana.android.Constants;
import org.sana.android.activity.settings.Settings;
import org.sana.android.app.ActivityRunner;
import org.sana.android.content.Intents;
import org.sana.android.content.Uris;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Notifications;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Subjects;
import org.sana.android.service.BackgroundUploader;
import org.sana.android.service.ServiceConnector;
import org.sana.android.service.ServiceListener;
import org.sana.android.task.MDSSyncTask;
import org.sana.android.task.ResetDatabaseTask;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


/**
 * Main Sana activity. When Sana is launched, this activity runs, allowing the 
 * user to either run a procedure, view notifications, or view pending 
 * transfers.
 * 
 * @author Sana Dev Team
 */
public class NavigationActivity extends BaseActivity implements View.OnClickListener {
    public static final String TAG = NavigationActivity.class.getSimpleName();

    // Option menu codes
    private static final int OPTION_RELOAD_DATABASE = 0;
    private static final int OPTION_SETTINGS = 1;
	private static final int OPTION_SYNC = 2;

    
    //Alert dialog codes
    private static final int DIALOG_INCORRECT_PASSWORD = 0;
	private static final int DIALOG_NO_CONNECTIVITY = 1;
	private static final int DIALOG_NO_PHONE_NAME = 2;
    
    private ServiceConnector mConnector = new ServiceConnector();
    private BackgroundUploader mUploadService = null;
    private ResetDatabaseTask mResetDatabaseTask;
    private MDSSyncTask mSyncTask;
    
    // State 
    private Bundle mSavedState;
    static final String STATE_CHECK_CREDENTIALS = "_credentials";
    static final String STATE_MDS_SYNC = "_mdssync";
    static final String STATE_RESET_DB = "_resetdb";

	ActivityRunner runner;
	
	/** {@inheritDoc} */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_navigation);
        
        View viewPatients = findViewById(R.id.moca_main_view_patients);
        viewPatients.setOnClickListener(this);
        
        View openProcedure = findViewById(R.id.moca_main_procedure);
        openProcedure.setOnClickListener(this);

        View viewTransfers = findViewById(R.id.moca_main_transfers);
        viewTransfers.setOnClickListener(this);

        View viewNotifications = findViewById(R.id.moca_main_notifications);
        viewNotifications.setOnClickListener(this);
    }
    
    void init(){
        SharedPreferences preferences = 
        		PreferenceManager.getDefaultSharedPreferences(this);
        if(!preferences.getBoolean(Constants.DB_INIT,false)){
        	doClearDatabase();
            // Make sure directory structure is in place on external drive
            //EducationResource.intializeDevice();
            //Procedure.intializeDevice();
            preferences.edit().putBoolean(Constants.DB_INIT, true).commit();
        }
    }
    

    
	/** {@inheritDoc} */
    @Override
    public void onClick(View arg0) {
		Log.d(TAG, "Button: " +arg0.getId());
		switch (arg0.getId()) {
		// buttons on the main screen
        case R.id.moca_main_view_patients:
            pickSubject(arg0);
            break;
		case R.id.moca_main_procedure:
			pickProcedure(arg0);
			break;
		case R.id.moca_main_transfers:
			pickSavedProcedure(arg0);
			break;
		case R.id.moca_main_notifications:
			pickNotification(arg0);
			break;
		default:
			break;
		}
	}
    
    // TODO Load these programmatically
    //--------------------------------------------------------------------------
    // onClick() callbacks
    //--------------------------------------------------------------------------
    public void pickSubject(View v){
    	Intent intent = Intents.copyOf(ActivityRunner.PICK_SUBJECT);
    	setResult(RESULT_OK, intent);
    	this.finish();
    }
    
    public void pickSavedProcedure(View v){
    	Intent intent = Intents.copyOf(ActivityRunner.PICK_ENCOUNTER);
    	setResult(RESULT_OK, intent);
    	finish();
    }
    
    public void pickNotification(View v){
    	Intent intent = Intents.copyOf(ActivityRunner.PICK_NOTIFICATION);
    	setResult(RESULT_OK, intent);
    	finish();
    }
    
    public void pickProcedure(View v){
    	Intent intent = Intents.copyOf(ActivityRunner.PICK_PROCEDURE);
    	setResult(RESULT_OK, intent);
    	finish();
    }
    
    /** {@inheritDoc}
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
                    i.setClass(NavigationActivity.this, Settings.class);
            		onSaveAppState(i);
                    startActivityForResult(i, Uris.SESSION_DIR);
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
                    i.setClass(NavigationActivity.this, Settings.class);
            		onSaveAppState(i);
                    startActivityForResult(i, Uris.SETTINGS_DIR);
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
    */
    
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
            startActivity(i);
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