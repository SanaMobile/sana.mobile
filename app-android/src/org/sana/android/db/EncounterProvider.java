package org.sana.android.db;

import java.util.ArrayList;
import java.util.HashMap;

import org.sana.android.db.SanaDB.BinarySQLFormat;
import org.sana.android.db.SanaDB.ImageSQLFormat;
import org.sana.android.db.SanaDB.SoundSQLFormat;
import org.sana.android.provider.Encounters;
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
public class EncounterProvider extends ContentProvider {
	
    private static final String TAG = EncounterProvider.class.getSimpleName();

    private static final String ENCOUNTER_TABLE = "encounters";
    
    private static final int ENCOUNTERS = 1;
    private static final int ENCOUNTER_ID = 2;
    
    private DatabaseHelper mOpenHelper;
    private static final UriMatcher sUriMatcher;
    private static HashMap<String,String> sProjectionMap;

    /** {@inheritDoc} */
    @Override
    public boolean onCreate() {
        Log.i(TAG, "onCreate()");
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }
    
    private void deleteRelated(String encounterId) {
		getContext().getContentResolver().delete(ImageSQLFormat.CONTENT_URI,
				ImageSQLFormat.ENCOUNTER_ID + " = ?",
				new String[] { encounterId });
		getContext().getContentResolver().delete(SoundSQLFormat.CONTENT_URI,
				SoundSQLFormat.ENCOUNTER_ID + " = ?",
				new String[] { encounterId });
		getContext().getContentResolver().delete(BinarySQLFormat.CONTENT_URI,
				BinarySQLFormat.ENCOUNTER_ID + " = ?",
				new String[] { encounterId });
		// TODO notifications too?
	}

    /** {@inheritDoc} */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        Log.i(TAG, "query() uri="+uri.toString() + " projection=" 
        		+ TextUtils.join(",",projection));
        
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(ENCOUNTER_TABLE);
        
        switch(sUriMatcher.match(uri)) {
        case ENCOUNTERS:    
            break;
        case ENCOUNTER_ID:
            qb.appendWhere(Encounters.Contract._ID + "=" 
            		+ uri.getPathSegments().get(1));
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        String orderBy;
        if(TextUtils.isEmpty(sortOrder)) {
            orderBy = Encounters.DEFAULT_SORT_ORDER;
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
        case ENCOUNTERS:
            count = db.update(ENCOUNTER_TABLE, values, selection, 
            		selectionArgs);
            break;
            
        case ENCOUNTER_ID:
            String procedureId = uri.getPathSegments().get(1);
            count = db.update(ENCOUNTER_TABLE, values, 
            		Encounters.Contract._ID + "=" + procedureId 
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
        case ENCOUNTERS:
        	
        	Cursor c = query(Encounters.CONTENT_URI, 
        			new String[] { Encounters.Contract._ID }, selection, 
        			selectionArgs, null);
        	ArrayList<String> idList = new ArrayList<String>(c.getCount());
        	if(c.moveToFirst()) {
        		while(!c.isAfterLast()) {
        			String id = c.getString(c.getColumnIndex(
        					Encounters.Contract._ID));
        			idList.add(id);
        			c.moveToNext();
        		}
        	}
        	c.deactivate();
        	
            count = db.delete(ENCOUNTER_TABLE, selection, 
            		selectionArgs);

            // Do this after so that SavedProcedures remain consistent, while 
            // everything else does not. 
            for(String id : idList) {
            	deleteRelated(id);
            }
            
            break;
        case ENCOUNTER_ID:
        	String procedureId = uri.getPathSegments().get(1);
            count = db.delete(ENCOUNTER_TABLE, 
            		Encounters.Contract._ID + "=" + procedureId 
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
        if (sUriMatcher.match(uri) != ENCOUNTERS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        ContentValues values;
        if(initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        
        Long now = Long.valueOf(System.currentTimeMillis());
        
        if(values.containsKey(Encounters.Contract.CREATED) == false) {
            values.put(Encounters.Contract.CREATED, now);
        }
        
        if(values.containsKey(Encounters.Contract.MODIFIED) == false) {
            values.put(Encounters.Contract.MODIFIED, now);
        }
        
        if(values.containsKey(Encounters.Contract.UUID) == false) {
        	values.put(Encounters.Contract.UUID, SanaUtil.randomString("SP",
        			20));
        }
        
        if(values.containsKey(Encounters.Contract.PROCEDURE) == false) {
            values.put(Encounters.Contract.PROCEDURE, -1);
        }
        
        if(values.containsKey(Encounters.Contract.STATE)== false){
            values.put(Encounters.Contract.STATE, "");
        }
        
        if(values.containsKey(Encounters.Contract.FINISHED) == false) {
            values.put(Encounters.Contract.FINISHED, false);
        }
        
        if(values.containsKey(Encounters.Contract.UPLOADED) == false) {
            values.put(Encounters.Contract.UPLOADED, false);
        }
        
        if(values.containsKey(Encounters.Contract.UPLOAD_STATUS) == false) {
            values.put(Encounters.Contract.UPLOAD_STATUS, -1);
        }
        
        if(values.containsKey(Encounters.Contract.UPLOAD_QUEUE) == false) {
            values.put(Encounters.Contract.UPLOAD_QUEUE, -1);
        }
        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(ENCOUNTER_TABLE, 
        		Encounters.Contract.STATE, values);
        if(rowId > 0) {
            Uri savedProcedureUri = ContentUris.withAppendedId(
            		Encounters.CONTENT_URI, rowId);
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
        case ENCOUNTERS:
            return Encounters.CONTENT_TYPE;
        case ENCOUNTER_ID:
            return Encounters.CONTENT_ITEM_TYPE;
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
        db.execSQL("CREATE TABLE " + ENCOUNTER_TABLE + " ("
                + Encounters.Contract._ID + " INTEGER PRIMARY KEY,"
                + Encounters.Contract.UUID + " TEXT,"
                + Encounters.Contract.PROCEDURE + " INTEGER,"
                + Encounters.Contract.SUBJECT + " INTEGER NOT NULL,"
                + Encounters.Contract.STATE + " TEXT,"
                + Encounters.Contract.FINISHED + " INTEGER,"
                + Encounters.Contract.UPLOADED + " INTEGER,"
                + Encounters.Contract.UPLOAD_STATUS + " TEXT,"
                + Encounters.Contract.UPLOAD_QUEUE + " TEXT,"
                + Encounters.Contract.CREATED + " INTEGER,"
                + Encounters.Contract.MODIFIED + " INTEGER"
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
        	String sql = "ALTER TABLE " + ENCOUNTER_TABLE +
        			" ADD COLUMN " + Encounters.Contract.SUBJECT + " INTEGER DEFAULT '-1'";
        	db.execSQL(sql);
        }
    }


    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Encounters.AUTHORITY, "savedProcedures", ENCOUNTERS);
        sUriMatcher.addURI(Encounters.AUTHORITY, "savedProcedures/#", ENCOUNTER_ID);
        
        sProjectionMap = new HashMap<String, String>();
        sProjectionMap.put(Encounters.Contract._ID, Encounters.Contract._ID);
        sProjectionMap.put(Encounters.Contract.PROCEDURE, Encounters.Contract.PROCEDURE);
        sProjectionMap.put(Encounters.Contract.UUID, Encounters.Contract.UUID);
        sProjectionMap.put(Encounters.Contract.STATE, Encounters.Contract.STATE);
        sProjectionMap.put(Encounters.Contract.FINISHED, Encounters.Contract.FINISHED);
        sProjectionMap.put(Encounters.Contract.UPLOADED, Encounters.Contract.UPLOADED);
        sProjectionMap.put(Encounters.Contract.UPLOAD_STATUS, Encounters.Contract.UPLOAD_STATUS);
        sProjectionMap.put(Encounters.Contract.UPLOAD_QUEUE, Encounters.Contract.UPLOAD_QUEUE);
        sProjectionMap.put(Encounters.Contract.CREATED, Encounters.Contract.CREATED);
        sProjectionMap.put(Encounters.Contract.MODIFIED, Encounters.Contract.MODIFIED);
        sProjectionMap.put(Encounters.Contract.SUBJECT, Encounters.Contract.SUBJECT);
    }
}
