
package org.sana.android.activity;

import org.sana.R;
import org.sana.android.app.Locales;
import org.sana.android.fragment.ProcedureRunnerFragment;
import org.sana.android.service.impl.InstrumentationService;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;

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
    
    
}
