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

import org.sana.android.content.ModelMatcher.Code;
import org.sana.android.db.DBUtils;
import org.sana.android.db.DatabaseOpenHelper;
import org.sana.android.db.TableHelper;
import org.sana.android.db.impl.ConceptsHelper;
import org.sana.android.db.impl.EncountersHelper;
import org.sana.android.db.impl.EventsHelper;
import org.sana.android.db.impl.InstructionsHelper;
import org.sana.android.db.impl.NotificationsHelper;
import org.sana.android.db.impl.ObservationsHelper;
import org.sana.android.db.impl.ObserversHelper;
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
 * classes extending {@link org.sana.Model} each of which maps to a table within
 * the database. This implementation uses the 
 * {@link org.sana.android.content.ModelMatcher} for mapping the 
 * {@link android.net.Uri Uri's} to each table and extensions of the 
 * {@link org.sana.android.db.TableHelper} class to handle interactions for
 * each table. Extending classes should only need to implement the {@link #onCreate()}
 * method which instantiates the {@link ModelMatcher} and provide the database 
 * name and version.
 * 
 * @author Sana Development
 *
 */
public abstract class ModelContentProvider extends ContentProvider {
	public static final String TAG = ModelContentProvider.class.getSimpleName();
	public static final String AUTHORITY = "org.sana.provider";
	
	protected static final String DATABASE = "models.db";
	
	protected DatabaseOpenHelper mOpener;
	
	// match types
	public static final int ITEMS = 0;
	public static final int ITEM_ID = 1;
	
	static final ModelMatcher mMatcher = ModelMatcher.getInstance();
	
	protected String getTable(Uri uri){
		return getTableHelper(uri).getTable();
	}
	
	private TableHelper<?> getTableHelper(Uri uri){
		int match = mMatcher.matchObject(uri);
		switch(match){
		case(Code.CONCEPT):
			return ConceptsHelper.getInstance();
		case(Code.ENCOUNTER):
			return EncountersHelper.getInstance();
		case(Code.EVENT):
			return EventsHelper.getInstance();
		case(Code.INSTRUCTION):
			return InstructionsHelper.getInstance();
		case(Code.NOTIFICATION):
			return NotificationsHelper.getInstance();
		case(Code.OBSERVATION):
			return ObservationsHelper.getInstance();
		case(Code.OBSERVER):
			return ObserversHelper.getInstance();
		case(Code.SUBJECT):
			return SubjectsHelper.getInstance();
		default:
			throw new IllegalArgumentException("Invalid uri in "
						+"getTableHelper(): " + uri.toString());
		}
	}
	
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, ".delete(" + uri.toString() +");");
		String whereClause = DBUtils.getWhereClause(uri, 
				mMatcher.match(uri), 
				selection);
		SQLiteDatabase db = mOpener.getWritableDatabase();
		int count = db.delete(getTable(uri), whereClause, selectionArgs);
		return count;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {
		return mMatcher.getType(uri);
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d(TAG, "insert(" + uri.toString() +", N = " 
	        	+ String.valueOf((values == null)?0:values.size()) + " values.)");
        SQLiteDatabase db = mOpener.getWritableDatabase();
        TableHelper<?> helper = getTableHelper(uri);
        
        // set default insert values and execute
        values = helper.onInsert(values);
		long id = db.insert(helper.getTable(), null, values);
		Uri result = ContentUris.withAppendedId(uri, id);
		getContext().getContentResolver().notifyChange(uri, null);
		Log.d(TAG, "insert(): Successfully inserted => " + result);
		return result;
	}


	/* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
        Log.d(TAG, ".query(" + uri.toString() +");");
        SQLiteDatabase db = mOpener.getReadableDatabase();
        TableHelper<?> helper = getTableHelper(uri);
        
        // set query and execute
        sortOrder = (TextUtils.isEmpty(sortOrder))? helper.onSort(uri): sortOrder;
		switch(mMatcher.matchContent(uri)){
		case(ModelMatcher.ITEM_ID):
			selection = DBUtils.getWhereClauseWithID(uri, selection);
			break;
		case(ModelMatcher.ITEM_UUID):
			selection = DBUtils.getWhereClauseWithUUID(uri, selection);
		default:
		}
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(helper.getTable());
        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null,
        		null, sortOrder);
        return cursor;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
        Log.d(TAG, ".update(" + uri.toString() +");");
		SQLiteDatabase db = mOpener.getWritableDatabase();
		
        // set any default update values
		TableHelper<?> helper = getTableHelper(uri);
		values = helper.onUpdate(uri, values);
		
		// set selection and execute
		switch(mMatcher.matchContent(uri)){
		case(ModelMatcher.ITEM_ID):
			selection = DBUtils.getWhereClauseWithID(uri, selection);
			break;
		case(ModelMatcher.ITEM_UUID):
			selection = DBUtils.getWhereClauseWithUUID(uri, selection);
		default:
		}
		int result = db.update(helper.getTable(), values, selection, selectionArgs);
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
        	        int i = c.getColumnIndex(column);
        	        path = c.getString(i);
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
        	if(modeBits == ParcelFileDescriptor.MODE_READ_ONLY)
        		throw new IllegalArgumentException("Read only open on empty File path "
            		+"in column '" + column + "' for uri: " + uri);
            // Open in read write with no file name
        	String ext = getTableHelper(uri).getFileExtension();
        	long id = ContentUris.parseId(uri);
        	File dir = getContext().getDir("files/" + helper.getTable(), 0);
        	fopen = new File(dir,String.format("%s.%s", id,ext));
        } else {
        	fopen = new File(path);
        }
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
