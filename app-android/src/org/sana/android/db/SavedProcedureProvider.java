package org.sana.android.db;

import java.util.ArrayList;
import java.util.HashMap;

import org.sana.android.db.SanaDB.BinarySQLFormat;
import org.sana.android.db.SanaDB.DatabaseHelper;
import org.sana.android.db.SanaDB.ImageSQLFormat;
import org.sana.android.db.SanaDB.SavedProcedureSQLFormat;
import org.sana.android.db.SanaDB.SoundSQLFormat;
import org.sana.android.util.SanaUtil;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Content provider for patient encounters.
 * 
 * @author Sana Development Team
 *
 */
public class SavedProcedureProvider extends ContentProvider {
	
    private static final String TAG = SavedProcedureProvider.class.getSimpleName();

    private static final String SAVED_PROCEDURE_TABLE_NAME = "saved_procedures";
    
    private static final int SAVED_PROCEDURES = 1;
    private static final int SAVED_PROCEDURE_ID = 2;
    
    private DatabaseHelper mOpenHelper;
    private static final UriMatcher sUriMatcher;
    private static HashMap<String,String> sSavedProcedureProjectionMap;

    /** {@inheritDoc} */
    @Override
    public boolean onCreate() {
        Log.i(TAG, "onCreate()");
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }
    
    private void deleteRelated(String savedProcedureId) {
		getContext().getContentResolver().delete(ImageSQLFormat.CONTENT_URI,
				ImageSQLFormat.SAVED_PROCEDURE_ID + " = ?",
				new String[] { savedProcedureId });
		getContext().getContentResolver().delete(SoundSQLFormat.CONTENT_URI,
				SoundSQLFormat.SAVED_PROCEDURE_ID + " = ?",
				new String[] { savedProcedureId });
		getContext().getContentResolver().delete(BinarySQLFormat.CONTENT_URI,
				BinarySQLFormat.SAVED_PROCEDURE_ID + " = ?",
				new String[] { savedProcedureId });
		// TODO notifications too?
	}

    /** {@inheritDoc} */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        Log.i(TAG, "query() uri="+uri.toString() + " projection=" 
        		+ TextUtils.join(",",projection));
        
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(SAVED_PROCEDURE_TABLE_NAME);
        
        switch(sUriMatcher.match(uri)) {
        case SAVED_PROCEDURES:    
            break;
        case SAVED_PROCEDURE_ID:
            qb.appendWhere(SavedProcedureSQLFormat._ID + "=" 
            		+ uri.getPathSegments().get(1));
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        String orderBy;
        if(TextUtils.isEmpty(sortOrder)) {
            orderBy = SavedProcedureSQLFormat.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }
        
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, 
        		null, orderBy);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    /** {@inheritDoc} */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = 0; 
        
        switch(sUriMatcher.match(uri)) {
        case SAVED_PROCEDURES:
            count = db.update(SAVED_PROCEDURE_TABLE_NAME, values, selection, 
            		selectionArgs);
            break;
            
        case SAVED_PROCEDURE_ID:
            String procedureId = uri.getPathSegments().get(1);
            count = db.update(SAVED_PROCEDURE_TABLE_NAME, values, 
            		SavedProcedureSQLFormat._ID + "=" + procedureId 
            		+ (!TextUtils.isEmpty(selection) ? " AND (" + selection 
            				+ ")" : ""), selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    /** {@inheritDoc} */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
    	Log.i(TAG, "delete: " + uri);
    	
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case SAVED_PROCEDURES:
        	
        	Cursor c = query(SavedProcedureSQLFormat.CONTENT_URI, 
        			new String[] { SavedProcedureSQLFormat._ID }, selection, 
        			selectionArgs, null);
        	ArrayList<String> idList = new ArrayList<String>(c.getCount());
        	if(c.moveToFirst()) {
        		while(!c.isAfterLast()) {
        			String id = c.getString(c.getColumnIndex(
        					SavedProcedureSQLFormat._ID));
        			idList.add(id);
        			c.moveToNext();
        		}
        	}
        	c.deactivate();
        	
            count = db.delete(SAVED_PROCEDURE_TABLE_NAME, selection, 
            		selectionArgs);

            // Do this after so that SavedProcedures remain consistent, while 
            // everything else does not. 
            for(String id : idList) {
            	deleteRelated(id);
            }
            
            break;
        case SAVED_PROCEDURE_ID:
        	String procedureId = uri.getPathSegments().get(1);
            count = db.delete(SAVED_PROCEDURE_TABLE_NAME, 
            		SavedProcedureSQLFormat._ID + "=" + procedureId 
            		+ (!TextUtils.isEmpty(selection) ? " AND (" + selection 
            				+ ")" : ""), selectionArgs);

            // Do this after so that SavedProcedures remain consistent, while 
            // everything else does not. 
            deleteRelated(procedureId);
            
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    /** {@inheritDoc} */
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (sUriMatcher.match(uri) != SAVED_PROCEDURES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        ContentValues values;
        if(initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        
        Long now = Long.valueOf(System.currentTimeMillis());
        
        if(values.containsKey(SavedProcedureSQLFormat.CREATED_DATE) == false) {
            values.put(SavedProcedureSQLFormat.CREATED_DATE, now);
        }
        
        if(values.containsKey(SavedProcedureSQLFormat.MODIFIED_DATE) == false) {
            values.put(SavedProcedureSQLFormat.MODIFIED_DATE, now);
        }
        
        if(values.containsKey(SavedProcedureSQLFormat.GUID) == false) {
        	values.put(SavedProcedureSQLFormat.GUID, SanaUtil.randomString("SP",
        			20));
        }
        
        if(values.containsKey(SavedProcedureSQLFormat.PROCEDURE_ID) == false) {
            values.put(SavedProcedureSQLFormat.PROCEDURE_ID, -1);
        }
        
        if(values.containsKey(SavedProcedureSQLFormat.PROCEDURE_STATE)== false){
            values.put(SavedProcedureSQLFormat.PROCEDURE_STATE, "");
        }
        
        if(values.containsKey(SavedProcedureSQLFormat.FINISHED) == false) {
            values.put(SavedProcedureSQLFormat.FINISHED, false);
        }
        
        if(values.containsKey(SavedProcedureSQLFormat.UPLOADED) == false) {
            values.put(SavedProcedureSQLFormat.UPLOADED, false);
        }
        
        if(values.containsKey(SavedProcedureSQLFormat.UPLOAD_STATUS) == false) {
            values.put(SavedProcedureSQLFormat.UPLOAD_STATUS, -1);
        }
        
        if(values.containsKey(SavedProcedureSQLFormat.UPLOAD_QUEUE) == false) {
            values.put(SavedProcedureSQLFormat.UPLOAD_QUEUE, -1);
        }
        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(SAVED_PROCEDURE_TABLE_NAME, 
        		SavedProcedureSQLFormat.PROCEDURE_STATE, values);
        if(rowId > 0) {
            Uri savedProcedureUri = ContentUris.withAppendedId(
            		SavedProcedureSQLFormat.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(savedProcedureUri,
            		null);
            return savedProcedureUri;
        }
       
        throw new SQLException("Failed to insert row into " + uri);
    }

    /** {@inheritDoc} */
    @Override
    public String getType(Uri uri) {
        Log.i(TAG, "getType(uri="+uri.toString()+")");
        switch(sUriMatcher.match(uri)) {
        case SAVED_PROCEDURES:
            return SavedProcedureSQLFormat.CONTENT_TYPE;
        case SAVED_PROCEDURE_ID:
            return SavedProcedureSQLFormat.CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    /**
     * Creates the table.
     * @param db the database to create the table in.
     */
    public static void onCreateDatabase(SQLiteDatabase db) {
        Log.i(TAG, "Creating Saved Procedure Table");
        db.execSQL("CREATE TABLE " + SAVED_PROCEDURE_TABLE_NAME + " ("
                + SavedProcedureSQLFormat._ID + " INTEGER PRIMARY KEY,"
                + SavedProcedureSQLFormat.GUID + " TEXT,"
                + SavedProcedureSQLFormat.PROCEDURE_ID + " INTEGER,"
                + SavedProcedureSQLFormat.SUBJECT + " INTEGER NOT NULL,"
                + SavedProcedureSQLFormat.PROCEDURE_STATE + " TEXT,"
                + SavedProcedureSQLFormat.FINISHED + " INTEGER,"
                + SavedProcedureSQLFormat.UPLOADED + " INTEGER,"
                + SavedProcedureSQLFormat.UPLOAD_STATUS + " TEXT,"
                + SavedProcedureSQLFormat.UPLOAD_QUEUE + " TEXT,"
                + SavedProcedureSQLFormat.CREATED_DATE + " INTEGER,"
                + SavedProcedureSQLFormat.MODIFIED_DATE + " INTEGER"
                + ");");
    }

    /**
     * Updates this providers table
     * @param db the db to update in 
     * @param oldVersion the current db version
     * @param newVersion the new db version
     */
    public static void onUpgradeDatabase(SQLiteDatabase db, int oldVersion, 
    		int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion);
        if (oldVersion == 1 && newVersion == 2) {
        	// Do nothing
        } else if (oldVersion <= 3 && newVersion == 4){
        	String sql = "ALTER TABLE " + SAVED_PROCEDURE_TABLE_NAME +
        			" ADD COLUMN " + SavedProcedureSQLFormat.SUBJECT + " INTEGER DEFAULT '-1'";
        	db.execSQL(sql);
        }
    }


    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(SanaDB.SAVED_PROCEDURE_AUTHORITY, "savedProcedures", SAVED_PROCEDURES);
        sUriMatcher.addURI(SanaDB.SAVED_PROCEDURE_AUTHORITY, "savedProcedures/#", SAVED_PROCEDURE_ID);
        
        sSavedProcedureProjectionMap = new HashMap<String, String>();
        sSavedProcedureProjectionMap.put(SavedProcedureSQLFormat._ID, SavedProcedureSQLFormat._ID);
        sSavedProcedureProjectionMap.put(SavedProcedureSQLFormat.PROCEDURE_ID, SavedProcedureSQLFormat.PROCEDURE_ID);
        sSavedProcedureProjectionMap.put(SavedProcedureSQLFormat.GUID, SavedProcedureSQLFormat.GUID);
        sSavedProcedureProjectionMap.put(SavedProcedureSQLFormat.PROCEDURE_STATE, SavedProcedureSQLFormat.PROCEDURE_STATE);
        sSavedProcedureProjectionMap.put(SavedProcedureSQLFormat.FINISHED, SavedProcedureSQLFormat.FINISHED);
        sSavedProcedureProjectionMap.put(SavedProcedureSQLFormat.UPLOADED, SavedProcedureSQLFormat.UPLOADED);
        sSavedProcedureProjectionMap.put(SavedProcedureSQLFormat.UPLOAD_STATUS, SavedProcedureSQLFormat.UPLOAD_STATUS);
        sSavedProcedureProjectionMap.put(SavedProcedureSQLFormat.UPLOAD_QUEUE, SavedProcedureSQLFormat.UPLOAD_QUEUE);
        sSavedProcedureProjectionMap.put(SavedProcedureSQLFormat.CREATED_DATE, SavedProcedureSQLFormat.CREATED_DATE);
        sSavedProcedureProjectionMap.put(SavedProcedureSQLFormat.MODIFIED_DATE, SavedProcedureSQLFormat.MODIFIED_DATE);
        sSavedProcedureProjectionMap.put(SavedProcedureSQLFormat.SUBJECT, SavedProcedureSQLFormat.SUBJECT);
    }
}
