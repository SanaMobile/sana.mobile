
package org.sana.android.activity;

import org.sana.R;
import org.sana.android.Constants;
import org.sana.android.app.Locales;
import org.sana.android.content.DispatchResponseReceiver;
import org.sana.android.content.Uris;
import org.sana.android.content.core.PatientWrapper;
import org.sana.android.db.PatientInfo;
import org.sana.android.fragment.PatientRunnerFragment;
import org.sana.android.media.EducationResource;
import org.sana.android.net.MDSInterface;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Events;
import org.sana.android.provider.Patients;
import org.sana.android.provider.Subjects;
import org.sana.android.service.impl.DispatchService;
import org.sana.android.util.UserDatabase;
import org.sana.core.Patient;
import org.sana.net.Response;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.UnsupportedEncodingException;

/** Activity for creating a new patient. Each question is wrapped in a container
 * which presents buttons for paging.
 * 
 * @author Sana Development Team */
public class PatientRunner extends BaseRunner {

    public static final String TAG = PatientRunner.class.getSimpleName();
    
    private PatientRunnerFragment mFragmentPatientRunner;

    /** {@inheritDoc} */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_runner_activity);
    }

    /** {@inheritDoc} */
    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment.getClass() == PatientRunnerFragment.class) {
            //mFragmentPatientRunner = (PatientRunnerFragment) fragment;
            mRunnerFragment = (PatientRunnerFragment) fragment;
            mRunnerFragment.setProcedureListener(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Locales.updateLocale(this, getString(R.string.force_locale));
        menu.add(0, OPTION_DISCARD_EXIT, 1, getString(R.string.menu_discard_exit));
        menu.add(0, OPTION_VIEW_PAGES, 2, getString(R.string.menu_view_pages));
        return true;
    }

    @Override
    public IntentFilter buildFilter(){
        Log.i(TAG, "buildFilter()");
        IntentFilter filter = new IntentFilter(Response.RESPONSE);
        filter.addDataScheme(Subjects.CONTENT_URI.getScheme());
        try {
            filter.addDataType(Subjects.CONTENT_ITEM_TYPE);
            filter.addDataType(Subjects.CONTENT_TYPE);
        } catch (Exception e) {

        }
        return filter;
    }

    @Override
    protected void handleBroadcastResultSuccess(Intent intent){
        Log.i(TAG, "handleBroadcastResultSuccess(Intent)");
        setUploading(false);
        hideUploadingDialog();
        Log.d(TAG, "...broadcast=" + intent.toUri(Intent.URI_INTENT_SCHEME));
        Response.Code code = Response.Code.get(intent.getIntExtra(
                Response.CODE, Response.Code.UNKNOWN.code));
        switch (code) {
            case OK:
                onReadSuccess(intent);
                break;
            case CREATED:
                onCreateSuccess(intent);
                break;
            case UPDATED:
                onUpdateSuccess(intent);
                break;
            default:
                Log.d(TAG,"...unhandled response code");
        }
    }

    @Override
    protected void handleBroadcastResultFailure(Intent intent){
        Log.i(TAG, "handleBroadcastResultFailure(Intent)");
        setUploading(false);
        hideUploadingDialog();
        onReadFailure(intent);
    }

    public void onProcedureComplete(Intent data){
        Log.i(TAG, "onProcedureComplete(): " + data);
        startService(data);
        setResult(RESULT_OK,data);
        finish();
    }

    @Override
    public void onProcedureCancelled(String message){
        Log.i(TAG, "onProcedureComplete(): " + message);
        setResult(RESULT_CANCELED,null);
        finish();
    }



    protected void onCreateSuccess(Intent intent){
        Log.i(TAG,"onCreateSuccess(Intent)");
        ((PatientRunnerFragment) mRunnerFragment).onCreateSuccess(intent);
    }

    protected void onCreateFailure(Intent intent){
        Log.i(TAG,"onCreateFailure(Intent)");

    }

    protected void onReadSuccess(Intent intent){
        Log.i(TAG,"onReadSuccess(Intent)");
        try {
            Uri data = intent.getData();
            String systemId = data.getQueryParameter(Patients.Contract.PATIENT_ID);
            Log.d(TAG, "...system_id=" + systemId);
            if (!TextUtils.isEmpty(systemId)) {
                ((PatientRunnerFragment) mRunnerFragment).onPatientLookupSuccess
                        (systemId);
            } else {
                Log.w(TAG, "...directory code. do nothing");
            }
        } catch (Exception e) {
            e.printStackTrace();
            onReadFailure(intent);
        }

    }

    protected void onReadFailure(Intent intent){
        Log.i(TAG,"onReadFailure(Intent)");
        try {
            Uri data = intent.getData();
            String systemId = data.getQueryParameter(Patients.Contract.PATIENT_ID);
            Log.d(TAG, "...system_id=" + systemId);
            if(!TextUtils.isEmpty(systemId)) {
                ((PatientRunnerFragment) mRunnerFragment).onPatientLookupFailure(
                        systemId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onUpdateSuccess(Intent intent){
        Log.i(TAG,"onUpdateSuccess(Intent)");
        ((PatientRunnerFragment) mRunnerFragment).onCreateSuccess(intent);
    }

    protected void onUpdateFailure(Intent intent){
        Log.i(TAG,"onUpdateFailure(Intent)");

    }
}
