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
package org.sana.android.service.impl;

import java.util.HashMap;
import java.util.UUID;

import org.apache.http.client.methods.HttpPost;
import org.sana.R;
import org.sana.android.Constants;
import org.sana.android.content.core.ObserverWrapper;
import org.sana.android.db.ModelWrapper;
import org.sana.android.net.HttpTask;
import org.sana.android.net.MDSInterface;
import org.sana.android.net.MDSInterface2;
import org.sana.net.MDSResult;
import org.sana.net.Response;
import org.sana.android.net.NetworkTaskListener;
import org.sana.android.provider.Observers;
import org.sana.android.service.ISessionCallback;
import org.sana.android.service.ISessionService;
import org.sana.api.IObserver;
import org.sana.util.UUIDUtil;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * Service which provides session based authentication into the system. 
 * 
 * @author Sana Development
 *
 */
public class SessionService extends Service{
	public static final String TAG = SessionService.class.getSimpleName();

	public static final String ACTION_START = "org.sana.service.SessionService.START";
	
	public static final UUID INVALID = UUIDUtil.uuid3(UUIDUtil.EMPTY, "invalid");
	public static final UUID INVALID_PASSWORD = UUIDUtil.uuid3(UUIDUtil.EMPTY, "password");
	public static final UUID VALID = UUIDUtil.uuid3(UUIDUtil.EMPTY, "valid");

	public static final int INDETERMINATE = -1;
	public static final int STATE_REMOTE = 1;
	public static final int STATE_LOCAL = 2;
	
	public static final int FAILURE = 0;
	public static final int SUCCESS = 1;
	
	//TODO replace the two HashMaps with thread safe versions.
	// map of user valid session key to credentials
	private static final HashMap<String,String[]> openSessions = new HashMap<String,String[]>();
	
	// map of temp key to credentials
	private static final HashMap<String,String[]> tempSessions = new HashMap<String,String[]>();
	
	private final RemoteCallbackList<ISessionCallback> mCallbacks
    	= new RemoteCallbackList<ISessionCallback>();

	private final ISessionService.Stub mBinder = new ISessionService.Stub() {
		@Override
		public boolean read(String arg0) throws RemoteException {
			boolean result = false;
			if(TextUtils.isEmpty(arg0))
				return false;
			else { 
				result = isOpen(arg0);
			}
			return result;
		}
		
		@Override
		public String create(String tempKey, String username, String password) throws RemoteException {
			//String tempKey = SessionUtil.generateSessionKey(username);
			addTempSession(tempKey, new String[]{ username, password });
			openSession(STATE_REMOTE, tempKey);
			return tempKey;
		}
		
		@Override
		public boolean delete(String arg0) throws RemoteException {
			return removeAuthenticatedSession(arg0);
		}

		@Override
		public void registerCallback(ISessionCallback arg0, String arg1)
				throws RemoteException {
			Log.i(TAG + ".mBinder", "registerCallback(): " + arg1);
			if (arg0 != null) mCallbacks.register(arg0, arg1);
		}

		@Override
		public void unregisterCallback(ISessionCallback arg0)
				throws RemoteException {
			Log.i(TAG + ".mBinder", "unregisterCallback(): " + arg0);
			if(arg0 != null) mCallbacks.unregister(arg0);
		}
		
	};
	
	//TODO refactor this out
	/**
	 * Simple callback interface for HttpSessions
	 * 
	 * @author Sana Development
	 *
	 *
	private class HttpSessionAuthListener implements NetworkTaskListener<MDSResult>{
		
		String tempKey = null;
		
		HttpSessionAuthListener(String key){
			this.tempKey = key;
		}
		
		@Override
		public void onTaskComplete(MDSResult t) {
			// TODO Auto-generated method stub
			if(t.succeeded()){
				// MDSResult should return the actual session key
				handleSessionAuthResult(SUCCESS, tempKey, t.getData());
			} else if(TextUtils.isEmpty(t.getCode())){
				handleSessionAuthResult(INDETERMINATE, tempKey, INVALID.toString());
			} else {
				// data should be some informative message
				handleSessionAuthResult(FAILURE, tempKey, INVALID.toString());
			}
		}
	}
	*/
	private class AuthListener implements NetworkTaskListener<Response<String>>{
		
		String tempKey = null;
		
		AuthListener(String key){
			this.tempKey = key;
		}
		
		@Override
		public void onTaskComplete(Response<String> t) {
			// TODO Auto-generated method stub
			if(t.succeeded()){
				// MDSResult should return the actual session key
				handleSessionAuthResult(SUCCESS, tempKey, t.getMessage());
			} else if (t.getCode() == -1){
				handleSessionAuthResult(INDETERMINATE, tempKey, INVALID.toString());
			} else {
				// data should be some informative message
				handleSessionAuthResult(FAILURE, tempKey, INVALID.toString());
			}
		}
	}

	//HttpSessionAuthListener mNetListener = null;
	
	HttpTask mNetTask = null;
	
	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(TAG, "onBind()");
		// Try the call back binder defined in IRemoteService.onBind()
		if(arg0.getAction().equals(SessionService.class.getName()) || 
					arg0.getAction().equals(ACTION_START))
				return mBinder;
		else
			return null;
	}
	
    @Override
    public void onDestroy() {
		Log.i(TAG, "onDestroy()");
    	if(mCallbacks != null)
    		mCallbacks.kill();
    	super.onDestroy();
    }
    
    @Override
    public boolean onUnbind(Intent arg0) {
		Log.i(TAG, "onUnbind()");
		int connections = 0;
		try{
			connections = mCallbacks.beginBroadcast();
		} finally {
			mCallbacks.finishBroadcast();
		}
		if(!(connections > 0))
			stopSelf();
		return super.onUnbind(arg0);
    	
    }
	
	/**
	 * Handles the logic for opening a session by first trying the remote
	 * dispatcher if connected and then falling back locally to either (1) the 
	 * credential cache in the Observer ContentProvider or (2) the default 
	 * admin credentials. 
	 *  
	 * @param state
	 * @param username
	 * @param password
	 */
	protected void openSession(int state, String tempKey){
		switch(state){
		case STATE_LOCAL:
			openLocalSession(tempKey);
			break;
		case STATE_REMOTE:
			openNetworkSession(tempKey);
			break;
		default:
			throw new IllegalArgumentException("Use STATE_LOCAL or STATE_REMOTE only");
		}
	}
	
	// Tries to open a session from the local ContentProvider and then the 
	// admin backdoor by default - admin backdoor only works once from the 
	// resource value
	private void openLocalSession(String tempKey){
		String[] credentials = tempSessions.get(tempKey);
		String username = credentials[0];
		String password = credentials[1];
		String session = INVALID.toString();
		if(isLocalUsername(username)){
			try{
				Log.d(TAG, "auth: " + username + " pass: " + password);
				IObserver o = ObserverWrapper.getOneByAuth(
						getContentResolver(), username, password);
		
				// user exists and was validated locally
				if(o != null){
					session = o.getUuid();
					handleSessionAuthResult(SUCCESS, tempKey, session);
					// the user was good but password didn't match, return a fail
				} else {
					handleSessionAuthResult(FAILURE, tempKey, username);
				}
			} catch (Exception e){
					e.printStackTrace();
					Log.e(TAG, e.getMessage());
					handleSessionAuthResult(FAILURE, tempKey, INVALID.toString());
			}
		
		// Try the admin backdoor for local connection only
		} else if(username.equals(getString(R.string.admin_username))
				&& password.equals(getString(R.string.admin_password))){
				session = UUIDUtil.generateObserverUUID(username).toString();
				registerNew(username,password,session);
				handleSessionAuthResult(SUCCESS, tempKey, username);
		} else {
			handleSessionAuthResult(FAILURE, tempKey, INVALID.toString());
		}
			
	}
	
	//TODO replace with an https connection.
	// Starts an async http task to POST credentials to dispatch server
	private void openNetworkSession(String tempKey){
		Log.d(TAG, "Opening network session: " + tempKey);
		if(!connected()){
			Log.d(TAG, "openNetworkSession()..connected = false");
			openLocalSession(tempKey);
		} else {
		try {
			Log.d(TAG, "openNetworkSession()..connected = true");
			String[] credentials = tempSessions.get(tempKey);
			HttpPost post = MDSInterface2.createSessionRequest(this, 
					credentials[0], credentials[1]);
			Log.i(TAG, "openNetworkSession(...) " + post.getURI());
			new HttpTask<String>(new AuthListener(tempKey)).execute(post);
		} catch(Exception e){
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
		}
	}
	
	/**
	 * Handles authentication attempts for temporary tempSessions. If successful,
	 * the temporary session will be moved to a list of authorized tempSessions.
	 * 
	 * @param status {@link #SUCCESS}, {@link #FAILURE}, {@link #INDETERMINATE}
	 * @param tempKey the temporary key initially passed from the client.
	 * @param sessionKey the authorized key.
	 */
	protected void handleSessionAuthResult(int status, String tempKey, 
			String sessionKey)
	{
		Log.i(TAG, "Handling result: '" + status 
				+ "' for temp session: " + tempKey);
		String[] credentials = tempSessions.get(tempKey);
		final String username = credentials[0];
		final String password = credentials[1];
		
		switch(status){
		case SUCCESS:
			// Got successful authentication from network
			// update cache with the validated password, may exist already but
			// this should handle any situations where the network value changed
			if(isLocalUsername(username)){
				Log.i(TAG,"Updating credentials for user: " + username);
				ContentValues values = new ContentValues();
				values.put(Observers.Contract.PASSWORD, encrypt(password));
				int updated = getContentResolver().update(
						Observers.CONTENT_URI,
						values,
						Observers.Contract.USERNAME + " = ?",
						new String[]{ username });
				if(updated == 1)
					Log.i(TAG, "Succesfully updated: " + username);
				else
					Log.w(TAG, "OOPS! Something went horribly wrong updating" +
							 	"the password for user: " + username
							 	+ " or the password was unchanged!");
				
			// i name didn't exist already we need to insert instead of update.
			} else { 
				ContentValues values = new ContentValues();
				values.put(Observers.Contract.USERNAME, username);
				values.put(Observers.Contract.UUID, sessionKey);
				values.put(Observers.Contract.PASSWORD, encrypt(password));
				getContentResolver().insert(Observers.CONTENT_URI,
						values);
			}
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(this.getBaseContext());
			preferences.edit().putString(
					Constants.PREFERENCE_EMR_USERNAME, username);
			preferences.edit().putString(
					Constants.PREFERENCE_EMR_PASSWORD, password);
			preferences.edit().commit();
			
			// send result to the call back (INVALID, user uuid)
			removeTempSession(tempKey);
			addAuthenticatedSession(sessionKey, credentials);
			sendResult(SUCCESS, tempKey, sessionKey);
			break;
		// connected and failed
		case FAILURE:
			// send result to the call back (INVALID, user )
			removeTempSession(tempKey);
			sendResult(FAILURE, tempKey,  sessionKey);
			break;
		case INDETERMINATE:
			openSession(STATE_LOCAL, tempKey);
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	// sends the callback message
	protected void sendResult(int status, String cookie,  String msg){
		int i = mCallbacks.beginBroadcast();
		Log.i(TAG, "sendResult( "+cookie+", "+ status+") --> " + i+" callbacks");
		while(i > 0){
			i--;
			String instanceKey = mCallbacks.getBroadcastCookie(i).toString();
			if(cookie.equals(instanceKey)){
				Log.i(TAG, "....Sending result to " + cookie);
				try {
					mCallbacks.getBroadcastItem(i).onValueChanged(status, cookie, msg);
				} catch (RemoteException e) {
					Log.e(TAG, "....disconnected from " + cookie); 
				}
				
			} else {
				Log.i(TAG, "....Trying next "+ i);
				
			}
		} 
		mCallbacks.finishBroadcast();
	}
	
	// queries the content provider for the username
	private boolean isLocalUsername(String username){
		Cursor c = null;
		boolean isValid = false;
		try{
			c = getContentResolver().query(Observers.CONTENT_URI, 
					new String[]{ Observers.Contract._ID }, 
					Observers.Contract.USERNAME + " = ?", 
					new String[]{ username }, null);
			//(Observers.CONTENT_URI, getContentResolver(),Observers.Contract.USERNAME,username);
			if(c != null && c.moveToFirst() && c.getCount() == 1)
				isValid = true;
		} finally {
			if(c != null)
				c.close();
		}
		return isValid;
	}
	
	private synchronized void addAuthenticatedSession(String sessionKey, String[] credentials){
		Log.d(TAG, "Adding authorized session: " + sessionKey+":"+credentials.length);
		openSessions.put(sessionKey, credentials);
	}
	
	private synchronized boolean removeAuthenticatedSession(String sessionKey){
		Log.d(TAG, "Removing authorized session: " + sessionKey);
		boolean result = false;
		result = (openSessions.remove(sessionKey) != null);
		return result;
	}
	
	private synchronized void addTempSession(String sessionKey, String[] credentials){
		Log.d(TAG, "Adding temp session: " + sessionKey+":"+credentials.length);
		tempSessions.put(sessionKey, credentials);
	}
	
	private synchronized boolean removeTempSession(String sessionKey){
		Log.d(TAG, "Removing temp session: " + sessionKey);
		boolean result = false;
		result = (tempSessions.remove(sessionKey) != null);
		return result;
	}
	
	private synchronized boolean isOpen(String sessionKey){
		boolean result = false;
		synchronized(openSessions){
			String[] session = openSessions.get(sessionKey);
			result = (session != null);
		}
		openSessions.notify();
		return result;
	}
	
	/**
	 * Retrieves the memory stored password for network authentication
	 * @param session
	 * @return
	 */
	protected String getPassword(String sessionKey){
		return decrypt(openSessions.get(sessionKey)[1]);
	}
	
	//TODO do placeholder for encrypting the in memory String
	private String encrypt(String str){
		return str;
	}
	
	//TODO do placeholder for decrypting the in memory String
	private String decrypt(String str){
		return str;
	}
	
	private Uri registerNew(String username, String password, String uuid){
		uuid = (uuid == null)? UUIDUtil.generateObserverUUID(username).toString(): uuid;
		ContentValues values = new ContentValues();
		values.put(Observers.Contract.USERNAME, username);
		values.put(Observers.Contract.PASSWORD, encrypt(password));
		values.put(Observers.Contract.UUID, uuid);
		Uri uri = getContentResolver().insert(Observers.CONTENT_URI,
				values);
		return uri;
	}
	
	
	protected boolean connected(){
		ConnectivityManager cm = (ConnectivityManager) 
		        getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return(activeNetwork != null &&
		                      activeNetwork.isConnectedOrConnecting());
		
	}
}
