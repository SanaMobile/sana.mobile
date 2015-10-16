
package org.sana.android.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.sana.R;
import org.sana.android.Constants;
import org.sana.android.activity.BaseRunner;
import org.sana.android.activity.ProcedureRunner;
import org.sana.android.app.Locales;
import org.sana.android.app.State.Keys;
import org.sana.android.content.Intents;
import org.sana.android.content.Uris;
import org.sana.android.db.EventDAO;
import org.sana.android.db.ModelWrapper;
import org.sana.android.db.PatientInfo;
import org.sana.android.db.ProcedureDAO;
import org.sana.android.db.SanaDB.ImageSQLFormat;
import org.sana.android.media.EducationResource.Audience;
import org.sana.android.net.MDSInterface;
import org.sana.android.procedure.Procedure;
import org.sana.android.procedure.ProcedureElement;
import org.sana.android.procedure.ProcedurePage;
import org.sana.android.procedure.ProcedureParseException;
import org.sana.android.procedure.ValidationError;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Events.EventType;
import org.sana.android.provider.Observations;
import org.sana.android.provider.Patients;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Subjects;
import org.sana.android.service.BackgroundUploader;
import org.sana.android.service.ServiceConnector;
import org.sana.android.service.ServiceListener;
import org.sana.android.service.impl.InstrumentationService;
import org.sana.android.task.PatientLookupListener;
import org.sana.android.task.PatientLookupTask;
import org.sana.android.util.Logf;
import org.sana.android.util.SanaUtil;
import org.sana.util.UUIDUtil;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
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
import android.text.TextUtils;
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

/**
 * Base class for running either a new encounter or a new patient. Individual
 * procedure steps are rendered to a view which is wrapped in a container which
 * presents buttons for paging. Additional logic is built into this class to
 * handle launching and capturing returned values from Activities used to
 * capture data along with initiating procedure saving, reloading, and
 * uploading.
 * 
 * @author Sana Development Team
 */
public abstract class BaseRunnerFragment extends BaseFragment implements View.OnClickListener,
        ServiceListener<BackgroundUploader>, PatientLookupListener{

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

    public static final int FLAG_OBJECT_TEMPORARY = 0;
    public static final int FLAG_OBJECT_UPDATED = 1;
    public static final int FLAG_OBJECT_PERSISTED = 2;


    public static interface ProcedureListener{
        void onProcedureComplete(Intent data);
        
        void onProcedureCancelled(String message);
    }

    protected ProcedureListener mProcedureListener = null;

    // Views
    private Button next, prev, info;
    private ViewAnimator baseViews;

    // Service
    private ServiceConnector mConnector = new ServiceConnector();
    private BackgroundUploader mUploadService = null;

    // State instance fields
    protected Procedure mProcedure = null;
    protected Uri uEncounter = Uri.EMPTY;
    protected String mSessionKey = "";
    protected Uri uSubject = Uri.EMPTY;
    protected Uri uProcedure = Uri.EMPTY;
    protected Uri uObserver = Uri.EMPTY;
    protected Uri uTask = Uri.EMPTY;
    protected Uri mData = Uri.EMPTY;
    
    protected int startPage = 0;
    protected boolean onDonePage = false;
    protected int objectFlag = FLAG_OBJECT_TEMPORARY;
    private PatientLookupTask patientLookupTask = null;

    protected boolean showCompleteConfirmation = true;
    // Activity Methods ///////////////////////////////////////////////////////
    /** {@inheritDoc} */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.dump();
    }

    /** {@inheritDoc} */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView(LayoutInflater,ViewGroup,Bundle");
        return inflater.inflate(R.layout.base_runner_fragment, container, false);
    }
    
    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle instance) {
        Log.i(getClassTag(), "onActivityCreated()");
        super.onActivityCreated(instance);
        
        try {;
            //mConnector.setServiceListener(this);
            //mConnector.connect(getActivity());
        } catch (Exception e) {
            Log.e(getClassTag(),
                    "Exception starting background upload service: " + e.toString());
            e.printStackTrace();
        }
        this.setRetainInstance(true);
        //if(instance != null)
        onUpdateAppState(instance);
        //else if(getActivity().getIntent() != null)
        onUpdateAppState(getActivity().getIntent());
        //logEvent(EventType.ENCOUNTER_ACTIVITY_START_OR_RESUME, "");
        logEvent(EventType.ENCOUNTER_ACTIVITY_START_OR_RESUME, ((instance != null)?instance.toString():null));
        //makeText("Instance is null: " + ((instance != null)? false: true));
        loadProcedure(instance);
    }

    /** {@inheritDoc} */
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

    // Dialogs ////////////////////////////////////////////////////////////////

    /**
     * Loads in a procedure from the bundle passed in to the fragment.
     * 
     * @param instance
     */
    protected abstract void loadProcedure(Bundle instance);

    /**
     * Serializes the current procedure to the database. Takes the answers map
     * from the procedure, serializes it to JSON, and stores it. If finished is
     * set, then it will set the procedure's row to finished. This will signal
     * to the upload service that it is ready for upload.
     * 
     * @param finished -- Whether to set the procedure as ready for upload.
     */
    public abstract void storeCurrentProcedure(boolean finished);
    

    public abstract void storeCurrentProcedure(boolean finished, boolean skipHidden);
    
    /** Removes the current procedure form the database. */
    public abstract void deleteCurrentProcedure();
    
    /**
     * Navigates to the next page.
     * 
     * @return true if navigating to the next page was successful, otherwise,
     *         false.
     */
    public synchronized boolean nextPage() {
        Log.i(TAG, "nextPage()");
        boolean succeed = true;
        try {
            mProcedure.current().validate();
        } catch (ValidationError e) {
            String message = e.getMessage();
            logEvent(EventType.ENCOUNTER_PAGE_VALIDATION_FAILED, message);
            SanaUtil.createAlertMessage(getActivity(), message);
            return false;
        }
        // Handles any post processing - allows pages to block
        if(!handlePostProcessedElements()){
            return false;
        }

        // Save answers
        storeCurrentProcedure(false);
        if (!mProcedure.hasNextShowable()) {
            Log.w(TAG, "...!has next showable");
            if (!onDonePage) {
                Log.w(TAG, "...!on done page");
                baseViews.setInAnimation(getActivity(), R.anim.slide_from_right);
                baseViews.setOutAnimation(getActivity(), R.anim.slide_to_left);

                onDonePage = true;
                if (!isShowCompleteConfirmation()){
                    getActivity().setProgress(10000);
                    // finished so we do a final call to persist data
                    storeCurrentProcedure(true);
                    uploadProcedureInBackground2();
                } else {
                    baseViews.showNext();
                }
                getActivity().setProgress(10000);
                updateNextPrev();
            } else {
                Log.d(TAG, "...on done page");
                succeed = false;
                if (!isShowCompleteConfirmation()){
                    getActivity().setProgress(10000);
                    // finished so we do a final call to persist data
                    storeCurrentProcedure(true);
                    uploadProcedureInBackground2();
                    // short circuits any call to update UI
                    return succeed;
                }
            }
        } else {
            Log.d(TAG, "...has next showable");
            //mProcedure.advance();
            ProcedurePage cp = mProcedure.advanceNext();
            while (cp != null){
            	
            	if(cp.displayForeground()) {
            		break;
            	} else {
                    storeCurrentProcedure(false,false);
            	}
            	cp = mProcedure.advanceNext();
            }
            logEvent(EventType.ENCOUNTER_NEXT_PAGE, Integer.toString(mProcedure.getCurrentIndex()));

            // Hide the keyboard
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (imm != null && mProcedure != null && mProcedure.getCachedView() != null)
                imm.hideSoftInputFromWindow(mProcedure.getCachedView().getWindowToken(), 0);

            Log.d(TAG, "...current page index is: "
                            + Integer.toString(mProcedure.getCurrentIndex()));
            getActivity().setProgress(currentProg());

            // Tell the current page to play its first audio prompt
            mProcedure.current().playFirstPrompt();
            updateNextPrev();
        }
        return succeed;
    }

    /**
     * Child classes should override to allow for any additional processing
     * that needs to occur prior to advancing.
     *
     * @return true if any post processing was handled. Default is to always
     *          return true
     */
    protected boolean handlePostProcessedElements() {
        return true;
    }

    /**
     * Displays the previous page.
     * 
     * @return true if navigating to the previous page was successful,
     *         otherwise, false.
     */
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
        // Back button will return a CANCELED message
        else if (!mProcedure.hasPrevShowable()) {
            // This quits when you hit back and have nowhere else to go back to.
            onExitNoSave();
            return succeed;
        } else if (mProcedure.hasPrevShowable()) {
        	
            mProcedure.back();
            Log.v("prev", Integer.toString(mProcedure.getCurrentIndex()));
            getActivity().setProgress(currentProg());

            logEvent(EventType.ENCOUNTER_PREVIOUS_PAGE,
                    Integer.toString(mProcedure.getCurrentIndex()));

            // Save answers
            storeCurrentProcedure(false);

            // Hide the keyboard
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (imm != null && mProcedure != null && mProcedure.getCachedView() != null)
                imm.hideSoftInputFromWindow(mProcedure.getCachedView().getWindowToken(), 0);

            // Tell the current page to play its first audio prompt
            mProcedure.current().playFirstPrompt();
        }

        updateNextPrev();
        return succeed;
    }

    /**
     * Launches the EducationList activity. Audience may be patient or worker.
     * 
     * @param audience the target audience.
     */
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
        if (MDSInterface.isProcedureAlreadyUploaded(uEncounter, getActivity()
                .getBaseContext())) {
            getActivity().showDialog(ProcedureRunner.DIALOG_ALREADY_UPLOADED);
        } else {
            Log.i(TAG, "Adding current procedure to background upload queue");
            if (mUploadService != null) {
                mUploadService.addProcedureToQueue(uEncounter);
            }
            logEvent(EventType.ENCOUNTER_SAVE_UPLOAD, "");
            getActivity().finish();
        }
    }
    
    public void uploadProcedureInBackground2() {
        Log.d(TAG, "uploadProcedureInBackground2() " + uEncounter);
    	uEncounter = (!Uris.isEmpty(uEncounter))? uEncounter: Uri.EMPTY;
    	Intent instrumentation = new Intent(getActivity(),InstrumentationService.class);
    	getActivity().stopService(instrumentation);
        Intent data;
        if(mProcedureListener != null){
            data = getResult(Intents.ACTION_CREATE);
            data.putExtra(Intents.EXTRA_ON_COMPLETE, mProcedure.getOnComplete());
            mProcedureListener.onProcedureComplete(data);
        } else{
            data = getResult();
            data.putExtra(Intents.EXTRA_ON_COMPLETE, mProcedure.getOnComplete());
            getActivity().setResult(Activity.RESULT_OK, data);
            getActivity().finish();
    	}
    }
    // current progress in the procedure
    protected int currentProg() {
        int pageCount = mProcedure.getVisiblePageCount();
        if (pageCount == 0)
            return 10000;
        return (int)(10000 * (double)(mProcedure.getCurrentVisibleIndex()) / pageCount);
    }

    /**
     * Logs an event.
     * 
     * @param type
     * @param value
     */
    public abstract void logEvent(EventType type, String value);

    /**
     * Logs an exception
     * 
     * @param t
     */
    protected void logException(Throwable t) {
        String stackTrace = EventDAO.getStackTrace(t);
        EventType et = EventType.EXCEPTION;
        if (t instanceof OutOfMemoryError) {
            et = EventType.OUT_OF_MEMORY;
        }
        logEvent(et, stackTrace);
    }

    /** {@inheritDoc} */
    @Override
    public void onClick(View v) {
        Log.i(TAG, "onClick(View)");
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
                    // finished so we do a final call to persist data
                    storeCurrentProcedure(true);
                    //uploadProcedureInBackground();
                    //showUploadingDialog();
                    uploadProcedureInBackground2();
                    break;
                default:
                    Log.e(TAG, "Got onClick from unexpected id " + v.getId());
            }
        }
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

    /**
     * Call this method from the containing activity so this fragment can handle
     * back button behavior.
     */
    public void onBackButtonPressed(boolean wasOnDonePage) {
        if (!wasOnDonePage) {
            prevPage();
        }
        setContentView(baseViews);
    }

    // Sets the view of this fragment
    private void setContentView(View view) {
        // Root view here is a FrameLayout so we know it is a ViewGroup
        // TODO should really replace this with a set of fragments
        ViewGroup root = (ViewGroup)getView();
        root.removeAllViews();
        root.addView(view);
    }

    /** Displays a list of all the questions in the procedure */
    public void pageList() {
        ListView mList = new ListView(getActivity());
        List<String> pList = mProcedure.toStringArray();

        // int currentPage = p.getCurrentIndex();

        mList.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.pageslist_item, pList));

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
                onDonePage = false;
                // wasOnDonePage = false;

                logEvent(EventType.ENCOUNTER_JUMP_TO_QUESTION, Integer.toString(position));
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
        Log.i(TAG, "createView()");
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
        baseViews
                .setOutAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_to_left));
        baseViews.addView(procedureView);

        // This should add it to baseViews, so don't add it manually.
        if(isShowCompleteConfirmation()) {
            View procedureDonePage = getActivity().getLayoutInflater().inflate(
                    R.layout.procedure_runner_done, baseViews);
            ((TextView) procedureDonePage.findViewById(R.id.procedure_done_text)).setTextAppearance(
                    getActivity(), android.R.style.TextAppearance_Large);
            procedureDonePage.findViewById(R.id.procedure_done_back).setOnClickListener(this);
            procedureDonePage.findViewById(R.id.procedure_done_upload).setOnClickListener(this);
        }
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

    /**
     * Takes a view and wraps it with next/previous buttons for navigating.
     * 
     * @param sub - the view which is to be wrapped
     * @return - a new view which is <param>sub</param> wrapped with next/prev
     *         buttons.
     */
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
        info.setText(getResources().getString(R.string.procedurerunner_info));
        info.setText(getResources().getString(R.string.procedurerunner_info));
        prev = new Button(getActivity());
        prev.setOnClickListener(this);

        next.setPadding(5,5,5,5);
        prev.setPadding(5,5,5,5);
        info.setPadding(5,5,5,5);
        updateNextPrev();
        // Are we dispalying Info button
        boolean showEdu = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(
                Constants.PREFERENCE_EDUCATION_RESOURCE, false);
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
        ViewGroup parent = (ViewGroup) sub.getParent();
        if (parent != null) {
            parent.removeView(sub);
        }
        base.addView(sub, new LinearLayout.LayoutParams(-1, -2, 0.99f));
        base.addView(ll, new LinearLayout.LayoutParams(-1, -2, 0.01f));

        base.setWeightSum(1.0f);

        return base;
    }

    /** Updates the next and previous page references. */
    public void updateNextPrev() {
    	Locales.updateLocale(getActivity(), getString(R.string.force_locale));
        prev.setEnabled(mProcedure.hasPrev());
        prev.setText(getResources().getString(R.string.procedurerunner_previous));
        if (mProcedure.hasNext()) {
            next.setText(getResources().getString(R.string.procedurerunner_next));
        } else {
            next.setText(getResources().getString(R.string.procedurerunner_done));
        }
    }

    int requested = 0;
    
    /**
     * A request for loading a procedure.
     * 
     * @author Sana Development Team
     */
    class ProcedureLoadRequest {
        Bundle instance = null;

        Intent intent = null;
    }

    /**
     * A task for loading a procedure.
     * 
     * @author Sana Development Team
     */
    class ProcedureLoaderTask extends AsyncTask<ProcedureLoadRequest, Void, ProcedureLoadResult> {
        Bundle instance;

        Intent intent;

        /** {@inheritDoc} */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialogFragment(getString(R.string.dialog_loading_procedure));
        }

        /** {@inheritDoc} */
        @Override
        protected ProcedureLoadResult doInBackground(ProcedureLoadRequest... params) {
        	requested++;
            Log.v(TAG, "ProcedureLoadRequest count: " + requested);
            ProcedureLoadRequest load = params[0];
            instance = load.instance;
            intent = load.intent;
            ProcedureLoadResult result = new ProcedureLoadResult();
            if (Uris.isEmpty(uEncounter) && instance == null && !intent.hasExtra("savedProcedureUri")) {
                
                // New Encounter
                Uri procedure = intent.getData();
                Log.i(TAG, "ProcedureLoadResult.doInBackground() : uri = " + procedure + "(" + procedure.getLastPathSegment() + "), savedUri="
                        + uEncounter);
                
                String uuid = procedure.getLastPathSegment();
                if(!UUIDUtil.isValid(uuid)){
                	uuid = ModelWrapper.getUuid(procedure,getActivity().getContentResolver());
                	procedure = Uris.withAppendedUuid(Procedures.CONTENT_URI, uuid);
                }
                Log.i(TAG, "preparing to load xml for uri = " + procedure); 
                String procedureXml = ProcedureDAO.getXMLForProcedure(getActivity(), procedure);

                // Record that we are starting a new encounter
                logEvent(EventType.ENCOUNTER_LOAD_NEW_ENCOUNTER, procedure.toString());
                // make sure we only insert once
                if(uEncounter == null || (uEncounter != null && (uEncounter.equals(Uri.EMPTY)))){
                    Log.w(TAG, "no Encounter: " + uEncounter);
                	ContentValues cv = new ContentValues();
                	cv.put(Encounters.Contract.UUID, UUID.randomUUID().toString());
                	cv.put(Encounters.Contract.SUBJECT, ModelWrapper.getUuid(uSubject, getActivity().getContentResolver()));
                	cv.put(Encounters.Contract.OBSERVER, ModelWrapper.getUuid(uObserver, getActivity().getContentResolver()));
                	cv.put(Encounters.Contract.PROCEDURE, procedure.getLastPathSegment());
                	cv.put(Encounters.Contract.STATE, "");
                	cv.put(Encounters.Contract.FINISHED, true);
                	cv.put(Encounters.Contract.UPLOADED, false);
                	cv.put(Encounters.Contract.UPLOAD_STATUS, 0);
                	uEncounter = getActivity().getContentResolver().insert(
                            Encounters.CONTENT_URI, cv);
                        //String euuid = ModelWrapper.getUuid(uEncounter,getActivity().getContentResolver());
                        //uEncounter = Uris.withAppendedUuid(Encounters.CONTENT_URI, euuid);
                    Log.w(TAG, "inserted Encounter: " + uEncounter);
                } else {
                    Log.w(TAG, "using Encounter: " + uEncounter);
                }
                Log.w(TAG, "current Encounter: " + uEncounter);
                Procedure p = null;
                try {
                    p = Procedure.fromXMLString(procedureXml);
                } catch (IOException e) {
                    Log.e(TAG, "Error loading procedure from XML: " + e.toString());
                    e.printStackTrace();
                    logException(e);
                } catch (ParserConfigurationException e) {
                    Log.e(TAG, "Error loading procedure from XML: " + e.toString());
                    e.printStackTrace();
                    logException(e);
                } catch (SAXException e) {
                    Log.e(TAG, "Error loading procedure from XML: " + e.toString());
                    e.printStackTrace();
                    logException(e);
                } catch (ProcedureParseException e) {
                    Log.e(TAG, "Error loading procedure from XML: " + e.toString());
                    e.printStackTrace();
                    logException(e);
                } catch (OutOfMemoryError e) {
                    Log.e(TAG, "Can't load procedure, out of memory.");
                    result.errorMessage = "Out of Memory.";
                    e.printStackTrace();
                    logException(e);
                }
                if (p != null) {
                    p.setInstanceUri(uEncounter);
                }

                result.p = p;
                result.success = p != null;
                result.procedureUri = procedure;
                result.savedProcedureUri = uEncounter;

            } else {

                // This is a saved encounter.
                //startPage = 0;
                PatientInfo pi = null;
                if(instance == null && Uris.isEmpty(uEncounter)){
                	Log.v(TAG, "No instance on warm boot.");
                	String savedProcedureUri = intent.getStringExtra("savedProcedureUri");
                	if (!TextUtils.isEmpty(savedProcedureUri))
                		uEncounter = Uri.parse(savedProcedureUri);
                
                	// Record that we are restoring a saved encounter
                	logEvent(EventType.ENCOUNTER_LOAD_SAVED, String.valueOf(uEncounter));
                    //startPage = intent.getIntExtra("currentPage", 0);
                    //onDonePage = intent.getBooleanExtra("onDonePage", false);

                	//pi = new PatientInfo();
                } else {
                    Log.v(TAG, "Instance present on warm boot.");
                	logEvent(EventType.ENCOUNTER_LOAD_HOTLOAD, String.valueOf(uEncounter));
                    //startPage = instance.getInt("currentPage");
                    //onDonePage = instance.getBoolean("onDonePage");
                	String savedProcedureUri = intent.getStringExtra("savedProcedureUri");
                	
                }
                Log.w(TAG, "Page: " + startPage + ", onDonePage: " + onDonePage );
                if (uEncounter == null) {
                    Log.e(TAG, "Couldn't determine the URI to warm boot with, " + "bailing.");
                    return result;
                }
                Log.i(TAG, "Warm boot occured for " + uEncounter + ", startPage:"
                        + startPage + " onDonePage: " + onDonePage);

                Cursor c = null;
                String procedureId = "";
                String answersJson = "";
                try {
                    c = getActivity().getContentResolver().query(uEncounter, new String[] {
                            Encounters.Contract.PROCEDURE, Encounters.Contract.STATE
                    }, null, null, null);
                    c.moveToFirst();
                    procedureId = c.getString(0);
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
                        String key = (String)it.next();
                        answersMap.put(key, answersDict.getString(key));
                        Log.i(TAG, "ProcedureLoaderTask loaded answer '" + key + "' : '"
                                + answersDict.getString(key) + "'");
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "onCreate() -- JSONException " + e.toString());
                    e.printStackTrace();
                }
                Uri procedureUri;
                if(UUIDUtil.isValid(procedureId)){
                	procedureUri = Uris.withAppendedUuid(Procedures.CONTENT_URI, procedureId);
                } else
                	procedureUri = ContentUris.withAppendedId(Procedures.CONTENT_URI, Long.parseLong(procedureId));
                Log.i(TAG, "preparing to load xml for uri = " + procedureUri); 
                String procedureXml = ProcedureDAO.getXMLForProcedure(getActivity(), procedureUri);
                Procedure procedure = null;
                try {
                    procedure = Procedure.fromXMLString(procedureXml);
                    procedure.setInstanceUri(uEncounter);
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
                    Log.e(TAG, "Error in Procedure.fromXML() : " + e.toString());
                    e.printStackTrace();
                } catch (OutOfMemoryError e) {
                    Log.e(TAG, "Can't load procedure, out of memory.");
                    result.errorMessage = "Out of Memory.";
                    e.printStackTrace();

                }
                Log.i(TAG, "onCreate() : warm-booted from uri =" + uEncounter);

                if (procedure != null && pi != null) {
                    procedure.setPatientInfo(pi);
                }

                result.p = procedure;
                result.success = procedure != null;
                result.savedProcedureUri = uEncounter;
                result.procedureUri = procedureUri;
            }
            return result;
        }

        /** {@inheritDoc} */
        @Override
        protected void onPostExecute(ProcedureLoadResult result) {
            super.onPostExecute(result);
            handleResult(result);
        }

        protected void handleResult(ProcedureLoadResult result){
            requested--;
            hideProgressDialogFragment();
            if (result != null && result.success) {
                mProcedure = result.p;
                uEncounter = result.savedProcedureUri;
                logEvent(EventType.ENCOUNTER_LOAD_FINISHED, "");
                if (mProcedure != null){
                    mProcedure.setInstanceUri(uEncounter);
                    boolean useId = getActivity().getResources().getBoolean(
                            R.bool.display_input_element_id);
                    Log.d(TAG, "...Setting page display id=" + useId);
                    mProcedure.setShowQuestionIds(useId);
                    createView();
                }
                else
                    logEvent(EventType.ENCOUNTER_LOAD_FAILED, "Null procedure");

            } else {
                // Show error
                logEvent(EventType.ENCOUNTER_LOAD_FAILED, "");
                getActivity().finish();
            }

        }
    }

    /**
     * The result of loading a procedure
     * 
     * @author Sana Development Team
     */
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

        // Display progress dialog
        String message = String.format(getString(R.string.dialog_look_up_patient), patientId);
        showProgressDialogFragment(message);

        if (patientLookupTask == null || patientLookupTask.getStatus() == Status.FINISHED) {
            patientLookupTask = new PatientLookupTask(getActivity());
            patientLookupTask.setPatientLookupListener(this);
            patientLookupTask.execute(patientId);
        }
    }

    /**
     * Callback to handle when a patient look up succeeds. Will result in an
     * alert being displayed prompting the user to confirm that it is the
     * correct patient.
     */
    public void onPatientLookupSuccess(final PatientInfo patientInfo) {
        Log.i(TAG,"onPatientLookupSuccess(PatientInfo)");
        logEvent(EventType.ENCOUNTER_LOOKUP_PATIENT_SUCCESS, patientInfo.getPatientIdentifier());
        hideProgressDialogFragment();

        // TODO: should move error messages to BaseFragment
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
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.general_yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                mProcedure.setPatientInfo(patientInfo);
                                nextPage();
                            }
                        })
                .setNegativeButton(getResources().getString(R.string.general_no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                mProcedure.current().getPatientIdElement().setAndRefreshAnswer("");
                            }
                        });
        AlertDialog alert = builder.create();
        if (!getActivity().isFinishing())
            alert.show();
    }

    /**
     * Callback to handle when a patient look up fails. Will result in an alert
     * being displayed prompting the user to input whether the patient should be
     * considered new.
     */
    public void onPatientLookupFailure(final String patientIdentifier) {
        logEvent(EventType.ENCOUNTER_LOOKUP_PATIENT_FAILED, patientIdentifier);
        Log.e(TAG, "Couldn't lookup patient. They might exist, but we don't "
                + "have their details.");

        hideProgressDialogFragment();

        // TODO: should move error messages to BaseFragment
        StringBuilder message = new StringBuilder();
        message.append("Could not find patient record for ");
        message.append(patientIdentifier);
        message.append(". Entering a new patient. Continue?");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.general_yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                PatientInfo pi = new PatientInfo();
                                pi.setPatientIdentifier(patientIdentifier);
                                mProcedure.setPatientInfo(pi);
                                nextPage();
                            }
                        })
                .setNegativeButton(getResources().getString(R.string.general_no),
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
    
    protected void onUpdateAppState(Bundle inState){
    	Log.w(TAG, "onUpdateAppState(Bundle)");
    	if(inState == null){
        	Log.e(TAG, "onUpdateAppState(Bundle)" + " null bundle");
    		return;
    	}
    	Uri uri = Uri.EMPTY;
    	if(inState.containsKey(Intents.EXTRA_ENCOUNTER)){
    		uri = inState.getParcelable(Intents.EXTRA_ENCOUNTER);
    		if(!Uris.isEmpty(uri)) uEncounter = uri;
    	}
    	if(inState.containsKey(Intents.EXTRA_SUBJECT)){
    		uri = inState.getParcelable(Intents.EXTRA_SUBJECT);
			if(!Uris.isEmpty(uri)) uSubject = uri;
    	}
    	if(inState.containsKey(Intents.EXTRA_PROCEDURE)){
        	uri = inState.getParcelable(Intents.EXTRA_PROCEDURE);
    		if(!Uris.isEmpty(uri)) uProcedure = uri;
        }
    	if(inState.containsKey(Intents.EXTRA_OBSERVER)){
    		uri = inState.getParcelable(Intents.EXTRA_OBSERVER);
			if(!Uris.isEmpty(uri)) uObserver = uri;
    	}

    	if(inState.containsKey(Intents.EXTRA_TASK)){
    		uri = inState.getParcelable(Intents.EXTRA_TASK);
			if(!Uris.isEmpty(uri)) uTask = uri;
        }
		if(inState.containsKey("currentPage"))
			startPage = inState.getInt("currentPage");
		if(inState.containsKey("onDonePage"))
			onDonePage = inState.getBoolean("onDonePage");
    	dump();
    	Log.w(TAG, "onUpdateAppState(Bundle): EXIT");
    }
    
    protected void onUpdateAppState(Intent inState){
    	Log.w(TAG, "onUpdateAppState(Intent)");
    	dump();
    	Uri uri = Uri.EMPTY;
    	if(inState.hasExtra(Intents.EXTRA_ENCOUNTER)){
    		uri = inState.getParcelableExtra(Intents.EXTRA_ENCOUNTER);
        	Log.w(TAG, "onUpdateAppState(Intent) encounter uri -->" + uri);
    		if(!Uris.isEmpty(uri)) uEncounter = uri;
    	}
    	if(inState.hasExtra(Intents.EXTRA_SUBJECT)){
    		uri = inState.getParcelableExtra(Intents.EXTRA_SUBJECT);
        	Log.w(TAG, "onUpdateAppState(Intent) subject uri -->" + uri);
			if(!Uris.isEmpty(uri)) uSubject = uri;
    	}
        if(inState.hasExtra(Intents.EXTRA_PROCEDURE)){
            uri = inState.getParcelableExtra(Intents.EXTRA_PROCEDURE);
            Log.w(TAG, "onUpdateAppState(Intent) procedure uri -->" + uri);
            if(!Uris.isEmpty(uri)) uProcedure = uri;
        }
        if(inState.hasExtra(Intents.EXTRA_OBSERVER)){
    		uri = inState.getParcelableExtra(Intents.EXTRA_OBSERVER);
        	Log.w(TAG, "onUpdateAppState(Intent) observer uri -->" + uri);
			if(!Uris.isEmpty(uri)) uObserver = uri;
    	}
		if(inState.hasExtra(Intents.EXTRA_TASK)){
    		uri = inState.getParcelableExtra(Intents.EXTRA_TASK);
        	Log.w(TAG, "onUpdateAppState(Intent) task uri -->" + uri);
			if(!Uris.isEmpty(uri)) uTask = uri;
    	}

		if(inState.hasExtra("currentPage"))
			startPage = inState.getIntExtra("currentPage", 0);
		if(inState.hasExtra("onDonePage"))
			onDonePage = inState.getBooleanExtra("onDonePage", false);
		
    	dump();
    	Log.w(TAG, "onUpdateAppState(Intent): EXIT");
    }
    
    protected void dump(){
    	Logf.D(this.getClassTag(),"dump()", String.format("{ 'encounter': '%s',"
    			+" 'observer': '%s', 'subject': '%s', 'procedure': '%s', 'task': '%s' }",
    			uEncounter, uObserver, uSubject, uProcedure,uTask));
    }
    protected void dump(Class<?> klazz){
    	Logf.D(klazz.getSimpleName(),"dump()", String.format("{ 'encounter': '%s',"
    			+" 'observer': '%s', 'subject': '%s', 'procedure': '%s', 'task': '%s' }",
    			uEncounter, uObserver, uSubject, uProcedure, uTask));
    }
    protected String getClassTag(){
    	return BaseRunnerFragment.class.getSimpleName();
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
    	Log.w(TAG, "onSaveAppState()");
    	dump();
    	outState.putString(Keys.SESSION_KEY, mSessionKey);
    	outState.putParcelable(Intents.EXTRA_ENCOUNTER, uEncounter);
    	outState.putParcelable(Intents.EXTRA_SUBJECT, uSubject);
    	outState.putParcelable(Intents.EXTRA_PROCEDURE, uProcedure);
    	outState.putParcelable(Intents.EXTRA_OBSERVER, uObserver);
    	outState.putParcelable(Intents.EXTRA_TASK, uTask);
    	outState.putInt("currentPage", ((mProcedure != null)? mProcedure.getCurrentIndex():0));
    	outState.putBoolean("onDonePage", onDonePage);
    	dump();
    	Log.w(TAG, "onSaveAppState(): Exit");
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
    	dump();
    	outState.putExtra(Keys.SESSION_KEY, mSessionKey);
    	outState.putExtra(Intents.EXTRA_ENCOUNTER, uEncounter);
    	outState.putExtra(Intents.EXTRA_SUBJECT, uSubject);
    	outState.putExtra(Intents.EXTRA_PROCEDURE, uProcedure);
    	outState.putExtra(Intents.EXTRA_OBSERVER, uObserver);
    	outState.putExtra(Intents.EXTRA_TASK, uTask);
    	outState.putExtra("currentPage", ((mProcedure != null) ? mProcedure.getCurrentIndex() : 0));
    	outState.putExtra("onDonePage", onDonePage);
    }
    
    
    public Uri getData(){
    	return mData;
    }
    
    public void setData(Uri uri){
    	mData = uri;
    }
    
    public boolean setValue(int pageIndex, String elementId, String value){
    	return mProcedure.setValue(pageIndex, elementId, value);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState){
    	super.onSaveInstanceState(outState);
    	onSaveAppState(outState);
    }
    
    public final void makeText(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG);
    }
    
    public void onRestoreInstanceState(Bundle inState){
    	this.onUpdateAppState(inState);
    }
    
    public int getCurrentPage(){
    	return (mProcedure != null)? mProcedure.getCurrentIndex():0;
    }
    
    public boolean onDonePage(){
    	return onDonePage;
	}
    
    protected void showUploadingDialog(){

    	if(getActivity() instanceof BaseRunner){
    		BaseRunner runner = (BaseRunner) getActivity();
    		runner.setUploading(true);
    		runner.showUploadingDialog();
    	}
    }
    
    public void setProcedureListener(ProcedureListener listener){
        mProcedureListener = listener;
    }
    
    public Intent getResult(){
        Intent result = new Intent();
        onSaveAppState(result);
        return result;
    }
    
    public Intent getResult(String action){
        String uuid = ModelWrapper.getUuid(uEncounter,getActivity().getContentResolver());
        Uri uri = Uris.withAppendedUuid(Encounters.CONTENT_URI, uuid);
        uEncounter = uri;
        return getResult(action,uri);
    }

    public Intent getResult(String action, Uri uri){
        Intent result = new Intent(action,uri);
        onSaveAppState(result);
        return result;
    }

    public boolean isShowCompleteConfirmation() {
        return showCompleteConfirmation;
    }

    /**
     * Handles exit and clean up with no save.
     */
    protected void onExitNoSave(){
        // This quits when you hit back and have nowhere else to go back to.
        getActivity().setResult(Activity.RESULT_CANCELED, null);
        logEvent(EventType.ENCOUNTER_EXIT_NO_SAVE, "");
        getActivity().finish();
    }
}
