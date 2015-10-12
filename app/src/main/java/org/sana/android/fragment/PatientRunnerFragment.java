
package org.sana.android.fragment;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.sana.R;
import org.sana.android.content.Intents;
import org.sana.android.content.Uris;
import org.sana.android.content.core.PatientParcel;
import org.sana.android.content.core.PatientWrapper;
import org.sana.android.db.ModelWrapper;
import org.sana.android.db.PatientInfo;
import org.sana.android.procedure.PatientIdElement;
import org.sana.android.procedure.Procedure;
import org.sana.android.procedure.ProcedureElement;
import org.sana.android.procedure.ProcedurePage;
import org.sana.android.provider.Events.EventType;
import org.sana.android.provider.Patients;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Subjects;
import org.sana.android.service.impl.InstrumentationService;
import org.sana.android.util.Dates;
import org.sana.core.Location;
import org.sana.core.Patient;
import org.sana.net.Response;
import org.sana.util.DateUtil;
import org.sana.util.UUIDUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

/** Class for creating a new patient.
 * 
 * @author Sana Development Team */
public class PatientRunnerFragment extends BaseRunnerFragment  {

    public static final String TAG = PatientRunnerFragment.class.getSimpleName();

    protected PatientParcel mPatient = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showCompleteConfirmation = false;
    }

    @Override
    protected boolean handlePostProcessedElements() {
        Log.i(TAG, "handlePostProcessedElements()");
        // Start the subject lookup;
        if (mProcedure.current().hasElementWithId(Patients.Contract.PATIENT_ID)
                && mProcedure.getPatientInfo() == null) {

            String patientId = mProcedure.current().getElementByType
                    (Patients.Contract.PATIENT_ID).getAnswer();
            Uri uri = Subjects.CONTENT_URI;
            Uri.Builder builder = uri.buildUpon();
            builder.appendQueryParameter(Patients.Contract.PATIENT_ID,
                    patientId);
            Intent intent = new Intent(Intents.ACTION_READ, builder.build());
            String message = String.format(
                    getString(R.string.dialog_look_up_patient), patientId);
            showProgressDialogFragment(message);
            getActivity().startService(intent);
            return false;
        } else {
            return true;
        }

    }

    /** {@inheritDoc} */
    @Override
    protected void loadProcedure(Bundle instance) {
        // Load procedure
        if (mProcedure == null) {
            ProcedureLoadRequest request = new ProcedureLoadRequest();
            request.instance = instance;
            request.intent = getActivity().getIntent();

            logEvent(EventType.ENCOUNTER_LOAD_STARTED, "");
            new CreatePatientTask().execute(request);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void logEvent(EventType type, String value) {
        // TODO: log patient specific events
    }

    /** 
     *
     * 
     * @param finished -- Whether to set the patient as finished creating.
     */
    @Override
    public void storeCurrentProcedure(boolean finished) {
        Log.i(TAG, "storeCurrentProcedure(boolean)");
        storeCurrentProcedure(finished, false);
    }

    public void storeCurrentProcedure(boolean finished, boolean skipHidden) {
        Log.i(TAG, "storeCurrentProcedure(boolean,boolean)");
        Log.d(TAG, "...Checking for null patient");
        if(mPatient == null){
            Log.w(TAG, "...Subject is null");
            return;
        }
        // Update the patient object from the elements
        ProcedurePage page = mProcedure.current();
        Log.d(TAG, "...current page: " + mProcedure.getCurrentIndex());
        Log.d(TAG, "...current page should display: " + page.shouldDisplay());
        Log.d(TAG, "...Patient: " + String.valueOf(mPatient));

        // Set any patient fields from current page elements
        for(String concept: page.getConcepts()){
            String id = page.elementWithConcept(concept);
            String val = page.getElementValue(id);
            String field = concept.replace(" ","_");
            Log.d(TAG,"\tsetting field'" + field + "' for concept '" +
                    concept + "'");
            if(field.compareToIgnoreCase(Patients.Contract.PATIENT_ID) == 0) {
                Log.d(TAG, "\tsetting '" + Patients.Contract.PATIENT_ID + "'=" + val);
                if(!TextUtils.isEmpty(val))
                    mPatient.setSystemId(val);
            }
            if(field.compareToIgnoreCase(Patients.Contract.GIVEN_NAME) == 0) {
                Log.d(TAG, "\tsetting '" + Patients.Contract.GIVEN_NAME + "'=" + val);
                if(!TextUtils.isEmpty(val))
                    mPatient.setGiven_name(val);
            }
            if(field.compareToIgnoreCase(Patients.Contract.FAMILY_NAME) == 0) {
                Log.d(TAG, "\tsetting '" + Patients.Contract.FAMILY_NAME + "'=" + val);
                if(!TextUtils.isEmpty(val))
                    mPatient.setFamily_name(val);
            }
            if(field.compareToIgnoreCase(Patients.Contract.DOB) == 0) {
                Log.d(TAG, "\tsetting '" + Patients.Contract.DOB + "'=" + val);
                try {
                    mPatient.setDob(DateUtil.parseDate(val));
                } catch (ParseException e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
            if(field.compareToIgnoreCase(Patients.Contract.GENDER) == 0) {
                Log.d(TAG, "\tsetting '" + Patients.Contract.GENDER +"'=" +val);
                mPatient.setGender(val);
            }
            if(field.compareToIgnoreCase(Patients.Contract.IMAGE) == 0) {
                Log.d(TAG, "\tsetting '" + Patients.Contract.IMAGE +"'=" +val);
                URI file = URI.create(val);
                mPatient.setImage(file);
            }
            if(field.compareToIgnoreCase(Patients.Contract.LOCATION) == 0) {
                Log.d(TAG, "\tsetting '" + Patients.Contract.LOCATION + "'=" + val);
                Location location = new Location();
                if(UUIDUtil.isValid(val)) {
                    location.setUuid(val);
                } else {
                    location.setName(val);
                }
                mPatient.setLocation(location);
            }
            if(field.compareToIgnoreCase(Patients.Contract.CONFIRMED) == 0) {
                Log.d(TAG, "\tsetting '" + Patients.Contract.CONFIRMED + "'=" + val);
                mPatient.setConfirmed(Boolean.valueOf(val));
            }
            if(field.compareToIgnoreCase(Patients.Contract.DOB_ESTIMATED) == 0) {
                Log.d(TAG, "\tsetting '" + Patients.Contract.DOB_ESTIMATED + "'=" + val);
                mPatient.setConfirmed(Boolean.valueOf(val));
            }
        }
        Log.d(TAG, "...Updated Patient: " + String.valueOf(mPatient));
        // Only save to database after we are finished
        Log.d(TAG,"...finished=" + finished);
        if (finished) {
            uSubject = PatientWrapper.getOrCreate(getActivity(), mPatient);
        }
    }

    /**
     * Don't actually upload the patient when this gets called. Simply mark
     * the patient state as to be uploaded.
     */
    @Override
    public void uploadProcedureInBackground() {
        storeCurrentProcedure(true);
    }
    
    /** A task for loading the patient registration procedure.
     * 
     * @author Sana Development Team */
    class CreatePatientTask extends ProcedureLoaderTask {

        @Override
        protected ProcedureLoadResult doInBackground(ProcedureLoadRequest... params) {
            ProcedureLoadRequest load = params[0];
            instance = load.instance;
            intent = load.intent;

            ProcedureLoadResult result = new ProcedureLoadResult();
            Uri procedureUri = uProcedure;
            Procedure p = null;

            // Create a stub for the patient
            if (Uris.isEmpty(uSubject)) {
                mPatient = new PatientParcel();
                mPatient.setUuid(UUID.randomUUID().toString());
                mPatient.setSystemId("");
                mPatient.setFamily_name("");
                mPatient.setGiven_name("");
                mPatient.setGender("M");
                mPatient.setDob(new Date());
                uSubject = writeObject(mPatient, 0);

            } else {
                // Exists we reload into the Parcelable as below
                mPatient = new PatientParcel(PatientWrapper.get(getActivity(),
                        uSubject));
            }
            Log.d(TAG, "...using subject:" + uSubject);
            Log.d(TAG, "...   data: " + mPatient);

            // Patient is loaded now we loaded procedure script
            Uri procedure = uProcedure;
            String uuid = procedure.getLastPathSegment();
            String procedureXml = null;
            if(!UUIDUtil.isValid(uuid)){
                Log.d(TAG, "...long format for uuid? = " + uuid);
                uuid = ModelWrapper.getUuid(procedure,getActivity().getContentResolver());
                procedure = Uris.withAppendedUuid(Procedures.CONTENT_URI, uuid);
            }


            try {
                // Check for the
                int procedureResource = intent.getIntExtra(
                        Intents.EXTRA_PROCEDURE_ID, R.raw.findpatient);
                InputStream rs = getActivity().getResources()
                        .openRawResource(procedureResource);
                byte[] data = new byte[rs.available()];
                rs.read(data);
                procedureXml = new String(data);
                p = Procedure.fromXMLString(procedureXml);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
            if (p != null) {
                p.setInstanceUri(uSubject);
            }

            result.p = p;
            result.success = p != null;
            result.procedureUri = procedure;
            result.savedProcedureUri = uSubject;
            return result;
        }

        @Override
        protected void handleResult(ProcedureLoadResult result){
            requested--;
            hideProgressDialogFragment();
            if (result != null && result.success) {
                mProcedure = result.p;
                uEncounter = result.savedProcedureUri;
                logEvent(EventType.ENCOUNTER_LOAD_FINISHED, "");
                if (mProcedure != null){
                    mProcedure.setInstanceUri(uEncounter);
                    boolean useId = getActivity().getResources().getBoolean(
                            R.bool.display_registration_input_element_id);
                    Log.d(TAG, "...Setting page display id=" + useId);
                    mProcedure.setShowQuestionIds(useId);
                    createView();
                }
                else
                    logEvent(EventType.ENCOUNTER_LOAD_FAILED, "Null procedure");

            } else {
                // Show error
                logEvent(EventType.ENCOUNTER_LOAD_FAILED, "");
                getActivity().finish();
            }

        }
    }
    @Override
    public void deleteCurrentProcedure() {
        Log.i(TAG, "deleteCurrentProcedure()");
        try {
            if(objectFlag == FLAG_OBJECT_TEMPORARY){
                Log.d(TAG, "...flushing temporary object: " + uSubject);
                int deleted = getActivity().getContentResolver().delete
                        (uSubject,null,null);
                Log.d(TAG, "..." + deleted + " deleted");
            } else {
                Log.w(TAG, "...Patient exists but not updating");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Convenience wrapper around the older lookup listener method.
     * @param systemId
     */
    public void onPatientLookupSuccess(String systemId){
        Log.i(TAG,"onPatientLookupSuccess(String)");
        Patient patient = null;
        try {
                patient = PatientWrapper.getOneBySystemId(getActivity()
                                .getContentResolver(), systemId
                );
        } catch(Exception e){
            e.printStackTrace();
        }
        Log.d(TAG, "...patient=" + patient);
        uSubject = Uris.withAppendedUuid(Subjects.CONTENT_URI, patient.getUuid());
        onPatientLookupSuccess(patient);
    }

    protected Uri getOrCreateStub(ContentValues values){
        Uri stub = PatientWrapper.getOrCreate(getActivity(), values);
        return stub;
    }


    /**
     * Callback to handle when a patient look up succeeds. Will result in an
     * alert being displayed prompting the user to confirm that it is the
     * correct patient.
     */

    public void onPatientLookupSuccess(final Patient patient) {
        Log.i(TAG,"onPatientLookupSuccess(Patient)");
        logEvent(EventType.ENCOUNTER_LOOKUP_PATIENT_SUCCESS, patient.getSystemId());
        hideProgressDialogFragment();

        // TODO: should move error messages to BaseFragment
        StringBuilder message = new StringBuilder();
        message.append("Found patient record for ID ");
        message.append(patient.getSystemId());
        message.append("\n");

        message.append("First Name: ");
        message.append(patient.getGiven_name());
        message.append("\n");

        message.append("Last Name: ");
        message.append(patient.getFamily_name());
        message.append("\n");

        message.append("Gender: ");
        message.append(patient.getGender());
        message.append("\n");

        message.append("Is this the correct patient?");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.general_yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                //mProcedure.setPatientInfo(patientInfo);
                                // Delete the stub
                                getActivity().getContentResolver().delete
                                        (uSubject,null,null);
                                // set the subject as the confirmed
                                setObject(patient);
                                setObjectFlag(FLAG_OBJECT_UPDATED);
                                mProcedure.restoreAnswers(getModelMap(patient));
                                nextPage();
                            }
                        })
                .setNegativeButton(getResources().getString(R.string.general_no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                patient.setSystemId("");
                                setObject(patient);
                                setObjectFlag(FLAG_OBJECT_TEMPORARY);
                                if(mProcedure.current().hasElementWithId
                                        (Patients.Contract.PATIENT_ID)) {

                                    ((PatientIdElement) mProcedure.current()
                                            .getElementByType(
                                                    Patients.Contract.PATIENT_ID))
                                            .setAndRefreshAnswer("");
                                }
                            }
                        });
        AlertDialog alert = builder.create();
        if (!getActivity().isFinishing())
            alert.show();
    }

    protected final void setObject(Patient patient){
        mPatient = new PatientParcel(patient);
        uSubject = PatientWrapper.getOrCreate(getActivity(),mPatient);

        PatientInfo pi = null;
        if(patient != null) {
            pi = new PatientInfo();
            pi.setPatientBirthdate(patient.getDob());
            pi.setPatientFirstName(patient.getGiven_name());
            pi.setPatientIdentifier(patient.getSystemId());
            pi.setPatientGender(patient.getGender());
            pi.setPatientLastName(patient.getFamily_name());
        }
        mProcedure.setPatientInfo(pi);
    }

    protected Map<String, String> getModelMap(Patient patient){
        Map<String, String> map = new HashMap<String, String>();
        map.put(Patients.Contract.PATIENT_ID, patient.getSystemId());
        map.put(Patients.Contract.GIVEN_NAME, patient.getGiven_name());
        map.put(Patients.Contract.FAMILY_NAME, patient.getFamily_name());
        map.put(Patients.Contract.DOB, Dates.toSQL(patient.getDob()));
        map.put(Patients.Contract.GENDER, patient.getGender());
        map.put(Patients.Contract.LOCATION,((patient.getLocation() != null)
                ? patient.getLocation().getUuid():
                getString(R.string.cfg_default_location)));
        //map.put(Patients.Contract., patient.);
        return map;
    }

    protected Uri writeObject(Patient patient, int state){
        // Create Stub for patient
        ContentValues values = new ContentValues();
        values.put(Patients.Contract.PATIENT_ID, patient.getSystemId());
        values.put(Patients.Contract.FAMILY_NAME, patient.getFamily_name());
        values.put(Patients.Contract.GIVEN_NAME, patient.getGiven_name());
        values.put(Patients.Contract.DOB, DateUtil.format(patient.getDob()));
        values.put(Patients.Contract.GENDER, patient.getGender());
        values.put(Patients.Contract.STATE, state);
        //TODO update db and uncomment
        //values.put(Patients.Contract.CONFIRMED, patient.getConfirmed());
        //values.put(Patients.Contract.DOB_ESTIMATED, patient.isDobEstimated());
        values.put(Patients.Contract.LOCATION, ((patient.getLocation() != null)
                ? patient.getLocation().getUuid() :
                getString(R.string.cfg_default_location)));
        // Add the UUID if this is a temporary object-i.e we are creating
        if(state == 0) {
            values.put(Patients.Contract.UUID, patient.getUuid());
        }
        objectFlag = state;
        return PatientWrapper.getOrCreate(getActivity(), patient);
    }

    @Override
    public Intent getResult(String action){
        Log.d(TAG, "getResult(String)");
        Intent result = new Intent(action, uSubject);
        onSaveAppState(result);
        return result;
    }

    public int getObjectFlag(){
        return objectFlag;
    }

    public void setObjectFlag(int flag){
        objectFlag = flag;
    }

    /**
     * Trigger an update or create in the network layer depending on the state
     * of the patient object.
     */
    @Override
    public void uploadProcedureInBackground2() {
        Log.i(TAG, "uploadProcedureInBackground2()");
        // Stops any instrumentation activity in the background service
        Intent instrumentation = new Intent(getActivity(),
                InstrumentationService.class);
        getActivity().stopService(instrumentation);

        //
        Intent data = null;
        if(mProcedureListener != null){
            switch (getObjectFlag()) {
                case FLAG_OBJECT_UPDATED:
                    data = getResult(Intents.ACTION_UPDATE);
                    break;
                case FLAG_OBJECT_TEMPORARY:
                    data = getResult(Intents.ACTION_CREATE);
                    break;
                default:
            }
            data.putExtra(Intents.EXTRA_ON_COMPLETE, mProcedure.getOnComplete());
            mProcedureListener.onProcedureComplete(data);
        } else{
            data = getResult();
            data.putExtra(Intents.EXTRA_ON_COMPLETE, mProcedure.getOnComplete());
            getActivity().setResult(Activity.RESULT_OK, data);
            getActivity().finish();
        }
    }

    public void onCreateSuccess(Intent intent){
        Uri uri = Uri.parse(intent.getStringExtra(Response.MESSAGE));
        Patient subject = PatientWrapper.get(getActivity(), uri);
        if(!subject.getUuid().equalsIgnoreCase(mPatient.getUuid())){
            if(objectFlag == FLAG_OBJECT_TEMPORARY)
                getActivity().getContentResolver().delete(uSubject, null, null);
            mPatient = new PatientParcel(subject);
            uSubject = uri;
        }
    }

    /**
     * Flushes any temporary subject registration in the database and
     * then calls super method.
     */
    protected void onExitNoSave(){
        Log.i(TAG, "onExitNoSave()");
        if(objectFlag == FLAG_OBJECT_TEMPORARY) {
            int deleted = getActivity().getContentResolver().delete(uSubject, null, null);
            Log.d(TAG, "\tdeleted n=" + deleted);
        }
        super.onExitNoSave();
    }
}
