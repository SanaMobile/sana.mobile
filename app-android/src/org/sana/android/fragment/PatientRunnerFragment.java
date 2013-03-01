
package org.sana.android.fragment;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.sana.R;
import org.sana.android.db.PatientInfo;
import org.sana.android.provider.Patients;
import org.sana.android.provider.Events.EventType;

import org.sana.android.procedure.Procedure;
import org.sana.android.procedure.ProcedureElement;
import org.sana.android.procedure.ProcedureParseException;
import org.xml.sax.SAXException;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/** Class for creating a new patient.
 * 
 * @author Sana Development Team */
public class PatientRunnerFragment extends BaseRunnerFragment {

    public static final String TAG = PatientRunnerFragment.class.getSimpleName();
    
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

            loadProgressDialog = new ProgressDialog(getActivity());
            loadProgressDialog.setMessage(getString(R.string.dialog_loading_procedure));
            loadProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            if (!getActivity().isFinishing())
                loadProgressDialog.show();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void logEvent(EventType type, String value) {
        // TODO: log patient specific events
    }

    /** 
     * Takes the answer map from the procedure and stores the appropriate fields
     * in the patient db.
     * 
     * This WILL NOT signal
     * to the upload service that it is ready for upload. The procedure or
     * encounter for this patient will do the actual creation of the patient
     * for the back-end.
     * 
     * @param finished -- Whether to set the patient as finished creating.
     */
    @Override
    public void storeCurrentProcedure(boolean finished) {
        if (mProcedure != null && thisSavedProcedure != null) {
            
            PatientInfo patientInfo = mProcedure.getPatientInfo();
            
            // Extract patient information from procedure
            ProcedureElement patientId = mProcedure.current().getElementByType("patientId");
            if (patientId != null) {
                patientInfo.setPatientIdentifier(patientId.getAnswer());
            }
            
            ProcedureElement patientFirstName = mProcedure.current().getElementByType("patientFirstName");
            if (patientFirstName != null) {
                patientInfo.setPatientFirstName(patientFirstName.getAnswer());
            }
            
            ProcedureElement patientLastName = mProcedure.current().getElementByType("patientLastName");
            if (patientLastName != null) {
                patientInfo.setPatientLastName(patientLastName.getAnswer());
            }
            
            ProcedureElement patientDobMonth = mProcedure.current().getElementByType("patientBirthdateMonth");
            ProcedureElement patientDobDay = mProcedure.current().getElementByType("patientBirthdateDay");
            ProcedureElement patientDobYear = mProcedure.current().getElementByType("patientBirthdateYear");
            if (patientDobMonth != null && patientDobDay != null && patientDobYear != null) {
                String month = patientDobMonth.getAnswer();
                String day = patientDobDay.getAnswer();
                String year = patientDobYear.getAnswer();
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MMMMM/dd", Locale.getDefault());
                    Date patientDob = dateFormat.parse(year + "/" + month + "/" + day);
                    patientInfo.setPatientBirthdate(patientDob);
                } catch (ParseException e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
            
            ProcedureElement patientGender = mProcedure.current().getElementByType("patientGender");
            if (patientGender != null) {
                patientInfo.setPatientGender(patientGender.getAnswer());
            }
            
            // TODO figure out patient image
            ProcedureElement patientImg = mProcedure.current().getElementByType("patientPhoto");
            if (patientImg != null) {
                Log.d(TAG, "patientImg answer: " + patientImg.getAnswer());
            }
            
            // Store extracted patient information into db
            ContentValues cv = new ContentValues();
            cv.put(Patients.Contract.PATIENT_ID, patientInfo.getPatientIdentifier());
            cv.put(Patients.Contract.GIVEN_NAME, patientInfo.getPatientFirstName());
            cv.put(Patients.Contract.FAMILY_NAME, patientInfo.getPatientLastName());
            // TODO: what is the format of a patient date?
//            if (patientInfo.getPatientBirthdate() != null) 
//                cv.put(PatientSQLFormat.PATIENT_DOB, patientInfo.getPatientBirthdate().get);
            cv.put(Patients.Contract.GENDER, patientInfo.getPatientGender());;
            
            if (finished)
                cv.put(Patients.Contract.STATE, 1);

            int updatedObjects = getActivity().getContentResolver()
                    .update(thisSavedProcedure, cv, null, null);
            Log.i(TAG, "patientInfo updated " + updatedObjects
                    + " objects. (SHOULD ONLY BE 1)");
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
    
    /** A task for loading the findpatient procedure.
     * 
     * @author Sana Development Team */
    class CreatePatientTask extends ProcedureLoaderTask {

        /** {@inheritDoc} */
        @Override
        protected ProcedureLoadResult doInBackground(ProcedureLoadRequest... params) {
            ProcedureLoadRequest load = params[0];
            instance = load.instance;
            intent = load.intent;

            ProcedureLoadResult result = new ProcedureLoadResult();
            if (instance == null && !intent.hasExtra("savedProcedureUri")) {
                
                // New Patient
                Uri procedure = intent.getData();
                String procedureXml = "";
                try {
                    InputStream rs = getActivity().getResources()
                            .openRawResource(R.raw.findpatient);
                    byte[] data = new byte[rs.available()];
                    rs.read(data);
                    procedureXml = new String(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Record that we are creating a new patient
                // TODO: do we want to log events (tracking) for creating a new
                // patient?
                // logEvent(EventType.ENCOUNTER_LOAD_NEW_ENCOUNTER,
                // procedure.toString());
                
                ContentValues cv = new ContentValues();
                cv.put(Patients.Contract.STATE, 0);
                
                thisSavedProcedure = getActivity().getContentResolver().insert(
                		Patients.CONTENT_URI, cv);
                
                Log.i(TAG, "onCreate() : uri = " + procedure.toString()
                        + " savedUri = " + thisSavedProcedure);
                
                Procedure p = null;
                try {
                    p = Procedure.fromXMLString(procedureXml);
                } catch (IOException e) {
                    Log.e(TAG, "Error loading procedure from XML: "
                            + e.toString());
                    e.printStackTrace();
                    logException(e);
                } catch (ParserConfigurationException e) {
                    Log.e(TAG, "Error loading procedure from XML: "
                            + e.toString());
                    e.printStackTrace();
                    logException(e);
                } catch (SAXException e) {
                    Log.e(TAG, "Error loading procedure from XML: "
                            + e.toString());
                    e.printStackTrace();
                    logException(e);
                } catch (ProcedureParseException e) {
                    Log.e(TAG, "Error loading procedure from XML: "
                            + e.toString());
                    e.printStackTrace();
                    logException(e);
                } catch (OutOfMemoryError e) {
                    Log.e(TAG, "Can't load procedure, out of memory.");
                    result.errorMessage = "Out of Memory.";
                    e.printStackTrace();
                    logException(e);
                }
                if (p != null) {
                    p.setInstanceUri(thisSavedProcedure);
                }

                result.p = p;
                result.success = p != null;
                result.procedureUri = procedure;
                result.savedProcedureUri = thisSavedProcedure;
            } else {
                // TODO: saved patient scenario. should have a tab section for
                // saved patients
            }

            return result;
        }
    }
}
