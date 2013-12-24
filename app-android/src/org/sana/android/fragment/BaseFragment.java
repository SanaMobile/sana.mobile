package org.sana.android.fragment;

import android.app.ProgressDialog;
import android.support.v4.app.Fragment;

/**
 * Base class for fragments. It contains some basic functionalities that most,
 * if not all, fragments should contain.
 * 
 * @author Sana Development Team
 */
public class BaseFragment extends Fragment {

    // Dialog for prompting the user that a long operation is being performed.
    ProgressDialog mWaitDialog;
    
    /**
     * Displays a progress dialog fragment with the provided message.
     * @param message
     */
    void showProgressDialogFragment(String message) {
        
        if (mWaitDialog != null && mWaitDialog.isShowing()) {
            hideProgressDialogFragment();
        }
        
        mWaitDialog = new ProgressDialog(getActivity());
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
        if(getActivity() != null)
        	if(getActivity().isFinishing())
        		mWaitDialog.cancel();
        	else
        		mWaitDialog.cancel();
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	hideProgressDialogFragment();
    }
    
}
