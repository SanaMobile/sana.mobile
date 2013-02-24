
package org.sana.android.activity;

import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

import org.sana.R;
import org.sana.android.Constants;
import org.sana.android.db.BinaryDAO;
import org.sana.android.db.EncounterDAO;
import org.sana.android.db.SanaDB.BinarySQLFormat;
import org.sana.android.db.SanaDB.EventSQLFormat.EventType;
import org.sana.android.fragment.BaseRunnerFragment;
import org.sana.android.media.EducationResource.Audience;
import org.sana.android.procedure.PictureElement;
import org.sana.android.procedure.Procedure;
import org.sana.android.service.PluginService;
import org.sana.android.task.ImageProcessingTask;
import org.sana.android.task.ImageProcessingTaskRequest;
import org.sana.android.util.SanaUtil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/** Base class activity for containing a BaseRunnerFragment. Additional logic is
 * built into this class to handle launching and capturing returned values from
 * Activities used to capture data along with initiating procedure saving,
 * reloading, and uploading.
 * 
 * @author Sana Development Team */
public abstract class BaseRunner extends SherlockFragmentActivity {

    public static final String TAG = BaseRunner.class.getSimpleName();
    public static final String INTENT_KEY_STRING = "intentKey";
    public static final String INTENT_EXTRAS_KEY = "extras";
    public static final String PLUGIN_INTENT_KEY = "pluginIntent";

    // Dialog IDs
    public static final int DIALOG_ALREADY_UPLOADED = 7;
    public static final int DIALOG_LOOKUP_PROGRESS = 1;
    public static final int DIALOG_LOAD_PROGRESS = 2;

    // Options
    public static final int OPTION_SAVE_EXIT = 0;
    public static final int OPTION_DISCARD_EXIT = 1;
    public static final int OPTION_VIEW_PAGES = 2;
    public static final int OPTION_HELP = 3;

    // Intent
    public static final int CAMERA_INTENT_REQUEST_CODE = 1;
    public static final int BARCODE_INTENT_REQUEST_CODE = 2;
    public static final int INFO_INTENT_REQUEST_CODE = 7;
    public static final int PLUGIN_INTENT_REQUEST_CODE = 4;
    public static final int IMPLICIT_PLUGIN_INTENT_REQUEST_CODE = 8;

    // State instance fields
    private static String[] params;
    private Procedure p = null;
    private Uri thisSavedProcedure;
    private Intent mEncounterState = new Intent();
    private boolean wasOnDonePage = false;

    private BaseRunnerFragment mRunnerFragment;

    /** {@inheritDoc} */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
    }

    /** {@inheritDoc} */
    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof BaseRunnerFragment) {
            mRunnerFragment = (BaseRunnerFragment) fragment;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, OPTION_SAVE_EXIT, 0, "Save & Exit");
        menu.add(0, OPTION_DISCARD_EXIT, 1, "Discard & Exit");
        menu.add(0, OPTION_VIEW_PAGES, 2, "View Pages");
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                Constants.PREFERENCE_EDUCATION_RESOURCE, false))
            menu.add(0, OPTION_HELP, 3, "Help");
        return true;
    }

    ReentrantLock lock = new ReentrantLock();

    /** {@inheritDoc} */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        lock.lock();
        try {
            switch (item.getItemId()) {
                case OPTION_SAVE_EXIT:
                    mRunnerFragment.storeCurrentProcedure(false);
                    setResult(RESULT_OK, null);
                    mRunnerFragment.logEvent(EventType.ENCOUNTER_SAVE_QUIT, "");
                    finish();
                    return true;
                case OPTION_DISCARD_EXIT:
                    mRunnerFragment.deleteCurrentProcedure();
                    mRunnerFragment.logEvent(EventType.ENCOUNTER_DISCARD, "");
                    setResult(RESULT_CANCELED, null);
                    finish();
                    return true;
                case OPTION_VIEW_PAGES:
                    this.wasOnDonePage = true;
                    mRunnerFragment.pageList();
                    return true;
                case OPTION_HELP:
                    mRunnerFragment.showInfo(Audience.WORKER);
                    return true;
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    /** The back key will activate previous page. */
    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch (keycode) {
            case KeyEvent.KEYCODE_BACK:
                mRunnerFragment.onBackButtonPressed(wasOnDonePage);
                wasOnDonePage = false;
                return true;
            default:
                return false;
        }
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

    /** Handles launching data capture Activities. */
    @Override
    protected void onNewIntent(Intent intent) {
        int description = intent.getExtras().getInt(INTENT_KEY_STRING);
        Log.i(TAG, "description = " + description);
        try {
            switch (description) {
                case 0: // intent comes from PictureElement to launch camera app
                    params = intent.getStringArrayExtra(PictureElement.PARAMS_NAME);

                    // For Android 1.1:
                    // Intent cameraIntent = new
                    // Intent("android.media.action.IMAGE_CAPTURE");

                    // For Android >=1.5:
                    Intent cameraIntent = new Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE);

                    // EXTRA_OUTPUT is broken on a lot of phones. The HTC G1,
                    // Tattoo,
                    // and Wildfire return a majorly downsampled version of the
                    // image. In the HTC Sense UI, this is a bug with their
                    // camera.
                    // With vanilla Android, it's a bug in 1.6.

                    Uri tempImageUri = Uri.fromFile(getTemporaryImageFile());
                    // This extra tells the camera to return a larger image -
                    // only works in >=1.5
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri);
                    startActivityForResult(cameraIntent, CAMERA_INTENT_REQUEST_CODE);
                    break;
                case PLUGIN_INTENT_REQUEST_CODE:
                    Log.d(TAG, "Got request to start plugin activity.");
                    mEncounterState = new Intent();
                    mEncounterState.setDataAndType(intent.getData(),
                            intent.getType());
                    Log.d(TAG, "State: " + mEncounterState.toUri(
                            Intent.URI_INTENT_SCHEME));
                    Intent plug = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                    // Add some state about the current encounter to the Intent
                    plug.putExtra("subject",
                            p.getPatientInfo().getPatientIdentifier());
                    plug.putExtra("encounter", EncounterDAO.getEncounterGuid(this,
                            thisSavedProcedure));
                    plug.putExtra("observation", intent.getStringExtra(
                            BinarySQLFormat.ELEMENT_ID));

                    Log.d(TAG, "Plug: " + plug.toUri(Intent.URI_INTENT_SCHEME));
                    startActivityForResult(plug, PLUGIN_INTENT_REQUEST_CODE);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            for (Object o : e.getStackTrace())
                Log.e(TAG, "...." + o);
            Toast.makeText(this, this.getString(R.string.msg_err_no_plugin),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /** Handles the results of Activities launched for data capture. */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            final Intent data)
    {
        Log.d(TAG, "Returned. requestCode: " + requestCode);
        Log.d(TAG, "......... resultCode : " + resultCode);
        Log.d(TAG, "......... obs        : " + mEncounterState.getData());
        Log.d(TAG, "......... type       : " + mEncounterState.getType());
        String answer = "";
        switch (resultCode) {
            case (RESULT_OK):
                try {
                    switch (requestCode) {
                    /*
                     * case (BARCODE_INTENT_REQUEST_CODE): String contents =
                     * data.getStringExtra("SCAN_RESULT"); String format =
                     * data.getStringExtra("SCAN_RESULT_FORMAT"); Log.i(TAG,
                     * "Got result from barcode intent: " + contents);
                     * ProcedurePage pp = p.current(); PatientIdElement
                     * patientId = pp.getPatientIdElement();
                     * patientId.setAndRefreshAnswer(contents); break;
                     */
                    // TODO the camera should get removed.
                        case (CAMERA_INTENT_REQUEST_CODE):
                            ImageProcessingTaskRequest request =
                                    new ImageProcessingTaskRequest();
                            request.savedProcedureId = params[0];
                            request.elementId = params[1];
                            request.tempImageFile = getTemporaryImageFile();
                            request.c = this;
                            request.intent = data;
                            Log.i(TAG, "savedProcedureId " + request.savedProcedureId
                                    + " and elementId " + request.elementId);

                            // Handles making a thumbnail of the image and
                            // moving it
                            // from the temporary location.
                            ImageProcessingTask imageTask = new ImageProcessingTask();
                            imageTask.execute(request);
                            break;
                        case (INFO_INTENT_REQUEST_CODE):
                            Log.d(TAG, "EducationResource intent: " + data.getType());
                            if (data.getType().contains("text/plain")) {
                                String text = data.getStringExtra("text");
                                String title = data.getStringExtra(Intent.EXTRA_TITLE);
                                SanaUtil.createDialog(this, title, text).show();
                            } else {
                                startActivity(data);
                            }
                            break;
                        case PLUGIN_INTENT_REQUEST_CODE:
                            Uri mObs = mEncounterState.getData();
                            String mObsType = mEncounterState.getType();
                            Intent result = PluginService.renderPluginActivityResult(
                                    getContentResolver(), data, mObs, mObsType);
                            String type = result.getType();
                            Uri rData = result.getData();

                            // Check if we get plain text first
                            if (type.equals("text/plain")) {
                                answer = rData.getFragment();

                                // Otherwise we have binary blob so we insert
                            } else {
                                Uri uri = BinaryDAO.updateOrCreate(
                                        getContentResolver(),
                                        mObs.getPathSegments().get(1),
                                        mObs.getPathSegments().get(2),
                                        rData, type);
                                Log.d(TAG, "Binary insert uri: " + uri.toString());
                                answer = BinaryDAO.getUUID(uri);
                            }
                            break;
                        default:
                            Log.e(TAG, "Unknown activity");
                            answer = "";
                            break;
                    }

                    p.current().setElementValue(
                            mEncounterState.getData().getPathSegments().get(2),
                            answer);
                    Log.d(TAG, "Got answer: " + answer);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error capturing answer from RESULT_OK: "
                            + e.toString());
                }
                break;
            default:
                Log.i(TAG, "Activity cancelled.");
                break;
        }

    }

    /** A static temporary image file
     * 
     * @return */
    protected static File getTemporaryImageFile() {
        return new File(Environment.getExternalStorageDirectory(), "sana.jpg");
    }
}
