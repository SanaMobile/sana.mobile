package org.sana.android.db;

import java.io.FileNotFoundException;
import java.util.HashMap;

import org.sana.android.db.SanaDB.EducationResourceSQLFormat;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

//TODO This is currently only a skeletal implementation.
/**
 * Content provider for education resources.
 * 
 * @author Sana Development Team
 *
 */
public class EducationResourceProvider extends ContentProvider {

    private static final String TAG = 
    	EducationResourceProvider.class.getSimpleName();
    
    private static final String TABLE = "info";
    
    private static final int ITEMS = 1;
    private static final int ITEM_ID = 2;
    
    private DatabaseHelper mOpenHelper;
    private static final UriMatcher sUriMatcher;
    private static HashMap<String,String> sMediaProjectionMap;

    /** {@inheritDoc} */
    @Override
    public boolean onCreate() {
        Log.i(TAG, "onCreate()");
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws 
    	FileNotFoundException 
    {
    	return null;
    }

    /** {@inheritDoc} */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getType(Uri uri) {
        Log.i(TAG, "getType(uri="+uri.toString()+")");
        switch(sUriMatcher.match(uri)) {
        case ITEMS:
            return EducationResourceSQLFormat.CONTENT_TYPE;
        case ITEM_ID:
            return EducationResourceSQLFormat.CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    /**
     * Creates the table.
     * @param db the database to create the table in.
     */
    public static void onCreateDatabase(SQLiteDatabase db) {
        Log.i(TAG, "Creating " + TAG + " Table");
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + EducationResourceSQLFormat._ID + " INTEGER PRIMARY KEY,"
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
        if(oldVersion > 1){
        	db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        }
        onCreateDatabase(db);
    }

    
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(SanaDB.EDUCATIONRESOURCE_AUTHORITY, TABLE, ITEMS);
        sUriMatcher.addURI(SanaDB.EDUCATIONRESOURCE_AUTHORITY, TABLE+"/#", ITEM_ID);
        
        sMediaProjectionMap = new HashMap<String, String>();
        sMediaProjectionMap.put(EducationResourceSQLFormat._ID, EducationResourceSQLFormat._ID);
    }
    
}
