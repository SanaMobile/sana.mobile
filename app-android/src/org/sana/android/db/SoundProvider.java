package org.sana.android.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import org.sana.android.db.SanaDB.SoundSQLFormat;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

/**
 * Content provider for audio content.
 * 
 * @author Sana Development Team
 *
 */
public class SoundProvider extends ContentProvider {

    private static final String TAG = "SoundProvider";
    
    private static final String SOUND_TABLE_NAME = "sounds";
    public static final String SOUND_BUCKET_NAME = "/sdcard/dcim/sana/";
    
    private static final int SOUNDS = 1;
    private static final int SOUND_ID = 2;
    
    private DatabaseHelper mOpenHelper;
    private static final UriMatcher sUriMatcher;
    private static HashMap<String,String> sSoundProjectionMap;
    
    /** {@inheritDoc} */
    @Override
    public boolean onCreate() {
        Log.i(TAG, "onCreate()");
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }
        
    private String buildFilenameFromId(String soundId) {
    	return "/data/data/org.sana/files/sound_" + soundId;
    }
    
    private String buildFilenameFromUri(Uri uri) {
    	return buildFilenameFromId(uri.getPathSegments().get(1));
    }
    
    private boolean deleteFile(String soundId) {
    	String filename = buildFilenameFromId(soundId);
    	
    	File f = new File(filename);
    	boolean result = f.delete();
    	Log.i(TAG, "Deleting file for id " + soundId + " : " + filename + " "
    			+ (result ? "succeeded" : "failed"));
    	return result;
    }
    
    private boolean deleteFile(Uri uri) {
    	String filename = buildFilenameFromUri(uri);
    	
    	File f = new File(filename);
    	boolean result = f.delete();
    	Log.i(TAG, "Deleting file for " + uri + " : " + filename + " " 
    			+ (result ? "succeeded" : "failed"));
    	return result;
    }

    /** {@inheritDoc} */
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws 
    	FileNotFoundException 
    {
        //String filename = SOUND_BUCKET_NAME + uri.getFragment();
        String filename = buildFilenameFromUri(uri);
        //String filename = "/data/data/org.sana/files/" + uri.getPathSegments().get(1);
        Log.i(TAG, "openFile() for filename: " + filename);
        File f = new File(filename);
       
        int m = ParcelFileDescriptor.MODE_READ_ONLY;
        if ("w".equals(mode)) {
            m = ParcelFileDescriptor.MODE_WRITE_ONLY 
            		| ParcelFileDescriptor.MODE_CREATE;
        } else if("rw".equals(mode) || "rwt".equals(mode)) {
            m = ParcelFileDescriptor.MODE_READ_WRITE;
        }
        return ParcelFileDescriptor.open(f,m);
    }
    
    /** {@inheritDoc} */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) 
    {
        Log.i(TAG, "query() uri="+uri.toString() + " projection=" 
        		+ TextUtils.join(",",projection));
        
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(SOUND_TABLE_NAME);
        
        switch(sUriMatcher.match(uri)) {
        case SOUNDS:    
            break;
        case SOUND_ID:
            qb.appendWhere(SoundSQLFormat._ID + "=" 
            		+ uri.getPathSegments().get(1));
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        String orderBy;
        if(TextUtils.isEmpty(sortOrder)) {
            orderBy = SoundSQLFormat.DEFAULT_SORT_ORDER;
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
        case SOUNDS:
            count = db.update(SOUND_TABLE_NAME, values,selection,selectionArgs);
            break;
            
        case SOUND_ID:
            String procedureId = uri.getPathSegments().get(1);
            count = db.update(SOUND_TABLE_NAME, values, SoundSQLFormat._ID + "="
            		+ procedureId + (!TextUtils.isEmpty(selection) 
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
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case SOUNDS:
            //count = db.delete(SOUND_TABLE_NAME, selection, selectionArgs);
            LinkedList<String> idList = new LinkedList<String>();
        	Cursor c = query(SoundSQLFormat.CONTENT_URI, new String[] { 
        			SoundSQLFormat._ID }, selection, selectionArgs, null);
        	if(c.moveToFirst()) {
        		while(!c.isAfterLast()) {
        			String id = c.getString(c.getColumnIndex(SoundSQLFormat._ID));
        			idList.add(id);
        			c.moveToNext();
        		}
        	}
        	c.deactivate();
        	
            count = db.delete(SOUND_TABLE_NAME, selection, selectionArgs);
            
            for(String id : idList) {
            	deleteFile(id);
            }
            break;
        case SOUND_ID:
            String soundId = uri.getPathSegments().get(1); 
            count = db.delete(SOUND_TABLE_NAME, SoundSQLFormat._ID + "=" 
            		+ soundId + (!TextUtils.isEmpty(selection) ? " AND (" 
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
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (sUriMatcher.match(uri) != SOUNDS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        ContentValues values;
        if(initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        
        Long now = Long.valueOf(System.currentTimeMillis());
        
        if(values.containsKey(SoundSQLFormat.CREATED_DATE) == false) {
            values.put(SoundSQLFormat.CREATED_DATE, now);
        }
        
        if(values.containsKey(SoundSQLFormat.MODIFIED_DATE) == false) {
            values.put(SoundSQLFormat.MODIFIED_DATE, now);
        }
        
        if(values.containsKey(SoundSQLFormat.ENCOUNTER_ID) == false) {
            values.put(SoundSQLFormat.ENCOUNTER_ID, "");
        }
        
        if(values.containsKey(SoundSQLFormat.FILE_URI) == false) {
            values.put(SoundSQLFormat.FILE_URI, "");
        }
        
//        if(values.containsKey(SoundSQLFormat.FILE_VALID) == false) {
//            values.put(SoundSQLFormat.FILE_VALID, false);
//        }
//        
//        if(values.containsKey(SoundSQLFormat.FILE_SIZE) == false) {
//            values.put(SoundSQLFormat.FILE_SIZE, 0);
//        }
        
        if(values.containsKey(SoundSQLFormat.UPLOAD_PROGRESS) == false) {
            values.put(SoundSQLFormat.UPLOAD_PROGRESS, 0);
        }
        
        if(values.containsKey(SoundSQLFormat.UPLOADED) == false) {
            values.put(SoundSQLFormat.UPLOADED, false);
        }
        
        if(values.containsKey(SoundSQLFormat.ELEMENT_ID) == false) {
            values.put(SoundSQLFormat.ELEMENT_ID, "");
        }
        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(SOUND_TABLE_NAME, 
        		SoundSQLFormat.ENCOUNTER_ID, values);
        if(rowId > 0) {
            
            String filename = rowId + "";
            try {
                getContext().openFileOutput(filename, 
                		Context.MODE_PRIVATE).close();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Couldn't make the file: " + e);
            } catch (IOException e) {
                Log.e(TAG, "Couldn't make the file: " + e);
            }
            String path = 
            	getContext().getFileStreamPath(filename).getAbsolutePath();
            Log.i(TAG, "File path is : " + path);
            Uri noteUri = ContentUris.withAppendedId(SoundSQLFormat.CONTENT_URI, 
            		rowId);
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
        case SOUNDS:
            return SoundSQLFormat.CONTENT_TYPE;
        case SOUND_ID:
            return SoundSQLFormat.CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
    
    /**
     * Creates the table.
     * @param db the database to create the table in.
     */
    public static void onCreateDatabase(SQLiteDatabase db) {
        Log.i(TAG, "Creating Sound Table");
        db.execSQL("CREATE TABLE " + SOUND_TABLE_NAME + " ("
                + SoundSQLFormat._ID + " INTEGER PRIMARY KEY,"
                + SoundSQLFormat.ENCOUNTER_ID + " TEXT,"
                + SoundSQLFormat.ELEMENT_ID + " TEXT,"
                + SoundSQLFormat.FILE_URI + " TEXT,"
//                + SoundSQLFormat.FILE_VALID + " INTEGER,"
//                + SoundSQLFormat.FILE_SIZE + " INTEGER,"
                + SoundSQLFormat.UPLOAD_PROGRESS + " INTEGER,"
                + SoundSQLFormat.UPLOADED + " INTEGER,"
                + SoundSQLFormat.CREATED_DATE + " INTEGER,"
                + SoundSQLFormat.MODIFIED_DATE + " INTEGER"
                + ");");
    }
    
    /**
     * Updates this providers table
     * @param db the db to update in 
     * @param oldVersion the current db version
     * @param newVersion the new db version
     */
    public static void onUpgradeDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion);
        if (oldVersion == 1 && newVersion == 2) {
        	// Do nothing
        }
    }

    
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(SanaDB.SOUND_AUTHORITY, "sounds", SOUNDS);
        sUriMatcher.addURI(SanaDB.SOUND_AUTHORITY, "sounds/#", SOUND_ID);
        
        sSoundProjectionMap = new HashMap<String, String>();
        sSoundProjectionMap.put(SoundSQLFormat._ID, SoundSQLFormat._ID);
        sSoundProjectionMap.put(SoundSQLFormat.ELEMENT_ID, SoundSQLFormat.ELEMENT_ID);
        sSoundProjectionMap.put(SoundSQLFormat.ENCOUNTER_ID, SoundSQLFormat.ENCOUNTER_ID);
        sSoundProjectionMap.put(SoundSQLFormat.FILE_URI, SoundSQLFormat.FILE_URI);
//        sSoundProjectionMap.put(SoundSQLFormat.FILE_VALID, SoundSQLFormat.FILE_VALID);
//        sSoundProjectionMap.put(SoundSQLFormat.FILE_SIZE, SoundSQLFormat.FILE_SIZE);
        sSoundProjectionMap.put(SoundSQLFormat.UPLOADED, SoundSQLFormat.UPLOADED);
        sSoundProjectionMap.put(SoundSQLFormat.UPLOAD_PROGRESS, SoundSQLFormat.UPLOAD_PROGRESS);
        sSoundProjectionMap.put(SoundSQLFormat.CREATED_DATE, SoundSQLFormat.CREATED_DATE);
        sSoundProjectionMap.put(SoundSQLFormat.MODIFIED_DATE, SoundSQLFormat.MODIFIED_DATE);
    }
    
}
