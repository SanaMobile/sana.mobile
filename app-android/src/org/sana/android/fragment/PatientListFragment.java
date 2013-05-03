
package org.sana.android.fragment;

import java.util.Locale;

import org.sana.R;
import org.sana.android.content.core.PatientWrapper;
import org.sana.android.provider.Patients;
import org.sana.util.StringUtil;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Fragment displaying all patients.
 * 
 * @author Sana Development Team
 */
public class PatientListFragment extends ListFragment implements LoaderCallbacks<Cursor> {

    private static final int PATIENTS_LOADER = 0;

    private Uri mUri;
    private PatientCursorAdapter mAdapter;
    private OnPatientSelectedListener mListener;

    //
    // Activity Methods
    //

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

        mUri = getActivity().getIntent().getData();
        if (mUri == null) {
            mUri = Patients.CONTENT_URI;
        }

        mAdapter = new PatientCursorAdapter(getActivity(), null);
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

    //
    // Loader Callbacks
    //

    /** {@inheritDoc} */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        CursorLoader loader = new CursorLoader(getActivity(), mUri, Patients.Projection.DISPLAY_NAME,
                null, null, Patients.GIVEN_NAME_SORT_ORDER);
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
        /**
         * Callback when a patient is selected in the list.
         * 
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

    /**
     * Adapter for patient information
     * 
     * @author Sana Development Team
     */
    public static class PatientCursorAdapter extends CursorAdapter {

        private int[] mRowStates;
        private AlphabetIndexer mAlphaIndexer;
        private PatientWrapper mWrapper;
        
        private static final int STATE_UNKNOWN = 0;
        private static final int STATE_LABELED = 1;
        private static final int STATE_UNLABELED = 2;
        
        private static final String ALPHABET = " ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        
        public PatientCursorAdapter(Context context, Cursor c) {
            super(context, c, true);
            init(c);
        }

        private void init(Cursor c) {
            if (c == null) {
                return;
            }
            mWrapper = new PatientWrapper(c);
            mRowStates = new int[c.getCount()];
            mAlphaIndexer = new AlphabetIndexer(c, 
                    c.getColumnIndex(Patients.Contract.GIVEN_NAME), 
                    ALPHABET);
        }
        
        @Override
        public Cursor swapCursor(Cursor newCursor) {
            init(newCursor);
            return super.swapCursor(newCursor);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            
            // Set patient name and image
            ImageView image = (ImageView)view.findViewById(R.id.image);
            String imageUri = mWrapper.getImage();
            if(!TextUtils.isEmpty(imageUri)){
            	image.setImageURI(Uri.parse(imageUri));
            } else {
            	image.setImageResource(R.drawable.unknown);
            }
            
            String displayName = StringUtil.formatPatientDisplayName(mWrapper.getGiven_name(), mWrapper.getFamily_name());
            TextView name = (TextView) view.findViewById(R.id.name);
            name.setText(displayName);
            
            // Alphabet divider
            boolean needsSeparator = false;
            int pos = cursor.getPosition();

            displayName = displayName.trim();
            if (!TextUtils.isEmpty(displayName)) {
                displayName = displayName.substring(0, 1).toLowerCase(Locale.getDefault());
            } else {
                displayName = " ";
            }

            switch (mRowStates[pos]) {
                case STATE_LABELED:
                    needsSeparator = true;
                    break;
                case STATE_UNLABELED:
                    needsSeparator = false;
                    break;
                case STATE_UNKNOWN:
                default:
                    // First cell always needs to be sectioned
                    if (pos == 0) {
                        needsSeparator = true;
                    } else {
                        cursor.moveToPosition(pos - 1);

                        String prevName = StringUtil.formatPatientDisplayName(mWrapper.getGiven_name(), mWrapper.getFamily_name());
                        prevName = prevName.trim();
                        if (!TextUtils.isEmpty(prevName)) {
                            prevName = prevName.substring(0, 1).toLowerCase(Locale.getDefault());
                        } else {
                            prevName = " ";
                        }

                        if (prevName.charAt(0) != displayName.charAt(0)) {
                            needsSeparator = true;
                        }

                        cursor.moveToPosition(pos);
                    }
                    break;
            }

            if (needsSeparator) {
                view.findViewById(R.id.header).setVisibility(View.VISIBLE);
                TextView label = (TextView) view.findViewById(R.id.txt_section);
                label.setText(("" + displayName.charAt(0)).toUpperCase(Locale.getDefault()));
            } else {
                view.findViewById(R.id.header).setVisibility(View.GONE);
            }

        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.patient_list_item, null);
            bindView(view, context, cursor);
            return view;
        }

    }
}
