/**
 * Copyright (c) 2013, Sana
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sana nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL Sana BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.sana.android.app;

import java.util.Stack;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.util.SparseArray;

import org.sana.android.content.Intents;
import org.sana.android.content.Uris;
import org.sana.android.provider.Concepts;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Events;
import org.sana.android.provider.Instructions;
import org.sana.android.provider.Notifications;
import org.sana.android.provider.Observations;
import org.sana.android.provider.Observers;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Subjects;

/**
 * Maintains state for the application and starts activities based on the
 * supplied data
 * 
 * current state		next state		Intent launches				
 * INITIAL, MAIN		PICK			AuthenticationAcitivity 
 * PICK					RUN				Sana					
 * RUN					MAIN			no action				
 * default				INITIAL			AuthenticationAcitivity				
 *
 * @author Sana Development
 *
 */
public abstract class ActivityRunner implements IntentRunner{
	
	// Initialize to launch activity to open session 
	
    protected Uri mConcept = Concepts.CONTENT_URI;
    protected Uri mEncounter = Encounters.CONTENT_URI;
    protected Uri mEvent = Events.CONTENT_URI;
    protected Uri mInstruction = Instructions.CONTENT_URI;
    protected Uri mObserver = Observers.CONTENT_URI;
    protected Uri mObservation = Observations.CONTENT_URI;
    protected Uri mNotification = Notifications.CONTENT_URI;
    protected Uri mProcedure = Procedures.CONTENT_URI;
    protected Uri mSubject = Subjects.CONTENT_URI;
	
    protected Stack<Intent> stack =  new Stack<Intent>();
    
    // Session intents
    /** An Intent to pick a session */ 
    public static final Intent PICK_SESSION = new Intent(Intent.ACTION_PICK);
    static{
    	
    }
    
    public static final Intent PICK_OBSERVER = new Intent(Intent.ACTION_PICK);
    static{
    	PICK_OBSERVER.setDataAndType(Observers.CONTENT_URI, Observers.CONTENT_TYPE);
    }
    
    /** An intent for the Sana navigation screen */
    public static final Intent HOME = new Intent(Intents.ACTION_PICK_ACTIVITY);
    static{
    	
    }
    // Encounter Intents
    /** An Intent to pick an Encounter */
    public static final Intent PICK_ENCOUNTER= new Intent(Intent.ACTION_PICK);
    static{
    	PICK_ENCOUNTER.setDataAndType(Encounters.CONTENT_URI, Encounters.CONTENT_TYPE);
    }
    
    /** An intent to pick a subject */
    public static final Intent PICK_SUBJECT = new Intent(Intent.ACTION_PICK);
    static{
    	PICK_SUBJECT.setDataAndType(Subjects.CONTENT_URI, Subjects.CONTENT_TYPE);    	
    }
    
    /** An Intent to pick a procedure */
    public static final Intent PICK_PROCEDURE = new Intent(Intent.ACTION_PICK);
    static{
    	PICK_PROCEDURE.setDataAndType(Procedures.CONTENT_URI, Procedures.CONTENT_TYPE);
    	
    }
    
    public static final Intent RUN_PROCEDURE = new Intent(Intent.ACTION_VIEW);

    /** An Intent to pick an Encounter */
    public static final Intent PICK_NOTIFICATION= new Intent(Intent.ACTION_PICK);
    static{
    	PICK_ENCOUNTER.setDataAndType(Notifications.CONTENT_URI, Notifications.CONTENT_TYPE);
    }
    
    //--------------------------------------------------------------------------
    // MAPPINGS
    //--------------------------------------------------------------------------
    /* 
     * The next state is calculated by 
     * 
     * result code + action + data --> request code, intent
     * 
     * Hence, two maps for result code:
     * 
     *  Activity.RESULT_OK
     *  Ativity.RESULT_CANCEL
     *  
     * 
     */
    private static final SparseArray<Pair<Integer,Intent>> resultOk = new SparseArray<Pair<Integer,Intent>>(8);
    static{
    	// Default
    	resultOk.append(Uris.NULL, Pair.create(Uris.SUBJECT_ITEM,PICK_SUBJECT));
    	
    	
    	// session item --> select patient
    	resultOk.append(Uris.SUBJECT_DIR, Pair.create(Uris.SUBJECT_ITEM,PICK_SUBJECT));
    	
    	// subject selected --> pick_procedure
    	resultOk.append(Uris.SUBJECT_ITEM, Pair.create(Uris.PROCEDURE_ITEM,PICK_PROCEDURE));
    	resultOk.append(Uris.SUBJECT_UUID, Pair.create(Uris.PROCEDURE_ITEM,PICK_PROCEDURE));
    	
    	// procedure selected --> run procedure
    	resultOk.append(Uris.PROCEDURE_ITEM, Pair.create(Uris.ENCOUNTER_ITEM, RUN_PROCEDURE));
    	resultOk.append(Uris.PROCEDURE_UUID, Pair.create(Uris.ENCOUNTER_ITEM, RUN_PROCEDURE));
    	
    	// encounter completed --> next procedure
    	resultOk.append(Uris.ENCOUNTER_ITEM, Pair.create(Uris.PROCEDURE_ITEM, PICK_PROCEDURE));
    	resultOk.append(Uris.ENCOUNTER_UUID, Pair.create(Uris.PROCEDURE_ITEM, PICK_PROCEDURE));
    }
    
    // Cancel mappings
    private static final SparseArray<Pair<Integer,Intent>> resultCancel = new SparseArray<Pair<Integer,Intent>>(4);
    static{
    	resultCancel.append(Uris.PROCEDURE_DIR, Pair.create(Uris.PROCEDURE_ITEM,PICK_PROCEDURE));
    	resultCancel.append(Uris.PROCEDURE_ITEM, Pair.create(Uris.PROCEDURE_ITEM,PICK_PROCEDURE));
    }
    
    public ActivityRunner(Intent intent){
    	stack.push(intent);
    }
    
	public ActivityRunner(){
		this(new Intent(Intent.ACTION_MAIN));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sana.analytics.Runner#next(java.lang.Object)
	 */
	@Override
	public Intent next(Intent intent) {
		// get the previous state
		Intent request = (stack.size() >= 1)? stack.pop(): null;
		
		// check for a null intent equivalent to CANCEL
		if(intent == null){
			stack.push(handleCancel(request)); 
		} else {
			stack.push(handleOk(request, intent));
		}
		return Intents.copyOf(stack.peek());
	}

	/*
	 * Unwinds the INTENT stack to 
	 */
	protected Intent goTo(int requestCode){
		
		while(!stack.isEmpty()){
			if (Intents.parseActionDescriptor(stack.peek()) == requestCode)
				return stack.pop();
			else
				stack.pop();
		}
		return new Intent(Intents.ACTION_FINISH);
	}
	
	protected abstract Intent handleCancel(Intent request);
	
	protected abstract Intent handleOk(Intent request, Intent response);
	
	public int getState(){
		return Intents.parseActionDescriptor(stack.peek());
	}
	
	public Intent getIntent(){
		Intent intent = new Intent();
		if(!stack.empty()){
			intent = Intents.copyOf(stack.peek());
			intent.putExtras(getDataBundle());
		}
		return intent;
	}


	
	public Intent setData(Intent intent, Uri data){
		switch(Uris.getDescriptor(data)){
		case Uris.CONCEPT_DIR:
		case Uris.CONCEPT_ITEM:
		case Uris.CONCEPT_UUID:
			intent.putExtra(Intents.EXTRA_CONCEPT, data);
			break;
		case Uris.ENCOUNTER_DIR:
		case Uris.ENCOUNTER_ITEM:
		case Uris.ENCOUNTER_UUID:
			intent.putExtra(Intents.EXTRA_ENCOUNTER, data);
			break;
		case Uris.EVENT_DIR:
		case Uris.EVENT_ITEM:
		case Uris.EVENT_UUID:
			intent.putExtra(Intents.EXTRA_EVENT, data);
			break;
		case Uris.INSTRUCTION_DIR:
		case Uris.INSTRUCTION_ITEM:
		case Uris.INSTRUCTION_UUID:
			intent.putExtra(Intents.EXTRA_INSTRUCTION, data);
			break;
		case Uris.OBSERVATION_DIR:
		case Uris.OBSERVATION_ITEM:
		case Uris.OBSERVATION_UUID:
			intent.putExtra(Intents.EXTRA_OBSERVATION, data);
			break;
		case Uris.OBSERVER_DIR:
		case Uris.OBSERVER_ITEM:
		case Uris.OBSERVER_UUID:
			intent.putExtra(Intents.EXTRA_OBSERVER, data);
			break;
		case Uris.NOTIFICATION_DIR:
		case Uris.NOTIFICATION_ITEM:
		case Uris.NOTIFICATION_UUID:
			intent.putExtra(Intents.EXTRA_NOTIFICATION, data);
			break;
		case Uris.PROCEDURE_DIR:
		case Uris.PROCEDURE_ITEM:
		case Uris.PROCEDURE_UUID:
			intent.putExtra(Intents.EXTRA_PROCEDURE, data);
			break;
		case Uris.SUBJECT_DIR:
		case Uris.SUBJECT_ITEM:
		case Uris.SUBJECT_UUID:
			intent.putExtra(Intents.EXTRA_SUBJECT, data);
			break;
		}
		return intent;
	}
	
	public void setData(Uri uri){
		int code = Uris.getTypeDescriptor(uri);
		setData(code, uri);
	}
	
	public void setData(int code, Uri uri){
		switch(code){
		case Uris.CONCEPT_DIR:
		case Uris.CONCEPT_ITEM:
		case Uris.CONCEPT_UUID:
			mConcept = uri;
			break;
		case Uris.ENCOUNTER_DIR:
		case Uris.ENCOUNTER_ITEM:
		case Uris.ENCOUNTER_UUID:
			mEncounter = uri;
			break;
		case Uris.EVENT_DIR:
		case Uris.EVENT_ITEM:
		case Uris.EVENT_UUID:
			mEvent = uri;
			break;
		case Uris.INSTRUCTION_DIR:
		case Uris.INSTRUCTION_ITEM:
		case Uris.INSTRUCTION_UUID:
			mInstruction = uri;
			break;
		case Uris.OBSERVATION_DIR:
		case Uris.OBSERVATION_ITEM:
		case Uris.OBSERVATION_UUID:
			mObservation = uri;
			break;
		case Uris.OBSERVER_DIR:
		case Uris.OBSERVER_ITEM:
		case Uris.OBSERVER_UUID:
			mObserver = uri;
			break;
		case Uris.NOTIFICATION_DIR:
		case Uris.NOTIFICATION_ITEM:
		case Uris.NOTIFICATION_UUID:
			mNotification = uri;
			break;
		case Uris.PROCEDURE_DIR:
		case Uris.PROCEDURE_ITEM:
		case Uris.PROCEDURE_UUID:
			mProcedure = uri;
			break;
		case Uris.SUBJECT_DIR:
		case Uris.SUBJECT_ITEM:
		case Uris.SUBJECT_UUID:
			mSubject = uri;
			break;
		}
	}
	
	/**
	 * Returns the data state bundle 
	 * @return
	 */
	private Bundle getDataBundle(){
		Bundle bundle = new Bundle();
		bundle.putParcelable(Intents.EXTRA_CONCEPT, mConcept);
		bundle.putParcelable(Intents.EXTRA_ENCOUNTER, mEncounter);
		bundle.putParcelable(Intents.EXTRA_EVENT, mEvent);
		bundle.putParcelable(Intents.EXTRA_INSTRUCTION, mInstruction);
		bundle.putParcelable(Intents.EXTRA_NOTIFICATION, mNotification);
		bundle.putParcelable(Intents.EXTRA_OBSERVATION, mObservation);
		bundle.putParcelable(Intents.EXTRA_OBSERVER, mObserver);
		bundle.putParcelable(Intents.EXTRA_PROCEDURE, mProcedure);
		bundle.putParcelable(Intents.EXTRA_SUBJECT, mSubject);
		return bundle;
	}
}