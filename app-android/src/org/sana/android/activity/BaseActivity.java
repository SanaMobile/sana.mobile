package org.sana.android.activity;

import java.util.UUID;

import org.sana.android.app.State.Keys;
import org.sana.android.util.DeviceUtil;
import org.sana.android.util.UriUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
/**
 * Base class that contains basic functionalities and behaviors that all
 * activities should do. 
 * @author Sana Dev Team
 */
public abstract class BaseActivity extends SherlockActivity {
    
	public static final String TAG = BaseActivity.class.getSimpleName();

    // Dialog for prompting the user that a long operation is being performed.
    ProgressDialog mWaitDialog;
    
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
	private String mInstanceKey = UUID.randomUUID().toString();
	// Authenticated session key default is null;
	private String mSessionKey = null;

    protected Uri mSubject = Uri.EMPTY;
    protected Uri mEncounter = Uri.EMPTY;
    protected Uri mProcedure = Uri.EMPTY;
    protected Uri mObserver = Uri.EMPTY;
	
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
     
    /**
     * Writes the state fields for this component to a bundle.
     * Currently this writes the following from the Bundle
     * <ul>
     * 	<li>instance key</li>
     * 	<li>session key</li>
     * 	<li>current encounter</li>
     * 	<li>current subject</li>
     * 	<li>current observer</li>
     * 	<li>current procedure</li>
     * </ul>
     * @param outState
     */
    protected void onSaveAppState(Bundle outState){
    	outState.putString(Keys.INSTANCE_KEY, mInstanceKey);
    	outState.putString(Keys.SESSION_KEY, mSessionKey);
    	outState.putParcelable(Keys.ENCOUNTER, mEncounter);
    	outState.putParcelable(Keys.SUBJECT, mSubject);
    	outState.putParcelable(Keys.PROCEDURE, mProcedure);
    	outState.putParcelable(Keys.OBSERVER, mObserver);
    }
    
    /**
     * Writes the state fields for this component to an Intent as Extras.
     * Currently this writes the following from the Intent.
     * <ul>
     * 	<li>instance key</li>
     * 	<li>session key</li>
     * 	<li>current encounter</li>
     * 	<li>current subject</li>
     * 	<li>current observer</li>
     * 	<li>current procedure</li>
     * </ul>
     * @param outState
     */
    protected void onSaveAppState(Intent outState){
    	outState.putExtra(Keys.INSTANCE_KEY, mInstanceKey);
    	outState.putExtra(Keys.SESSION_KEY, mSessionKey);
    	outState.putExtra(Keys.ENCOUNTER, mEncounter);
    	outState.putExtra(Keys.SUBJECT, mSubject);
    	outState.putExtra(Keys.PROCEDURE, mProcedure);
    	outState.putExtra(Keys.OBSERVER, mObserver);
    }
    
    /**
     * Sets the state fields for this component from an Intent.
     * Currently this attempts to read the following extras from the
     * Intent.
     * <ul>
     * 	<li>instance key</li>
     * 	<li>session key</li>
     * 	<li>current encounter</li>
     * 	<li>current subject</li>
     * 	<li>current observer</li>
     * 	<li>current procedure</li>
     * </ul>
     * 
     * @param inState
     */
    protected void onUpdateAppState(Intent inState){
    	String k = inState.getStringExtra(Keys.INSTANCE_KEY);
    	if(k != null)
    		mInstanceKey = new String(k);

		k = inState.getStringExtra(Keys.SESSION_KEY);
    	if(k!=null)
    		mSessionKey = new String(k);
    	
    	Uri temp = inState.getParcelableExtra(Keys.ENCOUNTER);
    	if(temp != null)
    		mEncounter = UriUtil.copyInstance(temp);
    	
    	temp = inState.getParcelableExtra(Keys.SUBJECT);
    	if(temp != null)
    		mSubject = UriUtil.copyInstance(temp);
    	
    	temp = inState.getParcelableExtra(Keys.PROCEDURE);
    	if(temp != null)
    		mProcedure = UriUtil.copyInstance(temp);
    	
    	temp = inState.getParcelableExtra(Keys.OBSERVER);
    	if(temp != null)
    		mObserver = UriUtil.copyInstance(temp);
    }
    
    /**
     * Sets the state fields for this component from a bundle.
     * Currently this attempts to read the following from the Bundle
     * <ul>
     * 	<li>instance key</li>
     * 	<li>session key</li>
     * 	<li>current encounter</li>
     * 	<li>current subject</li>
     * 	<li>current observer</li>
     * 	<li>current procedure</li>
     * </ul>
     * 
     * @param inState
     */
    protected void onUpdateAppState(Bundle inState){
    	String k = inState.getString(Keys.INSTANCE_KEY);
    	if(k!=null)
    		mInstanceKey = new String(k);
    	k = inState.getString(Keys.SESSION_KEY);
    	if(k!=null)
    		mSessionKey = new String(k);
    	
    	Uri temp = inState.getParcelable(Keys.ENCOUNTER);
    	if(temp != null)
    		mEncounter = UriUtil.copyInstance(temp);
    	
    	temp = inState.getParcelable(Keys.SUBJECT);
    	if(temp != null)
    		mSubject = UriUtil.copyInstance(temp);
    	
    	temp = inState.getParcelable(Keys.PROCEDURE);
    	if(temp != null)
    		mProcedure = UriUtil.copyInstance(temp);
    	
    	temp = inState.getParcelable(Keys.OBSERVER);
    	if(temp != null)
    		mObserver = UriUtil.copyInstance(temp);
    }

    /**
     * Writes the app state fields as extras to an Intent. Activities
     * will still need to call setResult(RESULT_OK, data) as well as
     * write any other data they wish to the data Intent.
     * 
     * @param data
     */
    protected void setResultAppData(Intent data){
    	onSaveAppState(data);
    }
    
    /*
     * (non-Javadoc)
     * @see com.actionbarsherlock.app.SherlockActivity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
    	onSaveAppState(savedInstanceState);
    }
    
    /*
     * (non-Javadoc)
     * @see com.actionbarsherlock.app.SherlockActivity#onRestoreInstanceState(android.os.Bundle)
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
    	onUpdateAppState(savedInstanceState);
    }
    
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
    	super.onCreate(savedInstanceState);
    	
    	DeviceUtil.enableOnlyDeviceSpecificActivities(this);
    	
    	Intent intent = getIntent();
    	// get the fields from the launch intent extras
    	if(intent != null)
    		onUpdateAppState(intent);
    }

    /**
     * Displays a progress dialog fragment with the provided message.
     * @param message
     */
    void showProgressDialogFragment(String message) {
        
        if (mWaitDialog != null && mWaitDialog.isShowing()) {
            hideProgressDialogFragment();
        }
        
        mWaitDialog = new ProgressDialog(this);
        mWaitDialog.setMessage(message);
        mWaitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mWaitDialog.show();
    }
    
    /**
     * Hides the progress dialog if it is shown.
     */
    void hideProgressDialogFragment() {
        
        if (mWaitDialog == null) {
            return;
        }
        
        mWaitDialog.hide();
    }
}
