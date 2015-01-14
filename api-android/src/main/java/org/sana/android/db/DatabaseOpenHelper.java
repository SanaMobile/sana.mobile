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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

/**
 * Implementation of a basic SQLiteOpenHelper class which performs common table
 * operations based on {@link android.net.Uri Uri} matching.
 * 
 * @author Sana Development
 *
 */
public abstract class DatabaseOpenHelper extends SQLiteOpenHelper
{
	public static final String TAG = DatabaseOpenHelper.class.getSimpleName();
	
	/**
	 * 
	 * @param context
	 * @param name
	 * @param matcher TODO
	 */
	public DatabaseOpenHelper(Context context, String name, int version){
		super(context,name,null,version);
	}
	
	/* (non-Javadoc)
	 * @see org.sana.android.db.InsertHelper#onInsert(android.content.ContentValues)
	@Override
	public ContentValues onInsert(ContentValues values) {
		SQLiteDatabase db = getWritableDatabase();
		long id = db.insert(getTable(uri), null, values);
		Uri result = ContentUris.withAppendedId(uri, id);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.sana.android.db.UpdateHelper#onUpdate(android.content.ContentValues, java.lang.String, java.lang.String[])
	 *
	@Override
	public ContentValues onUpdate(ContentValues values) 
	{
		SQLiteDatabase db = getWritableDatabase();
		return db.update(table, values, selection, selectionArgs);
	}
	

	/* (non-Javadoc)
	 * @see org.sana.android.db.FileHelper#openFile(long, java.lang.String)
	 *
	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws 
		FileNotFoundException 
	{
		String path = null;
		Cursor c = null;
		// _ID based where
		String whereClause = DBUtils.getWhereClause(uri,  mUriHelper.match(uri), null);
		String[] projection = new String[]{ getFileColumn(uri) };
		
		try{
			c = onQuery(null, projection, whereClause, null, null);
			if (c != null){
				if(c.moveToFirst() && c.getCount() == 1) {
					// If there is not exactly one result, throw an appropriate
					// exception.
					path = c.getString(c.getColumnIndexOrThrow(getFileColumn(uri)));
				} else {
	                throw new NullPointerException("No file entry for row: "+uri);
					
				}
			} else
			// Are we trying to open a null path
	        if (TextUtils.isEmpty(path)) 
	            throw new NullPointerException("Null value in file column: " 
	            		+ getFileColumn(uri));
		} finally {
			if (c!=null)
				c.close();
		}
        
        // Open and return
        return ParcelFileDescriptor.open(new File(path), modeToMode(mode));
	}

	/* (non-Javadoc)
	 * @see org.sana.android.db.FileHelper#deleteFile(android.net.Uri)
	 *
	@Override
	public int deleteFile(Uri uri) {
		
		String whereClause = DBUtils.getWhereClause(uri, mUriHelper.match(uri), null);
		return this.onDeleteRelated(uri, whereClause, null);
	}

	/* (non-Javadoc)
	 * @see org.sana.android.db.DeleteHelper#onDelete(java.lang.String, java.lang.String[])
	 *
	@Override
	public int onDelete(Uri uri, String selection, String[] selectionArgs) {
		if(TextUtils.isEmpty(getFileColumn(uri)))
			onDeleteRelated(uri,selection, selectionArgs);
		SQLiteDatabase db = getWritableDatabase();
		int count = db.delete(getTable(uri), selection, selectionArgs);
		return count;
	}
	
	public int onDeleteRelated(Uri uri, String selection, String[] selectionArgs) {
		Cursor cursor = null;
		String[] projection = new String[]{ getFileColumn(uri) };
		int count = 0;
		try{
			cursor = onQuery(null, projection, selection,selectionArgs, null);
			int column = cursor.getColumnIndex(getFileColumn(uri));
			while(cursor.moveToNext()){
				String path = (cursor.getString(column));
				if(!TextUtils.isEmpty(path)){
					count = (new File(path).delete())? count +1: count;
				}
			}
		} finally { 
			if(cursor!=null)
				cursor.close();
		}
		return count;
	}
	/* (non-Javadoc)
	 * @see org.sana.android.db.QueryHelper#onQuery(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 *
	@Override
	public Cursor onQuery(String table, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(getTable(null));
        return qb.query(db, projection, selection, selectionArgs, null,
        		null, sortOrder);
	}
	*/
	
	/**
	 * 
	 * @param db
	 * @param in
	 */
	public void onCreate(SQLiteDatabase db, InputStream in){
		Log.d(TAG, "Attempting database creation.");
		try {
			readAndExecuteStream(db,in);
			Log.d(TAG, "Database creation complete");
		} catch (IOException e) {
			Log.d(TAG, "Database creation failed.");
			throw new IllegalArgumentException("SQL table definition file " 
					+ "null or unreadable");
		}
	}
	
	/**
	 * 
	 * @param db
	 * @param oldVersion
	 * @param newVersion
	 * @param in
	 */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion, InputStream in) {
        if(oldVersion < newVersion){
    		Log.d(TAG, "Attempting database upgrade.");
    		try {
    			readAndExecuteStream(db,in);
    			Log.d(TAG, "Database upgrade complete");
    		} catch (IOException e) {
    			Log.d(TAG, "Database upgrade failed.");
    			throw new IllegalArgumentException("SQL upgrade definition file " 
    					+ "null or unreadable");
    		}
        }
	}
	
	protected void readAndExecuteStream(SQLiteDatabase db, InputStream in) 
			throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String sql = reader.readLine();
		while(!TextUtils.isEmpty(sql)){
				Log.d(TAG, "Executing: "+ sql);
				db.execSQL(sql);
				sql = reader.readLine();
		}
	}
	
	
}
