package org.sana.android.db;

import java.util.HashMap;

import org.sana.android.provider.Procedures;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Content provider for procedures.
 * 
 * @author Sana Development Team
 *
 */
public class ProcedureProvider extends ContentProvider {

    private static final String TAG = "ProcedureProvider";
    
    private static final String PROCEDURE_TABLE_NAME = "procedures";
    
    private static final int PROCEDURES = 1;
    private static final int PROCEDURE_ID = 2;
    
    private DatabaseHelper mOpenHelper;
    private static final UriMatcher sUriMatcher;
    private static HashMap<String,String> sProcedureProjectionMap;

    /** {@inheritDoc} */
    @Override
    public boolean onCreate() {
        Log.i(TAG, "onCreate()");
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        Log.i(TAG, "query() uri="+uri.toString() + " projection=" 
        		+ TextUtils.join(",",projection));
        
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(PROCEDURE_TABLE_NAME);
        
        switch(sUriMatcher.match(uri)) {
        case PROCEDURES:    
            break;
        case PROCEDURE_ID:
            qb.appendWhere(Procedures.Contract._ID + "=" 
            		+ uri.getPathSegments().get(1));
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        String orderBy;
        if(TextUtils.isEmpty(sortOrder)) {
            orderBy = Procedures.DEFAULT_SORT_ORDER;
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
        case PROCEDURES:
            count = db.update(PROCEDURE_TABLE_NAME, values, selection, 
            		selectionArgs);
            break;
            
        case PROCEDURE_ID:
            String procedureId = uri.getPathSegments().get(1);
            count = db.update(PROCEDURE_TABLE_NAME, values, 
            		Procedures.Contract._ID + "=" + procedureId 
            		+ (!TextUtils.isEmpty(selection) ? " AND (" 
            				+ selection + ")" : ""), selectionArgs);
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
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case PROCEDURES:
            count = db.delete(PROCEDURE_TABLE_NAME, selection, selectionArgs);
            break;
        case PROCEDURE_ID:
            String procedureId = uri.getPathSegments().get(1); 
            count = db.delete(PROCEDURE_TABLE_NAME, Procedures.Contract._ID 
            		+ "=" + procedureId + (!TextUtils.isEmpty(selection) 
            				? " AND (" + selection + ")" : ""), selectionArgs);
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
        if (sUriMatcher.match(uri) != PROCEDURES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        ContentValues values;
        if(initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        
        Long now = Long.valueOf(System.currentTimeMillis());
        
        if(values.containsKey(Procedures.Contract.CREATED) == false) {
            values.put(Procedures.Contract.CREATED, now);
        }
        
        if(values.containsKey(Procedures.Contract.MODIFIED) == false) {
            values.put(Procedures.Contract.MODIFIED, now);
        }
        
        if(values.containsKey(Procedures.Contract.TITLE) == false) {
            Resources r = Resources.getSystem();
            values.put(Procedures.Contract.TITLE, r.getString(
            		android.R.string.untitled));
        }
        
        if(values.containsKey(Procedures.Contract.AUTHOR) == false) {
            Resources r = Resources.getSystem();
            values.put(Procedures.Contract.AUTHOR, r.getString(
            		android.R.string.untitled));
        }
        
        if(values.containsKey(Procedures.Contract.UUID) == false) {
            Resources r = Resources.getSystem();
            values.put(Procedures.Contract.UUID, r.getString(
            		android.R.string.untitled));
        }
        
        if(values.containsKey(Procedures.Contract.PROCEDURE) == false) {
            values.put(Procedures.Contract.PROCEDURE, "");
        }
        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(PROCEDURE_TABLE_NAME, 
        		Procedures.Contract.PROCEDURE, values);
        if(rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(
            		Procedures.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }
        
        throw new SQLException("Failed to insert row into " + uri);
    }

    /** {@inheritDoc} */
    @Override
    public String getType(Uri uri) {
        Log.i(TAG, "getType(uri="+uri.toString()+")");
        switch(sUriMatcher.match(uri)) {
        case PROCEDURES:
            return Procedures.CONTENT_TYPE;
        case PROCEDURE_ID:
            return Procedures.CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    /**
     * Creates the table.
     * @param db the database to create the table in.
     */
    public static void onCreateDatabase(SQLiteDatabase db) {
        Log.i(TAG, "Creating Procedure Table");
        db.execSQL("CREATE TABLE " + PROCEDURE_TABLE_NAME + " ("
                + Procedures.Contract._ID + " INTEGER PRIMARY KEY,"
                + Procedures.Contract.TITLE + " TEXT,"
                + Procedures.Contract.AUTHOR + " TEXT,"
                + Procedures.Contract.UUID + " TEXT,"
                + Procedures.Contract.PROCEDURE + " TEXT,"
                + Procedures.Contract.CREATED + " INTEGER,"
                + Procedures.Contract.MODIFIED + " INTEGER"
                + ");");
    }
    
    /**
     * Updates this providers table
     * @param db the db to update in 
     * @param oldVersion the current db version
     * @param newVersion the new db version
     */
    public static void onUpgradeDatabase(SQLiteDatabase db, int oldVersion, 
    		int newVersion) 
    {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion);
        if (oldVersion == 1 && newVersion == 2) {
        	// Do nothing
        }
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Procedures.AUTHORITY, "procedures", PROCEDURES);
        sUriMatcher.addURI(Procedures.AUTHORITY, "procedures/#", PROCEDURE_ID);
        
        sProcedureProjectionMap = new HashMap<String, String>();
        sProcedureProjectionMap.put(Procedures.Contract._ID, Procedures.Contract._ID);
        sProcedureProjectionMap.put(Procedures.Contract.TITLE, Procedures.Contract.TITLE);
        sProcedureProjectionMap.put(Procedures.Contract.AUTHOR, Procedures.Contract.AUTHOR);
        sProcedureProjectionMap.put(Procedures.Contract.UUID, Procedures.Contract.UUID);
        sProcedureProjectionMap.put(Procedures.Contract.PROCEDURE, Procedures.Contract.PROCEDURE);
        sProcedureProjectionMap.put(Procedures.Contract.CREATED, Procedures.Contract.CREATED);
        sProcedureProjectionMap.put(Procedures.Contract.MODIFIED, Procedures.Contract.MODIFIED);
    }
    
}
