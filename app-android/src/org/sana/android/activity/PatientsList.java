
package org.sana.android.activity;

import org.sana.R;
import org.sana.android.fragment.PatientListFragment;
import org.sana.android.fragment.PatientListFragment.OnPatientSelectedListener;
import org.sana.android.fragment.PatientRunnerFragment;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/** Activity for creating new and display existing patients. The resulting
 * patient selected or created, will be returned to the calling Activity.
 * 
 * @author Sana Development Team */
public class PatientsList extends SherlockFragmentActivity implements
        OnPatientSelectedListener {

    /** Intent extra for a patient's ID. */
    public static final String EXTRA_PATIENT_ID = "extra_patient_id";

    public static final int INVALID_PATIENT_ID = -1;

    // Activity request codes
    /** Intent request code for creating a new patient. */
    // private static final int CREATE_PATIENT = 2;

    // Fragments
    private PatientListFragment mFragmentPatientList;
    private PatientRunnerFragment mFragmentPatientRunner;

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_list_activity);

        if (savedInstanceState == null) {
            mFragmentPatientList = new PatientListFragment();
        } else {
            mFragmentPatientList = (PatientListFragment) getSupportFragmentManager()
                    .findFragmentByTag(PatientListFragment.class.toString());
        }
        mFragmentPatientList.setOnPatientSelectedListener(this);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mFragmentPatientList,
                        PatientListFragment.class.toString())
                .commit();
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
        // Intent i = new Intent(Intent.ACTION_INSERT);
        // i.setType(PatientSQLFormat.CONTENT_TYPE);
        // i.setData(PatientSQLFormat.CONTENT_URI);
        // startActivityForResult(i, CREATE_PATIENT);
        // TODO
    }

    /** {@inheritDoc} */
    @Override
    public void onPatientSelected(long patientId) {
        // A patient was selected so return to caller activity.
        Intent data = getIntent();
        data.putExtra(EXTRA_PATIENT_ID, patientId);
        setResult(RESULT_OK, data);
        finish();
    }

}
