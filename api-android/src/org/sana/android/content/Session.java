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
package org.sana.android.content;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.sana.android.provider.Concepts;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Events;
import org.sana.android.provider.Instructions;
import org.sana.android.provider.Notifications;
import org.sana.android.provider.Observations;
import org.sana.android.provider.Observers;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Subjects;
import org.sana.android.util.SessionUtil;
import org.sana.util.UUIDUtil;

import android.net.Uri;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Container for keys to manage session based instances and authorization. The 
 * default implementation relies heavily on the {@link org.sana.util.UUIDUtil UUIDUtil}
 * class for generating and validating keys.  
 * 
 * @author Sana Development
 *
 */
public class Session implements Parcelable {

	/** A key representing an empty session. */
	public static final String NULL = "00000000-0000-0000-0000-000000000000";
	
	/** A key representing an invalid session. */
	public static final String INVALID = "zzzzzzzz-zzzz-zzzz-zzzz-zzzzzzzzzzzz";
	
	/** A key representing an invalid session. */
	public static final String UNAUTHENTICATED = "00000000-0000-0000-0000-000000000001";
	
	public static Session EMPTY = new Session(NULL, NULL);
	
    // instanceKey initialized to some random value for the instance;
	private final String mInstanceKey;
	
	// Authenticated session key default is null;
	private String mAuthKey = null;
	
	private int mode = -1;
	private List<Uri> data = new ArrayList<Uri>();
	
	private Uri concept = Uri.EMPTY;
    private Uri encounter = Uri.EMPTY;
    private Uri event = Uri.EMPTY;
    private Uri instruction = Uri.EMPTY;
    private Uri observer = Uri.EMPTY;
    private Uri observation = Uri.EMPTY;
    private Uri notification = Uri.EMPTY;
    private Uri procedure = Uri.EMPTY;
    private Uri subject = Uri.EMPTY;
	
	
	/**
	 * Creates a new instance with the instance key randomly generated. 
	 */
	public Session(){
		this(UUID.randomUUID().toString());
	}
	
	/**
	 * Creates a new instance with a specified instance key. 
	 *
	 * @param instanceKey
	 * @throws IllegalArgumentException if instanceKey is not formatted
	 */
	public Session(String instanceKey){
		this(instanceKey, NULL);
	}
	
	/**
	 * Creates a new instance with the specified instance and auth keys. 
	 * 
	 * @param instanceKey
	 * @param authKey
	 * @throws IllegalArgumentException if instanceKey is not formatted
	 */
	public Session(String instanceKey, String authKey){
		mInstanceKey = UUID.fromString(NULL).toString();
		mAuthKey = UUID.fromString(authKey).toString();
	}
	
	public Session(Parcel in){
		mInstanceKey = in.readString();
		mAuthKey = in.readString();
		
		concept = in.readParcelable(Uri.class.getClassLoader());
	    encounter = in.readParcelable(Uri.class.getClassLoader());
	    event = in.readParcelable(Uri.class.getClassLoader());
	    instruction = in.readParcelable(Uri.class.getClassLoader());
	    observer = in.readParcelable(Uri.class.getClassLoader());
	    observation = in.readParcelable(Uri.class.getClassLoader());
	    notification = in.readParcelable(Uri.class.getClassLoader());
	    procedure = in.readParcelable(Uri.class.getClassLoader());
	    subject = in.readParcelable(Uri.class.getClassLoader());
	}
	
	/* (non-Javadoc)
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		arg0.writeString(mInstanceKey);
		arg0.writeString(mAuthKey);
		Uri[] result = new Uri[data.size()];
		arg0.writeParcelableArray(data.toArray(result), Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
		arg0.writeParcelable(concept,0);
		arg0.writeParcelable(encounter,0);
		arg0.writeParcelable(event,0);
		arg0.writeParcelable(instruction,0);
		arg0.writeParcelable(notification,0);
		arg0.writeParcelable(observer,0);
		arg0.writeParcelable(observation,0);
		arg0.writeParcelable(procedure,0);
		arg0.writeParcelable(subject,0);
	}
	
	/**
	 * @return the mConcept
	 */
	public Uri getConcept() {
		return concept;
	}

	/**
	 * @param  uri the Uri of the Concept to set
	 */
	public void setConcept(Uri uri) {
		this.concept = uri;
	}

	/**
	 * @return the Encounter Uri
	 */
	public Uri getEncounter() {
		return encounter;
	}

	/**
	 * @param  uri the Uri of the Encounter to set
	 */
	public void setEncounter(Uri uri) {
		this.encounter = uri;
	}

	/**
	 * @return the Event
	 */
	public Uri getEvent() {
		return event;
	}

	/**
	 * @param event the mEvent to set
	 */
	public void setEvent(Uri uri) {
		this.event = uri;
	}

	/**
	 * @return the Instruction
	 */
	public Uri getInstruction() {
		return instruction;
	}

	/**
	 * @param  uri the Uri of the Instruction to set
	 */
	public void setInstruction(Uri uri) {
		this.instruction = uri;
	}

	/**
	 * @return the mObserver
	 */
	public Uri getObserver() {
		return observer;
	}

	/**
	 * @param observer the mObserver to set
	 */
	public void setObserver(Uri uri) {
		this.observer = uri;
	}

	/**
	 * @return the mObservation
	 */
	public Uri getmObservation() {
		return observation;
	}

	/**
	 * @param mObservation the mObservation to set
	 */
	public void setmObservation(Uri mObservation) {
		this.observation = mObservation;
	}

	/**
	 * @return the mNotification
	 */
	public Uri getmNotification() {
		return notification;
	}

	/**
	 * @param uri the Uri of the Notification to set
	 */
	public void setNotification(Uri uri) {
		this.notification = uri;
	}

	/**
	 * @return the Procedure Uri
	 */
	public Uri getProcedure() {
		return procedure;
	}

	/**
	 * @param  uri the Uri of the Procedure to set
	 */
	public void setProcedure(Uri uri) {
		this.procedure = uri;
	}

	/**
	 * @return the subject Uri
	 */
	public Uri getSubject() {
		return subject;
	}

	/**
	 * @param  uri the Uri of the subject(s) to set
	 */
	public void setSubject(Uri uri) {
		this.subject = uri;
	}

	/**
	 * @return the instance key
	 */
	public String getInstanceKey() {
		return mInstanceKey;
	}
	
	public static Parcelable.Creator<Session> CREATOR = new Parcelable.Creator<Session>() {

		@Override
		public Session createFromParcel(Parcel source) {
			return new Session(source);
		}

		@Override
		public Session[] newArray(final int size) {
			return new Session[size];
		}
	};
	
	/**
	 * Returns whether the session instance key or authenticated session key are
	 * empty. The object is considered empty if any of the following are 
	 * <code>true</code>:
	 * 
	 * <ol>
	 * <li>The object is <code>null</code></li>
	 * <li>The object instance key is <code>null</code> or an empty String.</li>
	 * <li>The object instance key is equal to {@link #NULL_KEY}</li>
	 * <li>The object auth key is <code>null</code> or an empty String.</li>
	 * <li>The object auth key is equal to {@link #NULL_KEY}</li>
	 * </ol>
	 * 
	 * <b>Warning.</b> This ,ethod makes no determination of whether the auth
	 * key is, in fact, valid in some domain. Rather, it merely returns whether
	 * the session holds some values that may refer to a valid session, which
	 * may, or may not, be authenticated.
	 * 
	 * @param session The session to check.
	 * @return true if session
	 */
	public static boolean isEmpty(Session session){
		if(session == null)
			return true;
		
		// null check on instance key
		if(TextUtils.isEmpty(session.mInstanceKey))
			return true;
		
		// check against universally invalid key
		if(session.mInstanceKey.equals(Session.NULL))
			return true;

		// null check on auth key
		if(TextUtils.isEmpty(session.mAuthKey))
			return true;

		// check against universally invalid key
		if(session.mAuthKey.equals(Session.NULL))
			return true;
		
		// The session refers to something so not empty. 
		return false;
	}
	
}
