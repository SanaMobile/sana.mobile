package org.sana.android.activity;

import org.sana.R;
import org.sana.android.db.SanaDB.PatientSQLFormat;
import org.sana.android.fragment.PatientListFragment;
import org.sana.android.fragment.PatientListFragment.OnPatientSelectedListener;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Contains Fragment for displaying all patients.
 *
 * @author Sana Development Team
 */
public class PatientsList extends FragmentActivity implements OnPatientSelectedListener {

    // Options menu code
    private static final int OPTIONS_NEW_PATIENT = 1;
    
    // Activity request codes
    /** Itent request code for creating a new patient. */
    private static final int CREATE_PATIENT = 2;
    
    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_list_activity);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment.getClass() == PatientListFragment.class) {
            ((PatientListFragment) fragment).setOnPatientSelectedListener(this);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, OPTIONS_NEW_PATIENT, 0, R.string.menu_new_patient);
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case OPTIONS_NEW_PATIENT:
                newPatient();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /** Starts Activity for creating a new patient */
    private void newPatient() {
        Intent i = new Intent(Intent.ACTION_INSERT);
        i.setType(PatientSQLFormat.CONTENT_TYPE);
        i.setData(PatientSQLFormat.CONTENT_URI);
        startActivityForResult(i, CREATE_PATIENT);
    }

    @Override
    public void onPatientSelected(long patientId) {
        // TODO : what happens after patient is selected/created?
        setResult(RESULT_OK);
        finish();
    }
    
}
