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
package org.sana.android.service;

import java.util.UUID;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

/**
 * @author Sana Development
 *
 */
public abstract class RemoteService extends Service {
	public static final String TAG = RemoteService.class.getSimpleName();
	
	public static final String NULL_SESSION = "0000-000-000-000-0000";
	
	protected final RemoteCallbackList<IRemoteCallback> mCallbacks
		= new RemoteCallbackList<IRemoteCallback>();
	

	public static final int INDETERMINATE = -1;
	public static final int STATE_REMOTE = 1;
	public static final int STATE_LOCAL = 2;
	
	public static final int FAILURE = 0;
	public static final int SUCCESS = 1;
	
	protected final IRemoteService.Stub mRemoteBinder = new IRemoteService.Stub(){

		@Override
		public String registerCallback(IRemoteCallback cb)
				throws RemoteException {
			UUID cookie = UUID.randomUUID();
			if(cb != null){
				Log.i(TAG, "IRemoteService.Stub" 
						+ ".registerCallback(): " + cookie);
				mCallbacks.register(cb,cookie);
			}
			return cookie.toString();
		}

		@Override
		public void unregisterCallback(IRemoteCallback cb)
				throws RemoteException {
			if(cb != null){
				Log.i(TAG, "IRemoteService.Stub." + "unRegisterCallback(): ");
				mCallbacks.unregister(cb);
			}
		}
	};
	
	// sends the callback message
	protected void sendResult(String cookie, int status, Bundle data){
		int i = mCallbacks.beginBroadcast();
		Log.i(TAG, "sendResult( "+cookie+", "+ status+") --> " + i+" callbacks");
		while(i > 0){
			i--;
			if(cookie.equals(mCallbacks.getBroadcastCookie(i).toString())){
				Log.i(TAG, "....Sending result to " + cookie);
				try {
					mCallbacks.getBroadcastItem(i).onResult(status, data);
					i =0;
				} catch (RemoteException e) {
					Log.e(TAG, "....disconnected from " + cookie); 
				}
			} else {
				Log.i(TAG, "....Trying next "+ i);
			}
		} 
		mCallbacks.finishBroadcast();
	}
}
