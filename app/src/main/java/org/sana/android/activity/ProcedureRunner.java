
package org.sana.android.activity;

import org.sana.R;
import org.sana.android.Constants;
import org.sana.android.app.Locales;
import org.sana.android.content.DispatchResponseReceiver;
import org.sana.android.fragment.ProcedureRunnerFragment;
import org.sana.android.fragment.BaseRunnerFragment.ProcedureListener;
import org.sana.android.service.impl.InstrumentationService;
import org.sana.android.util.Strings;
import org.sana.net.Response;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;

/** Activity which loops through the available steps within a procedure including
 * handling any branching logic. Individual procedure steps are rendered to a
 * view which is wrapped in a container which presents buttons for paging.
 * Additional logic is built into this class to handle launching and capturing
 * returned values from Activities used to capture data along with initiating
 * procedure saving, reloading, and uploading.
 * 
 * @author Sana Development Team */
public class ProcedureRunner extends BaseRunner
{
    public static final String TAG = ProcedureRunner.class.getSimpleName();

    // Fragment
    private ProcedureRunnerFragment mProcedureRunnerFragment = null;

    /** {@inheritDoc} */
    @Override
    public void onCreate(Bundle instance) {
        super.onCreate(instance);
    	Locales.updateLocale(this, getString(R.string.force_locale));
        setContentView(R.layout.procedure_runner_activity);
    }

    /** {@inheritDoc} */
    @Override
    public void onAttachFragment(Fragment fragment) {
    	Locales.updateLocale(this, getString(R.string.force_locale));
        super.onAttachFragment(fragment);
        if (fragment.getClass() == ProcedureRunnerFragment.class) {
            mProcedureRunnerFragment = (ProcedureRunnerFragment) fragment;
            mProcedureRunnerFragment.setProcedureListener(this);
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState){
    	super.onSaveInstanceState(outState);
    	if(mProcedureRunnerFragment != null)
    		mProcedureRunnerFragment.onSaveInstanceState(outState);
    }
    
    @Override
    public void onRestoreInstanceState(Bundle inState){
    	super.onRestoreInstanceState(inState);
    	if(mProcedureRunnerFragment != null)
    		mProcedureRunnerFragment.onRestoreInstanceState(inState);
    }
    
    
    @Override
    public void onConfigurationChanged(Configuration newConfig){
    	if(mProcedureRunnerFragment != null)
    		mProcedureRunnerFragment.storeCurrentProcedure(false);
    	super.onConfigurationChanged(newConfig);
    }
    
    @Override
    public void onDestroy(){
    	if(isFinishing()){
    		stopService(new Intent(getBaseContext(), InstrumentationService.class));
    	}
    	super.onDestroy();
    }

    /** {@inheritDoc} */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Locales.updateLocale(this, getString(R.string.force_locale));
        menu.add(0, OPTION_SAVE_EXIT, 0, getString(R.string.menu_save_exit));
        menu.add(0, OPTION_DISCARD_EXIT, 1, getString(R.string.menu_discard_exit));
        menu.add(0, OPTION_VIEW_PAGES, 2, getString(R.string.menu_view_pages));
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                Constants.PREFERENCE_EDUCATION_RESOURCE, false))
            menu.add(0, OPTION_HELP, 3, "Help");
        return true;
    }

    @Override
    public void onProcedureComplete(Intent data){
        Log.d(TAG, "onProcedureComplete(): " + data);
        startService(data);
        setResult(RESULT_OK,data);
        finish();
    }
    
    @Override
    public void onProcedureCancelled(String message){
        Log.d(TAG, "onProcedureComplete(): " + message);
		setResult(RESULT_CANCELED,null);
		finish();
    }

    /**
     * Handles a local broadcast response success. Subclasses should override
     * this method to change behavior.
     *
     * @param intent The response message
     */
    protected void handleBroadcastResultSuccess(Intent intent){
        Log.i(TAG, "handleBroadcastResultSuccess(Intent)");
        String text = intent.hasExtra(Response.MESSAGE)?
                intent.getStringExtra(Response.MESSAGE):
                Strings.getLocalizedString(this,R.string.general_ok);
        createUploadResultSuccessDialog(text).show();
    }

    /**
     * Handles a local broadcast response failure. Subclasses should override
     * this method to change behavior.
     *
     * @param intent The response message
     */
    protected void handleBroadcastResultFailure(Intent intent){
        Log.i(TAG, "handleBroadcastResultFailure(Intent)");
        String text = intent.hasExtra(Response.MESSAGE)?
                intent.getStringExtra(Response.MESSAGE):
                Strings.getLocalizedString(this,R.string.general_error);
        createUploadResultFailDialog(text).show();
    }
}
