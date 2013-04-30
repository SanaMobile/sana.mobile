
package org.sana.android.activity;

import org.sana.R;
import org.sana.android.provider.Patients;
import org.sana.android.app.State.Keys;
import org.sana.android.fragment.PatientListFragment;
import org.sana.android.fragment.PatientListFragment.OnPatientSelectedListener;
import org.sana.android.util.SanaUtil;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/** Activity for creating new and display existing patients. The resulting
 * patient selected or created, will be returned to the calling Activity.
 * 
 * @author Sana Development Team */
public class PatientsList extends SherlockFragmentActivity implements
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_list_activity);
    }

    /** {@inheritDoc} */
    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
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
        getSupportMenuInflater().inflate(R.menu.patients_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /** {@inheritDoc} */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_new_patient:
                registerNewPatient();
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
        startActivityForResult(intent, CREATE_PATIENT);
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
        setResult(RESULT_OK, data);
        finish();
    }

}
