
package org.sana.android.activity;

import org.sana.R;
import org.sana.android.app.Locales;
import org.sana.android.content.Intents;
import org.sana.android.content.Uris;
import org.sana.android.db.ModelWrapper;
import org.sana.android.fragment.EncounterTaskListFragment;
import org.sana.android.fragment.EncounterTaskListFragment.OnModelItemSelectedListener;
import org.sana.android.provider.EncounterTasks;
import org.sana.android.provider.Patients;
import org.sana.android.provider.Subjects;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

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
        }
    }

    @Override
    public void onModelItemSelected(long id) {
        // A patient was selected so return to caller activity.
        //Intent data = getIntent();
		Log.i(TAG, ".onModelItemSelected(): selected item: " + id);
    	Bundle selected = mListFragment.getSelectedData(id - 1);
    	if(selected != null){
    		Uri subj = selected.getParcelable(Intents.EXTRA_SUBJECT);
    		Uri procedure = selected.getParcelable(Intents.EXTRA_PROCEDURE);
    		Uri uri = ContentUris.withAppendedId(EncounterTasks.CONTENT_URI,id);
    		Intent data = new Intent();
    		data.setDataAndType(uri,EncounterTasks.CONTENT_ITEM_TYPE);
    		data.putExtra(Intents.EXTRA_SUBJECT, subj);
    		data.putExtra(Intents.EXTRA_PROCEDURE, procedure);
    		data.putExtra(Intents.EXTRA_TASK_ENCOUNTER, uri);
    		setResult(RESULT_OK, data);
    		finish();
    	} else {
    		Log.w(TAG, "Why are we not binding position: " + id);
    	}
    	
    }
 
    @Override
    public void onStart(){
    	super.onStart();
    	Log.d(TAG, "onStart()");
    	//bindService(new Intent(Intent.ACTION_SYNC, Subjects.CONTENT_URI), null, 0);
    }
    
    private static final int OPTION_SYNC_PATIENT = 1;
    private static final int OPTION_SYNC_TASKS = 2;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	Locales.updateLocale(this, getString(R.string.force_locale));
		menu.add(0, OPTION_SYNC_PATIENT, 2, getString(R.string.title_synch_subjects));
		menu.add(0, OPTION_SYNC_TASKS, 2, getString(R.string.title_synch_tasks));
        return true;
    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Uri observer = getIntent().getParcelableExtra(Intents.EXTRA_OBSERVER);
        switch (item.getItemId()) {
        case OPTION_SYNC_PATIENT:
        	getContentResolver().delete(EncounterTasks.CONTENT_URI, null,null);
        	Uri syncUri = EncounterTasks.CONTENT_URI;
            Log.i(TAG, "observer: " + observer);
            if(!Uris.isEmpty(observer)){
            	String observerUuid = ModelWrapper.getUuid(observer, getContentResolver());
                Log.i(TAG, "observer: " + observer);
            	Uri u = syncUri.buildUpon().appendQueryParameter("assigned_to__uuid",observerUuid).build();
            	mListFragment.sync(this, Patients.CONTENT_URI);
            }
			return true;
        case OPTION_SYNC_TASKS:
        	getContentResolver().delete(EncounterTasks.CONTENT_URI, null,null);
            Log.i(TAG, "observer: " + observer);
            if(!Uris.isEmpty(observer)){
            	String observerUuid = ModelWrapper.getUuid(observer, getContentResolver());
                Log.i(TAG, "observer: " + observer);
            	Uri u = EncounterTasks.CONTENT_URI.buildUpon().appendQueryParameter("assigned_to__uuid",observerUuid).build();
            	mListFragment.sync(this, u);
            }
			return true;
        }    
        return false;
    }
}
