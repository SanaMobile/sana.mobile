package org.sana.android.activity.tablet;

import org.sana.R;
import org.sana.android.activity.BaseRunner;
import org.sana.android.fragment.ProcedureRunnerFragment;
import org.sana.android.fragment.SubjectInfoFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Tablet view of org.sana.android.activity.ProcedureRunner. This Activity
 * also displays information about the patient.
 * @author Chris Arriola
 */
public class ProcedureTabletRunner extends BaseRunner {

    public static final String TAG = ProcedureTabletRunner.class.getSimpleName();
    
    // Fragments
    ProcedureRunnerFragment mProcedureRunnerFragment;
    SubjectInfoFragment mPatientInfoFragment;
    
    /** {@inheritDoc} */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.procedure_runner_tablet_activity);
        Log.i(TAG, "onCreate() triggered.");
        
        if (savedInstanceState == null) {
            mPatientInfoFragment = SubjectInfoFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                .add(R.id.patient_info_container, mPatientInfoFragment)
                .commit();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment.getClass() == ProcedureRunnerFragment.class) {
            mProcedureRunnerFragment = (ProcedureRunnerFragment) fragment;
        } else if (fragment.getClass() == SubjectInfoFragment.class) {
            mPatientInfoFragment = (SubjectInfoFragment) fragment;
        }
    }
}
