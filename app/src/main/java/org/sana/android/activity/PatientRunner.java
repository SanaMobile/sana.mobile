
package org.sana.android.activity;

import org.sana.R;
import org.sana.android.fragment.PatientRunnerFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/** Activity for creating a new patient. Each question is wrapped in a container
 * which presents buttons for paging.
 * 
 * @author Sana Development Team */
public class PatientRunner extends BaseRunner {

    public static final String TAG = PatientRunner.class.getSimpleName();
    
    private PatientRunnerFragment mFragmentPatientRunner;

    /** {@inheritDoc} */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_runner_activity);
    }

    /** {@inheritDoc} */
    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment.getClass() == PatientRunnerFragment.class) {
            mFragmentPatientRunner = (PatientRunnerFragment) fragment;
        }
    }
}
