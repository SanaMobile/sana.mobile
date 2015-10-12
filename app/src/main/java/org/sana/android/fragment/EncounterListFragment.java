
package org.sana.android.fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.joda.time.DateTime;
import org.sana.R;
import org.sana.android.Constants;
import org.sana.android.app.Locales;
import org.sana.android.app.Preferences;
import org.sana.android.content.DispatchResponseReceiver;
import org.sana.android.content.Intents;
import org.sana.android.content.Uris;
import org.sana.android.content.core.PatientWrapper;
import org.sana.android.db.SanaDB.ImageSQLFormat;
import org.sana.android.db.ModelWrapper;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Observations;
import org.sana.android.provider.Patients;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Subjects;
import org.sana.android.service.impl.DispatchService;
import org.sana.android.service.QueueManager;
import org.sana.android.util.Dates;
import org.sana.android.util.Logf;
import org.sana.android.util.SanaUtil;
import org.sana.api.IModel;
import org.sana.core.Encounter;

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
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Fragment displaying all patients.
 *
 * @author Sana Development Team
 */
public class EncounterListFragment extends ListFragment implements LoaderCallbacks<Cursor> {
    public static final String TAG = EncounterListFragment.class.getSimpleName();

    static final SimpleDateFormat sdf = new SimpleDateFormat(IModel.DATE_FORMAT,
        Locale.US);
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd",
        Locale.US);

    static final String[] mProjection = new String[] {
            Encounters.Contract._ID,
            Encounters.Contract.UUID,
            Encounters.Contract.PROCEDURE,
            Encounters.Contract.SUBJECT,
            Encounters.Contract.STATE,
            Encounters.Contract.UPLOAD_STATUS,
            Encounters.Contract.UPLOAD_QUEUE,
            Encounters.Contract.CREATED,
            Encounters.Contract.FINISHED };

    // Once a day 86400000
    long delta = 1000;
    private Uri mUri;
    private EncounterCursorAdapter mAdapter;
    private OnModelItemSelectedListener mListener;
    Handler mHandler;
    private boolean doSync = false;
    String[] months;
    LongSparseArray<Bundle> mData = new LongSparseArray<Bundle>();

    //
    // Activity Methods
    //

    /** {@inheritDoc} */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        Locales.updateLocale(this.getActivity(), getString(R.string.force_locale));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated()");
        super.onActivityCreated(savedInstanceState);
        Locale locale = new Locale(Preferences.getString(getActivity(),
                Constants.PREFERENCE_LOCALE, "EN"));
        df = new SimpleDateFormat(
                getString(R.string.display_date_format),
                locale);

        Locales.updateLocale(getActivity(), locale);
        months = getActivity().getResources().getStringArray(R.array.months_long_format);
        // signal the dispatcher to sync
        mUri = getActivity().getIntent().getData();
        if (mUri == null) {
            mUri = Encounters.CONTENT_URI;
        }
        mAdapter = new EncounterCursorAdapter(getActivity(), null, 0);
        setListAdapter(mAdapter);
        LoaderManager.enableDebugLogging(true);
        getActivity().getSupportLoaderManager().initLoader(Uris.ENCOUNTER_DIR, null, this);
    }

    /** {@inheritDoc} */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mListener != null) {
            mListener.onModelItemSelected(position);
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
                null,
                null,
                Encounters.Contract.CREATED + " DESC");
        return loader;
    }

    /** {@inheritDoc} */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished() ");

        if (cursor == null || (cursor !=null && cursor.getCount() == 0)) {
            setEmptyText(getString(R.string.msg_no_encounters));
        }
        if(cursor != null)
            ((EncounterCursorAdapter) this.getListAdapter()).swapCursor(cursor);
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.support.v4.content.Loader)
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset() ");
        ((EncounterCursorAdapter) this.getListAdapter()).swapCursor(null);
    }

    /**
     * Events specific to this PatientListFragment
     *
     * @author Sana Development Team
     */
    public interface OnModelItemSelectedListener {
        /**
         * Callback when a patient is selected in the list.
         *
         * @param id The selected patient's ID.
         */
        public void onModelItemSelected(long id);
    }

    /**
     * Sets a listener to this fragment.
     *
     * @param listener
     */
    public void setOnModelItemSelectedListener(OnModelItemSelectedListener listener) {
        mListener = listener;
    }

    static class ViewHolder{
        ImageView image;
        TextView name;
        TextView systemId;
        TextView location;
        TextView label;
        int position = 1;
        Encounter task = null;
    }

    public String getObserver(){
        Uri obsUri = getActivity().getIntent().getParcelableExtra(Intents.EXTRA_OBSERVER);
        String observer = ModelWrapper.getUuid(obsUri, getActivity().getContentResolver());
        return (TextUtils.isEmpty(observer))? "": observer;
    }

    //TODO try reading from intent
    public String getSelectedStatus(){
        return "Assigned";
    }

    public String getSelection(){
        return null;
    }

    /**
     * @author Sana Development Team
     */
    public class EncounterCursorAdapter extends CursorAdapter{

        private final LayoutInflater mInflater;
        private ArrayList<Boolean> itemChecked = new ArrayList<Boolean>();
        String[] months;
        public EncounterCursorAdapter(Context context, Cursor c) {
            super(context.getApplicationContext(),c,false);
            mInflater = LayoutInflater.from(context);
            for (int i = 0; i < this.getCount(); i++) {
                itemChecked.add(i, false); // initializes all items value with false
            }
        }

        public EncounterCursorAdapter(Context context) {
            this(context, null, 0);
        }

        public EncounterCursorAdapter(Context context, Cursor c, int flags) {
            super(context,c, flags);
            mInflater = LayoutInflater.from(context);
        }


        @Override
        public void changeCursor (Cursor cursor){
            // need to update to checked records
            updateChecked(cursor);
            super.changeCursor(cursor);
        }

        @Override
        public Cursor swapCursor(Cursor newCursor) {
            // need to update to checked records
            updateChecked(newCursor);
            return super.swapCursor(newCursor);
        }

        private void updateChecked(Cursor newCursor){
            Log.i(TAG, "updateChecked()");
            int sizeOf = (newCursor != null)? newCursor.getCount(): 0;
            int sizeOld = itemChecked.size();
            ArrayList<Boolean> checked = new ArrayList<Boolean>(sizeOf);
            for(int i = 0; i< sizeOf; i++){
                checked.add(false);
            }
            int index = 0;
            Log.d(TAG, "....sizeOld: " + sizeOld);
            Log.d(TAG, "....sizeOf: " + sizeOf);
            while(index < sizeOf){
                if(sizeOld > 0 && index < sizeOld){
                    checked.set(index, itemChecked.get(index));
                } else {
                    checked.set(index, false);
                }
                index++;
            }
            itemChecked = checked;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Log.d(TAG+".mAdapter", "bindView(): cursor position: " + ((cursor != null)? cursor.getPosition(): 0));
            final int position = this.getCursor().getPosition();
            Bundle data = new Bundle();
            // Get the encounter UUID
            String uuid = cursor.getString(1);
            String state = cursor.getString(4);
            Boolean finished = Boolean.valueOf(cursor.getString(8));
            data.putParcelable(Intents.EXTRA_ENCOUNTER, Uris.withAppendedUuid(Encounters.CONTENT_URI, uuid));
            data.putBoolean(Encounters.Contract.FINISHED,finished);

            final String procedureUuid = cursor.getString(2);
            final String date = cursor.getString(7);
            final int status = cursor.getInt(5);
            final int queuePosition = cursor.getInt(6);
            final String patientUUid = cursor.getString(3);
            // MAke sure we bind the text views to something before
            // anything else happens
            ((TextView) view.findViewById(R.id.procedure)).setText(procedureUuid);
            ((TextView) view.findViewById(R.id.subject)).setText(patientUUid);
            ((TextView) view.findViewById(R.id.procedure_date)).setText(date);
            ((TextView) view.findViewById(R.id.queue_status)).setText("" + status + " - " + queuePosition);
            //((CheckBox) view.findViewById(R.id.checkbox));

            //view.setTag(0, uuid);
            //view.setTag(1,state);

            // TODO move this to a background thread?
            // Sets the procedure title string
            setProcedure(context,view,procedureUuid);
            // Sets the date string
            setDate(view, date);
            // Sets the status string
            setUploadStatus(view,status,queuePosition,finished);
            // Sets the patient name and id string
            setPatient(context,view,patientUUid);
            Log.d(TAG, "Putting data into position: " + position);
            mData.put(position,data);
        }

        public View getView(final int pos, View inView, ViewGroup parent) {
            Log.i(TAG+".mAdapter", "getView()");
            if (inView == null) {
                //LayoutInflater inflater = (LayoutInflater) context
                //        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                inView = mInflater.inflate(R.layout.encounterlist_item, parent,false);
            }

            final CheckBox cBox = (CheckBox) inView.findViewById(R.id.checkbox);
            // CheckBox
            cBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Need the size check
                    CheckBox cb = (CheckBox) v.findViewById(R.id.checkbox);
                    if (cb.isChecked()) {
                        if(itemChecked.size() > 0)
                            itemChecked.set(pos, true);
                    } else if (!cb.isChecked()) {
                        if(itemChecked.size() > 0)
                            itemChecked.set(pos, false);
                    }
                }
            });
            if(itemChecked.size() > 0)
                cBox.setChecked(itemChecked.get(pos));
            return super.getView(pos, inView, parent);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            Log.d(TAG+".mAdapter", "new view  cursor position: " + ((cursor != null)? cursor.getPosition(): 0));
            View view = mInflater.inflate(R.layout.encounterlist_item, viewGroup, false);
            bindView(view, context, cursor);
            return view;
        }

    }

    public void setProcedure(Context context, View view, String uuid){
        Log.i(TAG, "setProcedure() " + uuid);
        TextView name = (TextView) view.findViewById(R.id.procedure);

        String title = "null";
        Cursor cur2 = null;
        Uri procedure = Uri.parse(Procedures.CONTENT_URI.toString() + "/" + uuid);
        try {
            cur2 = getActivity().getContentResolver().query(procedure,
                    new String[]{
                            Procedures.Contract._ID,
                            Procedures.Contract.TITLE},
                    null, null, null);
            if (cur2.moveToFirst()) {
                title = cur2.getString(1);
            }
        }catch(Exception e){
            e.printStackTrace();
        } finally {
            if(cur2 != null) cur2.close();
        }
        name.setText((TextUtils.isEmpty(title)? "null": title));
    }

    public void setPatient(Context context, View view, String uuid){
        Log.i(TAG, "setPatient() " + uuid);
        TextView name = (TextView) view.findViewById(R.id.subject);
        Cursor c = null;
        String familyName = null;
        String givenName = null;
        String displayName = null;
        String id = null;
        try{
            c = ModelWrapper.getOneByUuid(Subjects.CONTENT_URI, context.getContentResolver(), uuid);
            if(c != null && c.moveToFirst()){
                familyName = c.getString(c.getColumnIndex(Patients.Contract.FAMILY_NAME));
                givenName = c.getString(c.getColumnIndex(Patients.Contract.GIVEN_NAME));
                id = c.getString(c.getColumnIndex(Patients.Contract.PATIENT_ID));
                displayName = StringUtil.formatPatientDisplayName(givenName, familyName);
            }
        } finally {
            if(c != null) c.close();
            name.setText((TextUtils.isEmpty(displayName)? "null": displayName) + " "+ id);
        }
    }

    public void setDate(View view, String date){
        Log.i(TAG, "setDate() " + date);

        /*
        TextView dateView = (TextView)view.findViewById(R.id.procedure_date);
        Date dateObj = null;
        try {
            dateObj = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String display = (dateObj == null)? "null date": df.format(dateObj);
        dateView.setText(display);
        */
        TextView dateView = (TextView)view.findViewById(R.id.procedure_date);
        try {
            Date d = Dates.fromSQL(date);
            DateTime dt = new DateTime(d);
            int month = dt.getMonthOfYear();
            int dayOfMonth = dt.getDayOfMonth();
            int year = dt.getYear();
            String localizedMonth = months[month - 1];
            dateView.setText(String.format("%02d %s %04d", dayOfMonth, localizedMonth, year));
        } catch (ParseException e){
            e.printStackTrace();
        }
    }

    public void setUploadStatus(View view, int queueStatus, int queuePosition,boolean complete){
        Log.i(TAG, "setUploadStatus() " + queueStatus + ":" + queuePosition);
        TextView statusView = (TextView)view.findViewById(R.id.queue_status);
        statusView.setText("" + queueStatus + " - " + queuePosition);
        String message = "";

        if (queueStatus == 0)
            message = getString(R.string.not_uploaded);
        else if (queueStatus == QueueManager.UPLOAD_STATUS_WAITING) {
            message = "Waiting in the queue to be uploaded, " + queuePosition;
            if (queuePosition == -1)
                message = "Waiting in the queue to be uploaded";
            else if (queuePosition == 1)
                message += "st in line";
            else if (queuePosition == 2)
                message += "nd in line";
            else if (queuePosition == 3)
                message += "rd in line";
            else
                message += "th in line";
        } else if (queueStatus == QueueManager.UPLOAD_STATUS_SUCCESS)
            message = getString(R.string.upload_success);
        else if (queueStatus == QueueManager.UPLOAD_STATUS_IN_PROGRESS)
            message = getString(R.string.general_upload_in_progress);
        else if (queueStatus == QueueManager.UPLOAD_NO_CONNECTIVITY)
            message = "Upload stalled - Waiting for connectivity";
        else if (queueStatus == QueueManager.UPLOAD_STATUS_FAILURE)
            message = getString(R.string.upload_fail);
        else if (queueStatus == QueueManager.UPLOAD_STATUS_CREDENTIALS_INVALID)
            message = "Upload stalled - username/password incorrect";
        else Log.i(TAG, "Not a valid number stored in database.");
        //TODO Fix this so that it shows whether finished
        /*
        if(complete)
            message = message + "-" + StringUtils.getLocalizedString(getActivity(),R.string.general_complete);
        else
            message = message + "-" + StringUtils.getLocalizedString(getActivity(),R.string.general_incomplete);
        */
        statusView.setText(message);
    }

    /** All checkboxes will be checked */
    public void selectAllProcedures() {
        for (int x = 0; x < getListAdapter().getCount(); x++) {
            try {
                CheckBox checkbox = (CheckBox) getListView().getChildAt(x)
                                                .findViewById(R.id.checkbox);
                checkbox.setChecked(true);
                Log.i(TAG, "....Is checkbox checked? (Should be true): "
                        + checkbox.isChecked());
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Exception in selectAll(): pos: " + x+" ," + e.getMessage());
            }
        }
    }

    /*
     * Unselect all checked items in the list.
     */
    public void unselectAllProcedures() {
        try {
            for (int x = 0; x < getListAdapter().getCount(); x++) {
                View v = getListView().getChildAt(x);
                //getListView().setItemChecked(x, false);
                CheckBox checkbox = (CheckBox) v.findViewById(R.id.checkbox);
                checkbox.setChecked(false);

            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception in unselectAll(): " + e.toString());
        }
    }

    public List<Uri> getSelected(){
        Log.i(TAG, "getSelected()");
        List<Uri> uris = new ArrayList<Uri>();
        long[] ids = getChecked();
        //if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO)
        int count = ids.length;
        Log.d(TAG, "....count=" + count);
        // iterate over list and resend checked
        for (int x = 0; x < count; x++) {
            //View child = view.getChildAt(x);
            Bundle data = mData.get(x);
            if(data != null){
                Uri encounter = data.getParcelable(Intents.EXTRA_ENCOUNTER);
                uris.add(encounter);
            } else {
                Log.e(TAG,"....NULL data bundle");
            }
        }
        return uris;
    }

    public List<Uri> getSelectedFinished(){
        Log.i(TAG, "getSelectedFinished()");
        List<Uri> uris = new ArrayList<Uri>();
        long[] ids = getChecked();
        int count = ids.length;
        Log.d(TAG, "....count=" + count);
        // iterate over list and resend checked
        for (int x = 0; x < count; x++) {
            //View child = view.getChildAt(x);
            boolean finished = isItemFinished(x);
            if(finished){
                Uri encounter = getItemUri(x);
                uris.add(encounter);
            } else {
                Log.e(TAG,"....NULL data bundle");
            }
        }
        return uris;
    }

    public long[] getChecked(){
        Log.i(TAG,"getChecked()");
        List<Long> checked = new ArrayList<Long>();
        for(int x = 0;x < getListAdapter().getCount(); x++){
            if(getItemChecked(x)){
                checked.add(new Long(x));
            }
        }
        long[] ids = new long[checked.size()];
        for(int y=0; y < checked.size();y++){
            ids[y] = checked.get(y);
        }
        return ids;
    }

    public boolean getItemChecked(int id){
        Log.i(TAG,"getItemChecked() " + id);
        ListView view = getListView();
        final View child = view.getChildAt(id);
        if(child == null)
            return false;
        CheckBox checkbox = (CheckBox) child.findViewById(R.id.checkbox);
        return (checkbox != null)? checkbox.isChecked(): false;
    }

    public boolean isItemFinished(long id){
        Log.i(TAG,"isItemFinished() " + id);
        Bundle data = mData.get(id);
        boolean finished = data.getBoolean(Encounters.Contract.FINISHED, false);
        return finished;
    }

    public Uri getItemUri(long id){
        Bundle data = mData.get(id);
        if(data != null){
            Uri encounter = data.getParcelable(Intents.EXTRA_ENCOUNTER);
            return encounter;
        }
        return Uri.EMPTY;
    }

    public int deleteSelected(){
        Log.i(TAG, "deleteSelected()");
        int count = 0;
        int obsCount = 0;
        int imageCount = 0;
        long[] checked = getChecked();
        List<Long> ids = new ArrayList<Long>();

        for(long id:checked){
            ids.add(id);
        }
        for(Long id:ids){
            try{
                String uuid = ((Cursor) getListAdapter().getItem(id.intValue())).getString(1);
                Log.d(TAG,"....uuid = " + uuid);
                obsCount += getActivity().getContentResolver().delete(Observations.CONTENT_URI,
                    Observations.Contract.ENCOUNTER + " = ?",
                    new String[]{ uuid });
                imageCount += getActivity().getContentResolver().delete(ImageSQLFormat.CONTENT_URI,
                    ImageSQLFormat.ENCOUNTER_ID+ " = ?",
                    new String[]{ String.valueOf(id) });
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        Log.d(TAG, "....deleted:");
        Log.d(TAG, "........images=" + imageCount);
        Log.d(TAG, "........observations" + obsCount);
        try{
            if(ids.size() > 1) {
                String idList = SanaUtil.formatPrimaryKeyList(ids);
                count = getActivity().getContentResolver().delete(Encounters.CONTENT_URI,
                        Encounters.Contract._ID + " IN " + idList, null);
            } else if(ids.size() == 1){
                count = getActivity().getContentResolver().delete(
                        Encounters.CONTENT_URI,
                        Encounters.Contract._ID + " = ?",
                        new String[]{ String.valueOf(ids.get(0)) });
            } else {
                Log.d(TAG, "Delete with None selected");
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        Log.d(TAG,"....deleted encounters = " + count);
        return count;
    }

    public Bundle getSelectedData(long id){
        Bundle data = mData.get(id);
        return data;
    }
}
