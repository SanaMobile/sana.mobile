
package org.sana.android.fragment;

import java.util.Locale;

import org.sana.R;
import org.sana.android.app.Locales;
import org.sana.android.content.core.PatientWrapper;
import org.sana.android.provider.Patients;
import org.sana.android.provider.Patients.Contract;
import org.sana.android.util.Logf;

import org.sana.util.StringUtil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.Log;
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
    public static final String TAG = PatientListFragment.class.getSimpleName();

    private static final int PATIENTS_LOADER = 0;
    static final String[] mProjection = new String[] {
		Contract._ID, 
		Contract.GIVEN_NAME, 
		Contract.FAMILY_NAME, 
		Contract.PATIENT_ID,
		Contract.LOCATION,
		Contract.IMAGE
		};
    
    private Uri mUri;
    private PatientCursorAdapter mAdapter;
    private SimpleCursorAdapter uAdapter;
    private OnPatientSelectedListener mListener;
    Handler mHandler; 
    private boolean doSync = false;
    //
    // Activity Methods
    //

    /** {@inheritDoc} */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
    	Locales.updateLocale(this.getActivity(), getString(R.string.force_locale));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	Log.d(TAG, "onActivityCreated()");
        super.onActivityCreated(savedInstanceState);
        
        // signal the dispatcher to sync
        mUri = getActivity().getIntent().getData();
        if (mUri == null) {
            mUri = Patients.CONTENT_URI;
        }
    	Log.d(TAG, "onActivityCreated(): sync?");
        getLoaderManager().initLoader(PATIENTS_LOADER, null, this);
        mAdapter = new PatientCursorAdapter(getActivity(), null, 0);
        setListAdapter(mAdapter);
        
        if(!doSync){
        	sync(getActivity(),mUri);
        	doSync = true;
        }
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
    	Log.d(TAG, "onCreateLoader() "); 
        CursorLoader loader = new CursorLoader(getActivity(), 
        		mUri,
        		mProjection,
                null, null, Patients.GIVEN_NAME_SORT_ORDER);
        return loader;
    }

    /** {@inheritDoc} */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    	Log.d(TAG, "onLoadFinished() "); 

        if (cursor == null || (cursor !=null && cursor.getCount() == 0)) {
            setEmptyText(getString(R.string.msg_no_patients));
        }
        //mAdapter.swapCursor(cursor);
        ((PatientCursorAdapter) this.getListAdapter()).swapCursor(cursor);
    }

    /** {@inheritDoc} */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    	//mAdapter.swapCursor(null);
    	((PatientCursorAdapter) this.getListAdapter()).swapCursor(null);
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

    	static class ViewHolder {
    		TextView name;
    		ImageView image;
    		TextView systemId;
    		TextView location;
    		TextView textSection;
    		View header;
    	}
    	
    	//private final Activity context;
        private int[] mRowStates;
        private AlphabetIndexer mAlphaIndexer;
        private PatientWrapper mWrapper;
        private final LayoutInflater mInflater;
        
        private static final int STATE_UNKNOWN = 0;
        private static final int STATE_LABELED = 1;
        private static final int STATE_UNLABELED = 2;
        
        private static final String ALPHABET = " ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        /*
        public PatientCursorAdapter(Context context) {
            super(context, null, 0);
            		//CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            //this.context = context;
            mInflater = LayoutInflater.from(context);
        }
		*/
        public PatientCursorAdapter(Context context, Cursor c, int flags) {
        	super(context,c, flags);
        	/*
            super(context, R.layout.patient_list_item, c, 
            		Patients.Projection.DISPLAY_NAME,
            		new int[]{R.id.name, R.id.system_id, R.id.location, R.id.image},
            		CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            */
            //this.context = context;
            mInflater = LayoutInflater.from(context);
            init(c);
        }
        
        private void init(Cursor c) {
            if (c == null) {
                return;
            }
            //mWrapper = new PatientWrapper(c);
            //c.setNotificationUri(context.getContentResolver(), Patients.CONTENT_URI);
            mRowStates = new int[c.getCount()];
            mAlphaIndexer = new AlphabetIndexer(c, 
                    c.getColumnIndex(Patients.Contract.GIVEN_NAME), 
                    ALPHABET);
        }
        
        @Override
        public Cursor swapCursor(Cursor newCursor) {
        	Log.d(TAG+".mAdapter", "swap cursor "); 
            init(newCursor);
            return super.swapCursor(newCursor);
        }
        
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
        	Log.d(TAG+".mAdapter", "binding view "); 
        	Log.d(TAG+".mAdapter", "bindView(): cursor position: " + cursor.getPosition());             
            // Set patient name and image
            ImageView image = (ImageView)view.findViewById(R.id.image);
            String imagePath = cursor.getString(cursor.getColumnIndex(Patients.Contract.IMAGE));
            
            if(imagePath != null){
            	try{
            		image.setImageURI(Uri.parse(imagePath));
            	} catch (Exception e){
                	image.setImageResource(R.drawable.unknown);
            	}
            } else {
            	image.setImageResource(R.drawable.unknown);
            }
            
        	//image.setImageResource(R.drawable.unknown);

            String familyName = cursor.getString(cursor.getColumnIndex(Patients.Contract.FAMILY_NAME));
            String givenName = cursor.getString(cursor.getColumnIndex(Patients.Contract.GIVEN_NAME));
            String displayName = StringUtil.formatPatientDisplayName(givenName, familyName);
            TextView name = (TextView) view.findViewById(R.id.name);
            name.setText(displayName);
            
            TextView systemId = (TextView)view.findViewById(R.id.system_id);
            String id = cursor.getString(cursor.getColumnIndex(Patients.Contract.PATIENT_ID));
            //String id = mWrapper.getStringField(Contract.PATIENT_ID);
            systemId.setText((TextUtils.isEmpty(id)? "000000":id));
            
            TextView location = (TextView)view.findViewById(R.id.location);
            String locationVal = cursor.getString(cursor.getColumnIndex(Patients.Contract.LOCATION));
            location.setText(locationVal);
            
            
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
                        String prevName = StringUtil.formatPatientDisplayName(givenName, familyName);
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
        	Log.d(TAG+".mAdapter", "new view ");
            View view = mInflater.inflate(R.layout.patient_list_item, null);
            //bindView(view, context, cursor);
            return view;
        }
        
    }
    
    final void sync(Context context, Uri uri){
    	Logf.D(TAG, "sync()");
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    	long lastSync = prefs.getLong("patient_sync", 0);
    	long now = System.currentTimeMillis();
    	Log.d(TAG, "last: " + lastSync +", now: " + now+ ", delta: " + (now-lastSync));
    	if((now - lastSync) > 86400000){
    		prefs.edit().putLong("patient_sync", now).commit();
    		Intent intent = new Intent(context.getString(R.string.intent_action_read),uri);
    		context.startService(intent);
    	}
    	doSync = false;
    }
}
