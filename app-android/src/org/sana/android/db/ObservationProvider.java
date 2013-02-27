/**
 * 
 */
package org.sana.android.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.sana.android.db.SanaDB.DatabaseHelper;
import org.sana.android.provider.Observations;
import org.sana.android.content.FileContentProvider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.support.v4.database.DatabaseUtilsCompat;
import android.text.TextUtils;
import android.util.Log;

/**
 * ContentProvider for the observation table.
 * 
 * @author Sana Development Team
 */
public class ObservationProvider extends FileContentProvider implements Observations.Contract{
	public static final String TAG = ObservationProvider.class.getSimpleName();
	
	public static final String DEFAULT_SORT_ORDER = "modified DESC";
	private static final String TABLE = "observation";
	
    private static final int ITEMS = 0;
    private static final int ITEM_ID = 1;

	static final String DB = "sana.db";
    private DatabaseHelper mOpenHelper;
    private static final UriMatcher sUriMatcher;
    private static final Map<String,String> sProjMap = new HashMap<String, String>();
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Observations.AUTHORITY, "observations", ITEMS);
        sUriMatcher.addURI(Observations.AUTHORITY, "observations/#", ITEM_ID);
        
        sProjMap.put(_ID, _ID);
        sProjMap.put(UUID, UUID);
        sProjMap.put(ENCOUNTER, ENCOUNTER);
        sProjMap.put(CONCEPT, CONCEPT);
        sProjMap.put(SUBJECT, SUBJECT);
        sProjMap.put(ID, ID);
        sProjMap.put(PARENT, PARENT);
        sProjMap.put(VALUE_TEXT, VALUE_TEXT);
        sProjMap.put(VALUE_COMPLEX,VALUE_COMPLEX);
        sProjMap.put(UPLOADED, UPLOADED);
        sProjMap.put(UPLOAD_PROGRESS, UPLOAD_PROGRESS);
        sProjMap.put(CREATED, CREATED);
        sProjMap.put(MODIFIED, MODIFIED);
    }
	
    /* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, ".delete(" + uri.toString() +");");
		String whereClause = DBUtils.getWhereWithIdOrReturn(uri, sUriMatcher.match(uri), selection);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count = db.delete(TABLE,whereClause,selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {
        Log.d(TAG, ".getType(" + uri.toString() +");");
        switch (sUriMatcher.match(uri)) {
        case ITEMS:
        	return Observations.CONTENT_TYPE;
        case ITEM_ID:
        	return Observations.CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert(" + uri.toString() +", N = " 
        	+ String.valueOf((values == null)?0:values.size()) + " values.)");
        
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		
		//allow created and modified to be set manually here.
		Long now = Long.valueOf(System.currentTimeMillis());
		if(values.containsKey(CREATED) == false) {
            values.put(CREATED, now);
        }
		
        if(values.containsKey(MODIFIED) == false) {
            values.put(MODIFIED, now);
        }

		Uri result = ContentUris.withAppendedId(uri, 
				db.insert(TABLE, null, values));
		getContext().getContentResolver().notifyChange(uri, null);
		Log.d(TAG, "insert(): Successfully inserted => " + result);
		return result;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
        Log.d(TAG, ".onCreate();");
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
        Log.d(TAG, ".query(" + uri.toString() +");");

		String orderBy;
        if(TextUtils.isEmpty(sortOrder)) {
            orderBy = DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TABLE);	
		String whereClause = DBUtils.getWhereWithIdOrReturn(uri, sUriMatcher.match(uri), selection);
        Cursor c = qb.query(db, projection, whereClause, selectionArgs, null,
        		null, orderBy);
		return c;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
        Log.d(TAG, ".update(" + uri.toString() +");");
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		String whereClause = DBUtils.getWhereWithIdOrReturn(uri, sUriMatcher.match(uri), selection);

		// Always update modified time on update
		Long now = Long.valueOf(System.currentTimeMillis());
		values.put(MODIFIED, now);

		int updated = db.update(TABLE, values, whereClause, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return updated;
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException{
        Log.d(TAG, ".openFile(" + uri.toString() +");");
		return openFileHelper(uri,VALUE_COMPLEX,mode);
	}
	
    /**
     * Updates this providers table
     * @param db the db to update in 
     * @param oldVersion the current db version
     * @param newVersion the new db version
     */
    public static void onUpgradeDatabase(SQLiteDatabase db, int oldVersion, 
    		int newVersion) {
        Log.d(TAG, "Upgrading " +TAG+ " tables from version " + oldVersion + " to "
                + newVersion);
        if (oldVersion <= 4)
        	return;
    }

    // Column defs declared in Observations. interface
    private static final String CREATE_TABLE  =
    		"CREATE TABLE " + TABLE + " ("
            + _ID + " INTEGER PRIMARY KEY,"
            + UUID + " TEXT,"
            + ENCOUNTER + " INTEGER NOT NULL,"
            + CONCEPT + " TEXT NOT NULL,"
            + SUBJECT + " TEXT NOT NULL,"
            + ID + " TEXT NOT NULL,"
            + VALUE_TEXT + " TEXT,"
            + VALUE_COMPLEX + " TEXT,"
            + UPLOAD_PROGRESS + " INTEGER,"
            + UPLOADED + " INTEGER,"
            + CREATED + " INTEGER,"
            + MODIFIED + " INTEGER"
            + ");";
    
    /**
     * Creates the table.
     * @param db the database to create the table in.
     */
    public static void onCreateDatabase(SQLiteDatabase db) {
        Log.d(TAG, "Creating " +TAG+ " tables ");
        db.execSQL(CREATE_TABLE);
        Log.d(TAG, "SUCCESS! creating " +TAG+ " tables");
    }

    /**
     * @see FileContentProvider#getFileColumn.
     * 
     * @returns {@link org.sana.android.Observations.Contract#VALUE_COMPLEX Observations.Contract.VALUE_COMPLEX} 
     */
	@Override
	protected String getFileColumn() {
		return VALUE_COMPLEX;
	}


	@Override
	protected File insertFileHelper(Uri uri, ContentValues values) throws 
		FileNotFoundException 
	{
		//TODO finish this
		return null;
	}
	
	boolean isComplexObservation(Uri uri){
		Cursor cursor;
		cursor = getContext().getContentResolver().query(uri, new String[]{ CONCEPT },
				null,null,null);
		String concept = null;
		if(cursor != null){
			try{
				if(cursor.moveToFirst()){
						
					concept = cursor.getString(0);
					// should never be NULL based on table constraints
				}
			} finally {
				cursor.close();
			}
		}
		// Get the Concept and see if it is complex
		//cursor= getContext().getContentResolver().query()
		return true;
	}
}
