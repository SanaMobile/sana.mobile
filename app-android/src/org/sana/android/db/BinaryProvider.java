package org.sana.android.db;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import org.sana.android.db.SanaDB.BinarySQLFormat;

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
 * Content provider for binary file content.
 * 
 * @author Sana Development Team
 *
 */
public class BinaryProvider extends ContentProvider {

    public static final String TAG = BinaryProvider.class.getSimpleName();
    
    protected static final String TABLE = "binaries";
    protected static final String[] PROJ_ITEM_CONTENT = new String[]{ 
    	BinarySQLFormat.CONTENT };
    
    protected static final String[] PROJ_ID_AND_CONTENT = new String[]{ 
    	BinarySQLFormat._ID,  BinarySQLFormat.CONTENT };
    
    protected static final String OBS_WHERE =  
    	BinarySQLFormat.ENCOUNTER_ID + " = ? AND " 
    	+  BinarySQLFormat.ELEMENT_ID +" = ?" ;
    
    protected static final String[] PROJ_ID = new String[]{ 
    	BinarySQLFormat._ID };
    
    private static final int ITEMS = 1;
    private static final int ITEM_ID = 2;
    
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
        
    private String getFileUriString(Uri uri){
    	String fUri = null;
        Cursor c = null;
        try{
        	c = query(uri, PROJ_ITEM_CONTENT, null, null, null);
        	if(c.moveToFirst()){
        		int fColumn = c.getColumnIndex(BinarySQLFormat.CONTENT);
        		fUri = c.getString(fColumn);
        	}
        } finally {
        	if(c != null) c.close();
        }
        return fUri;
    }
    
    private Uri getItemContentUri(Uri uri){
    	String fUri = getFileUriString(uri);
    	return (TextUtils.isEmpty(fUri))? null: Uri.parse(fUri);
    }

    /** {@inheritDoc} */
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws 
    	FileNotFoundException 
    {
        Uri fUri = null;
        String fString = null;
        try{
        	fUri = getItemContentUri(uri);
        	Log.i(TAG, "openFile() for file uri: " + fUri);
        	return getContext().getContentResolver().openFileDescriptor(fUri, 
        			mode);
        } catch (FileNotFoundException e){
        	Log.e(TAG, "File not found: " + fString);
        	throw e;
        } catch (Exception e){
        	throw new FileNotFoundException(e.getMessage() + ":" + fString);
        } 
        
    }
    
    /** {@inheritDoc} */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) 
    {
        Log.i(TAG, "query() uri="+uri.toString() + " projection=" 
        		+ TextUtils.join(",",projection));
        
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE);
        
        switch(sUriMatcher.match(uri)) {
        case ITEMS:    
            break;
        case ITEM_ID:
            qb.appendWhere(BinarySQLFormat._ID + "=" 
            		+ uri.getPathSegments().get(1));
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        String orderBy;
        if(TextUtils.isEmpty(sortOrder)) {
            orderBy = BinarySQLFormat.DEFAULT_SORT_ORDER;
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
        case ITEMS:
            count = db.update(TABLE, values,selection,selectionArgs);
            break;
            
        case ITEM_ID:
            String procedureId = uri.getPathSegments().get(1);
            count = db.update(TABLE, values, BinarySQLFormat._ID + "="
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
		Log.d(TAG, "Content uri: " + BinarySQLFormat.CONTENT_URI);
		Log.d(TAG, "Args: " + selectionArgs);
		Log.d(TAG, "Selection: " + selection);
        int count;
        switch (sUriMatcher.match(uri)) {
        case ITEMS:
        	Cursor c = null;
        	c = query(BinarySQLFormat.CONTENT_URI, 
        			new String[]{ BinarySQLFormat._ID, 
        						  BinarySQLFormat.CONTENT }, 
        			selection, selectionArgs, null);
        	if(c.moveToFirst()) {
        		while(!c.isAfterLast()) {
        			String u = null;
        			long id = 0;
        			id = c.getLong(c.getColumnIndex(BinarySQLFormat._ID));
        			u = c.getString(c.getColumnIndex(
        					BinarySQLFormat.CONTENT));
        			Log.d(TAG, "Deleting: " + id);
        			Log.d(TAG, "Deleting: " + u);
        			Uri fUri = Uri.parse(u);
        			Log.d(TAG, "Deleting: " + fUri);
        			if(fUri != null)
        				try{
        					getContext().getContentResolver().delete(fUri,
        						null,null);
        				} catch(Exception e){
        					Log.e(TAG, e.getMessage());
        				}
        			c.moveToNext();
        		}
        	} 
        	if(c != null)
        		c.close();
            count = db.delete(TABLE, selection, selectionArgs);
            break;
        case ITEM_ID:
            String binId = uri.getPathSegments().get(1);
            Uri fUri = getItemContentUri(uri);
            getContext().getContentResolver().delete(fUri, null,null);
            count = db.delete(TABLE, BinarySQLFormat._ID + "=" 
            		+ binId + (!TextUtils.isEmpty(selection) ? " AND (" 
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
        if (sUriMatcher.match(uri) != ITEMS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        ContentValues values;
        if(initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        
        Long now = Long.valueOf(System.currentTimeMillis());
        
        if(values.containsKey(BinarySQLFormat.CREATED_DATE) == false) {
            values.put(BinarySQLFormat.CREATED_DATE, now);
        }
        
        if(values.containsKey(BinarySQLFormat.MODIFIED_DATE) == false) {
            values.put(BinarySQLFormat.MODIFIED_DATE, now);
        }
        
        if(values.containsKey(BinarySQLFormat.ENCOUNTER_ID) == false) {
            values.put(BinarySQLFormat.ENCOUNTER_ID, "");
        }
        
        if(values.containsKey(BinarySQLFormat.UPLOAD_PROGRESS) == false) {
            values.put(BinarySQLFormat.UPLOAD_PROGRESS, 0);
        }
        
        if(values.containsKey(BinarySQLFormat.UPLOADED) == false) {
            values.put(BinarySQLFormat.UPLOADED, false);
        }
        
        if(values.containsKey(BinarySQLFormat.ELEMENT_ID) == false) {
            values.put(BinarySQLFormat.ELEMENT_ID, "");
        }
        
        if(values.containsKey(BinarySQLFormat.CONTENT) == false) {
            values.put(BinarySQLFormat.CONTENT, "");
        }
        
        if(values.containsKey(BinarySQLFormat.MIME) == false) {
            values.put(BinarySQLFormat.MIME, "application/octet-stream");
        }
        
        // Defaults to empty string for file path
        if(values.containsKey(BinarySQLFormat.DATA) == false) {
            values.put(BinarySQLFormat.DATA, "");
        }
        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(TABLE, 
        		BinarySQLFormat.ENCOUNTER_ID, values);
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
            Uri noteUri = ContentUris.withAppendedId(BinarySQLFormat.CONTENT_URI, 
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
        case ITEMS:
            return BinarySQLFormat.CONTENT_TYPE;
        case ITEM_ID:
            return BinarySQLFormat.CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
    
    /**
     * Creates the table.
     * @param db the database to create the table in.
     */
    public static void onCreateDatabase(SQLiteDatabase db) {
        Log.i(TAG, "Creating Binary Table");
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + BinarySQLFormat._ID + " INTEGER PRIMARY KEY,"
                + BinarySQLFormat.ENCOUNTER_ID + " TEXT,"
                + BinarySQLFormat.ELEMENT_ID + " TEXT,"
                + BinarySQLFormat.UPLOAD_PROGRESS + " INTEGER,"
                + BinarySQLFormat.UPLOADED + " INTEGER,"
                + BinarySQLFormat.CREATED_DATE + " INTEGER,"
                + BinarySQLFormat.MODIFIED_DATE + " INTEGER,"
                + BinarySQLFormat.CONTENT + " TEXT,"
                + BinarySQLFormat.MIME + " TEXT,"
                + BinarySQLFormat.DATA + " TEXT"
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
        // BinaryProvider only shows up in version 3 or higher
        if(oldVersion <= 2){
        	db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        }
        onCreateDatabase(db);
    }

    
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(SanaDB.BINARY_AUTHORITY, TABLE, ITEMS);
        sUriMatcher.addURI(SanaDB.BINARY_AUTHORITY, TABLE+"/#", ITEM_ID);
        
        sProjectionMap = new HashMap<String, String>();
        sProjectionMap.put(BinarySQLFormat._ID, BinarySQLFormat._ID);
        sProjectionMap.put(BinarySQLFormat.ELEMENT_ID, BinarySQLFormat.ELEMENT_ID);
        sProjectionMap.put(BinarySQLFormat.ENCOUNTER_ID, BinarySQLFormat.ENCOUNTER_ID);
        sProjectionMap.put(BinarySQLFormat.UPLOADED, BinarySQLFormat.UPLOADED);
        sProjectionMap.put(BinarySQLFormat.UPLOAD_PROGRESS, BinarySQLFormat.UPLOAD_PROGRESS);
        sProjectionMap.put(BinarySQLFormat.CREATED_DATE, BinarySQLFormat.CREATED_DATE);
        sProjectionMap.put(BinarySQLFormat.MODIFIED_DATE, BinarySQLFormat.MODIFIED_DATE);
        sProjectionMap.put(BinarySQLFormat.CONTENT, BinarySQLFormat.CONTENT);
        sProjectionMap.put(BinarySQLFormat.MIME, BinarySQLFormat.MIME);
        sProjectionMap.put(BinarySQLFormat.CONTENT, BinarySQLFormat.CONTENT);
    }
    
}
