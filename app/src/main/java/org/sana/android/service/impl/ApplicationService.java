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

import org.sana.R;
import org.sana.android.provider.Procedures;
import org.sana.android.util.Logf;
import org.sana.android.util.SanaUtil;

import android.app.Application;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

/**
 * @author Sana Development
 *
 */
public class ApplicationService extends IntentService {
	public static final String TAG = ApplicationService.class.getSimpleName();
	/**
	 * @param name
	 */
	public ApplicationService() {
		super(Application.class.getName());
		
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		Logf.D(TAG, "onHandleIntent(Intent)", "handling");
		checkForInitialization();
		//checkForUpdate();
	}
	
	final void checkForInitialization(){
		Logf.D(TAG, "initialize()", "Entering");
	    SharedPreferences preferences = 
        		PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	    
	    String dbKey = getString(R.string.cfg_db_init);
	    String dbVersion = getString(R.string.cfg_db_version);
	    // check whether the db is initialized and create if not
	    boolean doInit = preferences.getBoolean(dbKey, false);
		Logf.D(TAG, "initialize()", "dbs initialized: " + doInit);
		
	    if(!doInit){
	    	getContentResolver().acquireContentProviderClient(Procedures.CONTENT_URI).release();
	    	SanaUtil.loadDefaultDatabase(getBaseContext());
	    	preferences.edit().putBoolean(dbKey, true)
	    		.putInt("s_app_sync_period", 1209600000)
	    		.putLong("s_app_sync_last", System.currentTimeMillis())
	    		.putInt(dbVersion, getResources().getInteger(R.integer.cfg_db_version_value))
	    		.commit();
	    }
	}
	
	final void checkForUpdate(){
		Logf.D(TAG, "update()", "Entering");
	    String dbVersion = getString(R.string.cfg_db_version);
	    int version = getResources().getInteger(R.integer.cfg_db_version_value);
	    String dbKey = getString(R.string.cfg_db_init);

	    SharedPreferences preferences = 
        		PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	    // check whether the db is initialized and create if not
	   int currentVersion = preferences.getInt(dbVersion, 0);
	   
	   boolean doUpdate = (currentVersion != 0) && currentVersion < version;
		Logf.D(TAG, "update()", "updating: " + doUpdate);
	    if(doUpdate){
	    	getContentResolver().delete(Procedures.CONTENT_URI, null, null);
	    	SanaUtil.loadDefaultDatabase(getBaseContext());
	    	preferences.edit().putBoolean(dbKey, true)
	    		.putInt(dbVersion, version).commit();
	    }
	}
}
