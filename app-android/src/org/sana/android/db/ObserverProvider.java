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
package org.sana.android.db;

import java.util.HashMap;
import java.util.Map;

import org.sana.R;
import org.sana.android.content.BasicContentProvider;
import org.sana.android.provider.Observers;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * @author Sana Development
 *
 */
public class ObserverProvider extends BasicContentProvider{

	static final String TAG = ObserverProvider.class.getSimpleName();
	/*
	private class OpenHelper extends DatabaseOpenHelper{

			protected OpenHelper(Context arg0, String arg1, int arg2) {
				super(arg0, arg1, arg2, null);
			}
			
			@Override
			public void onCreate(SQLiteDatabase db){
				onCreate(db, null);
			}
			
			@Override
			public void onCreate(SQLiteDatabase db, String[] columns) {
				super.onCreate(db, new String[]{ 
						Observers.Contract.USERNAME + " TEXT NOT NULL",
						Observers.Contract.PASSWORD + " TEXT NOT NULL",
						Observers.Contract.ROLE + " TEXT"});
			}

			@Override
			public String onSort(Uri uri) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getTable(Uri uri) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getFileColumn(Uri uri) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getType(Uri uri) {
				// TODO Auto-generated method stub
				return null;
			}
	    }
	*/
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
	        Log.d(TAG, ".onCreate();");
			return true;
	}
	
	static final String TABLE = "observers";
	static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH); 
	
	private static final Map<String,String> sProjMap = new HashMap<String, String>();
	static{
        sProjMap.put(Observers.Contract._ID, Observers.Contract._ID);
        sProjMap.put(Observers.Contract.CREATED, Observers.Contract.CREATED);
        sProjMap.put(Observers.Contract.MODIFIED, Observers.Contract.MODIFIED);
        sProjMap.put(Observers.Contract.UUID, Observers.Contract.UUID);
        sProjMap.put(Observers.Contract.USERNAME,Observers.Contract.USERNAME);
        sProjMap.put(Observers.Contract.PASSWORD,Observers.Contract.PASSWORD);
        sProjMap.put(Observers.Contract.ROLE,Observers.Contract.ROLE);
	}
	
	
	private static final String CREATE_TABLE =
			"CREATE TABLE " + TABLE + " ("
					+ Observers.Contract._ID + " INTEGER PRIMARY KEY,"
					+ Observers.Contract.UUID + " TEXT,"
					+ Observers.Contract.USERNAME + " TEXT NOT NULL,"
					+ Observers.Contract.PASSWORD + " TEXT NOT NULL,"
					+ Observers.Contract.ROLE + " TEXT,"
					+ Observers.Contract.CREATED + " DATE,"
					+ Observers.Contract.MODIFIED + " DATE"
					+ ");";

	
    public static void onCreateDatabase(SQLiteDatabase db){
        Log.i(TAG, "onCreateDatabase() => Executing CREATE for table: " 
        		+ ObserverProvider.TABLE);
    	db.execSQL(CREATE_TABLE);
        Log.i(TAG, "onCreateDatabase() => Completed executing CREATE for table: " 
        		+ ObserverProvider.TABLE);
    }


	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}


	/* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}



}
