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

import java.io.File;
import java.io.FileNotFoundException;

import org.sana.android.db.DBUtils;
import org.sana.android.db.DatabaseManager;
import org.sana.android.db.DatabaseOpenHelper;
import org.sana.android.db.TableHelper;
import org.sana.android.db.impl.ConceptsHelper;
import org.sana.android.db.impl.EncounterTasksHelper;
import org.sana.android.db.impl.EncountersHelper;
import org.sana.android.db.impl.EventsHelper;
import org.sana.android.db.impl.InstructionsHelper;
import org.sana.android.db.impl.NotificationsHelper;
import org.sana.android.db.impl.ObservationsHelper;
import org.sana.android.db.impl.ObserversHelper;
import org.sana.android.db.impl.ProceduresHelper;
import org.sana.android.db.impl.SubjectsHelper;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

/**
 * Abstract implementation of of {@link android.content.ContentProvider} for
 * classes extending {@link org.sana.api.IModel} each of which maps to a table within
 * the database. This implementation uses the 
 * {@link org.sana.android.content.Uris Uris} for mapping the 
 * {@link android.net.Uri Uri's} to each table and extensions of the 
 * {@link org.sana.android.db.TableHelper} class to handle interactions for
 * each table. Extending classes should only need to implement the {@link #onCreate()}
 * method which provide the database name and version.
 * 
 * @author Sana Development
 *
 */
public abstract class ModelContentProvider extends ContentProvider {
	public static final String TAG = ModelContentProvider.class.getSimpleName();
	public static final String AUTHORITY = "org.sana.provider";
	
	protected static final String DATABASE = "models.db";
	
	protected DatabaseOpenHelper mOpener;
	protected DatabaseManager mManager;
	// match types
	public static final int ITEMS = 0;
	public static final int ITEM_ID = 1;
	
	static final ModelMatcher mMatcher = ModelMatcher.getInstance();
	
	protected String getTable(Uri uri){
		return getTableHelper(uri).getTable();
	}
	
	protected TableHelper<?> getTableHelper(Uri uri){
		int match = Uris.getContentDescriptor(uri);
		switch(match){
		case(Uris.CONCEPT):
			return ConceptsHelper.getInstance();
		case(Uris.ENCOUNTER):
			return EncountersHelper.getInstance();
		case(Uris.EVENT):
			return EventsHelper.getInstance();
		case(Uris.INSTRUCTION):
			return InstructionsHelper.getInstance();
		case(Uris.NOTIFICATION):
			return NotificationsHelper.getInstance();
		case(Uris.OBSERVATION):
			return ObservationsHelper.getInstance();
		case(Uris.OBSERVER):
			return ObserversHelper.getInstance();
		case(Uris.PROCEDURE):
			return ProceduresHelper.getInstance();
		case(Uris.SUBJECT):
			return SubjectsHelper.getInstance();
		case(Uris.ENCOUNTER_TASK):
			return EncounterTasksHelper.getInstance();
		default:
			throw new IllegalArgumentException("Invalid uri in "
						+"getTableHelper(): " + uri.toString());
		}
	}
	
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public synchronized  int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete() uri=" + uri 
				+ ", selection= " + selection
			    + ", selectionArgs=" + ((selectionArgs != null)?TextUtils.join(",", selectionArgs):"null")
				+ " );");
		String whereClause = DBUtils.getWhereClause(uri, 
				Uris.getDescriptor(uri), 
				selection);

		switch(Uris.getTypeDescriptor(uri)){
		case(Uris.ITEM_ID):
			selection = DBUtils.getWhereClauseWithID(uri, selection);
			break;
		case(Uris.ITEM_UUID):
			selection = DBUtils.getWhereClauseWithUUID(uri, selection);
		default:
		}
        TableHelper<?> helper = getTableHelper(uri);
		String table = helper.getTable();
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
		int count = db.delete(table, selection, selectionArgs);
        DatabaseManager.getInstance().closeDatabase();
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {
		return Uris.getType(uri);
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public synchronized Uri insert(Uri uri, ContentValues values) {
		Log.d(TAG, "insert(" + uri.toString() +", N = " 
	        	+ String.valueOf((values == null)?0:values.size()) + " values.)");
        TableHelper<?> helper = getTableHelper(uri);
        
        // set default insert values and execute
        values = helper.onInsert(values);
		String table = helper.getTable();
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();//mOpener.getWritableDatabase();
		long id = db.insert(table, null, values);
		DatabaseManager.getInstance().closeDatabase();
		
		Uri result = ContentUris.withAppendedId(uri, id);
		getContext().getContentResolver().notifyChange(uri, null);
		Log.d(TAG, "insert(): Successfully inserted => " + result);
		return result;
	}


	/* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public synchronized Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
        Log.d(TAG, ".query(" + uri.toString() +");");
        TableHelper<?> helper = getTableHelper(uri);
        
        // set query and execute
        sortOrder = (TextUtils.isEmpty(sortOrder))? helper.onSort(uri): sortOrder;
		switch(Uris.getTypeDescriptor(uri)){
		case(Uris.ITEM_ID):
			selection = DBUtils.getWhereClauseWithID(uri, selection);
			break;
		case(Uris.ITEM_UUID):
			selection = DBUtils.getWhereClauseWithUUID(uri, selection);
		default:
		}
        Log.d(TAG, ".query(.) selection = " + selection);
        String uriQS = DBUtils.convertUriQueryToSelect(uri);
        Log.d(TAG, ".query(.) uri qs = " + selection);
        if(!TextUtils.isEmpty(uriQS)){
        	selection = String.format("%s %s", selection, uriQS);
        	Log.d(TAG, ".query(.) selection --> " + selection);
		}
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(helper.getTable());
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();//mOpener.getReadableDatabase();
        //Cursor cursor = helper.onQuery(db, projection, selection, selectionArgs, sortOrder);
        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        Log.d(TAG, ".query(" + uri.toString() +") count = " + ((cursor!=null)?cursor.getCount():0));
        return cursor;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public synchronized int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
        Log.d(TAG, ".update(" + uri.toString() +");");//mOpener.getWritableDatabase();
		
        // set any default update values
		TableHelper<?> helper = getTableHelper(uri);
		values = helper.onUpdate(uri, values);
		String table = helper.getTable();
		
		// set selection and execute
		switch(Uris.getTypeDescriptor(uri)){
		case(Uris.ITEM_ID):
			selection = DBUtils.getWhereClauseWithID(uri, selection);
			break;
		case(Uris.ITEM_UUID):
			selection = DBUtils.getWhereClauseWithUUID(uri, selection);
		default:
		}
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
		int result = db.update(table, values, selection, selectionArgs);
		DatabaseManager.getInstance().closeDatabase();
		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.content.ContentProvider#openFile(android.net.Uri, java.lang.String)
	 */
	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws FileNotFoundException 
	{
		Log.i(TAG, "openFile()" + uri);
    	Log.d(TAG,"...uri: " + uri);
    	Log.d(TAG,"...mode: " + mode);
    	
    	String ext = getTableHelper(uri).getFileExtension();
		int match = Uris.getContentDescriptor(uri);
		switch(match){
		case(Uris.OBSERVATION):
		case(Uris.SUBJECT):
			break;
		default:
			throw new FileNotFoundException("Unsupported content type. No files.");
		}
		TableHelper<?> helper = getTableHelper(uri);
		String column = helper.getFileColumn();
		Cursor c = query(uri, new String[]{ column }, null, null, null);
		
		String path = null;
        if (c != null) {
        	try{
        		if(c.moveToFirst()){
        			// Should never get more than one back
        			if(c.getCount() > 1)
        				throw new IllegalArgumentException(
        						"Vaild for single row only");
        			// get file and open
        	        path = c.getString(0);
        	        Log.d(TAG, "...opening file path: " + path);
        		} else {
                    throw new IllegalArgumentException("Invalid Uri: " + uri);
        		}
        	} finally {
        		c.close();
        	}
        }
        
        File fopen;
    	int modeBits = modeToMode(mode);
    	// Create file
        if (TextUtils.isEmpty(path)){
        	Log.d(TAG,"...path was empty.");
        	if(modeBits == ParcelFileDescriptor.MODE_READ_ONLY)
        		throw new IllegalArgumentException("Read only open on empty File path "
            		+"in column '" + column + "' for uri: " + uri);
            // Open in read write with no file name
        	long id = ContentUris.parseId(uri);
        	File dir = getContext().getExternalFilesDir(helper.getTable());
        	boolean created = dir.mkdirs();
        	Log.d(TAG,"...created parent dirs: " + created);
        	//File dir = new File(fDir,helper.getTable());
        	dir.mkdirs();
        	fopen = new File(dir,String.format("%s.%s", id,ext));
        	// Update file column with absolute path
        	ContentValues values = new ContentValues();
        	values.put(column, fopen.getAbsolutePath());
        	int updated = getContext().getContentResolver().update(uri, values, null,null);
        	Log.d(TAG, "updated: " + updated + ", file: " + fopen.getAbsolutePath());
        } else {
        	fopen = new File(path);
        }
    	Log.d(TAG,"...opening file: " + fopen.getAbsolutePath());
    	Log.d(TAG,"...opening in mode: " + mode);
        return ParcelFileDescriptor.open(fopen, modeBits);
	}
	
	protected final int modeToMode(String mode){
		int modeBits;
		if ("r".equals(mode)) {
			modeBits = ParcelFileDescriptor.MODE_READ_ONLY;
		} else if ("w".equals(mode) || "wt".equals(mode)) {
			modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
					| ParcelFileDescriptor.MODE_CREATE
					| ParcelFileDescriptor.MODE_TRUNCATE;
		} else if ("wa".equals(mode)) {
			modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
					| ParcelFileDescriptor.MODE_CREATE
					| ParcelFileDescriptor.MODE_APPEND;
		} else if ("rw".equals(mode)) {
			modeBits = ParcelFileDescriptor.MODE_READ_WRITE
					| ParcelFileDescriptor.MODE_CREATE;
		} else if ("rwt".equals(mode)) {
			modeBits = ParcelFileDescriptor.MODE_READ_WRITE
					| ParcelFileDescriptor.MODE_CREATE
					| ParcelFileDescriptor.MODE_TRUNCATE;
		} else {
			throw new IllegalArgumentException("Bad mode: " + mode);
		}
		return modeBits;
	}
}
