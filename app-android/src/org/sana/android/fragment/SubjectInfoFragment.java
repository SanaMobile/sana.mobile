package org.sana.android.fragment;

import org.sana.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment that displays information about a patient.
 * @author Sana Dev Team
 */
public class SubjectInfoFragment extends BaseFragment {

    /** Bundle extra for a subject's ID. */
    public static final String EXTRA_SUBJECT_ID = "extra_subject_id";
    
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
    public static SubjectInfoFragment newInstance(long subjectId) {
        SubjectInfoFragment frag = new SubjectInfoFragment();
        Bundle args = new Bundle();
        args.putLong(EXTRA_SUBJECT_ID, subjectId);
        frag.setArguments(args);
        return frag;
    }
    
}
