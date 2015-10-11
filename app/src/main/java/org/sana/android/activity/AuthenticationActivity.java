
package org.sana.android.activity;

import org.sana.R;
import org.sana.android.activity.settings.BasicSettings;
import org.sana.android.app.Locales;
import org.sana.android.content.Intents;
import org.sana.android.content.Uris;
import org.sana.android.provider.Observers;
import org.sana.android.service.ISessionCallback;
import org.sana.android.service.ISessionService;
import org.sana.android.service.impl.SessionService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

/**
 * Activity that handles user authentication. When finishing with RESULT_OK,
 * will return a valid session key String or
 * {@link org.sana.android.service.impl.SessionService#INVALID INVALID}
 * as an Intent extra String keyed to {@link BaseActivity#SESSION_KEY}.
 *
 * @author Sana Dev Team
 */
public class AuthenticationActivity extends BaseActivity {

    public static final String TAG = AuthenticationActivity.class.getSimpleName();

    // Session Related
    // Number of allowed authentication attempts
    private int loginsRemaining = 0;
    private int loginAttempts = 0;
    private final int maxLogins = 3;
    private boolean loginSuccessful = false;
    
    protected boolean mBound = false;
    protected ISessionService mService = null;

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
            Message response = Message.obtain(mHandler);
            response.setData(data);
            response.what = arg0;
            response.sendToTarget();
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

    // This is the handler which responds to the SessionService
    // It expects a Message with msg.what = FAILURE or SUCCESS
    // and a Bundle with the new session key if successful.
    private Handler mHandler = new Handler() {

        @Override public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage(): " 
                        + ((msg != null)?((msg.what == 1)? "success":"failure"):"null"));
            int state = msg.what;
            Intent data = null;
            cancelProgressDialogFragment();
            switch(state){
            case SessionService.FAILURE:
                loginsRemaining--;
                enableInput();
                // TODO use a string resource
                Toast.makeText(AuthenticationActivity.this,
                        String.format("%s. %s: %d",
                                getString(R.string.msg_login_failed),
                                getString(R.string.authentication_logins_remaining),
                                loginsRemaining),
                        Toast.LENGTH_SHORT).show();
                break;
            case SessionService.SUCCESS:
                loginsRemaining = 0;
                loginSuccessful = true;
                Bundle b = msg.getData(); //(Bundle)msg.obj;
                Log.i(TAG,"...handleMessage(...) SUCCESS: ");
                for(String key:b.keySet())
                    Log.d(TAG+".handleMessage(...)", "...."+key +":"+ String.valueOf(b.get(key)));
                String uuid = b.getString(Intents.EXTRA_OBSERVER);
                Log.i(TAG,"...handleMessage(...) SUCCESS: observer=" + uuid);
                Uri uri = Uris.withAppendedUuid(Observers.CONTENT_URI, uuid);
                Log.i(TAG, uri.toString());
                b.remove(Intents.EXTRA_OBSERVER);
                b.putParcelable(Intents.EXTRA_OBSERVER, uri);
                onUpdateAppState(b);
                data = new Intent();
                data.setData(uri);
                data.putExtras(b);
                onSaveAppState(data);
                setResult(RESULT_OK,data);
                break;
            default:
                Log.e(TAG, "Should never get here");
            }

            // Finish if remaining logins => 0
            Log.i(TAG, "handleMessage(): logins remaining: " + loginsRemaining);
            if(loginsRemaining == 0){
                if(!loginSuccessful){
                        setResult(RESULT_CANCELED);
                }
                finish();
            } 
        }

    };

    // Views
    EditText mInputUsername;

    EditText mInputPassword;

    Button mBtnLogin;
    Button mBtnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewLocalized(R.layout.activity_authentication);
        showBuildString(R.id.text_local_version);
        mInputUsername = (EditText) findViewById(R.id.input_username);
        mInputPassword = (EditText) findViewById(R.id.input_password);
        loginsRemaining = getResources().getInteger(R.integer.max_login_attempts);
        loadCredentials();
    }

    /* Loads the debug credentials from res/values/debug.xml if debug_credentials = true */
    private final void loadCredentials(){
        if(getResources().getBoolean(R.bool.debug_credentials)){
            mInputUsername.setText(getString(R.string.debug_user));
            mInputPassword.setText(getString(R.string.debug_password));
        }
    }

    protected void showBuildString(int id){
        TextView tv = (TextView) findViewById(id);
        // TODO Fix the getBuildString to read from the manifest correctly
        tv.setText(getBuildString());
        //tv.setText(getString(R.string.display_version));
    }

    private void disableInput(){
        mInputUsername.setEnabled(false);
        mInputPassword.setEnabled(false);
        ((Button) findViewById(R.id.btn_login)).setEnabled(false);
        ((Button) findViewById(R.id.btn_configure)).setEnabled(false);
    }

    private void enableInput(){
        mInputUsername.setEnabled(true);
        mInputPassword.setEnabled(true);
        ((Button) findViewById(R.id.btn_login)).setEnabled(true);
        ((Button) findViewById(R.id.btn_configure)).setEnabled(true);
    }

    // Attempts a log-in
    private void logIn() {
        loginAttempts++;
        // disable input until we get a result back from the service
        disableInput();
    
        // get the data
        String username = mInputUsername.getText().toString();
        String password = mInputPassword.getText().toString();

        if(mBound &&
            validUsernameAndPasswordFormat(username, password)){
                Log.d(TAG, "login(): user name and password format valid");
                Locales.updateLocale(this, getString(R.string.force_locale));
                showProgressDialogFragment(getString(R.string.dialog_logging_in));
                try {
                    mService.create(getInstanceKey(), username,password);// register the callback to the username
                    //cache the credentials
                    setCurrentCredentials(username, password);
                } catch (RemoteException e) {
                    Log.e(TAG, "login()" + e.toString());
                    e.printStackTrace();
                }
        }
    }

    /**
     * Validates that the credentials are valid.
     *
     * @param username The username credential.
     * @param password The password credential.
     * @return
     */
    protected boolean validUsernameAndPasswordFormat(String username,
            String password){
        return !(TextUtils.isEmpty(username) || TextUtils.isEmpty(password));
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onStart()
     */
    @Override
    protected void onStart(){
        Log.i(TAG, "onStart()");
        super.onStart();
        bindSessionService();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop()");
        super.onStop();
        unbindSessionService();
    }

    // handles initiating the session service binding
    private void bindSessionService(){
        Log.i(TAG, "bindSessionService()");
        if(!mBound){
            bindService(new Intent(SessionService.ACTION_START), mConnection, Context.BIND_AUTO_CREATE);
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
            unbindService(mConnection);
            mBound = false;
        }
    }

    // Registers the callback using the instance key
    private void registerCallback(){
        try {
            mService.registerCallback(mCallback, getInstanceKey());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
    public void submit(View v){
        switch(v.getId()){
            case R.id.btn_login:
                logIn();
                break;
            case R.id.btn_exit:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.btn_configure:
                Intent configure = new Intent(this, BasicSettings.class);
                startActivity(configure);
                break;
        default:
        }
    }
}
