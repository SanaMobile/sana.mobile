
package org.sana.android.activity;

import java.util.concurrent.atomic.AtomicBoolean;

import org.sana.R;
import org.sana.net.Response;
import org.sana.android.app.Locales;
import org.sana.android.content.DispatchResponseReceiver;
import org.sana.android.content.Intents;
import org.sana.android.content.Uris;
import org.sana.android.db.ModelWrapper;
import org.sana.android.fragment.EncounterTaskListFragment;
import org.sana.android.fragment.EncounterTaskListCompleteFragment;
import org.sana.android.fragment.EncounterTaskListFragment.OnModelItemSelectedListener;
import org.sana.android.provider.EncounterTasks;
import org.sana.android.provider.Subjects;
import org.sana.android.util.Logf;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/** Activity for creating new and display existing patients. The resulting
 * patient selected or created, will be returned to the calling Activity.
 *
 * @author Sana Development Team */
public class EncounterTaskList extends FragmentActivity implements
        OnModelItemSelectedListener {

    public static final String TAG = EncounterTaskList.class.getSimpleName();

    /** Intent extra for a patient's ID. */
    public static final String EXTRA_PATIENT_ID = "extra_patient_id";

    public static final int INVALID_PATIENT_ID = -1;

    // Fragments
    private EncounterTaskListFragment mListFragment;
    private EncounterTaskListCompleteFragment mCompleteListFragment;

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onStart()");
        super.onCreate(savedInstanceState);
        Locales.updateLocale(this, getString(R.string.force_locale));
        setContentView(R.layout.encountertask_list_activity);
    }

    /** {@inheritDoc} */
    @Override
    public void onAttachFragment(Fragment fragment) {
        Log.d(TAG, "onStart()");
        super.onAttachFragment(fragment);
        Locales.updateLocale(this, getString(R.string.force_locale));
        if (fragment.getClass() == EncounterTaskListFragment.class) {
            mListFragment = (EncounterTaskListFragment) fragment;
            mListFragment.setOnModelItemSelectedListener(this);
            showProgressDialogFragment(getString(R.string.network_synchronizing));
        }
        else if (fragment.getClass() == EncounterTaskListCompleteFragment.class) {
            mCompleteListFragment = (EncounterTaskListCompleteFragment) fragment;
            mCompleteListFragment.setOnModelItemSelectedListener(this);
            showProgressDialogFragment(getString(R.string.network_synchronizing));;
        }
    }

    @Override
    public void onModelItemSelected(long id) {
        // A patient was selected so return to caller activity.
        Log.i(TAG, ".onModelItemSelected(): selected item: " + id);
        Bundle selected = mListFragment.getSelectedData(id);
        if(selected != null){
            Uri subj = selected.getParcelable(Intents.EXTRA_SUBJECT);
            Uri procedure = selected.getParcelable(Intents.EXTRA_PROCEDURE);
            Uri task = selected.getParcelable(Intents.EXTRA_TASK_ENCOUNTER);
            Intent data = new Intent();
            data.setDataAndType(task,EncounterTasks.CONTENT_ITEM_TYPE);
            data.putExtra(Intents.EXTRA_SUBJECT, subj);
            data.putExtra(Intents.EXTRA_PROCEDURE, procedure);
            data.putExtra(Intents.EXTRA_TASK, task);
            String status = selected.getString(EncounterTasks.Contract.STATUS);

            boolean complete = (status.compareToIgnoreCase("Completed") == 0)? true: false;
            if(complete)
                data.addCategory(Intents.CATEGORY_TASK_COMPLETE);
                // set the notification flag
            setResult(RESULT_OK, data);
            finish();
        } else {
            Log.w(TAG, "Why are we not binding position: " + id);
        }

    }

    @Override
    public void onPause(){
        super.onPause();
        if(mWaitDialog != null){
            mWaitDialog.dismiss();
            mWaitDialog = null;
        }
        LocalBroadcastManager.getInstance(this.getApplicationContext()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(mWaiting.get()){
            if(mWaitDialog != null){
                Logf.D(TAG, "mUploadingDialog != null && mUploading.get() = true");
                mWaitDialog.show();
            } else {
                Logf.D(TAG, "mUploadingDialog == null && mUploading.get() = true");
                mWaitDialog = new ProgressDialog(this);
                mWaitDialog.setTitle(R.string.general_upload_in_progress);
                mWaitDialog.setCancelable(false);
                mWaitDialog.show();
            }
        }
        LocalBroadcastManager.getInstance(this.getApplicationContext()).registerReceiver(mReceiver, buildFilter());

    }

    public IntentFilter buildFilter(){
        IntentFilter filter = new IntentFilter(Response.RESPONSE);
        try{
            filter.addDataType(EncounterTasks.CONTENT_TYPE);
            filter.addDataType(EncounterTasks.CONTENT_ITEM_TYPE);
            filter.addDataType(Subjects.CONTENT_TYPE);
        } catch (MalformedMimeTypeException e) {
        }
        return filter;
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.d(TAG, "onStart()");
        //bindService(new Intent(Intent.ACTION_SYNC, Subjects.CONTENT_URI), null, 0);
    }

    private static final int OPTION_SYNC_PATIENT = 1;
    private static final int OPTION_SYNC_TASKS = 2;
    private static final int OPTION_CLEAR = 3;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Locales.updateLocale(this, getString(R.string.force_locale));
        menu.add(0, OPTION_SYNC_PATIENT, 0, getString(R.string.title_synch_subjects));
        menu.add(0, OPTION_SYNC_TASKS, 1, getString(R.string.title_synch_tasks));
        //menu.add(0, OPTION_CLEAR, 2, "CLEAR");
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Uri observer = getIntent().getParcelableExtra(Intents.EXTRA_OBSERVER);
        switch (item.getItemId()) {
        case OPTION_SYNC_PATIENT:
            //getContentResolver().delete(EncounterTasks.CONTENT_URI, null,null);
            Log.i(TAG, "observer: " + observer);
            if(!Uris.isEmpty(observer)){
                Log.i(TAG, "observer: " + observer);
                mListFragment.sync(this, Subjects.CONTENT_URI);
            }
            return true;
        case OPTION_SYNC_TASKS:
            Log.i(TAG, "observer: " + observer);
            if(!Uris.isEmpty(observer)){
                String observerUuid = ModelWrapper.getUuid(observer, getContentResolver());
                Log.i(TAG, "observer: " + observer);
                Uri u = EncounterTasks.CONTENT_URI.buildUpon().appendQueryParameter("assigned_to__uuid",observerUuid).build();
                mListFragment.sync(this, u);
            }
            return true;

        case OPTION_CLEAR:
            getContentResolver().delete(EncounterTasks.CONTENT_URI, null,null);
            return true;
        }
        return false;
    }

    ProgressDialog mWaitDialog = null;
    AtomicBoolean mWaiting = new AtomicBoolean(false);

    void showProgressDialogFragment(String message) {
        if (mWaitDialog != null && mWaitDialog.isShowing()) {
            hideProgressDialogFragment();
        }
        // No need to create dialog if this is finishing
        if(isFinishing())
            return;

        mWaitDialog = new ProgressDialog(this);
        mWaitDialog.setMessage(message);
        mWaitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mWaitDialog.show();
        mWaiting.set(true);
    }

    void hideProgressDialogFragment() {
        mWaiting.set(false);
        if (mWaitDialog == null) {
            return;
        }
        // dismiss if finishing
        try{
            if(isFinishing())
                mWaitDialog.dismiss();
            else
                mWaitDialog.hide();
            } catch (Exception e){
                e.printStackTrace();
        }
    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            Log.d(TAG, "context: " + context.getClass().getSimpleName() + ", intent: " + intent.toUri(Intent.URI_INTENT_SCHEME));
            onDispatchResult(intent);
          }
        };

    public final void onDispatchResult(Intent intent){
        Log.i(TAG,"onDispatchResult()");
        String text = intent.hasExtra(Response.MESSAGE)? intent.getStringExtra(DispatchResponseReceiver.KEY_RESPONSE_MESSAGE): "Upload Result Received: " + intent.getDataString();
        int result = intent.getIntExtra(Response.CODE, 400);
        Uri uri = intent.getData();
        Uri observer = getIntent().getParcelableExtra(Intents.EXTRA_OBSERVER);

        int descriptor = Uris.getContentDescriptor(uri);
        Log.d(TAG, "....result: " + result );
        Log.d(TAG, "....descriptor: " + descriptor );
        if(result == 200){
            Log.d(TAG, ".... got 200");
            switch(descriptor){
            case Uris.SUBJECT:
                Log.d(TAG, ".... got subject 200");
                String observerUuid = ModelWrapper.getUuid(observer, getContentResolver());
                Uri u = EncounterTasks.CONTENT_URI.buildUpon().appendQueryParameter("assigned_to__uuid",observerUuid).build();
                mListFragment.sync(this, u);
            break;
            case Uris.ENCOUNTER_TASK:
                Log.d(TAG, ".... got EncounterTask 200");
                hideProgressDialogFragment();
            break;
            }
            return;
        } else if(result == 100){
            Log.d(TAG, ".... got 100");
            switch(descriptor){
            case Uris.SUBJECT:
                Log.d(TAG, ".... got subject 100");
            break;
            case Uris.ENCOUNTER_TASK:
                Log.d(TAG, ".... got EncounterTask 100");
            break;
            }
            return;
        } else if(result == 400){
            Log.e(TAG, ".... got error");
            hideProgressDialogFragment();
            Toast.makeText(this, R.string.network_synch_error,
                    Toast.LENGTH_LONG);
        } else {
            hideProgressDialogFragment();
        }
    }
}
