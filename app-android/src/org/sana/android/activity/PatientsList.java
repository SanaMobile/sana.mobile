
package org.sana.android.activity;

import org.sana.R;
import org.sana.android.app.Locales;
import org.sana.android.content.Intents;
import org.sana.android.fragment.PatientListFragment;
import org.sana.android.fragment.PatientListFragment.OnPatientSelectedListener;
import org.sana.android.provider.Patients;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Subjects;
import org.sana.android.service.impl.DispatchService;
import org.sana.android.util.SanaUtil;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/** Activity for creating new and display existing patients. The resulting
 * patient selected or created, will be returned to the calling Activity.
 * 
 * @author Sana Development Team */
public class PatientsList extends FragmentActivity implements
        OnPatientSelectedListener {

    public static final String TAG = PatientsList.class.getSimpleName();

    /** Intent extra for a patient's ID. */
    public static final String EXTRA_PATIENT_ID = "extra_patient_id";

    public static final int INVALID_PATIENT_ID = -1;

    // Activity request codes
    /** Intent request code for creating a new patient. */
    private static final int CREATE_PATIENT = 2;

    // Fragments
    private PatientListFragment mFragmentPatientList;

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "onStart()");
        super.onCreate(savedInstanceState);
    	Locales.updateLocale(this, getString(R.string.force_locale));
        setContentView(R.layout.patient_list_activity);
    }

    /** {@inheritDoc} */
    @Override
    public void onAttachFragment(Fragment fragment) {
    	Log.d(TAG, "onStart()");
        super.onAttachFragment(fragment);
    	Locales.updateLocale(this, getString(R.string.force_locale));
        if (fragment.getClass() == PatientListFragment.class) {
            mFragmentPatientList = (PatientListFragment) fragment;
            mFragmentPatientList.setOnPatientSelectedListener(this);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        SanaUtil.logActivityResult(TAG, requestCode, resultCode);
        switch (requestCode) {
            case CREATE_PATIENT:
                // TODO
                if (resultCode == RESULT_OK) {
                } else {
                }
                break;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.patients_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /** {@inheritDoc} */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_new_patient:
                registerNewPatient();
                return true;
            case R.id.menu_sync_patients:
            	mFragmentPatientList.sync(this, Patients.CONTENT_URI);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Starts PatientRunnerFragment for creating a new patient.
    private void registerNewPatient() {
        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intent.setType(Patients.CONTENT_TYPE);
        intent.setData(Patients.CONTENT_URI);
        Toast.makeText(this, "Not available.", Toast.LENGTH_LONG);
        // startActivityForResult(intent, CREATE_PATIENT);
    }

    /** {@inheritDoc} */
    @Override
    public void onPatientSelected(long patientId) {
        // A patient was selected so return to caller activity.
        //Intent data = getIntent();
    	Uri uri = ContentUris.withAppendedId(Patients.CONTENT_URI,patientId);
        Intent data = new Intent();
        data.setDataAndType(uri,Patients.CONTENT_ITEM_TYPE);
        data.putExtra(EXTRA_PATIENT_ID, patientId);
        data.putExtra(Intents.EXTRA_SUBJECT, uri);
        setResult(RESULT_OK, data);
        finish();
    }
 
    @Override
    public void onStart(){
    	super.onStart();
    	Log.d(TAG, "onStart()");
    	//bindService(new Intent(Intent.ACTION_SYNC, Subjects.CONTENT_URI), null, 0);
    }

    Binder mBinder = null;
    boolean mBound = false;
 	protected ServiceConnection mConnection = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBinder = (Binder) service;
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBinder = null;
			mBound = false;
		}
 		
 	};
}
