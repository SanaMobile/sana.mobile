package org.sana.android.activity;

import org.sana.android.service.impl.SessionService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Main Activity which handles user authentication and initializes services that
 * Sana uses.
 * @author Sana Dev Team
 */
public class MainActivity extends BaseActivity {

	public static final String TAG = MainActivity.class.getSimpleName();
	public static final String CLOSE = "org.sana.android.intent.CLOSE";
	
	/**
	 * States which the Activity can be in corresponding to the Acitivities of
	 * the app.
	 * 
	 * @author Sana Development
	 *
	 */
	public static enum State{
		INITIAL,
		LOGIN,
		LOGOUT,
		HOME,
		SELECT_SUBJECT,
		VIEW_SUBJECT,
		SELECT_PROCEDURE,
		SELECT_ENCOUNTER,
		VIEW_ENCOUNTER,
		SELECT_NOTIFICATION,
		VIEW_NOTIFICATION,
		SETTINGS_USER,
		SETTINGS_ADMIN,
		SETTINGS_NETWORK,
		COMPLETE
	}
	
	//TODO Refactor this out
	/**
	 * Interface for walking through the application.
	 * 
	 * @author Sana Development
	 *
	 */
	public static interface ActivityRunner{
		
		/**
		 * Given a current state String, produces the next state and takes some 
		 * action.
		 * @param context the Context which the action will be taken in.
		 * @param state the current state.
		 */
		public void next(Context context, Intent state);
		
	}
	
	/**
	 * Maintains state for the application and starts activities based on the
	 * supplied data
	 * 
	 * current state		next state		launches				returns	
	 * INITIAL, LOGOUT		LOGIN			AuthenticationAcitivity String extra, sessionKey
	 * LOGIN				HOME			Sana					bundle
	 * COMPLETE				INITIAL			finishes				null
	 * default				INITIAL			finishes				null
	 *
	 * After setting the next state, the ActivityRunner will start the Activity
	 * or finish.  
	 */
	private final ActivityRunner runner = new ActivityRunner(){
		State state = State.INITIAL;
		
		public void next(Context context, Intent state) {
	        Intent intent = new Intent(CLOSE);
			switch(this.state){
			case INITIAL:
	    	// after we leave HOME go back to login? Means we need an exit
			case HOME:
    		case LOGOUT:
    			setSessionKey(null);
    			this.state = State.LOGIN;
    			intent = new Intent();
    	        intent.setClass(context, AuthenticationActivity.class);
    	        break;
    	    // Successful login we launch Sana
    		case LOGIN:
    			String key = state.getStringExtra(SESSION_KEY);
    			setSessionKey(key);
    			this.state = State.HOME;
    			intent = new Intent();
    	        intent.setClass(context, Sana.class);
    	        break;
    		case COMPLETE:
    		default:
    			this.state = State.INITIAL;
    			setSessionKey(null);
    			intent = new Intent(CLOSE);
    			break;
			}
			if(intent.getAction() != null && intent.getAction().equals(CLOSE)){
		        if(context instanceof Activity){
					((Activity)context).finish();
		        }
			} else if(context instanceof Activity){
					((Activity)context).startActivityForResult(intent, this.state.ordinal());
			} else { 
	        	context.startActivity(intent);
			}
		}

	};
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	switch(resultCode){
    	case RESULT_OK:
    		runner.next(this, data);
    	case RESULT_CANCELED:
    		finish();
    	}
    }
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    protected void onStart(){
    	super.onStart();
    	Log.d(TAG, "onStart()");
        // We want the session service to be running if Main has been started 
    	// and until after it stops.
    	startService(new Intent(SessionService.ACTION_START));
        runner.next(this, null);
    }
    
    @Override
    protected void onStop(){
    	super.onStop();
    	Log.d(TAG, "onStop()");
    	// kill the session service if there is nothing else bound
    	stopService(new Intent(SessionService.ACTION_START));
    }
	
    /**
     * Launches the home activity
     */
    void showHomeActivity() {
        Intent intent = new Intent();
        intent.setClass(this, Sana.class);
        startActivityForResult(intent, State.HOME.ordinal());
    }
    
    /**
     * Launches the authentication activity
     */
    void showAuthenticationActivity() {
        Intent intent = new Intent();
        intent.setClass(this, AuthenticationActivity.class);
        startActivityForResult(intent, State.LOGIN.ordinal());
    }

    
}
