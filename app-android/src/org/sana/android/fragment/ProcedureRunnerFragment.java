package org.sana.android.fragment;

import org.sana.R;
import org.sana.android.db.EncounterDAO;
import org.sana.android.db.EventDAO;
import org.sana.android.db.PatientInfo;
import org.sana.android.provider.Events.EventType;

import android.app.ProgressDialog;
import android.os.Bundle;

/**
 * Class for running a new encounter.
 * 
 * @author Sana Development Team
 */
public class ProcedureRunnerFragment extends BaseRunnerFragment {

    /** {@inheritDoc} */
    @Override
    protected void loadProcedure(Bundle instance) {
        // Load procedure
        if (mProcedure == null) {
            ProcedureLoadRequest request = new ProcedureLoadRequest();
            request.instance = instance;
            request.intent = getActivity().getIntent();

            logEvent(EventType.ENCOUNTER_LOAD_STARTED, "");
            new ProcedureLoaderTask().execute(request);
            
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
        String savedProcedureGuid = "";
        String patientId = "";
        String userId = "";

        if (thisSavedProcedure != null) {
            savedProcedureGuid = EncounterDAO.getEncounterGuid(getActivity(),
                    thisSavedProcedure);
        }

        if (mProcedure != null) {
            PatientInfo pi = mProcedure.getPatientInfo();
            if (pi != null) {
                patientId = pi.getPatientIdentifier();
            } else {
                // TODO find the patient ID in the form and look at its answer
            }
        }
        // TODO lookup current user
        EventDAO.registerEvent(getActivity(), type, value, savedProcedureGuid,
                patientId, userId); 
    }

}
