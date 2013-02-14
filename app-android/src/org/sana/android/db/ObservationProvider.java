/**
 * 
 */
package org.sana.android.db;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.sana.android.db.SanaDB.DatabaseHelper;

import android.content.ContentProvider;
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
 * Observation content provider.
 * 
 * @author Sana Development Team
 */
public class ObservationProvider extends ContentProvider {
	public static final String TAG = ObservationProvider.class.getSimpleName();
	public static final String PACKAGE = "org.sana";
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
        
        sProjMap.put(Observations._ID, Observations._ID);
        sProjMap.put(Observations.UUID, Observations.UUID);
        sProjMap.put(Observations.ENCOUNTER, Observations.ENCOUNTER);
        sProjMap.put(Observations.CONCEPT, Observations.CONCEPT);
        sProjMap.put(Observations.SUBJECT, Observations.SUBJECT);
        sProjMap.put(Observations.ID, Observations.ID);
        sProjMap.put(Observations.PARENT, Observations.PARENT);
        sProjMap.put(Observations.VALUE_TEXT, Observations.VALUE_TEXT);
        sProjMap.put(Observations.VALUE_COMPLEX, Observations.VALUE_COMPLEX);
        sProjMap.put(Observations.UPLOADED, Observations.UPLOADED);
        sProjMap.put(Observations.UPLOAD_PROGRESS, Observations.UPLOAD_PROGRESS);
        sProjMap.put(Observations.CREATED, Observations.CREATED);
        sProjMap.put(Observations.MODIFIED, Observations.MODIFIED);
    }
	
    /**
     * Meta data for Observation records
     *
     * @author Sana Development Team
     */
    public static final class Observations implements ObservationContract{
    	public static final String AUTHORITY = PACKAGE + ".provider.Observation";

    	public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    	private Observations(){}

    	/*** The content type for one or more records */
    	public static final String CONTENT_TYPE =
    							"vnd.android.cursor.dir/org.sana.observation";

    	/** The content type for a single record*/
    	public static final String CONTENT_ITEM_TYPE =
    							"vnd.android.cursor.item/org.sana.observation";

    	/** The content style URI */
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(
    			AUTHORITY_URI, "observations");

    }
	
    /**
     * Contract for Observation objects. Naming convention is for columns 
     * that begin with _ to be considered hidden except locally. Hidden
     * columns must not persist upstream.
     *
     * @author Sana Development Team
     */
	public static interface ObservationContract extends BaseColumns{

    	/** Unique id attribute within the encounter */
    	public static final String UUID = "uuid";
		
    	/** Provides the context for the collected data */
    	public static final String CONCEPT = "concept";

    	/** The encounter where this was collected */
    	public static final String ENCOUNTER = "encounter_id";
    	
    	/** The subject who data was collected about; i.e. the patient */
    	public static final String SUBJECT = "subject_id";

    	/** Unique id attribute within the encounter */
    	public static final String ID = "id";

        /** The date the record was created. */
        public static final String CREATED = "created";

        /** The date the record was last modified. */
        public static final String MODIFIED = "modified";
    	
    	/** Mapping to parent node. Defaults to -1 if at top level of encounter */
    	public static final String PARENT = "parent_id";

    	/** Text representation of the observation data */
    	public static final String VALUE_TEXT = "value_text";
    	
    	/** Blob observation data. Synonym for the _data column. Should map to 
    	 *  value_complex when POSTED
    	 */
    	public static final String VALUE_COMPLEX = "_data";

    	/** The number of packetized bytes successfully uploaded to the MDS. */
        public static final String UPLOAD_PROGRESS = "_upload_progress";

        /** Indicates whether a Blob is completely uploaded */
        public static final String UPLOADED = "_uploaded";
	}
	
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, ".delete(" + uri.toString() +");");
        String whereClause = getWhereWithIdOrReturn(uri, selection);
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
        Log.d(TAG, ".insert(" + uri.toString() +");");
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        
		//allow created and modified to be set manually here.
		Long now = Long.valueOf(System.currentTimeMillis());
		if(values.containsKey(Observations.CREATED) == false) {
            values.put(Observations.CREATED, now);
        }
        if(values.containsKey(Observations.MODIFIED) == false) {
            values.put(Observations.MODIFIED, now);
        }

		db.insert(TABLE, null, values);
		getContext().getContentResolver().notifyChange(uri, null);
		return null;
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
		String whereClause = getWhereWithIdOrReturn(uri, selection);
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
		String whereClause = getWhereWithIdOrReturn(uri, selection);

		// Always update modified time on update
		Long now = Long.valueOf(System.currentTimeMillis());
		values.put(Observations.MODIFIED, now);

		int updated = db.update(TABLE, values, whereClause, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return updated;
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException{
        Log.d(TAG, ".openFile(" + uri.toString() +");");
		return openFileHelper(uri,mode);
	}
	
	/**
	 * Returns the selection statement as
	 *  ( _ID = "uri.getPathSegments().get(1)" ) AND ( selection )
	 * or as the original based on whether the uri match was a dir or item.
	 * Relies on matcher values for *.dir being even integers.
	 * @param uri
	 * @param selection
	 * @return
	 */
	protected String getWhereWithIdOrReturn(Uri uri, String selection){
		String select = selection;
		int match = sUriMatcher.match(uri);
		if((match & 1) != 0)
			select = DatabaseUtilsCompat.concatenateWhere(selection,
					BaseColumns._ID + " = " + uri.getPathSegments().get(1));
		return select;
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

    private static final String CREATE_TABLE  =
    		"CREATE TABLE " + TABLE + " ("
            + Observations._ID + " INTEGER PRIMARY KEY,"
            + Observations.UUID + " TEXT,"
            + Observations.ENCOUNTER + " INTEGER NOT NULL,"
            + Observations.CONCEPT + " TEXT NOT NULL,"
            + Observations.SUBJECT + " TEXT NOT NULL,"
            + Observations.ID + " TEXT NOT NULL,"
            + Observations.VALUE_TEXT + " TEXT,"
            + Observations.VALUE_COMPLEX + " TEXT,"
            + Observations.UPLOAD_PROGRESS + " INTEGER,"
            + Observations.UPLOADED + " INTEGER,"
            + Observations.CREATED + " INTEGER,"
            + Observations.MODIFIED + " INTEGER"
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
}
