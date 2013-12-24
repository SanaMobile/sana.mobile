package org.sana.android.activity;

import java.net.URISyntaxException;

import org.sana.R;
import org.sana.analytics.Runner;
import org.sana.android.Constants;
import org.sana.android.app.DefaultActivityRunner;
import org.sana.android.app.Locales;
import org.sana.android.content.Intents;
import org.sana.android.content.Uris;
import org.sana.android.fragment.AuthenticationDialogFragment.AuthenticationDialogListener;
import org.sana.android.media.EducationResource;
import org.sana.android.procedure.Procedure;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Observers;
import org.sana.android.provider.Patients;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Subjects;
import org.sana.android.service.ISessionCallback;
import org.sana.android.service.ISessionService;
import org.sana.android.service.impl.DispatchService;
import org.sana.android.service.impl.SessionService;
import org.sana.android.task.ResetDatabaseTask;
import org.sana.android.util.Logf;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.AsyncTask.Status;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Main Activity which handles user authentication and initializes services that
 * Sana uses.
 * @author Sana Dev Team
 */
public class MainActivity extends BaseActivity implements AuthenticationDialogListener{

	public static final String TAG = MainActivity.class.getSimpleName();
	public static final String CLOSE = "org.sana.android.intent.CLOSE";
	
	public static final int PICK_PATIENT = 1;
	public static final int PICK_ENCOUNTER = 2;
	public static final int PICK_ENCOUNTER_TASK = 3;
	public static final int PICK_PROCEDURE = 4;
	public static final int RUN_PROCEDURE = 5;
	public static final int RUN_REGISTRATION = 6;
	public static final int VIEW_ENCOUNTER = 7;
	
	private Runner<Intent,Intent> runner;
	
	private String mWorkflow = "org.sana.android.app.workflow.DEFAULT";
	// This is the initial state
	private Intent mIntent = new Intent(Intent.ACTION_MAIN); 
	private Intent mPrevious = new Intent(Intent.ACTION_MAIN);
	private boolean checkUpdate = true;
	private boolean init = false;
	
	@Override
    protected void onNewIntent(Intent intent){
    	Logf.D(TAG, "onNewIntent()");
    	super.onNewIntent(intent);
    	dump();
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	Logf.I(TAG, "onActivityResult()", ((resultCode == RESULT_OK)? "OK":"CANCELED" ));
    	switch(resultCode){
    	case RESULT_CANCELED:
    		data = new Intent(Intents.ACTION_CANCEL);
    		//onNext(data);
    		break;
    	case RESULT_OK:
    		if(data != null)
    			onUpdateAppState(data);
			Intent intent = new Intent();
			//if(data != null)
			onSaveAppState(intent);
    		switch(requestCode){
    		case PICK_PATIENT:
    			intent.setAction(Intent.ACTION_PICK)
    			.setData(Procedures.CONTENT_URI)
    			.putExtras(data);
    			startActivityForResult(intent, PICK_PROCEDURE);
    			break;
    		case PICK_PROCEDURE:
    			intent.setAction(Intent.ACTION_VIEW)
    			.setData(data.getData())
    			.putExtras(data);
    			startActivityForResult(intent, RUN_PROCEDURE);
    			break;
    		case PICK_ENCOUNTER:
    			//intent.setAction(Intent.ACTION_VIEW)
    			//.setData(data.getData())
    			intent.setClass(getBaseContext(), ObservationList.class)
    			.setData(data.getData())
    			.putExtras(data);
    			startActivity(intent);
    			break;
    		default:
    		}
    			
    		//data.setAction(Intents.ACTION_OK);
    		//onNext(data);
    		break;
    	}
    }
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	Logf.I(TAG, "onCreate()");
		Locales.updateLocale(getApplicationContext(), getString(R.string.force_locale));
        setContentView(R.layout.main);
    	checkUpdate(Uris.buildUri("package", "org.sana.provider" , ""));
    	mWorkflow = this.getString(R.string.default_workflow);
        runner = new DefaultActivityRunner();
    	onNext(mIntent);
    	init();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
    	Logf.D(TAG, "onPause()");
    	dump();
    }

    @Override
    protected void onResume() {
        super.onResume();
    	Logf.I(TAG, "onResume()");
    	//bindSessionService();
    	// This prevents us from relaunching the login on every resume
    	dump();
    	/*
    	if(mIntent != null){
    		String action = mIntent.getAction();
    		if(action != null && action.equals(Intent.ACTION_MAIN))
    			onNext(mIntent);
    	}
    	*/
    }

    @Override
    protected void onStart(){
    	super.onStart();
    	Logf.I(TAG, "onStart()");
        // We want the session service to be running if Main has been started 
    	// and until after it stops.
    	startService(new Intent(SessionService.ACTION_START));
    }
	
    @Override
    protected void onStop() {
    	Log.d(TAG, "onDestroy()");
        super.onStop();
        // kill the session service if there is nothing else bound
        if(mBound)
        	stopService(new Intent(SessionService.ACTION_START));
    }
    
    @Override
    protected void onDestroy() {
    	Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }
    
    /**
     * Launches the home activity
     */
    void showHomeActivity() {
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        // pass the app state fields
        onSaveAppState(intent);
        //startActivityForResult(intent, State.HOME.ordinal());
    }
    
    /**
     * Launches the authentication activity
     */
    void showAuthenticationActivity() {
        Intent intent = new Intent();
        intent.setClass(this, AuthenticationActivity.class);
        // pass the app state fields
        onSaveAppState(intent);
        //startActivityForResult(intent, State.LOGIN.ordinal());
    }

    protected void onNext(Intent intent){
    	if(intent == null){
    		showAuthenticationDialog();
    		return;
    	}
    	intent.putExtra(Intents.EXTRA_REQUEST, mIntent);
    	mPrevious = Intents.copyOf(mIntent);
        mIntent = runner.next(intent);
        if(mIntent.hasExtra(Intents.EXTRA_TASKS)){
        	Logf.D(TAG, "onNext(Intent)", "sending tasks to dispatcher");
        	Intent tasks = new Intent(MainActivity.this, DispatchService.class);
        	tasks.putExtra(Intents.EXTRA_TASKS, mIntent.getParcelableArrayListExtra(Intents.EXTRA_TASKS));  
        	startService(tasks);
        }
        if(mIntent.getAction().equals(Intents.ACTION_FINISH)){
        	Logf.D(TAG, "onNext(Intent)", "finishing");
        	finish();
        } else {
        	Logf.D(TAG, "onNext(Intent)", "starting for result");
        	this.onSaveAppState(mIntent);
        	startActivityForResult(mIntent, Intents.parseActionDescriptor(mIntent));
        }
    }
    /**
     * Checks whether the application needs to be updated.
     */
    protected void checkUpdate(Uri uri){
    	if(!checkUpdate){
    		Intent update = new Intent("org.sana.android.intent.action.READ");
    		update.setData(uri);//, "application/vnd.android.package-archive");
    		startService(update);
    		checkUpdate = false;
    	}
    }

    int loginsRemaining = 0;
	protected boolean mBound = false;
	protected ISessionService mService = null;
	
	// connector to the session service
 	protected ServiceConnection mConnection = new ServiceConnection(){
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 	    	Log.i(TAG, "ServiceConnection.onServiceConnected()");
 			mService = ISessionService.Stub.asInterface(service);
 			mBound = true;
 		}
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 	    	Log.i(TAG, "ServiceConnection.onServiceDisconnected()");
 			mService = null;
 			mBound = false;
 		}
 	};
 	
 	// handles initiating the session service binding
    protected void bindSessionService(){
    	Log.i(TAG, "bindSessionService()");
    	if(!mBound){
    		if(mService == null){
    			bindService(new Intent(SessionService.ACTION_START), mConnection, Context.BIND_AUTO_CREATE);
    			//bindService(new Intent(SessionService.BIND_REMOTE), mCallbackConnection, Context.BIND_AUTO_CREATE);
    		}
    	}
    }
    
    // handles disconnecting from the session service bindings
    protected void unbindSessionService(){
        // Unbind from the service
        if (mBound){
        	// Detach
    		unbindService(mConnection);
            mBound = false;
        }
    }

    private ResetDatabaseTask mResetDatabaseTask;
    
    
    void init(){
    	Logf.D(TAG, "init()");
    	PreferenceManager.setDefaultValues(this, R.xml.settings, false);
    	PreferenceManager.setDefaultValues(this, R.xml.network_settings,false);
    	PreferenceManager.setDefaultValues(this, R.xml.resource_settings, false);
    	
    	SharedPreferences preferences = 
        		PreferenceManager.getDefaultSharedPreferences(this);
        if(!init){
        	if(!preferences.getBoolean(Constants.DB_INIT, false)){
        		Logf.D(TAG, "init()", "Initializing");
        		doClearDatabase();
            // Make sure directory structure is in place on external drive
            	EducationResource.intializeDevice();
            	Procedure.intializeDevice();
            	preferences.edit().putBoolean(Constants.DB_INIT, true).commit();
            	init = true;
        	} else {
        		Logf.D(TAG, "init()", "reloading");
        		// RELOAD Database
        		doClearDatabase();
        		
        	}
        	preferences.edit().putString("s_phone_name", getPhoneNumber()).commit();
        }
    }
    
    
    /** Executes a task to clear out the database */
    private void doClearDatabase() {
    	// TODO: context leak
    	if(mResetDatabaseTask!= null && mResetDatabaseTask.getStatus() != Status.FINISHED)
    			return;
    	mResetDatabaseTask = 
    			(ResetDatabaseTask) new ResetDatabaseTask(this,true).execute(this);
    }
    
    private void saveLocalTaskState(Bundle outState){
    	final ResetDatabaseTask rTask = mResetDatabaseTask;
        if (rTask != null && rTask.getStatus() != Status.FINISHED) {
        	rTask.cancel(true);
        	outState.putBoolean("_resetdb", true);
        }
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        try {
			mIntent = Intent.parseUri(inState.getString("mIntent"), Intent.URI_INTENT_SCHEME);
			mPrevious = Intent.parseUri(inState.getString("mPrevious"), Intent.URI_INTENT_SCHEME);
			checkUpdate = inState.getBoolean("update");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveLocalTaskState(outState);
        outState.putString("mIntent", mIntent.toUri(Intent.URI_INTENT_SCHEME));
        outState.putString("mPrevious", mPrevious.toUri(Intent.URI_INTENT_SCHEME));
        outState.putBoolean("update", checkUpdate);
    }

    void showAuthenticationDialog(){
    	
    }
    
	/* (non-Javadoc)
	 * @see org.sana.android.fragment.AuthenticationDialogFragment.AuthenticationDialogListener#onDialogPositiveClick(android.support.v4.app.DialogFragment)
	 */
	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		EditText userEdit = (EditText)dialog.getDialog().findViewById(R.id.username);
		EditText userPassword = (EditText)dialog.getDialog().findViewById(R.id.username);
	}

	/* (non-Javadoc)
	 * @see org.sana.android.fragment.AuthenticationDialogFragment.AuthenticationDialogListener#onDialogNegativeClick(android.support.v4.app.DialogFragment)
	 */
	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		try{
			dialog.dismiss();
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			this.finish();
		}
	}
	
	/**
	 * REtrieves the device phone number using the TelephonyManager
	 * @return the device phone number
	 */
	public String getPhoneNumber(){
		TelephonyManager tMgr =(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		String phoneNumber = tMgr.getLine1Number();
		return phoneNumber;
	}
	
	public void submit(View v){
        Intent intent = null;
		switch(v.getId()){
		case R.id.btn_main_select_patient:
			intent = new Intent(Intent.ACTION_PICK);
			intent.setDataAndType(Subjects.CONTENT_URI, Subjects.CONTENT_TYPE);
			startActivityForResult(intent, PICK_PATIENT);
			break;
		case R.id.btn_main_transfers:
			intent = new Intent(Intent.ACTION_PICK);
			intent.setDataAndType(Encounters.CONTENT_URI, Encounters.CONTENT_TYPE);
			startActivityForResult(intent, PICK_ENCOUNTER);
			break;
		case R.id.btn_main_register_patient:
			intent = new Intent(Intent.ACTION_INSERT);
			intent.setDataAndType(Patients.CONTENT_URI, Patients.CONTENT_TYPE);
			startActivityForResult(intent, RUN_REGISTRATION);
			break;
		case R.id.btn_main_procedures:
			intent = new Intent(Intent.ACTION_PICK);
			intent.setDataAndType(Procedures.CONTENT_URI, Procedures.CONTENT_TYPE);
			startActivityForResult(intent, PICK_PROCEDURE);
			break;
		}
	}
	
	// The callback to get data from the asynchronous calls
	private ISessionCallback mCallback = new ISessionCallback.Stub() {
			
			@Override
			public void onValueChanged(int arg0, String arg1, String arg2)
					throws RemoteException {
				Log.d(TAG,  ".mCallback.onValueChanged( " +arg0 +", "+arg1+ 
						", " + arg2+ " )");
				Bundle data = new Bundle();
				data.putString(Intents.EXTRA_INSTANCE, arg1);
				data.putString(Intents.EXTRA_OBSERVER, arg2);
				mHandler.sendMessage(mHandler.obtainMessage(arg0, data));
			}

		};
		
		// This is the handler which responds to the SessionService
		// It expects a Message with msg.what = FAILURE or SUCCESS
		// and a Bundle with the new session key if successful.
	    private Handler mHandler = new Handler() {
	    	
	        @Override public void handleMessage(Message msg) {
	    		int state = msg.what;
	    		Log.i(TAG, "handleMessage(): " + msg.what);
	    		cancelProgressDialogFragment();
	        	switch(state){
	        	case SessionService.FAILURE:
	        		loginsRemaining--;
	        		// TODO use a string resource
	        		Toast.makeText(getApplicationContext(), 
	        				"Username and password incorrect! Logins remaining: " 
	        						+loginsRemaining, 
	        				Toast.LENGTH_SHORT).show();
	        		showAuthenticationDialog();
	        		break;
	        	case SessionService.SUCCESS:
	        		loginsRemaining = 0;
	        		Bundle b = msg.getData(); //(Bundle)msg.obj;
	        		Uri uri = Uris.withAppendedUuid(Observers.CONTENT_URI,
	        				b.getString(Intents.EXTRA_OBSERVER));
	        		Log.i(TAG, uri.toString());
	        		onUpdateAppState(b);
	        		Intent data = new Intent();
	        		data.setData(uri);
	        		data.putExtras(b);
	        		onSaveAppState(data);
	        		mIntent = new Intent(Intent.ACTION_MAIN); 
	        		
	        		break;
	        	default:
	        		Log.e(TAG, "Should never get here");
	        	}

	        	// Finish if remaining logins => 0
	        	if(loginsRemaining == 0){
	        		finish();
	        	}
	        }
	    
	    };	

}
