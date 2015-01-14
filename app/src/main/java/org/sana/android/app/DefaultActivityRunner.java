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

import java.net.URISyntaxException;

import org.sana.android.content.Intents;
import org.sana.android.content.Uris;
import org.sana.android.util.Logf;

import android.content.Intent;
import android.net.Uri;

/**
 * This is the default workflow which mimics the behavior of the version 1.x 
 * clients with the addition of the authentication screen.
 * 
 * The Activity flow is given by
 * 1. Main => AuthenticationActivity 
 * 2. AuthenticationActivity => data + OK,CANCEL
 *  a. null + CANCEL => AuthenticationActivity (1)
 *  b. Data + OK => NavigationActivity
 * 3. NavigationActivity => Intent + OK,CANCEL
 * 	    Intent E [SubjectList, ProcedureList, EncounterList, NotificationList]
 * 4. ModelList => data + OK,CANCEL
 *  a. data + CANCEL => NavigationActivity (3)
 *  b. data +OK => Uri + OK
 *     i. Uri = DIR => InsertModel
 *     ii.Uri = Item => ViewModel (3)
 * 5. InsertModel => data + OK,CANCEL
 *  a. data + CANCEL => NavigationActivity (3)
 *  b. data + OK => NavigationActivity (3)
 * 6. ViewModel => NavigationActivity (3)
 *  
 * @author Sana Development
 *
 */
public class DefaultActivityRunner extends ActivityRunner {
	public static final String TAG = DefaultActivityRunner.class.getSimpleName();
	
	public DefaultActivityRunner(){
		super();
	}
	
	public DefaultActivityRunner(Intent intent){
		super(intent);
	}
	

	public Intent evaluate(Intent input) {
		// Log input if we are debugging
		Logf.I(TAG, "next(Intent)", String.format("input=%s", 
				((input != null)?input.toUri(Intent.URI_INTENT_SCHEME):"null")));
	
		Intent output = null;
		switch(Intents.parseActionDescriptor(input)){
		// CREATE
		case Intents.INSERT:
		case Intents.INSERT_OR_EDIT:
			
			break;
		// READ
		case Intents.PICK:
			break;
		case Intents.PICK_ACTIVITY:
		
			break;
		// UPDATE
		case Intents.EDIT:
			
			break;
		// DELETE
		case Intents.DELETE:

			break;
		default:
			
		}
		return output;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sana.analytics.Runner#next(java.lang.Object)
	 */
	@Override
	public Intent next(Intent response) {
		// get the previous state
		Intent request = (stack.size() >= 1)? stack.pop(): null;
		
		// Log request and response
		String reqLog = (request != null)?request.toUri(Intent.URI_INTENT_SCHEME):"null";
		String respLog = (response != null)?response.toUri(Intent.URI_INTENT_SCHEME):"null";
		Logf.I(TAG, "next(Intent)", String.format("request=%s;response=%s", reqLog, respLog));
		
		// check for a null intent equivalent to CANCEL
		if(response == null){
			stack.push(handleCancel(request)); 
		} else {
			stack.push(handleOk(request, response));
		}
		return Intents.copyOf(stack.peek());
	}
	
	@Override
	protected Intent handleCancel(Intent request){
		// Gives us the requestCode of the originating request 
		int requestCode = (request != null)
				? Intents.parseActionDescriptor(request): Intents.NULL;
		Uri uri = (request != null)? request.getData(): Uri.EMPTY;
		Logf.W(TAG, "handleCancel(Intent)", "requestCode=" + requestCode 
				+";descriptor=" + Uris.getDescriptor(uri));
		switch(requestCode){
		case Intents.MAIN:
			// top of stack is MAIN
			return PICK_OBSERVER;
		case Intents.NULL:
			// If stack was empty send finish code
			Logf.D(TAG, "handleCancel(Intent)","....at NULL");
			return new Intent(Intents.ACTION_FINISH);
		case Intents.PICK_ACTIVITY:
			// top was Navigation activity so we are effectively logging out
			Logf.D(TAG, "handleCancel(Intent)","....at PICK_ACTIVITY");
			return PICK_OBSERVER;
		case Intents.PICK:
			// Check if we are pressing back from an observer pick which is 
			// effectively a finish for Main
			Logf.D(TAG, "handleCancel(Intent)","....at PICK");
			if(Uris.getDescriptor(uri) == Uris.OBSERVER_DIR)
				return new Intent(Intents.ACTION_FINISH);
			else
				return goTo(Intents.PICK_ACTIVITY);
		case Intents.VIEW:
			// Return to the pick immediately below
			Logf.D(TAG,"handleCancel(Intent)", "....at VIEW");
			Intent intent = goTo(Intents.PICK);
			return intent;
		default:
			// everything else we unwind the stack until we get to the 
			// navigation screen or Main
			Logf.D(TAG, "handleCancel(Intent)","....at default");
			while(!stack.isEmpty()){
				int d = Intents.parseActionDescriptor(stack.peek());
				if (d == Intents.PICK_ACTIVITY)
					return stack.pop();
				else if(d == Intents.MAIN){
					return PICK_OBSERVER;
				}
					
			}
			// The stack is empty-should never get here
			return new Intent(Intents.ACTION_FINISH);
		}
	}
		
	@Override
	protected Intent handleOk(Intent request, Intent response){
		int requestCode = (request != null)
				? Intents.parseActionDescriptor(request): Intents.NULL;
		Uri uri = (response != null)? response.getData(): Uri.EMPTY;
		Logf.D(TAG, "handleOk(Intent)", "requestCode: "+ requestCode 
				+", response descriptor: " + Uris.getDescriptor(uri));
		boolean m = false;
		// determine and return what replaces the top of the stack
		switch(requestCode){
		case Intents.NULL:
			Logf.D(TAG, "handleOk(Intent)","....at NULL");
			// If stack was empty send finish code
			if(request == null){
				Logf.D(TAG, ".... Empty stack or unrecognizable Uri");
				return new Intent(Intents.ACTION_FINISH);
			}else{
				Logf.D(TAG,"handleOk(Intent)", ".... Returning to pick activity");
				return goTo(Intents.PICK_ACTIVITY);
			}
		case Intents.MAIN:
			Logf.D(TAG, "handleOk(Intent)","....at MAIN");
			// MAIN should be root of stack so we 
			return PICK_OBSERVER;
		case Intents.PICK_ACTIVITY:
			Logf.D(TAG,"handleOk(Intent)", "....at PICK_ACTIVITY");
			// the returned intent should hold the next intent to launch as 
			// the data 
			try {
				return Intent.parseUri(response.getDataString(),
						Intent.URI_INTENT_SCHEME);
			} catch (URISyntaxException e) {
				e.printStackTrace();
				throw new IllegalArgumentException(e);
			}
		case Intents.INSERT:
			if(!m) Logf.D(TAG,"handleOk(Intent)", "....at INSERT"); m = true; 
		case Intents.INSERT_OR_EDIT:
			if(!m) Logf.D(TAG, "handleOk(Intent)","....at INSERT_OR_EDIT"); m = true; 
		case Intents.PICK:
			if (!m) Logf.D(TAG, "handleOk(Intent)","....at PICK"); m = true;
			// content pick intent we should get data back
			switch(Uris.getDescriptor(uri)){
			case Uris.OBSERVER_ITEM:
			case Uris.OBSERVER_UUID:
				Logf.D(TAG, "handleOk(Intent)","....at PICK Observer-Selected");
				return new Intent(Intents.ACTION_PICK_ACTIVITY);
			case Uris.SUBJECT_ITEM:
			case Uris.SUBJECT_UUID:
				Logf.D(TAG, "handleOk(Intent)","....at PICK Subject-Selected");
				return PICK_PROCEDURE;
			case Uris.ENCOUNTER_ITEM:
			case Uris.ENCOUNTER_UUID:
				Logf.D(TAG,"handleOk(Intent)", "....at PICK Procedure-Selected");
				// require the subject to be set 
				return new Intent(Intent.ACTION_VIEW, uri); 
			case Uris.NOTIFICATION_ITEM:
			case Uris.NOTIFICATION_UUID:
				Logf.D(TAG,"handleOk(Intent)", "....at PICK Notification-Selected");
				// require the subject to be set 
				return new Intent(Intent.ACTION_VIEW, uri); 
			case Uris.PROCEDURE_ITEM:
			case Uris.PROCEDURE_UUID:
				Logf.D(TAG, "handleOk(Intent)","....at PICK Procedure-Selected");
				// require the subject to be set 
				return new Intent(Intent.ACTION_VIEW, uri); 	
			// DIR should indicate create new
			case Uris.SUBJECT_DIR:
					Logf.D(TAG, "handleOk(Intent)","....at PICK Subject-None Picked");
			default:
				return goTo(Intents.PICK_ACTIVITY);
			}
		case Intents.VIEW:
			// return to the predecessor of the view
			Logf.D(TAG, "....at VIEW");
			return goTo(Intents.PICK_ACTIVITY);
		}
		
		// Return the result intent back to the top
		// stack should never be empty here
		return goTo(Intents.PICK_ACTIVITY);
	}
	
	
	
}
