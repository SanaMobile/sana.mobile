/**
 * 
 */
package org.sana.android.db;

import java.util.HashMap;
import java.util.Map;

import org.sana.core.Observation;
import org.sana.R;
import org.sana.android.content.BasicContentProvider;
import org.sana.android.provider.Observations;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * ContentProvider for the observation table.
 * 
 * @author Sana Development Team
 */
public class ObservationProvider extends ContentProvider{
	/*
    private class OpenHelper extends DatabaseOpenHelper{

		protected OpenHelper(Context context, String name, int version,
				UriMatcher matcher) {
			super(context, name, version, null);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db){
			onCreate(db, null);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db, String[] columns) {
			super.onCreate(db, new String[]{ 
					Observations.Contract.ENCOUNTER + " INTEGER NOT NULL",
					Observations.Contract.CONCEPT + " TEXT NOT NULL",
					Observations.Contract.SUBJECT + " TEXT NOT NULL",
					Observations.Contract.ID + " TEXT NOT NULL",
					Observations.Contract.PARENT + " TEXT",
					Observations.Contract.VALUE_TEXT + " TEXT,",
					Observations.Contract.VALUE_COMPLEX + " TEXT,",
					Observations.Contract.UPLOAD_PROGRESS + " INTEGER",
					Observations.Contract.UPLOADED + " INTEGER" });
		}

		@Override
		public String onSort(Uri uri) {
			switch(match(uri)){
			
			}
			return null;
		}

		@Override
		public String getTable(Uri uri) {
			switch(match(uri)){
			
			}
			return null;
		}

		@Override
		public String getFileColumn(Uri uri) {
			switch(match(uri)){
			
			}
			return null;
		}

		@Override
		public String getType(Uri uri) {
			switch(match(uri)){
			
			}
			return null;
		}
    }
	*/
	public static final String TAG = ObservationProvider.class.getSimpleName();
	
	public static final String DEFAULT_SORT_ORDER = "modified DESC";
	private static final String TABLE = "observations";
	
    private static final int ITEMS = 0;
    private static final int ITEM_ID = 1;
    
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final Map<String,String> sProjMap = new HashMap<String, String>();
    static {
        sProjMap.put(Observations.Contract._ID, Observations.Contract._ID);
        sProjMap.put(Observations.Contract.UUID, Observations.Contract.UUID);
        sProjMap.put(Observations.Contract.ENCOUNTER, Observations.Contract.ENCOUNTER);
        sProjMap.put(Observations.Contract.CONCEPT, Observations.Contract.CONCEPT);
        sProjMap.put(Observations.Contract.SUBJECT, Observations.Contract.SUBJECT);
        sProjMap.put(Observations.Contract.ID, Observations.Contract.ID);
        sProjMap.put(Observations.Contract.PARENT, Observations.Contract.PARENT);
        sProjMap.put(Observations.Contract.VALUE_TEXT, Observations.Contract.VALUE_TEXT);
        sProjMap.put(Observations.Contract.VALUE_COMPLEX,Observations.Contract.VALUE_COMPLEX);
        sProjMap.put(Observations.Contract.UPLOADED, Observations.Contract.UPLOADED);
        sProjMap.put(Observations.Contract.UPLOAD_PROGRESS, Observations.Contract.UPLOAD_PROGRESS);
        sProjMap.put(Observations.Contract.CREATED, Observations.Contract.CREATED);
        sProjMap.put(Observations.Contract.MODIFIED, Observations.Contract.MODIFIED);
    }

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert(" + uri.toString() +", N = " 
        	+ String.valueOf((values == null)?0:values.size()) + " values.)");

		if(values.containsKey(Observations.Contract.UPLOADED) == false) {
            values.put(Observations.Contract.UPLOADED, false);
        }

		if(values.containsKey(Observations.Contract.UPLOAD_PROGRESS) == false) {
            values.put(Observations.Contract.UPLOAD_PROGRESS, -1);
        }
		Uri result = null;//super.insert(uri, values);
		Log.d(TAG, "insert(): Successfully inserted => " + result);
		return result;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
	        Log.d(TAG, ".onCreate();");
	        /*
	        setHelper(new OpenHelper(getContext(), 
	        		getContext().getString(R.string.db_name),
	        		getContext().getResources().getInteger(R.integer.db_version),
	        		mUriMatcher));
	        */
			return true;
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
            + Observations.Contract._ID + " INTEGER PRIMARY KEY,"
            + Observations.Contract.UUID + " TEXT,"
            + Observations.Contract.ENCOUNTER + " INTEGER NOT NULL,"
            + Observations.Contract.CONCEPT + " TEXT NOT NULL,"
            + Observations.Contract.SUBJECT + " TEXT NOT NULL,"
            + Observations.Contract.ID + " TEXT NOT NULL,"
            + Observations.Contract.PARENT + " TEXT,"
            + Observations.Contract.VALUE_TEXT + " TEXT,"
            + Observations.Contract.VALUE_COMPLEX + " TEXT,"
            + Observations.Contract.UPLOAD_PROGRESS + " INTEGER,"
            + Observations.Contract.UPLOADED + " INTEGER,"
            + Observations.Contract.CREATED + " INTEGER,"
            + Observations.Contract.MODIFIED + " INTEGER"
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


	
	boolean isComplexObservation(Uri uri){
		Cursor cursor;
		cursor = getContext().getContentResolver().query(uri, new String[]{ Observations.Contract.CONCEPT },
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

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
