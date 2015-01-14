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
package org.sana.android.service.impl.test;

import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.sana.android.activity.BaseActivity;
import org.sana.android.service.IRemoteService;
import org.sana.android.service.ISessionCallback;
import org.sana.android.service.ISessionService;
import org.sana.android.service.IStringCallback;
import org.sana.android.service.impl.SessionService;
import org.sana.android.util.SessionUtil;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.service.textservice.SpellCheckerService.Session;
import android.test.ServiceTestCase;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Sana Development
 *
 */
public class SessionServiceTest extends ServiceTestCase<SessionService> {

	public static final String TAG = SessionServiceTest.class.getSimpleName();

	// valid values from the sana demo server - swap out wih yours
	String username = "admin";
	String password = "Sanamobile1";
	
    String instanceKey;
    String sessionKey;
	boolean mBound;
	ISessionService mService = null;
	
	ReentrantLock lock;
	
	// callback for the Session service
	private ISessionCallback mCallback = null;
		
	 private Handler mHandler = null;
		
	public SessionServiceTest() {
		super(SessionService.class);
	}
	
	protected void setUp() throws Exception{
		super.setUp();
		instanceKey = UUID.randomUUID().toString();
		sessionKey = null;
		mBound = false;
		mService = null;
		lock = new ReentrantLock();
		mHandler = new Handler() {
	        @Override public void handleMessage(Message msg) {
	    		Log.e(TAG, "handleMessage(): " + msg.what);
	    		lock.unlock();
	        }
	    };
	    mCallback = new ISessionCallback.Stub() {
			
			@Override
			public void onValueChanged(int arg0, String arg1, String arg2) throws RemoteException {
				Log.d(TAG,  ".mCallback.onValueChanged( " +arg0 +", "+arg1+ 
						", " + arg2+ " )");
				Bundle data = new Bundle();
				data.putString(BaseActivity.INSTANCE_KEY, arg1);
				data.putString(BaseActivity.SESSION_KEY, arg2);
				mHandler.sendMessage(mHandler.obtainMessage(arg0, data));
			}
		};
	}
	
	protected void tearDown() throws Exception{
		super.tearDown();
		mHandler = null;
		mCallback = null;
		lock = null;
	}
	
	public void testStartStopService(){
		startService(new Intent(SessionService.ACTION_START));
	}
	
	public void testBindings(){
		mService = (ISessionService) bindService(new Intent(SessionService.ACTION_START));
	}
	
	public void testRegisterCallback(){
		mService = (ISessionService) bindService(new Intent(SessionService.ACTION_START));
		Exception exception = null;
		try {
			mService.registerCallback(mCallback, instanceKey);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			exception = e;
		}
		assertNull(exception);
	}
	
	public void testCreate(){
		mService = (ISessionService) bindService(new Intent(SessionService.ACTION_START));
		Exception exception = null;
		try {
			mService.registerCallback(mCallback, instanceKey);
			lock.lock();
			mService.create(instanceKey,username, password);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			exception = e;
		}
		assertNull(exception);
		int index = 0;
		while(lock.isLocked() && index < 20)
			index++;
			try {
				Thread.sleep(100);
				Log.d(TAG, "Waiting...");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if(lock.isLocked())
			lock.unlock();
		assertEquals(index,20);
	}
	
	public void testCreateFail(){
		
	}
	
	public void testRead(){
		
	}
	
	public void testReadFail(){
		
	}
	
	public void testDelete(){
		
	}

	public void testDeleteFail(){
		
	}
}
