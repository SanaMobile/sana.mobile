
package org.sana.android.fragment;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.sana.R;
import org.sana.android.Constants;
import org.sana.android.activity.ProcedureRunner;
import org.sana.android.db.EventDAO;
import org.sana.android.db.PatientInfo;
import org.sana.android.db.ProcedureDAO;
import org.sana.android.db.SanaDB.EventSQLFormat.EventType;
import org.sana.android.db.SanaDB.ProcedureSQLFormat;
import org.sana.android.db.SanaDB.SavedProcedureSQLFormat;
import org.sana.android.media.EducationResource.Audience;
import org.sana.android.net.MDSInterface;
import org.sana.android.procedure.Procedure;
import org.sana.android.procedure.ProcedureElement;
import org.sana.android.procedure.ProcedureParseException;
import org.sana.android.procedure.ValidationError;
import org.sana.android.service.BackgroundUploader;
import org.sana.android.service.ServiceConnector;
import org.sana.android.service.ServiceListener;
import org.sana.android.task.PatientLookupListener;
import org.sana.android.task.PatientLookupTask;
import org.sana.android.util.SanaUtil;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

/** Base class for running either a new encounter or a new patient. Individual
 * procedure steps are rendered to a view which is wrapped in a container which
 * presents buttons for paging. Additional logic is built into this class to
 * handle launching and capturing returned values from Activities used to
 * capture data along with initiating procedure saving, reloading, and
 * uploading.
 * 
 * @author Sana Development Team */
public abstract class BaseRunnerFragment extends Fragment implements View.OnClickListener,
        ServiceListener<BackgroundUploader>, PatientLookupListener {

    public static final String TAG = BaseRunnerFragment.class.getSimpleName();
    public static final String INTENT_KEY_STRING = "intentKey";
    public static final String INTENT_EXTRAS_KEY = "extras";
    public static final String PLUGIN_INTENT_KEY = "pluginIntent";

    // Intent
    public static final int CAMERA_INTENT_REQUEST_CODE = 1;
    public static final int BARCODE_INTENT_REQUEST_CODE = 2;
    public static final int INFO_INTENT_REQUEST_CODE = 7;
    public static final int PLUGIN_INTENT_REQUEST_CODE = 4;
    public static final int IMPLICIT_PLUGIN_INTENT_REQUEST_CODE = 8;

    // Dialog
    protected ProgressDialog lookupProgress = null;
    protected ProgressDialog loadProgressDialog = null;

    // Views
    private Button next, prev, info;
    private ViewAnimator baseViews;

    // Service
    private ServiceConnector mConnector = new ServiceConnector();
    private BackgroundUploader mUploadService = null;

    // State instance fields
    protected Procedure mProcedure = null;
    protected Uri thisSavedProcedure;
    protected int startPage = 0;
    protected boolean onDonePage = false;
    private PatientLookupTask patientLookupTask = null;

    /** {@inheritDoc} */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /** {@inheritDoc} */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.base_runner_fragment, null);
    }

    /** {@inheritDoc} */
    @Override
    public void onActivityCreated(Bundle instance) {
        super.onActivityCreated(instance);
        Log.v(getClass().getSimpleName(), "onActivityCreate");

        try {
            mConnector.setServiceListener(this);
            mConnector.connect(getActivity());
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(),
                    "Exception starting background upload service: " + e.toString());
            e.printStackTrace();
        }

        logEvent(EventType.ENCOUNTER_ACTIVITY_START_OR_RESUME, "");

        loadProcedure(instance);
    }

    /** Loads in a procedure from the bundle passed in to the fragment.
     * 
     * @param instance */
    protected abstract void loadProcedure(Bundle instance);

    /** {@inheritDoc} */
    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");

        if (lookupProgress != null) {
            lookupProgress.dismiss();
            lookupProgress = null;
        }
        if (loadProgressDialog != null) {
            loadProgressDialog.dismiss();
            loadProgressDialog = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if (mConnector != null) {
            try {
                mConnector.disconnect(getActivity());
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "While disconnecting service got exception: " + e);
                e.printStackTrace();
            }
        }
        if (mProcedure != null) {
            mProcedure.clearCachedViews();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onClick(View v) {

        if (v == next) {
            nextPage();
        } else if (v == prev) {
            prevPage();
        } else if (v == info) {
            showInfo(Audience.WORKER);
        } else {
            switch (v.getId()) {
                case R.id.procedure_done_back:
                    prevPage();
                    break;
                case R.id.procedure_done_upload:
                    uploadProcedureInBackground();
                    break;
                default:
                    Log.e(TAG, "Got onClick from unexpected id " + v.getId());
            }
        }

    }

    /** Serializes the current procedure to the database. Takes the answers map
     * from the procedure, serializes it to JSON, and stores it. If finished is
     * set, then it will set the procedure's row to finished. This will signal
     * to the upload service that it is ready for upload.
     * 
     * @param finished -- Whether to set the procedure as ready for upload. */
    public void storeCurrentProcedure(boolean finished) {
        if (mProcedure != null && thisSavedProcedure != null) {
            JSONObject answersMap = new JSONObject(mProcedure.toAnswers());
            String json = answersMap.toString();

            ContentValues cv = new ContentValues();
            cv.put(SavedProcedureSQLFormat.PROCEDURE_STATE, json);

            if (finished)
                cv.put(SavedProcedureSQLFormat.FINISHED, finished);

            int updatedObjects = getActivity().getContentResolver().update(thisSavedProcedure,
                    cv, null, null);
            Log.i(TAG, "storeCurrentProcedure updated " + updatedObjects
                    + " objects. (SHOULD ONLY BE 1)");
        }
    }

    /** Removes the current procedure form the database. */
    public void deleteCurrentProcedure() {
        getActivity().getContentResolver().delete(thisSavedProcedure, null, null);
    }

    /** Navigates to the next page.
     * 
     * @return true if navigating to the next page was successful, otherwise,
     *         false. */
    public synchronized boolean nextPage() {
        boolean succeed = true;
        try {
            mProcedure.current().validate();
        } catch (ValidationError e) {
            String message = e.getMessage();
            logEvent(EventType.ENCOUNTER_PAGE_VALIDATION_FAILED, message);
            SanaUtil.createAlertMessage(getActivity(), message);
            return false;
        }

        ProcedureElement patientId = mProcedure.current().getElementByType("patientId");
        if (patientId != null && mProcedure.getPatientInfo() == null) {
            // The patient ID question is on this page. Look up the ID in an
            // AsyncTask
            lookupPatient(patientId.getAnswer());
            return false;
        }

        // Save answers
        storeCurrentProcedure(false);
        if (!mProcedure.hasNextShowable()) {
            if (!onDonePage) {
                baseViews.setInAnimation(getActivity(), R.anim.slide_from_right);
                baseViews.setOutAnimation(getActivity(), R.anim.slide_to_left);
                baseViews.showNext();
                onDonePage = true;
                getActivity().setProgress(10000);
            } else {
                succeed = false;
            }
        } else {
            mProcedure.advance();

            logEvent(EventType.ENCOUNTER_NEXT_PAGE, Integer.toString(
                    mProcedure.getCurrentIndex()));

            // Hide the keyboard
            InputMethodManager imm = (InputMethodManager) getActivity().
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && mProcedure != null && mProcedure.getCachedView() != null)
                imm.hideSoftInputFromWindow(mProcedure.getCachedView().getWindowToken(),
                        0);

            Log.v(TAG, "In nextPage(), current page index is: "
                    + Integer.toString(mProcedure.getCurrentIndex()));
            getActivity().setProgress(currentProg());

            // Tell the current page to play its first audio prompt
            mProcedure.current().playFirstPrompt();
        }

        updateNextPrev();
        return succeed;
    }

    /** Displays the previous page.
     * 
     * @return true if navigating to the previous page was successful,
     *         otherwise, false. */
    public synchronized boolean prevPage() {
        boolean succeed = true;

        if (onDonePage) {
            baseViews.setInAnimation(getActivity(), R.anim.slide_from_left);
            baseViews.setOutAnimation(getActivity(), R.anim.slide_to_right);
            baseViews.showPrevious();
            onDonePage = false;
            getActivity().setProgress(currentProg());

            // Tell the current page to play its first audio prompt
            mProcedure.current().playFirstPrompt();
        }
        // If was on start of procedures page
        // Back button will go back to procedures list page
        else if (!mProcedure.hasPrevShowable()) {
            // This quits when you hit back and have nowhere else to go back to.
            getActivity().setResult(Activity.RESULT_CANCELED, null);
            logEvent(EventType.ENCOUNTER_EXIT_NO_SAVE, "");
            getActivity().finish();
            return succeed;
        }
        else if (mProcedure.hasPrevShowable()) {
            mProcedure.back();
            Log.v("prev", Integer.toString(mProcedure.getCurrentIndex()));
            getActivity().setProgress(currentProg());

            logEvent(EventType.ENCOUNTER_PREVIOUS_PAGE,
                    Integer.toString(mProcedure.getCurrentIndex()));

            // Save answers
            storeCurrentProcedure(false);

            // Hide the keyboard
            InputMethodManager imm = (InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && mProcedure != null && mProcedure.getCachedView() != null)
                imm.hideSoftInputFromWindow(mProcedure.getCachedView().getWindowToken(),
                        0);

            // Tell the current page to play its first audio prompt
            mProcedure.current().playFirstPrompt();
        }

        updateNextPrev();
        return succeed;
    }

    /** Launches the EducationList activity. Audience may be patient or worker.
     * 
     * @param audience the target audience. */
    public synchronized void showInfo(Audience audience) {
        Log.d(TAG, "Launching Help, audience: " + audience);
        // Gets the elements of the current page which have help
        Intent i = mProcedure.current().educationResources(audience);
        if (i == null) {
            Toast.makeText(getActivity(), getString(R.string.dialog_no_help_available),
                    Toast.LENGTH_SHORT).show();
        } else {
            try {
                startActivityForResult(i, INFO_INTENT_REQUEST_CODE);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    /** Adds current procedure to queue for upload. */
    public void uploadProcedureInBackground() {
        storeCurrentProcedure(true);
        // First check to make sure procedure has not already been uploaded
        if (MDSInterface.isProcedureAlreadyUploaded(thisSavedProcedure,
                getActivity().getBaseContext())) {
            getActivity().showDialog(ProcedureRunner.DIALOG_ALREADY_UPLOADED);
        }
        else {
            Log.i(TAG, "Adding current procedure to background upload queue");
            if (mUploadService != null) {
                mUploadService.addProcedureToQueue(thisSavedProcedure);
            }
            logEvent(EventType.ENCOUNTER_SAVE_UPLOAD, "");
            getActivity().finish();
        }
    }

    // current progress in the procedure
    private int currentProg() {
        int pageCount = mProcedure.getVisiblePageCount();
        if (pageCount == 0)
            return 10000;
        return (int) (10000 * (double) (mProcedure.getCurrentVisibleIndex()) / pageCount);
    }

    /** Logs an event.
     * 
     * @param type
     * @param value */
    public abstract void logEvent(EventType type, String value);

    /** Logs an exception
     * 
     * @param t */
    protected void logException(Throwable t) {
        String stackTrace = EventDAO.getStackTrace(t);
        EventType et = EventType.EXCEPTION;
        if (t instanceof OutOfMemoryError) {
            et = EventType.OUT_OF_MEMORY;
        }
        logEvent(et, stackTrace);
    }

    /** For connecting to the BackgroundUploader. */
    @Override
    public void onConnect(BackgroundUploader uploadService) {
        Log.i(TAG, "onServiceConnected");
        mUploadService = uploadService;
    }

    /** For disconnecting from the BackgroundUploader. */
    @Override
    public void onDisconnect(BackgroundUploader uploadService) {
        Log.i(TAG, "onServiceDisconnected");
        mUploadService = null;
    }

    /** Call this method from the containing activity so this fragment can handle
     * back button behavior. */
    public void onBackButtonPressed(boolean wasOnDonePage) {
        if (!wasOnDonePage) {
            prevPage();
        }
        setContentView(baseViews);
    }

    // Sets the view of this fragment
    private void setContentView(View view) {
        ViewGroup root = (ViewGroup) getView().findViewById(R.id.base_runner_root);
        root.removeAllViews();
        root.addView(view);
    }

    /** Displays a list of all the questions in the procedure */
    public void pageList() {
        ListView mList = new ListView(getActivity());
        List<String> pList = mProcedure.toStringArray();

        // int currentPage = p.getCurrentIndex();

        mList.setAdapter(new ArrayAdapter<String>(getActivity(),
                R.layout.pageslist_item, pList));

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parentView, View childView,
                    int position, long id) {
                onDonePage = false;
                // wasOnDonePage = false;

                logEvent(EventType.ENCOUNTER_JUMP_TO_QUESTION,
                        Integer.toString(position));
                mProcedure.jumpToVisiblePage(position);
                baseViews.setDisplayedChild(0);
                getActivity().setProgress(currentProg());
                updateNextPrev();
                setContentView(baseViews);
            }
        });
        setContentView(mList);
    }

    /** Creates the base view of this object. */
    public void createView() {
        if (mProcedure == null)
            return;
        getActivity().setTitle(mProcedure.getTitle());
        View procedureView = wrapViewWithInterface(mProcedure.toView(getActivity()));

        // Now that the view is active, go to the correct page.
        if (mProcedure.getCurrentIndex() != startPage) {
            mProcedure.jumpToPage(startPage);
            updateNextPrev();
        }

        baseViews = new ViewAnimator(getActivity());
        baseViews.setBackgroundResource(android.R.drawable.alert_dark_frame);
        baseViews.setInAnimation(AnimationUtils.loadAnimation(getActivity(),
                R.anim.slide_from_right));
        baseViews.setOutAnimation(AnimationUtils.loadAnimation(getActivity(),
                R.anim.slide_to_left));
        baseViews.addView(procedureView);

        // This should add it to baseViews, so don't add it manually.
        View procedureDonePage = getActivity().getLayoutInflater().inflate(
                R.layout.procedure_runner_done, baseViews);
        ((TextView) procedureDonePage.findViewById(R.id.procedure_done_text))
                .setTextAppearance(getActivity(), android.R.style.TextAppearance_Large);
        procedureDonePage.findViewById(R.id.procedure_done_back)
                .setOnClickListener(this);
        procedureDonePage.findViewById(R.id.procedure_done_upload)
                .setOnClickListener(this);

        if (onDonePage) {
            baseViews.setInAnimation(null);
            baseViews.setOutAnimation(null);
            baseViews.showNext();
            baseViews.setInAnimation(AnimationUtils.loadAnimation(getActivity(),
                    R.anim.slide_from_right));
            baseViews.setOutAnimation(AnimationUtils.loadAnimation(getActivity(),
                    R.anim.slide_to_left));
        }

        setContentView(baseViews);
        getActivity().setProgressBarVisibility(true);
        getActivity().setProgress(0);
    }

    /** Takes a view and wraps it with next/previous buttons for navigating.
     * 
     * @param sub - the view which is to be wrapped
     * @return - a new view which is <param>sub</param> wrapped with next/prev
     *         buttons. */
    public View wrapViewWithInterface(View sub) {
        // View sub = state.current().toView(this);
        // RelativeLayout rl = new RelativeLayout(this);
        // rl.setLayoutParams( new ViewGroup.LayoutParams(
        // LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT ) );

        LinearLayout base = new LinearLayout(getActivity());
        base.setOrientation(LinearLayout.VERTICAL);

        LinearLayout ll = new LinearLayout(getActivity());
        ll.setOrientation(LinearLayout.HORIZONTAL);
        next = new Button(getActivity());
        next.setOnClickListener(this);
        info = new Button(getActivity());
        info.setOnClickListener(this);
        info.setText(getResources().getString(
                R.string.procedurerunner_info));
        info.setText(getResources().getString(R.string.procedurerunner_info));
        prev = new Button(getActivity());
        prev.setOnClickListener(this);

        updateNextPrev();
        // Are we dispalying Info button
        boolean showEdu = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getBoolean(Constants.PREFERENCE_EDUCATION_RESOURCE, false);
        float nextWeight = showEdu ? 0.333f : 0.5f;
        float infoWeight = showEdu ? 0.334f : 0.0f;
        float prevWeight = showEdu ? 0.333f : 0.5f;

        ll.addView(prev, new LinearLayout.LayoutParams(-2, -1, prevWeight));
        // Only show info button if Education Resource Setting is true
        if (showEdu)
            ll.addView(info, new LinearLayout.LayoutParams(-2, -1, infoWeight));
        ll.addView(next, new LinearLayout.LayoutParams(-2, -1, nextWeight));
        ll.setWeightSum(1.0f);

        // RelativeLayout.LayoutParams ll_lp = new
        // RelativeLayout.LayoutParams(-1,100);
        // ll_lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        // rl.addView(ll, ll_lp);

        // RelativeLayout.LayoutParams sub_lp = new
        // RelativeLayout.LayoutParams(-1,-1);
        // sub_lp.addRule(RelativeLayout.ABOVE, ll.getId());
        // rl.addView(sub, sub_lp);

        // ScrollView sv = new ScrollView(this);
        // sv.addView(sub, new ViewGroup.LayoutParams(-1,-1));
        // base.addView(sv, new LinearLayout.LayoutParams(-1,-2,0.99f));
        base.addView(sub, new LinearLayout.LayoutParams(-1, -2, 0.99f));
        base.addView(ll, new LinearLayout.LayoutParams(-1, -2, 0.01f));

        base.setWeightSum(1.0f);

        return base;
    }

    /** Updates the next and previous page references. */
    public void updateNextPrev() {
        prev.setEnabled(mProcedure.hasPrev());
        prev.setText(getResources().getString(
                R.string.procedurerunner_previous));
        if (mProcedure.hasNext()) {
            next.setText(getResources().getString(
                    R.string.procedurerunner_next));
        } else {
            next.setText(getResources().getString(
                    R.string.procedurerunner_done));
        }
    }

    /** A request for loading a procedure.
     * 
     * @author Sana Development Team */
    class ProcedureLoadRequest {
        Bundle instance;
        Intent intent;
    }

    /** A task for loading a procedure.
     * 
     * @author Sana Development Team */
    class ProcedureLoaderTask extends AsyncTask<ProcedureLoadRequest, Void,
            ProcedureLoadResult>
    {
        Bundle instance;
        Intent intent;

        /** {@inheritDoc} */
        @Override
        protected ProcedureLoadResult doInBackground(
                ProcedureLoadRequest... params)
        {
            ProcedureLoadRequest load = params[0];
            instance = load.instance;
            intent = load.intent;

            ProcedureLoadResult result = new ProcedureLoadResult();
            if (instance == null && !intent.hasExtra("savedProcedureUri")) {
                // New Encounter
                Uri procedure = intent.getData();
                int procedureId = Integer.parseInt(
                        procedure.getPathSegments().get(1));
                String procedureXml = ProcedureDAO.getXMLForProcedure(
                        getActivity(), procedure);

                // Record that we are starting a new encounter
                logEvent(EventType.ENCOUNTER_LOAD_NEW_ENCOUNTER,
                        procedure.toString());

                ContentValues cv = new ContentValues();
                cv.put(SavedProcedureSQLFormat.PROCEDURE_ID, procedureId);
                cv.put(SavedProcedureSQLFormat.PROCEDURE_STATE, "");
                cv.put(SavedProcedureSQLFormat.FINISHED, true);
                cv.put(SavedProcedureSQLFormat.UPLOADED, false);
                cv.put(SavedProcedureSQLFormat.UPLOAD_STATUS, 0);

                thisSavedProcedure = getActivity().getContentResolver().insert(
                        SavedProcedureSQLFormat.CONTENT_URI, cv);

                Log.i(TAG, "onCreate() : uri = " + procedure.toString()
                        + " savedUri=" + thisSavedProcedure);

                Procedure p = null;
                try {
                    p = Procedure.fromXMLString(procedureXml);
                } catch (IOException e) {
                    Log.e(TAG, "Error loading procedure from XML: "
                            + e.toString());
                    e.printStackTrace();
                    logException(e);
                } catch (ParserConfigurationException e) {
                    Log.e(TAG, "Error loading procedure from XML: "
                            + e.toString());
                    e.printStackTrace();
                    logException(e);
                } catch (SAXException e) {
                    Log.e(TAG, "Error loading procedure from XML: "
                            + e.toString());
                    e.printStackTrace();
                    logException(e);
                } catch (ProcedureParseException e) {
                    Log.e(TAG, "Error loading procedure from XML: "
                            + e.toString());
                    e.printStackTrace();
                    logException(e);
                } catch (OutOfMemoryError e) {
                    Log.e(TAG, "Can't load procedure, out of memory.");
                    result.errorMessage = "Out of Memory.";
                    e.printStackTrace();
                    logException(e);
                }
                if (p != null) {
                    p.setInstanceUri(thisSavedProcedure);
                }

                result.p = p;
                result.success = p != null;
                result.procedureUri = procedure;
                result.savedProcedureUri = thisSavedProcedure;

            } else {

                // This is a saved encounter.
                startPage = 0;
                PatientInfo pi = null;

                Log.v(TAG, "No instance on warm boot.");
                startPage = 0;
                onDonePage = false;
                String savedProcedureUri = intent.getStringExtra(
                        "savedProcedureUri");
                if (savedProcedureUri != null)
                    thisSavedProcedure = Uri.parse(savedProcedureUri);

                // Record that we are restoring a saved encounter
                logEvent(EventType.ENCOUNTER_LOAD_SAVED, "");

                pi = new PatientInfo();

                if (thisSavedProcedure == null) {
                    Log.e(TAG, "Couldn't determine the URI to warm boot with, " +
                            "bailing.");
                    return result;
                }
                Log.i(TAG, "Warm boot occured for " + thisSavedProcedure
                        + ", startPage:" + startPage + " onDonePage: "
                        + onDonePage);

                Cursor c = null;
                int procedureId = -1;
                String answersJson = "";
                try {
                    c = getActivity().getContentResolver().query(thisSavedProcedure,
                            new String[] {
                                    SavedProcedureSQLFormat.PROCEDURE_ID,
                                    SavedProcedureSQLFormat.PROCEDURE_STATE
                            },
                            null, null, null);
                    c.moveToFirst();
                    procedureId = c.getInt(0);
                    answersJson = c.getString(1);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    e.printStackTrace();
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }

                Map<String, String> answersMap = new HashMap<String, String>();
                try {
                    JSONTokener tokener = new JSONTokener(answersJson);
                    JSONObject answersDict = new JSONObject(tokener);
                    Iterator it = answersDict.keys();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        answersMap.put(key, answersDict.getString(key));
                        Log.i(TAG, "ProcedureLoaderTask loaded answer '"
                                + key + "' : '" + answersDict.getString(key)
                                + "'");
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "onCreate() -- JSONException " + e.toString());
                    e.printStackTrace();
                }

                Uri procedureUri = ContentUris.withAppendedId(
                        ProcedureSQLFormat.CONTENT_URI, procedureId);
                String procedureXml = ProcedureDAO.getXMLForProcedure(
                        getActivity(), procedureUri);
                Procedure procedure = null;
                try {
                    procedure = Procedure.fromXMLString(procedureXml);
                    procedure.setInstanceUri(thisSavedProcedure);
                    procedure.restoreAnswers(answersMap);
                } catch (IOException e) {
                    Log.e(TAG, "onCreate() -- IOException " + e.toString());
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    Log.e(TAG, "onCreate() -- couldn't create parser");
                    e.printStackTrace();
                } catch (SAXException e) {
                    Log.e(TAG, "onCreate() -- Couldn't parse XML");
                    e.printStackTrace();
                } catch (ProcedureParseException e) {
                    Log.e(TAG, "Error in Procedure.fromXML() : "
                            + e.toString());
                    e.printStackTrace();
                } catch (OutOfMemoryError e) {
                    Log.e(TAG, "Can't load procedure, out of memory.");
                    result.errorMessage = "Out of Memory.";
                    e.printStackTrace();

                }
                Log.i(TAG, "onCreate() : warm-booted from uri ="
                        + thisSavedProcedure);

                if (procedure != null && pi != null) {
                    procedure.setPatientInfo(pi);
                }

                result.p = procedure;
                result.success = procedure != null;
                result.savedProcedureUri = thisSavedProcedure;
                result.procedureUri = procedureUri;
            }

            return result;
        }

        /** {@inheritDoc} */
        @Override
        protected void onPostExecute(ProcedureLoadResult result) {
            super.onPostExecute(result);
            if (loadProgressDialog != null) {
                loadProgressDialog.dismiss();
                loadProgressDialog = null;
            }
            if (result != null && result.success) {
                mProcedure = result.p;
                thisSavedProcedure = result.savedProcedureUri;
                logEvent(EventType.ENCOUNTER_LOAD_FINISHED, "");
                if (mProcedure != null)
                    createView();
                else
                    logEvent(EventType.ENCOUNTER_LOAD_FAILED, "Null procedure");

            } else {
                // Show error
                logEvent(EventType.ENCOUNTER_LOAD_FAILED, "");
                getActivity().finish();
            }
        }
    }

    /** The result of loading a procedure
     * 
     * @author Sana Development Team */
    class ProcedureLoadResult {
        Uri procedureUri;
        Uri savedProcedureUri;
        Procedure p = null;
        boolean success = false;
        String errorMessage = "";
    }

    // starts a new patient look up task
    private void lookupPatient(String patientId) {
        logEvent(EventType.ENCOUNTER_LOOKUP_PATIENT_START, patientId);
        if (lookupProgress == null) {
            lookupProgress = new ProgressDialog(getActivity());
            lookupProgress.setMessage("Looking up patient \"" + patientId + "\""); // TODO
                                                                                   // i18n
            lookupProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            if (!getActivity().isFinishing())
                lookupProgress.show();

            if (patientLookupTask == null ||
                    patientLookupTask.getStatus() == Status.FINISHED) {
                patientLookupTask = new PatientLookupTask(getActivity());
                patientLookupTask.setPatientLookupListener(this);
                patientLookupTask.execute(patientId);
            }
        }
    }

    /** Callback to handle when a patient look up succeeds. Will result in an
     * alert being displayed prompting the user to confirm that it is the
     * correct patient. */
    public void onPatientLookupSuccess(final PatientInfo patientInfo) {
        logEvent(EventType.ENCOUNTER_LOOKUP_PATIENT_SUCCESS,
                patientInfo.getPatientIdentifier());
        if (lookupProgress != null) {
            lookupProgress.dismiss();
            lookupProgress = null;
        }

        StringBuilder message = new StringBuilder();
        message.append("Found patient record for ID ");
        message.append(patientInfo.getPatientIdentifier());
        message.append("\n");

        message.append("First Name: ");
        message.append(patientInfo.getPatientFirstName());
        message.append("\n");

        message.append("Last Name: ");
        message.append(patientInfo.getPatientLastName());
        message.append("\n");

        message.append("Gender: ");
        message.append(patientInfo.getPatientGender());
        message.append("\n");

        message.append("Is this the correct patient?");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message).setCancelable(false).setPositiveButton(
                getResources().getString(R.string.general_yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        mProcedure.setPatientInfo(patientInfo);
                        nextPage();
                    }
                }).setNegativeButton(
                getResources().getString(R.string.general_no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        mProcedure.current().getPatientIdElement()
                                .setAndRefreshAnswer("");
                    }
                });
        AlertDialog alert = builder.create();
        if (!getActivity().isFinishing())
            alert.show();
    }

    /** Callback to handle when a patient look up fails. Will result in an alert
     * being displayed prompting the user to input whether the patient should be
     * considered new. */
    public void onPatientLookupFailure(final String patientIdentifier) {
        logEvent(EventType.ENCOUNTER_LOOKUP_PATIENT_FAILED, patientIdentifier);

        Log.e(TAG, "Couldn't lookup patient. They might exist, but we don't "
                + "have their details.");
        if (lookupProgress != null) {
            lookupProgress.dismiss();
            lookupProgress = null;
        }

        StringBuilder message = new StringBuilder();
        message.append("Could not find patient record for ");
        message.append(patientIdentifier);
        message.append(". Entering a new patient. Continue?");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message).setCancelable(false).setPositiveButton(
                getResources().getString(R.string.general_yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        PatientInfo pi = new PatientInfo();
                        pi.setPatientIdentifier(patientIdentifier);
                        mProcedure.setPatientInfo(pi);
                        nextPage();
                    }
                }).setNegativeButton(
                getResources().getString(R.string.general_no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        if (!getActivity().isFinishing())
            alert.show();
    }
}
