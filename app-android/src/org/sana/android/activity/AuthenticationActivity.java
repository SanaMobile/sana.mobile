
package org.sana.android.activity;

import org.sana.R;
import org.sana.android.content.core.ObserverParcel;
import org.sana.android.net.HttpTask;
import org.sana.android.service.IRemoteService;
import org.sana.android.service.ISessionCallback;
import org.sana.android.service.ISessionService;
import org.sana.android.service.IStringCallback;
import org.sana.android.service.impl.SessionService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Activity that handles user authentication. When finishing with RESULT_OK,
 * will return a valid session key String or {@link org.sana.android.service.impl.SessionService#INVALID INVALID}
 * as an Intent extra String keyed to {@link BaseActivity#SESSION_KEY}. 
 * 
 * @author Sana Dev Team
 */
public class AuthenticationActivity extends BaseActivity {

	public static final String TAG = AuthenticationActivity.class.getSimpleName();

	// Session Related
	// Number of allowed authentication attempts
	private int loginsRemaining = 0;
	protected boolean mBound = false;
	protected ISessionService mService = null;
	
	// The cacllback to get data from the asynchronous calls
	private ISessionCallback mCallback = new ISessionCallback.Stub() {
		
		@Override
		public void onValueChanged(int arg0, String arg1, String arg2)
				throws RemoteException {
			Log.d(TAG,  ".mCallback.onValueChanged( " +arg0 +", "+arg1+ 
					", " + arg2+ " )");
			Bundle data = new Bundle();
			data.putString(INSTANCE_KEY, arg1);
			data.putString(SESSION_KEY, arg2);
			mHandler.sendMessage(mHandler.obtainMessage(arg0, data));
		}

	};
	
	// connector to the session service
	protected ServiceConnection mConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
	    	Log.i(TAG, "ServiceConnection.onServiceConnected()");
			mService = ISessionService.Stub.asInterface(service);
			mBound = true;
			registerCallback();
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
	    	Log.i(TAG, "ServiceConnection.onServiceDisconnected()");
			mService = null;
			mBound = false;
		}
	};
		
    private Handler mHandler = new Handler() {
    	
        @Override public void handleMessage(Message msg) {
    		int state = msg.what;
    		Log.i(TAG, "handleMessage(): " + msg.what);
        	switch(state){
        	case SessionService.FAILURE:
        		loginsRemaining--;
        		enableInput();
        		// TODO use a string resource
        		Toast.makeText(getApplicationContext(), 
        				"Username and password incorrect! Logins remaining: " 
        						+loginsRemaining, 
        				Toast.LENGTH_SHORT).show();
        		break;
        	case SessionService.SUCCESS:
        		loginsRemaining = 0;
        		Bundle b = msg.getData();
        		Intent data = new Intent();
        		data.putExtra(INSTANCE_KEY, b.getString(INSTANCE_KEY));
        		data.putExtra(SESSION_KEY, b.getString(SESSION_KEY));
        		setResult(RESULT_OK,data);
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
	
	// Views
    EditText mInputUsername;

    EditText mInputPassword;

    Button mBtnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        
        mInputUsername = (EditText) findViewById(R.id.input_username);
        mInputPassword = (EditText) findViewById(R.id.input_password);
        mBtnLogin = (Button) findViewById(R.id.btn_login);
        mBtnLogin.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                logIn();
            }
        });
        loginsRemaining = getResources().getInteger(R.integer.max_login_attempts);
    }
    
    void disableInput(){
    	mInputUsername.setEnabled(false);
    	mInputPassword.setEnabled(false);
    	mBtnLogin.setEnabled(false);
    }
    
    void enableInput(){
    	mInputUsername.setEnabled(true);
    	mInputPassword.setEnabled(true);
    	mBtnLogin.setEnabled(true);
    }
    
    // Attempts a log-in
    private void logIn() {
        // disable input until we get a result back from the service
    	disableInput();
    	
    	// get the data
    	String username = mInputUsername.getText().toString();
    	String password = mInputPassword.getText().toString();
    	
    	if(isBound() && 
   			validUsernameAndPasswordFormat(username, password)){
    			Log.d(TAG, "login(): user name and password format valid");
    			try {
    				mService.create(getInstanceKey(), username,password);// register the callback to the username

    			} catch (RemoteException e) {
    				Log.e(TAG, "login()" + e.toString());
    				e.printStackTrace();
    			}
    	}
    }
    
    protected boolean validUsernameAndPasswordFormat(String username,
    		String password){
    	return !(TextUtils.isEmpty(username) || TextUtils.isEmpty(password));
    }
    
    protected boolean isBound(){
    	return mBound;
    }
    
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onStart()
     */
	@Override
    protected void onStart(){
    	super.onStart();
    	bindSessionService();
    }
    
    /*
     * (non-Javadoc)
     * @see com.actionbarsherlock.app.SherlockActivity#onStop()
     */
    @Override
    protected void onStop() {
        super.onStop();
        unbindSessionService();
    }
    
	// handles initiating the session service binding
    private void bindSessionService(){
    	Log.i(TAG, "bindSessionService()");
    	if(!mBound){
        	Log.d(TAG, "mBound = " + mBound);
    		if(mService == null){
    	    	Log.d(TAG, "mService binder is null"); 
    			bindService(new Intent(SessionService.ACTION_START), mConnection, Context.BIND_AUTO_CREATE);
    			//bindService(new Intent(SessionService.BIND_REMOTE), mCallbackConnection, Context.BIND_AUTO_CREATE);
    		}
    	}
    }
    
    // handles disconnecting from the session service bindings
    private void unbindSessionService(){
        // Unbind from the service
        if (mBound){
        	if(mService != null)
        		try{
        			mService.unregisterCallback(mCallback);
        		} catch(Exception e){
        			Log.e(TAG, "Failure unbinding Sessionservice",e);
        		}
        	// Detach
    		//unbindService(mCallbackConnection);
    		unbindService(mConnection);
            mBound = false;
        }
    }

    void registerCallback(){
    	try {
			mService.registerCallback(mCallback, getInstanceKey());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }
}
