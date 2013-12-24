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

import java.util.ArrayList;

import org.sana.android.content.Intents;
import org.sana.android.content.Uris;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Observers;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Subjects;
import org.sana.android.util.Logf;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

/**
 * This is the default workflow which mimics the behavior of the version 1.x 
 * clients with the addition of the authentication screen.
 * 
 * The Activity flow is given by
 * 1. Main returns PICK + Observer 
 * 2. Pick + Observer returns OK + data || CANCEL
 *  a. If CANCEL then ACTION_FINISH
 *  b. If OK + data then PICK + Subject
 * 3. PICK + Subject returns OK + data || CANCEL
 * 	a. If CANCEL then 
 *  b. If OK + data then PICK + Procedure
 * 4. PICK + Procedure returns OK + data || CANCEL
 *  a. If CANCEL then PICK + SUBJECT
 *  b. If OK + data then RUN + Procedure
 * 5. RUN + Procedure returns OK + data || CANCEL
 *  a. data + CANCEL => NavigationActivity (3)
 *  b. data + OK => NavigationActivity (3)
 * 6. ViewModel => NavigationActivity (3)
 *  
 * @author Sana Development
 *
 */
public class SSIActivityRunner implements IntentRunner {
	public static final String TAG = SSIActivityRunner.class.getSimpleName();
	
	public SSIActivityRunner(){
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sana.analytics.Runner#next(java.lang.Object)
	 */
	@Override
	public Intent next(Intent response) {
		Intent request = response.getParcelableExtra(Intents.EXTRA_REQUEST);
		// Log input and output if we are debugging
		String reqLog = (request != null)? request.toUri(Intent.URI_INTENT_SCHEME):"null";
		String respLog = (response != null)?response.toUri(Intent.URI_INTENT_SCHEME):"null";
		Logf.D(TAG, "next(Intent,Intent)", String.format("request=%s;response=%s", reqLog, respLog));
				
		// check for a null intent equivalent to CANCEL
		if(response.getAction().equals(Intents.ACTION_CANCEL)){
			response = handleCancel(request, response); 
		} else {
			response = handleOk(request, response);
		}
		return response;
	}
	
	protected Intent handleCancel(Intent request, Intent response){
		Logf.I(TAG, "handleCancel(Intent,Intent)");
	
		// Default Intent we return
		Intent output = new Intent(Intents.ACTION_FINISH);
		Uri uri = (request != null)?
				((request.getData() != null)?request.getData():Uri.EMPTY):
				Uri.EMPTY;
				
		int uDescriptor = Uris.getDescriptor(uri);
		
		switch(Intents.parseActionDescriptor(request)){
		case Intents.PICK:
			switch(uDescriptor){
			case Uris.SUBJECT_DIR:
				output.setAction(Intent.ACTION_PICK).setData(Observers.CONTENT_URI);
				break;
			case Uris.PROCEDURE_DIR:
				output.setAction(Intent.ACTION_PICK).setData(Subjects.CONTENT_URI);
	        	break;
			case Uris.ENCOUNTER_DIR:
				output.setAction(Intent.ACTION_PICK).setData(Procedures.CONTENT_URI);
			default:
				output.setAction(Intents.ACTION_FINISH);
			}
			break;
		// Expect a Cancel back on a view - for now we always go 
		case Intents.VIEW:
			switch(uDescriptor){
			case Uris.SUBJECT_ITEM:
			case Uris.SUBJECT_UUID:
				output.setAction(Intent.ACTION_PICK).setData(Procedures.CONTENT_URI);
				break;
			// Should restart the 
			case Uris.PROCEDURE_ITEM:
			case Uris.PROCEDURE_UUID:
				output.setAction(Intents.ACTION_RUN_PROCEDURE).setData(uri);
				break;
			case Uris.ENCOUNTER_ITEM:
			case Uris.ENCOUNTER_UUID:
				output.setAction(Intent.ACTION_PICK).setData(Encounters.CONTENT_URI);
				break;
			default:
			}
			break;
		case Intents.RUN:
		case Intents.RUN_PROCEDURE:
			output.setAction(Intent.ACTION_PICK).setData(Procedures.CONTENT_URI);
			break;
		case Intents.RESUME_PROCEDURE:
			output.setAction(Intent.ACTION_PICK).setData(Encounters.CONTENT_URI);
			break;
		default:
		}
		return output;
	}
	
	protected Intent handleOk(Intent request, Intent response){
		Logf.I(TAG, "handleOK(Intent,Intent)");
		Intent output = new Intent(Intents.ACTION_FINISH);
		ArrayList<Intent> tasks = new ArrayList<Intent>();
		Uri uri = (response != null)?
				((response.getData() != null)?response.getData():Uri.EMPTY):
				Uri.EMPTY;
		int uDescriptor = Uris.getDescriptor(uri);
		switch(Intents.parseActionDescriptor(request)){
		case Intents.MAIN:
			output.setAction(Intent.ACTION_PICK).setData(Observers.CONTENT_URI);
			break;
		case Intents.PICK:
			switch(uDescriptor){
			case Uris.OBSERVER_ITEM:
			case Uris.OBSERVER_UUID:
				output.setAction(Intent.ACTION_PICK).setData(Subjects.CONTENT_URI);
				// TODO read this from a file
				//tasks.add((new Intent(Intent.ACTION_SYNC)).setData(Subjects.CONTENT_URI));
				output.putExtra(Intents.EXTRA_OBSERVER, uri);
				break;
			case Uris.SUBJECT_ITEM:
			case Uris.SUBJECT_UUID:
				output.setAction(Intent.ACTION_PICK).setData(Procedures.CONTENT_URI);
				output.putExtra(Intents.EXTRA_SUBJECT, uri);
				// TODO read this from a file
				//tasks.add((new Intent(Intent.ACTION_SYNC)).setData(Procedures.CONTENT_URI));
				break;
			case Uris.PROCEDURE_ITEM:
			case Uris.PROCEDURE_UUID:
				output = new Intent(Intent.ACTION_RUN, uri);
				output.putExtra(Intents.EXTRA_PROCEDURE, uri);
	        	break;
			case Uris.ENCOUNTER_ITEM:
			case Uris.ENCOUNTER_UUID:
				output.setAction(Intent.ACTION_VIEW).setData(uri);
				output.putExtra(Intents.EXTRA_ENCOUNTER, uri);
			default:
			}
			break;
		case Intents.VIEW:
			switch(uDescriptor){
			case Uris.SUBJECT_ITEM:
			case Uris.SUBJECT_UUID:
				output.setAction(Intent.ACTION_PICK).setData(Procedures.CONTENT_URI);
				break;
			case Uris.PROCEDURE_ITEM:
			case Uris.PROCEDURE_UUID:
				output = new Intent(Intents.ACTION_RUN_PROCEDURE, uri);
				break;
			case Uris.ENCOUNTER_ITEM:
			case Uris.ENCOUNTER_UUID:
				output.setAction(Intent.ACTION_PICK).setData(Encounters.CONTENT_URI);
				break;
			default:
			}
			
			break;
		case Intents.RUN:
		case Intents.RUN_PROCEDURE:
			output.setAction(Intent.ACTION_PICK).setData(Procedures.CONTENT_URI);
			break;
		case Intents.RESUME_PROCEDURE:	
			output.setAction(Intent.ACTION_PICK).setData(Encounters.CONTENT_URI);
			break;
		default:
			
		}
		if(tasks.size() > 0)
			output.putExtra(Intents.EXTRA_TASKS, tasks);
		return output;
	
	}
	
}
