package org.sana.android.fragment;

import java.net.URISyntaxException;
import java.util.UUID;

import org.json.JSONObject;
import org.sana.android.activity.ProcedureRunner;
import org.sana.android.content.Uris;
import org.sana.android.content.core.ObservationWrapper;
import org.sana.android.db.EncounterDAO;
import org.sana.android.db.EventDAO;
import org.sana.android.db.ModelWrapper;
import org.sana.android.procedure.ProcedureElement;
import org.sana.android.provider.BaseContract;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Events.EventType;
import org.sana.android.provider.Observations;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

/**
 * Class for running a new encounter.
 * 
 * @author Sana Development Team
 */
public class ProcedureRunnerFragment extends BaseRunnerFragment {
	public static final String TAG = ProcedureRunnerFragment.class.getSimpleName();
    /** {@inheritDoc} */
    @Override
    protected void loadProcedure(Bundle instance) {
    	Log.d(TAG, "loadProcedure(Bundle)");
        onUpdateAppState(getActivity().getIntent().getExtras());
        // Load procedure
        if (mProcedure == null) {
            ProcedureLoadRequest request = new ProcedureLoadRequest();
            request.instance = instance;
            request.intent = getActivity().getIntent();

            logEvent(EventType.ENCOUNTER_LOAD_STARTED, "");
            new ProcedureLoaderTask().execute(request);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void logEvent(EventType type, String value) {
        String savedProcedureGuid = "";
        String patientId = (Uris.isEmpty(uSubject))? "NOT SET": uSubject.getLastPathSegment();
        String userId = (Uris.isEmpty(uObserver))? "NOT SET": uObserver.getLastPathSegment();

        if (uEncounter != null) {
            savedProcedureGuid = EncounterDAO.getEncounterGuid(getActivity(),
                    uEncounter);
        }
        /*
        if (mProcedure != null) {
            PatientInfo pi = mProcedure.getPatientInfo();
            if (pi != null) {
                patientId = pi.getPatientIdentifier();
            } else {
                // TODO find the patient ID in the form and look at its answer
            }
        } 
        */
        // TODO lookup current user
        EventDAO.registerEvent(getActivity(), type, value, savedProcedureGuid,
                patientId, userId); 
    }
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    }

    public void storeCurrentProcedure(boolean finished) {
    	this.storeCurrentProcedure(finished, true);
    }
    /**
     * Serializes the current procedure to the database. Takes the answers map
     * from the procedure, serializes it to JSON, and stores it. If finished is
     * set, then it will set the procedure's row to finished. This will signal
     * to the upload service that it is ready for upload.
     * 
     * @param finished -- Whether to set the procedure as ready for upload.
     */
    @Override
    public void storeCurrentProcedure(boolean finished, boolean skipHidden) {
    	java.util.Map<String,ProcedureElement> map = mProcedure.current().getElementMap();

    	for(ProcedureElement el:map.values()){
    		ProcedureElement.ElementType type = el.getType();
    		// skip TEXT types
    		if(type.equals(ProcedureElement.ElementType.TEXT))
    			continue;

    		// for non TEXT types we want to be certain we have an entry in the 
    		// Observation table
    		mData = ObservationWrapper.getReferenceByEncounterAndId(
    				getActivity().getContentResolver(),
    				uEncounter.toString(),
    				el.getId()
    				);
    		// Map values to an Observation
    		ContentValues vals = new ContentValues();
    		vals.put(Observations.Contract.ENCOUNTER, uEncounter.toString());
    		vals.put(Observations.Contract.SUBJECT, uSubject.toString());
    		vals.put(Observations.Contract.CONCEPT , el.getConcept());
    		vals.put(Observations.Contract.ID, el.getId());


    		//if(!TextUtils.isEmpty(el.getAnswer())){
    		Log.d(TAG+".storeCurrentProcedure()", "observation uri ::" + mData);
    		if(mData == Observations.CONTENT_URI){
    			vals.put(Observations.Contract.UUID, UUID.randomUUID().toString());
    			mData = getActivity().getContentResolver().insert(Observations.CONTENT_URI, vals);
    		}
    		Log.d(TAG, "uri ::" + mData);
    		boolean updated = false;
    		switch(type){
    		case TEXT:
    			break;
    		case HIDDEN:
    			vals.put(Observations.Contract.VALUE, el.getAnswer());
    			if(!skipHidden && !TextUtils.isEmpty(el.getAction())){
    				Intent intent;
    				try {
    					Intent reply = new Intent();
    					reply.setClass(getActivity().getApplicationContext(), 
    							ProcedureRunner.class);
    					reply.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    					reply.putExtra(ProcedureRunner.INTENT_KEY_STRING, 
    							ProcedureRunner.OBSERVATION_RESULT_CODE);
    					reply.putExtra("id", el.getId());
    					// the getCurrentIndex() returns a 1 based index
    					reply.putExtra("page", this.mProcedure.getCurrentIndex() - 1);
    					PendingIntent replyTo = PendingIntent.getActivity(
    							getActivity().getApplicationContext(), 
    							ProcedureRunner.OBSERVATION_RESULT_CODE, 
    							reply, 0);
    					intent = Intent.parseUri(el.getAction(), 
    							Intent.URI_INTENT_SCHEME);
    					intent.setData(mData);
    					intent.putExtra(Intent.EXTRA_INTENT, replyTo);
    					intent.putExtra("extra_data", reply.getExtras());
    					getActivity().startService(intent);
    				} catch (URISyntaxException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    			}
    			break;
    		case PICTURE:
    			String[] answers = (!TextUtils.isEmpty(el.getAnswer()))?
    					el.getAnswer().split(","):
    						new String[]{};
    					for(String answer:answers){
    						vals.put(Observations.Contract.VALUE, answer);
    						vals.put(Observations.Contract.PARENT, mData.toString());
    						boolean exists = false;
    						Cursor c = null;
    						Uri mObs = Uri.EMPTY;
    						long id = -1;
    						try{
    							c = getActivity().getContentResolver().query(
    									Observations.CONTENT_URI,
    									new String[]{ Observations.Contract._ID },
    									Observations.Contract.ID + "= ? AND " 
    											+ Observations.Contract.PARENT + "= ? AND "
    											+ Observations.Contract.ENCOUNTER + "= ? ", 
    											new String[]{ el.getId(), 
    											mData.toString(), 
    											uEncounter.toString()},
    											null);
    							if(c != null && c.moveToFirst()){
    								exists = true;
    								id = c.getLong(0);
    								mObs = ContentUris.withAppendedId(Observations.CONTENT_URI, id);
    							}
    						} catch (Exception e){

    						} finally {
    							if(c != null) c.close();
    						}
    						if(!exists)
    							getActivity().getContentResolver().insert(
    									Observations.CONTENT_URI, vals);
    						else {
    							getActivity().getContentResolver().update(
    									mObs, vals, null, null);
    						}
    					}
    					break;
    		default:
    			vals.put(Observations.Contract.VALUE, el.getAnswer());
    			getActivity().getContentResolver().update(mData, vals, null, null);
    			break;
    		}

    		Log.d(TAG, "updated: " + updated + "::" + mData);
    		Log.d(TAG, String.format("{ 'id': %s, 'concept': %s, 'value': %s", 
    				el.getId(), el.getConcept(), el.getAnswer()));	
    		
    	}
    	// This handles the old API we are still using for display
    	// TODO get rid of this.
    	if (mProcedure != null && uEncounter != null) {
    		JSONObject answersMap = new JSONObject(mProcedure.toAnswers());
    		String json = answersMap.toString();

    		ContentValues cv = new ContentValues();
    		cv.put(Encounters.Contract.STATE, json);

    		if (finished)
    			cv.put(Encounters.Contract.FINISHED, finished);

    		int updatedObjects = getActivity().getContentResolver().update(uEncounter, cv,
    				null, null);
    		Log.i(TAG, "storeCurrentProcedure updated " + updatedObjects
    				+ " objects. (SHOULD ONLY BE 1)");
    	}
    }
    
}
