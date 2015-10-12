package org.sana.android.activity;

import java.io.IOException;
import java.net.URISyntaxException;

import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;

import org.sana.R;
import org.sana.api.IModel;
import org.sana.net.Response;
import org.sana.analytics.Runner;
import org.sana.android.Constants;
import org.sana.android.activity.settings.Settings;
import org.sana.android.app.DefaultActivityRunner;
import org.sana.android.app.Locales;
import org.sana.android.content.DispatchResponseReceiver;
import org.sana.android.content.Intents;
import org.sana.android.content.Uris;
import org.sana.android.db.ModelWrapper;
import org.sana.android.fragment.AuthenticationDialogFragment.AuthenticationDialogListener;
import org.sana.android.media.EducationResource;
import org.sana.android.procedure.Procedure;
import org.sana.android.provider.EncounterTasks;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Observers;
import org.sana.android.provider.Patients;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Subjects;
import org.sana.android.provider.Tasks;
import org.sana.android.service.ISessionCallback;
import org.sana.android.service.ISessionService;
import org.sana.android.service.impl.DispatchService;
import org.sana.android.service.impl.SessionService;
import org.sana.android.task.ResetDatabaseTask;
import org.sana.android.util.Logf;
import org.sana.android.util.SanaUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.AsyncTask.Status;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Main Activity which handles user authentication and initializes services that
 * Sana uses.
 * @author Sana Dev Team
 */
public class MainActivity extends BaseActivity implements AuthenticationDialogListener{

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String CLOSE = "org.sana.android.intent.CLOSE";

    public static final int AUTHENTICATE = 0;
    public static final int PICK_PATIENT = 1;
    public static final int PICK_ENCOUNTER = 2;
    public static final int PICK_ENCOUNTER_TASK = 3;
    public static final int PICK_PROCEDURE = 4;
    public static final int RUN_PROCEDURE = 5;
    public static final int RUN_REGISTRATION = 6;
    public static final int VIEW_ENCOUNTER = 7;
    public static final int SETTINGS = 8;
    public static final int EXECUTE_TASK = 9;
    private Runner<Intent,Intent> runner;

    private String mWorkflow = "org.sana.android.app.workflow.DEFAULT";
    // This is the initial state
    private Intent mIntent = new Intent(Intent.ACTION_MAIN);
    private Intent mPrevious = new Intent(Intent.ACTION_MAIN);
    private boolean checkUpdate = true;
    private boolean init = false;

    @Override
    protected void onNewIntent(Intent intent){
        Logf.D(TAG, "onNewIntent()");
        //super.onNewIntent(intent);
        onUpdateAppState(intent);
        dump();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Logf.I(TAG, "onActivityResult()", ((resultCode == RESULT_OK)? "OK":"CANCELED" ));
        Uri uri = Uri.EMPTY;
        switch(resultCode){
        case RESULT_CANCELED:

            switch(requestCode){
            case AUTHENTICATE:
                Logf.W(TAG, "onActivityResult()", "Authentication failure..Exiting");
                finish();
            case SETTINGS:
                break;
            }
            
            //onNext(data);
            break;
        case RESULT_OK:
            if(data != null)
                onUpdateAppState(data);
            Uri dataUri = (data != null)? data.getData(): Uri.EMPTY;
            Intent intent = new Intent();
            onSaveAppState(intent);
            switch(requestCode){
            case AUTHENTICATE:
                hideViewsByRole();
                break;
            case RUN_REGISTRATION:
                mEncounter = Uri.EMPTY;
                break;
            case PICK_PATIENT:
                intent.setAction(Intent.ACTION_PICK)
                .setData(Procedures.CONTENT_URI)
                .putExtras(data);
                startActivityForResult(intent, PICK_PROCEDURE);
                break;
            case PICK_PROCEDURE:
                intent.setAction(Intents.ACTION_RUN_PROCEDURE)
                .setData(data.getData())
                .putExtras(data);
                startActivityForResult(intent, RUN_PROCEDURE);
                break;
            case PICK_ENCOUNTER:
                //intent.setAction(Intent.ACTION_VIEW)
                //.setData(data.getData())
                intent.setClass(this, ObservationList.class)
                .setData(data.getData())
                .putExtras(data);
                startActivity(intent);
                break;
            case PICK_ENCOUNTER_TASK:
                    //Uri task = data.getParcelableExtra(Intents.EXTRA_TASK);
                    int flags = data.getFlags();
                    uri = Uri.EMPTY;
                    if(data.hasCategory(Intents.CATEGORY_TASK_COMPLETE)){
                        Log.i(TAG, "....Task complete: "+ mTask);
                        uri = intent.getParcelableExtra(Intents.EXTRA_ENCOUNTER);
                        intent.setClass(this, ObservationList.class)
                        .setData(uri)
                        .putExtras(data);
                        startActivity(intent);
                    } else {
                        Log.i(TAG, "....Task in progress: "+ mTask);
                        //markTaskStatusInProgress(mTask);
                        uri = intent.getParcelableExtra(Intents.EXTRA_PROCEDURE);
                        intent.setAction(Intent.ACTION_VIEW)
                        .setData(uri)
                        .putExtras(data);
                        startActivityForResult(intent, EXECUTE_TASK);
                    }
                    break;
            case EXECUTE_TASK:
                    dump();
                    startService(data);
                    markTaskStatusCompleted(mTask,mEncounter);
                    mTask = Uri.EMPTY;
                    mEncounter = Uri.EMPTY;
                    break;
            case RUN_PROCEDURE:
            case Intents.RUN_PROCEDURE:
                // Handle any content type specific actions
                switch(Uris.getDescriptor(dataUri)){
                    case Uris.ENCOUNTER_ITEM:
                        //startService(data);
                    break;
                    default:
                }
                mEncounter = Uri.EMPTY;
                String onComplete = data.getStringExtra(Intents.EXTRA_ON_COMPLETE);
                // If procedure onComplete was set start it
                if(!TextUtils.isEmpty(onComplete)) {
                    try {
                        Intent next = null;
                        next = Intent.parseUri(onComplete, Intent
                            .URI_INTENT_SCHEME);
                        onSaveAppState(next);
                        startActivityForResult(next, RUN_PROCEDURE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
            }

            //data.setAction(Intents.ACTION_OK);
            //onNext(data);
            break;
        }
    }

    public void hideViewsByRole(){
        mRoot = isAdmin(mObserver);
        if(!mRoot){
            LinearLayout main = (LinearLayout) findViewById(R.id.main_root);
            // TODO RBAC
            //main.removeView(findViewById(R.id.btn_main_select_patient));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logf.I(TAG, "onCreate()");

        mDebug = this.getResources().getBoolean(R.bool.debug);
        Locales.updateLocale(this, getString(R.string.force_locale));
        setContentView(R.layout.main);
        /*
        if(mDebug)
            setContentView(R.layout.main);
        else
            setContentView(R.layout.main_ht);
        */
        // TODO rethink where to integrate this
        checkUpdate(Uris.buildUri("package", "org.sana.provider" , ""));
        runner = new DefaultActivityRunner();
        init();
        if(Uris.isEmpty(mObserver)){
            onNext(null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logf.I(TAG, "onPause()");
        LocalBroadcastManager.getInstance(this.getApplicationContext()).unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logf.I(TAG, "onResume()");
        IntentFilter filter = new IntentFilter(Response.RESPONSE);
        try{
            filter.addDataType(Encounters.CONTENT_ITEM_TYPE);
            filter.addDataType(EncounterTasks.CONTENT_ITEM_TYPE);
        } catch (Exception e){}
        LocalBroadcastManager.getInstance(this.getApplicationContext()).registerReceiver(mReceiver, filter);
        //bindSessionService();
        // This prevents us from relaunching the login on every resume
        dump();
        hideViewsByRole();
    }

    @Override
    protected void onStart(){
        super.onStart();
        Logf.I(TAG, "onStart()");
        // We want the session service to be running if Main has been started
        // and until after it stops.
        startService(new Intent(SessionService.ACTION_START));
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onDestroy()");
        super.onStop();
        // kill the session service if there is nothing else bound
        if(mBound)
            stopService(new Intent(SessionService.ACTION_START));
        else
            stopService(new Intent(SessionService.ACTION_START));
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

    // Option menu codes
    private static final int OPTION_EXPORT_DATABASE = 0;
    private static final int OPTION_SETTINGS = 1;
    private static final int OPTION_SYNC = 2;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if(mDebug){
            menu.add(0, OPTION_EXPORT_DATABASE, 0, "Export Data");
        }
        if(mRoot || mDebug){
            menu.add(0, OPTION_SETTINGS, 1, getString(R.string.menu_settings));
            menu.add(0, OPTION_SYNC, 2, getString(R.string.menu_sync));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
        case OPTION_EXPORT_DATABASE:
            try {
                boolean exported = SanaUtil.exportDatabase(this,  "models.db");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        case OPTION_SETTINGS:
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setClass(this, Settings.class);
            startActivityForResult(i, SETTINGS);
            return true;
        case OPTION_SYNC:
            //doUpdatePatientDatabase();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        clearCredentials();
        onClearAppState();
        onNext(null);
    }

    /**
     * Launches the home activity
     */
    void showHomeActivity() {
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        // pass the app state fields
        onSaveAppState(intent);
    }

    /**
     * Launches the authentication activity
     */
    void showAuthenticationActivity() {
        Intent intent = new Intent();
        intent.setClass(this, AuthenticationActivity.class);
        startActivityForResult(intent, AUTHENTICATE);
    }

    protected void onNext(Intent intent){
        if(intent == null){
            showAuthenticationActivity();
            return;
        }
        intent.putExtra(Intents.EXTRA_REQUEST, mIntent);
        mPrevious = Intents.copyOf(mIntent);
        mIntent = runner.next(intent);
        if(mIntent.hasExtra(Intents.EXTRA_TASKS)){
            Logf.D(TAG, "onNext(Intent)", "sending tasks to dispatcher");
            Intent tasks = new Intent(MainActivity.this, DispatchService.class);
            tasks.putExtra(Intents.EXTRA_TASKS, mIntent.getParcelableArrayListExtra(Intents.EXTRA_TASKS));
            startService(tasks);
        }
        if(mIntent.getAction().equals(Intents.ACTION_FINISH)){
            Logf.D(TAG, "onNext(Intent)", "finishing");
            finish();
        } else {
            Logf.D(TAG, "onNext(Intent)", "starting for result");
            this.onSaveAppState(mIntent);
            startActivityForResult(mIntent, Intents.parseActionDescriptor(mIntent));
        }
    }
    /**
     * Checks whether the application needs to be updated.
     */
    protected void checkUpdate(Uri uri){
        if(!checkUpdate){
            Intent update = new Intent("org.sana.intent.action.READ");
            update.setData(uri);//, "application/vnd.android.package-archive");
            startService(update);
            checkUpdate = false;
        }
    }

    int loginsRemaining = 0;
    protected boolean mBound = false;
    protected ISessionService mService = null;

    // connector to the session service
    protected ServiceConnection mConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "ServiceConnection.onServiceConnected()");
            mService = ISessionService.Stub.asInterface(service);
            mBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "ServiceConnection.onServiceDisconnected()");
            mService = null;
            mBound = false;
        }
    };

    // handles initiating the session service binding
    protected void bindSessionService(){
        Log.i(TAG, "bindSessionService()");
        if(!mBound){
            if(mService == null){
                bindService(new Intent(SessionService.ACTION_START), mConnection, Context.BIND_AUTO_CREATE);
                //bindService(new Intent(SessionService.BIND_REMOTE), mCallbackConnection, Context.BIND_AUTO_CREATE);
            }
        }
    }

    // handles disconnecting from the session service bindings
    protected void unbindSessionService(){
        // Unbind from the service
        if (mBound){
            // Detach
            unbindService(mConnection);
            mBound = false;
        }
    }

    private ResetDatabaseTask mResetDatabaseTask;


    void init(){
        Logf.D(TAG, "init()");
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        boolean dbInit =preferences.getBoolean(Constants.DB_INIT, false);


        PreferenceManager.setDefaultValues(this, R.xml.settings, true);
        PreferenceManager.setDefaultValues(this, R.xml.network_settings,true);
        PreferenceManager.setDefaultValues(this, R.xml.resource_settings, true);
        PreferenceManager.setDefaultValues(this, R.xml.notifications, true);
        if(!dbInit){
                Logf.D(TAG, "init()", "Initializing");
                doClearDatabase();
            // Make sure directory structure is in place on external drive
                EducationResource.intializeDevice();
                Procedure.intializeDevice();
                preferences.edit().putBoolean(Constants.DB_INIT, true).commit();
                init = true;
        } else {
            Logf.D(TAG, "init()", "reloading");
            // RELOAD Database
            //preferences.edit().clear().commit();
            //PreferenceManager.setDefaultValues(this, R.xml.network_settings,true);
            doClearDatabase(new Uri[]{ Procedures.CONTENT_URI });
            preferences.edit().putBoolean(Constants.DB_INIT, true).commit();

        }
        preferences.edit().putString("s_phone_name", getPhoneNumber()).commit();
    }


    /** Executes a task to clear out the database */
    private void doClearDatabase() {
        // TODO: context leak
        if(mResetDatabaseTask!= null && mResetDatabaseTask.getStatus() != Status.FINISHED)
                return;
        mResetDatabaseTask =
                (ResetDatabaseTask) new ResetDatabaseTask(this,true).execute(this);
    }

    private void doClearDatabase(Uri[] uris) {
        // TODO: context leak
        if(mResetDatabaseTask!= null && mResetDatabaseTask.getStatus() != Status.FINISHED)
                return;
        mResetDatabaseTask =
                (ResetDatabaseTask) new ResetDatabaseTask(this,true,uris).execute(this);
    }

    private void saveLocalTaskState(Bundle outState){
        final ResetDatabaseTask rTask = mResetDatabaseTask;
        if (rTask != null && rTask.getStatus() != Status.FINISHED) {
            rTask.cancel(true);
            outState.putBoolean("_resetdb", true);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        try {
            mIntent = Intent.parseUri(inState.getString("mIntent"), Intent.URI_INTENT_SCHEME);
            mPrevious = Intent.parseUri(inState.getString("mPrevious"), Intent.URI_INTENT_SCHEME);
            checkUpdate = inState.getBoolean("update");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveLocalTaskState(outState);
        outState.putString("mIntent", mIntent.toUri(Intent.URI_INTENT_SCHEME));
        outState.putString("mPrevious", mPrevious.toUri(Intent.URI_INTENT_SCHEME));
        outState.putBoolean("update", checkUpdate);
    }

    void showAuthenticationDialog(){

    }

    /* (non-Javadoc)
     * @see org.sana.android.fragment.AuthenticationDialogFragment.AuthenticationDialogListener#onDialogPositiveClick(android.support.v4.app.DialogFragment)
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        EditText userEdit = (EditText)dialog.getDialog().findViewById(R.id.username);
        EditText userPassword = (EditText)dialog.getDialog().findViewById(R.id.username);
    }

    /* (non-Javadoc)
     * @see org.sana.android.fragment.AuthenticationDialogFragment.AuthenticationDialogListener#onDialogNegativeClick(android.support.v4.app.DialogFragment)
     */
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        try{
            dialog.dismiss();
        } catch(Exception e){
            e.printStackTrace();
        } finally {
            if(dialog.getId() == R.id.dialog_authentication)
                this.finish();
        }
    }

    /**
     * Retrieves the device phone number using the TelephonyManager
     * @return the device phone number
     */
    public String getPhoneNumber(){
        TelephonyManager tMgr =(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber = tMgr.getLine1Number();
        return phoneNumber;
    }

    public void submit(View v){
        Intent intent = null;
        switch(v.getId()){
            case R.id.btn_main_select_patient:
                intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(Subjects.CONTENT_URI, Subjects.CONTENT_TYPE);
                startActivityForResult(intent, PICK_PATIENT);
                break;
            case R.id.btn_main_transfers:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Encounters.CONTENT_URI, Encounters.CONTENT_TYPE);
                startActivityForResult(intent, PICK_ENCOUNTER);
                break;
            case R.id.btn_main_register_patient:
                intent = new Intent(Intent.ACTION_INSERT);
                intent.setDataAndType(Patients.CONTENT_URI, Subjects.CONTENT_TYPE)
                        .putExtra(Intents.EXTRA_PROCEDURE, Uris.withAppendedUuid(Procedures.CONTENT_URI,
                                getString(R.string.procs_subject_short_form)))
                        .putExtra(Intents.EXTRA_OBSERVER, mObserver);
                startActivityForResult(intent, Intents.RUN_PROCEDURE);
                break;
            case R.id.btn_main_procedures:
                intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(Procedures.CONTENT_URI, Procedures.CONTENT_TYPE);
                startActivityForResult(intent, PICK_PROCEDURE);
                break;
            case R.id.btn_main_tasks:
                intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(EncounterTasks.CONTENT_URI, EncounterTasks.CONTENT_TYPE);
                onSaveAppState(intent);
                startActivityForResult(intent, PICK_ENCOUNTER_TASK);
                break;
            case R.id.btn_training_mode:
                String subj = getString(R.string.tr_subject);
                String proc = getString(R.string.tr_procedure);
                intent = new Intent(Intent.ACTION_VIEW)
                        .setData(Uris.withAppendedUuid(Procedures.CONTENT_URI, proc))
                        .putExtra(Intents.EXTRA_SUBJECT, Uris.withAppendedUuid(Subjects.CONTENT_URI, subj))
                        .putExtra(Intents.EXTRA_OBSERVER, mObserver);
                startActivityForResult(intent, RUN_PROCEDURE);
                break;
            /*
            case R.id.btn_main_unregistered_subject:
                intent = new Intent(Intents.ACTION_RUN_PROCEDURE);
                intent.setDataAndType(Patients.CONTENT_URI, Subjects.CONTENT_TYPE)
                        .putExtra(Intents.EXTRA_PROCEDURE, Uris.withAppendedUuid(Procedures.CONTENT_URI,
                                getString(R.string.procs_subject_short_form)))
                        .putExtra(Intents.EXTRA_PROCEDURE_ID,R.raw
                                .registration_short_en)
                        .putExtra(Intents.EXTRA_OBSERVER, mObserver);
                startActivityForResult(intent, Intents.RUN_PROCEDURE);
                break;
            */
            case R.id.btn_exit:
                clearCredentials();
                onClearAppState();
                onNext(null);
                break;
        }
    }

    // The callback to get data from the asynchronous calls
    private ISessionCallback mCallback = new ISessionCallback.Stub() {

            @Override
            public void onValueChanged(int arg0, String arg1, String arg2)
                    throws RemoteException {
                Log.d(TAG,  ".mCallback.onValueChanged( " +arg0 +", "+arg1+
                        ", " + arg2+ " )");
                Bundle data = new Bundle();
                data.putString(Intents.EXTRA_INSTANCE, arg1);
                data.putString(Intents.EXTRA_OBSERVER, arg2);
                mHandler.sendMessage(mHandler.obtainMessage(arg0, data));
            }

        };

        // This is the handler which responds to the SessionService
        // It expects a Message with msg.what = FAILURE or SUCCESS
        // and a Bundle with the new session key if successful.
        private Handler mHandler = new Handler() {

            @Override public void handleMessage(Message msg) {
                int state = msg.what;
                Log.i(TAG, "handleMessage(): " + msg.what);
                cancelProgressDialogFragment();
                switch(state){
                case SessionService.FAILURE:
                    loginsRemaining--;
                    // TODO use a string resource
                    Toast.makeText(MainActivity.this,
                            "Username and password incorrect! Logins remaining: "
                                    +loginsRemaining,
                            Toast.LENGTH_SHORT).show();
                    showAuthenticationDialog();
                    break;
                case SessionService.SUCCESS:
                    loginsRemaining = 0;
                    Bundle b = msg.getData(); //(Bundle)msg.obj;
                    Uri uri = Uris.withAppendedUuid(Observers.CONTENT_URI,
                            b.getString(Intents.EXTRA_OBSERVER));
                    Log.i(TAG, uri.toString());
                    onUpdateAppState(b);
                    Intent data = new Intent();
                    data.setData(uri);
                    data.putExtras(b);
                    onSaveAppState(data);
                    mIntent = new Intent(Intent.ACTION_MAIN);

                    break;
                default:
                    Log.e(TAG, "Should never get here");
                }

                // Finish if remaining logins => 0
                if(loginsRemaining == 0){
                    finish();
                }
            }
        };

        public boolean isAdmin(Uri observer){
            Log.i(TAG,"isAdmin() " + observer);
            if(Uris.isEmpty(observer))
                return false;
            String uuid = observer.getLastPathSegment();
            String[] admins = this.getResources().getStringArray(R.array.admins);
            boolean admin = false;
            for(String adminUuid:admins)
                if(uuid.compareTo(adminUuid) == 0){
                    admin = true;
                    break;
                }
            Log.d(TAG,"...." + admin);
            return admin;
        }



    public void handleTaskStatusChange(Uri task, org.sana.api.task.Status status, String now){
        handleTaskStatusChange(task,status,now,null);
    }

    public void handleTaskStatusChange(Uri task, org.sana.api.task.Status status, String now, ContentValues values){
        if(now == null) now = timeStamp();
        if(values == null)
            values = new ContentValues();
        Log.i(TAG, "Updating status: " + now + " --> " + status + " --> " + values.size() + " --> " + task);

        // update in db
        values.put(Tasks.Contract.STATUS, status.toString());
        values.put(Tasks.Contract.MODIFIED, now);

        // Convert to a Bundle so that we can pass it
        Log.d(TAG, "FORM data");
        Bundle form = new Bundle();
        form.putString(Tasks.Contract.STATUS,String.valueOf(status.code));
        form.putString(Tasks.Contract.MODIFIED,now);
        getContentResolver().update(task,values,null,null);

        // send to sync
        Intent intent = new Intent(Intents.ACTION_UPDATE,task);
        intent.putExtra("form", form);
        startService(intent);
    }
    public void markTaskStatusInProgress(Uri task){
        Log.i(TAG, "markStatusInProgress(): " + task);
        org.sana.api.task.Status status = org.sana.api.task.Status.IN_PROGRESS;
        String now = timeStamp();
        ContentValues values = new ContentValues();
        values.put(Tasks.Contract.STATUS, "In Progress");
        values.put(Tasks.Contract.MODIFIED, now);
        values.put(Tasks.Contract.STARTED, now);
        getContentResolver().update(task,values,null,null);

        Bundle form = new Bundle();
        form.putString(Tasks.Contract.STATUS, "In Progress");
        form.putString(Tasks.Contract.MODIFIED,now);
        form.putString(Tasks.Contract.STARTED,now);

        // send to sync
        Intent intent = new Intent(Intents.ACTION_UPDATE,task);
        intent.putExtra("form", form);
        startService(intent);
    }

    public void markTaskStatusCompleted(Uri task, Uri encounter){
        Log.i(TAG, "markStatusCompleted(): " + task);
        org.sana.api.task.Status status = org.sana.api.task.Status.COMPLETED;
        String now = timeStamp();
        String uuid = ModelWrapper.getUuid(encounter,getContentResolver());
        ContentValues values = new ContentValues();
        values.put(Tasks.Contract.STATUS, status.toString());
        values.put(Tasks.Contract.COMPLETED, now);
        values.put(EncounterTasks.Contract.ENCOUNTER, uuid);
        values.put(Tasks.Contract.MODIFIED, now);

        Bundle form = new Bundle();
        form.putString(Tasks.Contract.STATUS, status.toString());
        form.putString(Tasks.Contract.MODIFIED,now);
        form.putString(Tasks.Contract.COMPLETED,now);
        form.putString(EncounterTasks.Contract.ENCOUNTER, uuid);

        // send to sync
        Intent intent = new Intent(Intents.ACTION_UPDATE,task);
        intent.putExtra("form", form);
        startService(intent);
    }

    static final SimpleDateFormat sdf = new SimpleDateFormat(IModel.DATE_FORMAT,
        Locale.US);

    public String timeStamp(String key, ContentValues values){
        Date now = new Date();
        String nowStr = sdf.format(now);
        values.put(key, nowStr);
        return nowStr;
    }

    public String timeStamp(){
        Date now = new Date();
        String nowStr = sdf.format(now);
        return nowStr;
    }

    @Override
    protected void handleBroadcast(Intent data){
        Log.i(TAG,"handleBroadcast()");
        cancelProgressDialogFragment();
        String message = data.getStringExtra(Response.MESSAGE);
        Response.Code code = Response.Code.get(data.getIntExtra(Response.CODE,-1));
        Uri uri = data.getData();
        int descriptor = (uri != null)? Uris.getDescriptor(uri): Uris.NO_MATCH;
        
        Log.i(TAG,"....descriptor="+descriptor);
        switch(code){
            case CONTINUE:
                switch(descriptor){
                    case Uris.ENCOUNTER_ITEM:
                    case Uris.ENCOUNTER_UUID:
                        if(showProgressForeground())
                            showProgressDialogFragment(message);
                        break;
                default:
                }
            default:
                switch(descriptor){
                    case Uris.ENCOUNTER_ITEM:
                    case Uris.ENCOUNTER_UUID:
                        hideProgressDialogFragment();
                        if(!TextUtils.isEmpty(message)){
                            Toast.makeText(this,message,Toast.LENGTH_LONG).show();
                        }
                        break;
                    default:
                }
        }
    }
}
