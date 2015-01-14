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
package org.sana.android.db.impl;

import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

import org.sana.android.db.TableHelper;
import org.sana.android.provider.Notifications.Contract;
import org.sana.core.Notification;

/**
 * A database table helper for a table of instructions.
 * 
 * @author Sana Development
 *
 */
public class NotificationsHelper extends TableHelper<Notification>{
	public static final String TAG = NotificationsHelper.class.getSimpleName();

	private static final NotificationsHelper HELPER = new NotificationsHelper();
	
	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return An instance of this class.
	 */
	public static NotificationsHelper getInstance(){
		return HELPER;
	}
	
	protected NotificationsHelper(){
		super(Notification.class);
	}
	
	/* (non-Javadoc)
	 * @see org.sana.android.db.InsertHelper#onInsert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public ContentValues onInsert(ContentValues values) {
		return super.onInsert(values);
	}

	/* (non-Javadoc)
	 * @see org.sana.android.db.UpdateHelper#onUpdate(java.lang.String, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public ContentValues onUpdate(Uri uri, ContentValues values) {
		return super.onUpdate(uri, values);
	}

	/* (non-Javadoc)
	 * @see org.sana.android.db.CreateHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public String onCreate() {
		Log.i(TAG, "onCreate()");
		return "CREATE TABLE " + getTable() + " ("
				+ Contract._ID 			+ " INTEGER PRIMARY KEY,"
				+ Contract.UUID 		+ " TEXT NOT NULL, "
				+ Contract.CREATED 		+ " DATE, "
				+ Contract.MODIFIED 	+ " DATE, "
				+ Contract.PROCEDURE_ID + " TEXT, "
				+ Contract.PATIENT_ID 	+ " TEXT NOT NULL, "
				+ Contract.MESSAGE 		+ " TEXT, "
				+ Contract.FULL_MESSAGE + " TEXT, "
				+ Contract.DOWNLOADED 	+ " INTEGER"
        		+ ");";
		
	}

	/* (non-Javadoc)
	 * @see org.sana.android.db.UpgradeHelper#onUpgrade(int, int)
	 */
	@Override
	public String onUpgrade(int oldVersion, int newVersion) {
		if(oldVersion < newVersion){
			
		}
		return null;
	}
	
}
