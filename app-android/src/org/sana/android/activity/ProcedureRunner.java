
package org.sana.android.activity;

import org.sana.R;
import org.sana.android.fragment.ProcedureRunnerFragment;

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
    ProcedureRunnerFragment mProcedureRunnerFragment;

    /** {@inheritDoc} */
    @Override
    public void onCreate(Bundle instance) {
        super.onCreate(instance);
        setContentView(R.layout.procedure_runner_activity);
    }

    /** {@inheritDoc} */
    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment.getClass() == ProcedureRunnerFragment.class) {
            mProcedureRunnerFragment = (ProcedureRunnerFragment) fragment;
        }
    }

}
