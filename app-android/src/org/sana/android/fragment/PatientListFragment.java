package org.sana.android.fragment;

import org.sana.R;
import org.sana.android.activity.PatientsList;
import org.sana.android.provider.Patients;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;

/**
 * Fragment displaying all patients.
 *
 * @author Sana Development Team
 */
public class PatientListFragment extends ListFragment implements LoaderCallbacks<Cursor> {

    private static final int PATIENTS_LOADER = 0;
    
    private PatientsList mActivity;
    private Uri mUri;
    private SimpleCursorAdapter mAdapter;
    private OnPatientSelectedListener mListener;
    
    /** {@inheritDoc} */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        mActivity = (PatientsList) getActivity();
        
        mUri = mActivity.getIntent().getData();
        if (mUri == null) {
            mUri = Patients.CONTENT_URI;
        }
        
        mAdapter = new SimpleCursorAdapter(mActivity, 
                R.layout.patient_list_row, null, 
                new String[] { Patients.Contract.FAMILY_NAME, 
                               Patients.Contract.GIVEN_NAME },
                new int[] { R.id.last_name, R.id.first_name }, 
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        setListAdapter(mAdapter);
                                   
        getLoaderManager().initLoader(PATIENTS_LOADER, null, this);
    }

    /** {@inheritDoc} */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mListener != null) {
            mListener.onPatientSelected(id);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        CursorLoader loader = new CursorLoader(mActivity, mUri, 
        		Patients.Projection.DISPLAY_NAME,
                null, null, Patients.DEFAULT_SORT_ORDER);
        return loader;
    }

    /** {@inheritDoc} */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        
        if (cursor == null || cursor.getCount() == 0) {
            setEmptyText(getString(R.string.msg_no_patients));
        }
        mAdapter.swapCursor(cursor);
        mAdapter.notifyDataSetChanged();
    }

    /** {@inheritDoc} */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    /**
     * Events specific to this PatientListFragment
     *
     * @author Sana Development Team
     */
    public interface OnPatientSelectedListener {
        /** Callback when a patient is selected in the list. 
         * @param patientId The selected patient's ID.
         */
        public void onPatientSelected(long patientId);
    }
    
    /**
     * Sets a listener to this fragment.
     * 
     * @param listener
     */
    public void setOnPatientSelectedListener(OnPatientSelectedListener listener) {
        mListener = listener;
    }
}
