package org.sana.android.activity;

import org.sana.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

/**
 * Base class activity for containing a BaseRunnerFragment.
 *
 * Additional logic is built into this class to handle launching and capturing 
 * returned values from Activities used to capture data along with initiating 
 * procedure saving, reloading, and uploading.
 * 
 * @author Sana Development Team
 */
public abstract class BaseRunner extends FragmentActivity {

    // Dialog IDs
    public static final int DIALOG_ALREADY_UPLOADED = 7;
    public static final int DIALOG_LOOKUP_PROGRESS = 1;
    public static final int DIALOG_LOAD_PROGRESS = 2;

    // Options
    public static final int OPTION_SAVE_EXIT = 0;
    public static final int OPTION_DISCARD_EXIT = 1;
    public static final int OPTION_VIEW_PAGES = 2;
    public static final int OPTION_HELP = 3; 
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
    }

    /** {@inheritDoc} */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_ALREADY_UPLOADED:
                return new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(
                                R.string.general_alert))
                        .setMessage(getResources().getString(
                                R.string.dialog_already_uploaded))
                        .setNeutralButton(getResources().getString(R.string.general_ok),
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // close without saving
                                        setResult(RESULT_OK, null);
                                    }
                                })
                        .setCancelable(false)
                        .create();
            default:
                break;
        }
        return null;
    }


}
