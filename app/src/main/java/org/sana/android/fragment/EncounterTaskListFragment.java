
package org.sana.android.fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.sana.android.db.ModelWrapper;
import org.sana.android.provider.EncounterTasks;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Patients;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.EncounterTasks.Contract;
import org.sana.android.provider.Subjects;
import org.sana.android.service.impl.DispatchService;
import org.sana.android.util.Bitmaps;
import org.sana.android.util.Dates;
import org.sana.android.util.Logf;
import org.sana.api.IModel;
import org.sana.api.task.EncounterTask;

import org.sana.api.task.Status;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Fragment displaying all patients.
 *
 * @author Sana Development Team
 */
public class EncounterTaskListFragment extends ListFragment implements LoaderCallbacks<Cursor> {
    public static final String TAG = EncounterTaskListFragment.class.getSimpleName();

    public static SimpleDateFormat sdf = new SimpleDateFormat(IModel.DATE_FORMAT,
            Locale.US);
    public static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm",
                    Locale.US);
    //TODO start using a proper inner join for the query instead of the
    //repetitive queries for the patient and procedure info.
    protected static final String SELECT = "SELECT" +
        "encountertask.uuid AS uuid, " +
        "encountertask.due_date AS due_on, " +
        "encountertask.subject AS subject_uuid, " +
        "subject.given_name AS subject_given_name, " +
        "subject.family_name AS subject_family_name, " +
        "subject.system_id AS subject_system_id, " +
        "encountertask.procedure AS procedure_uuid, " +
        "procedure.title AS procedure_title " +
        "FROM encountertask " +
        "INNER JOIN procedure ON encountertask.procedure = procedure._id " +
        "INNER JOIN subject ON encountertask.subject = subject.uuid;";

    static final String SIMPLE_SELECT = EncounterTasks.Contract.OBSERVER + " = ?"
            + " AND " + EncounterTasks.Contract.STATUS + " = ? ";

    static final String[] mProjection = new String[] {
        Contract._ID,
        Contract.SUBJECT,
        Contract.PROCEDURE,
        Contract.DUE_DATE,
        Contract.UUID,
        Contract.STATUS,
                Contract.COMPLETED,
                Contract.ENCOUNTER
        };

    // Once a day 86400000
    long delta = 1000;
    protected Uri mUri;
    protected EncounterTaskCursorAdapter mAdapter;
    protected OnModelItemSelectedListener mListener;
    protected Handler mHandler;
    protected boolean doSync = false;
    String[] months;

    protected LongSparseArray<Bundle> mData = new LongSparseArray<Bundle>();

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
        Locale locale = new Locale(Preferences.getString(getActivity(),
                Constants.PREFERENCE_LOCALE, "en"));
        df = new SimpleDateFormat(
                getActivity().getString(R.string.display_date_time_format),
                locale);
        Locales.updateLocale(getActivity(), locale);
        months = getActivity().getResources().getStringArray(R.array.months_long_format);

        // signal the dispatcher to sync
        mUri = getActivity().getIntent().getData();
        if (mUri == null) {
            mUri = EncounterTasks.CONTENT_URI;
        }
        mAdapter = new EncounterTaskCursorAdapter(getActivity(), null, 0);
        setListAdapter(mAdapter);
        Uri syncUri = EncounterTasks.CONTENT_URI;
        Uri observer = getActivity().getIntent().getParcelableExtra(
            Intents.EXTRA_OBSERVER);
        delta = getActivity().getResources().getInteger(
            R.integer.sync_delta_encountertasks);
        // Always sync subjects first
        syncSubjects(getActivity(),Subjects.CONTENT_URI);

        // sync for specific observer or all tasks
        if(!Uris.isEmpty(observer)){
            Log.i(TAG, "sync: observer = " + observer);
            // sync subjects first
            sync(getActivity(), Subjects.CONTENT_URI);
            String observerUuid = ModelWrapper.getUuid(
                observer, getActivity().getContentResolver());
            Uri u = EncounterTasks.CONTENT_URI.buildUpon().appendQueryParameter(
                "assigned_to__uuid",observerUuid).build();
            sync(getActivity(), u);
        } else {
            Log.i(TAG, "sync: all ");
            // sync subjects first
            sync(getActivity(), Subjects.CONTENT_URI);
            sync(getActivity(), EncounterTasks.CONTENT_URI);
        }
        LoaderManager.enableDebugLogging(true);
        getActivity().getSupportLoaderManager().initLoader(Uris.ENCOUNTER_TASK_DIR, null, this);
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
                        getSelection(),
                new String[]{ getObserver() , getSelectedStatus() },
                Contract.DUE_DATE + " ASC");
        return loader;
    }

    /** {@inheritDoc} */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished() ");

        if (cursor == null || (cursor !=null && cursor.getCount() == 0)) {
            setEmptyText(getString(R.string.msg_no_patients));
        }
        if(cursor != null)
            ((EncounterTaskCursorAdapter) this.getListAdapter()).swapCursor(cursor);
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.support.v4.content.Loader)
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset() ");
        ((EncounterTaskCursorAdapter) this.getListAdapter()).swapCursor(null);
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

    public static class ViewHolder{
        ImageView image;
        TextView name;
        TextView systemId;
        TextView location;
        TextView label;
        int position = 1;
        EncounterTask task = null;
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
        return SIMPLE_SELECT;
    }

    /**
     * @author Sana Development Team
     */
    public class EncounterTaskCursorAdapter extends CursorAdapter{

        private final LayoutInflater mInflater;
        // Holders for the cursor column index values
        protected int idIndex = -1;
        protected int subjectIndex = -1;
        protected int procedureIndex = -1;
        protected int dueOnIndex = -1;
        protected int statusIndex = -1;
        protected int completedIndex = -1;
        protected int uuidIndex = -1;
        protected int encounterIndex = -1;
        protected String[] months;
        public EncounterTaskCursorAdapter(Context context, Cursor c) {
            super(context.getApplicationContext(),c,false);
            mInflater = LayoutInflater.from(context);
        }

        public EncounterTaskCursorAdapter(Context context) {
            this(context, null, 0);
        }

        public EncounterTaskCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            mInflater = LayoutInflater.from(context);
            setColumnIndexes(c);
        }


        @Override
        public void changeCursor (Cursor cursor){
            setColumnIndexes(cursor);
            super.changeCursor(cursor);
        }

        @Override
        public Cursor swapCursor(Cursor newCursor) {
            setColumnIndexes(newCursor);
            return super.swapCursor(newCursor);

        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Log.d(TAG+".mAdapter", "bindView(): cursor position: "
                + ((cursor != null)? cursor.getPosition(): 0));
            int position = this.getCursor().getPosition();
            String uuid = ((Cursor) getItem(position)).getString(uuidIndex);
            String due_on = ((Cursor) this.getItem(position)).getString(dueOnIndex);
            String status = ((Cursor) this.getItem(position)).getString(statusIndex);
            String completed = ((Cursor) getItem(position)).getString(completedIndex);
            boolean complete = false;
            boolean reviewed = false;
            Status stat = Status.fromString(status);
            switch(stat){
                case COMPLETED:
                case REVIEWED:
                    complete = true;
                    break;
                default:
                    complete = false;
            }
            if(complete)
                setDate(view,true,completed,position);
            else
                setDate(view,due_on, position);
            String patientUUid = ((Cursor) getItem(position)).getString(subjectIndex);
            setPatient(context,view,patientUUid);
            String procedureUuid = ((Cursor) getItem(position)).getString(procedureIndex);
            setProcedure(context,view,procedureUuid);
            String encounter = ((Cursor) getItem(position)).getString(encounterIndex);
            Bundle data = new Bundle();
            data.putString(Contract.STATUS, status);
            data.putParcelable(Intents.EXTRA_TASK,
                Uris.withAppendedUuid(EncounterTasks.CONTENT_URI, uuid));
            data.putParcelable(Intents.EXTRA_SUBJECT,
                Uris.withAppendedUuid(Subjects.CONTENT_URI, patientUUid));
            data.putParcelable(Intents.EXTRA_PROCEDURE,
                Uris.withAppendedUuid(Procedures.CONTENT_URI, procedureUuid));
            data.putParcelable(Intents.EXTRA_TASK_ENCOUNTER,
                Uris.withAppendedUuid(EncounterTasks.CONTENT_URI, uuid));
            if(!TextUtils.isEmpty(encounter))
                data.putParcelable(Intents.EXTRA_ENCOUNTER,
                    Uris.withAppendedUuid(Encounters.CONTENT_URI, encounter));
            view.setTag(data);
            mData.put(position, data);
            Log.i(TAG, "Finished setting data for: " + position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(TAG+".mAdapter", "get view ");
            return super.getView(position,convertView, parent);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            Log.d(TAG+".mAdapter", "new view  cursor position: "
                + ((cursor != null)? cursor.getPosition(): 0));
            View view = mInflater.inflate(R.layout.encountertask_list_item, null);
            bindView(view, context, cursor);
            return view;
        }

        protected void setColumnIndexes(Cursor cursor){
            // Do a null check just in case
            if(cursor == null) {
                Log.w(TAG, "setColumnIndexes(null)");
                return;
            }
            idIndex = cursor.getColumnIndex(Contract._ID);
            subjectIndex = cursor.getColumnIndex(Contract.SUBJECT);
            procedureIndex = cursor.getColumnIndex(Contract.PROCEDURE);
            dueOnIndex = cursor.getColumnIndex(Contract.DUE_DATE);
            statusIndex = cursor.getColumnIndex(Contract.STATUS);
            completedIndex = cursor.getColumnIndex(Contract.COMPLETED);
            uuidIndex = cursor.getColumnIndex(Contract.UUID);
            encounterIndex = cursor.getColumnIndex(Contract.ENCOUNTER);
        }
    }

    public final void sync(Context context, Uri uri){
        Logf.D(TAG, "sync() --> " + uri);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        long lastSync = 0;
        int descriptor = Uris.getContentDescriptor(uri);
        switch(descriptor){
            case Uris.SUBJECT:
                lastSync = prefs.getLong("patient_sync", 0);
                break;
            case Uris.ENCOUNTER_TASK:
                lastSync = prefs.getLong("encountertask_sync", 0);
                break;
            default:
                return;
        }

        long now = System.currentTimeMillis();
        Log.d(TAG, "last: " + lastSync +", now: " + now+ ", delta: " + (now-lastSync) + ", doSync: " + ((now - lastSync) > delta));
        // TODO
        if((now - lastSync) > delta){
            Logf.I(TAG, "sync(): synchronizing list: " + uri);
            switch(descriptor){
            case Uris.SUBJECT:
                prefs.edit().putLong("patient_sync", now).commit();
                break;
            case Uris.ENCOUNTER_TASK:
                prefs.edit().putLong("encountertask_sync", now).commit();
                break;
            default:
            }
            Intent intent = new Intent(Intents.ACTION_READ, uri);
            context.startService(intent);
        }
    }

    public final void syncSubjects(Context context, Uri uri){
        Logf.D(TAG, "sync() --> " + uri);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        long lastSync = prefs.getLong("patient_sync", 0);
        long now = System.currentTimeMillis();
        Log.d(TAG, "last: " + lastSync +", now: " + now+ ", delta: " + (now-lastSync) + ", doSync: " + ((now - lastSync) > delta));
        // TODO
        if((now - lastSync) > delta){
            Logf.I(TAG, "sync(): synchronizing list: " + uri);
            prefs.edit().putLong("patient_sync", now).commit();

            Intent intent;
            intent = new Intent(Intents.ACTION_READ, uri);
            context.startService(intent);
        } else {
            /*
            Intent broadcast = new Intent(DispatchResponseReceiver.BROADCAST_RESPONSE);
            broadcast.setData(EncounterTasks.CONTENT_URI);
            Locales.updateLocale(getActivity(), getString(R.string.force_locale));
            broadcast.putExtra(DispatchResponseReceiver.KEY_RESPONSE_MESSAGE, "");
            broadcast.putExtra(DispatchResponseReceiver.KEY_RESPONSE_CODE, 200);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(broadcast);
            */
        }
    }

    public final void setProcedure(Context context, View view, String uuid){
        Log.d(TAG+".setProcedure", "procedure uuid: " + uuid);
        Cursor c = null;
        String title = null;
        TextView procedureTitle = (TextView)view.findViewById(R.id.procedure_title);
        try{
            Uri uri = Uris.withAppendedUuid(Procedures.CONTENT_URI, uuid);
            c = context.getContentResolver().query(uri, new String[]{ Procedures.Contract.TITLE } , null,null,null);
            if(c != null && c.moveToFirst()){
                title = c.getString(0);
            }
        } finally {
            if(c != null) c.close();
            procedureTitle.setText((TextUtils.isEmpty(title)? "null": title));
        }
        Log.d(TAG+".setProcedure", "finished setting view: " + title);
    }

    public void setDate(View view, String due_on, long id){
        setDate(view,false,due_on,id);
    }
    public void setDate(View view, boolean completed, String due_on, long id){
        TextView dueOn = (TextView)view.findViewById(R.id.due_on);
        Log.i(TAG, "due_on:" + due_on);

        Date date = new Date();
        Date now = new Date();

        try {
            date = sdf.parse(due_on);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        DateTime dt = new DateTime(date);
        int month = dt.getMonthOfYear();
        int dayOfMonth = dt.getDayOfMonth();
        int year = dt.getYear();
        String localizedMonth = months[month - 1];
        due_on = String.format("%02d %s %04d", dayOfMonth, localizedMonth, year);

        Log.i(TAG, "due_on(formatted):" + due_on);
        dueOn.setText(due_on);
        if(completed){
            dueOn.setTextColor(getResources().getColor(R.color.complete));
        } else if(now.getDay() == date.getDay()
                    && now.getMonth() == date.getMonth()
                    && now.getYear() == date.getYear()){
            if(now.after(date)){
                dueOn.setTextColor(getResources().getColor(R.color.past_due));
                Log.w(TAG, "DUE TODAY-PAST DUE:" + id);
            } else {
                Log.w(TAG, "DUE TODAY:" + id);
                dueOn.setTextColor(getResources().getColor(R.color.due_today));
            }
        } else if(now.after(date)){
            dueOn.setTextColor(getResources().getColor(R.color.past_due));
            Log.w(TAG, "PAST DUE DATE:" + id);
        } else {
            Log.i(TAG, "due date ok:" + id);
            dueOn.setTextColor(getResources().getColor(android.R.color.white));
        }
    }

    public void setPatient(Context context, View view, String uuid){
        Cursor c = null;
        String familyName = null;
        String givenName = null;
        String displayName = null;
        String id = null;
        String imagePath = null;
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView systemId = (TextView)view.findViewById(R.id.system_id);
        ImageView image = (ImageView)view.findViewById(R.id.image);
        try{
            c = ModelWrapper.getOneByUuid(Subjects.CONTENT_URI, context.getContentResolver(), uuid);
            if(c != null && c.moveToFirst()){
                familyName = c.getString(c.getColumnIndex(Patients.Contract.FAMILY_NAME));
                givenName = c.getString(c.getColumnIndex(Patients.Contract.GIVEN_NAME));
                id = c.getString(c.getColumnIndex(Patients.Contract.PATIENT_ID));
                displayName = StringUtil.formatPatientDisplayName(givenName, familyName);
                imagePath = c.getString(c.getColumnIndex(Patients.Contract.IMAGE));
            }
        } finally {
            if(c != null) c.close();
            name.setText((TextUtils.isEmpty(displayName)? "null": displayName));
            systemId.setText((TextUtils.isEmpty(id)? "null id":id));
            setImage(image,imagePath);
        }
    }

    public void setImage(ImageView view, String imagePath){
        // Set patient name and image
        if(imagePath != null){
            try{
                view.setImageBitmap(Bitmaps.decodeSampledBitmapFromFile(
                    Uri.parse(imagePath).getPath(), 128,128));
            } catch (Exception e){
                Log.e(TAG+".mAdapter", "bindView(): " + e.getMessage());
            }
        }
    }

    public Bundle getSelectedData(long id){
        Bundle data = mData.get(id);
        return data;
    }
}
