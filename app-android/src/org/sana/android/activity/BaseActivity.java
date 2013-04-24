package org.sana.android.activity;

import java.util.UUID;

import android.content.Intent;

import com.actionbarsherlock.app.SherlockActivity;

/**
 * Base class that contains basic functionalities and behaviors that all
 * activities should do. 
 * @author Sana Dev Team
 */
public abstract class BaseActivity extends SherlockActivity {
    
	public static final String TAG = BaseActivity.class.getSimpleName();

	/**
     * Finishes the calling activity and launches the activity contained in
     * <code>intent</code>
     * @param intent
     */
    void switchActivity(Intent intent) {
        startActivity(intent);
        finish();
    }
    
	// Session related 
    public static final String INSTANCE_KEY = "instanceKey";
    public static final String SESSION_KEY = "sessionKey";
    
    // instanceKey initialized to some random value for the instance;
	private final String mInstanceKey = UUID.randomUUID().toString();
	// Authenticated session key default is null;
	private String mSessionKey = null;
	
	
	/**
	 * Returns the value of the instance key which is created when the object is
	 * instantiated.
	 * @return
	 */
    protected String getInstanceKey(){
    	return mInstanceKey;
    }
	
	/**
	 * Returns the value of the session key. Warning: any key returned must be 
	 * authenticated with the session service.
	 * @return
	 */
    protected String getSessionKey(){
    	return mSessionKey;
    }
    
    /**
     * Sets the value of the session key. Warning: this method does not make
     * any atempt to validate whether the session is authenticated.
     * @param sessionKey
     */
    protected void setSessionKey(String sessionKey){
    	mSessionKey = sessionKey;
    }
    
    
}
