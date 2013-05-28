package org.sana.android.fragment;

import org.sana.R;
import org.sana.android.activity.BaseActivity;
import org.sana.android.content.core.PatientWrapper;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment that displays information about a patient.
 * @author Sana Dev Team
 */
public class SubjectInfoFragment extends BaseFragment {

    public static final String TAG = SubjectInfoFragment.class.getSimpleName();
    
    /** Bundle extra for a subject's ID. */
    public static final String EXTRA_SUBJECT_ID = "extra_subject_id";
    
    /** {@inheritDoc} */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        Uri subjectUri = ((BaseActivity) getActivity()).getSubject();
//        Cursor c = getActivity().getContentResolver()
//                .query(subjectUri, null, null, null, null);
//        PatientWrapper patientWrapper = new PatientWrapper(c);
//        
//        Log.i(TAG, "Given name: " + patientWrapper.getGiven_name());
//        Log.i(TAG, "Family name: " + patientWrapper.getFamily_name());
//        Log.i(TAG, "Gender: " + patientWrapper.getGender());
    }

    /** {@inheritDoc} */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.patient_info_fragment, null);
    }

    /**
     * Creates a PatientInfoFragment for the given patient provided by subjectId
     * @param subjectId
     * @return
     */
    public static SubjectInfoFragment newInstance() {
        SubjectInfoFragment frag = new SubjectInfoFragment();
        return frag;
    }
    
}
