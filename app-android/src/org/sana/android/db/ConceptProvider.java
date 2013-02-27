package org.sana.android.db;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.sana.Constants;
import org.sana.android.db.SanaDB.DatabaseHelper;
import org.sana.android.provider.Concepts;

public class ConceptProvider extends ContentProvider implements Concepts.Contract{

	static final String TAG = ConceptProvider.class.getSimpleName(); 
	private static final String TABLE = "concept";
	public static final String DEFAULT_SORT_ORDER = NAME + " ASC";
	
    private static final int ITEMS = 0;
    private static final int ITEM_ID = 1;
    static final String DB = "sana.db";
    private DatabaseHelper mOpenHelper;
    private static final UriMatcher sUriMatcher;
    private static final Map<String,String> sProjMap = new HashMap<String, String>();
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Concepts.AUTHORITY, "concepts", ITEMS);
        sUriMatcher.addURI(Concepts.AUTHORITY, "concepts/#", ITEM_ID);
    
    }
    
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(Uri,String,String[])
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
	 * @see android.content.ContentProvider#getType()
	 */ 
	@Override
	public String getType(Uri uri) {
        Log.d(TAG, ".getType(" + uri.toString() +");");
        switch (sUriMatcher.match(uri)) {
        case ITEMS:
        	return Concepts.CONTENT_TYPE;
        case ITEM_ID:
        	return Concepts.CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
	}


	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(Uri,ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {Log.d(TAG, "insert(" + uri.toString() +", N = " 
        	+ String.valueOf((values == null)?0:values.size()) + " values.)");
        
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		String now = new SimpleDateFormat(Constants.DATE_FORMAT).format(
				Calendar.getInstance());
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
	 * @see android.content.ContentProvider#query(Uri,String[],String,String[],String) 
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
	 * @see android.content.ContentProvider#update(Uri,ContentValues,String,String[])
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
	
    private static final String CREATE_TABLE  =
    		"CREATE TABLE " + TABLE + " ("
            + _ID + " INTEGER PRIMARY KEY,"
            + UUID + " TEXT,"
            + NAME + " INTEGER NOT NULL,"
            + DISPLAY_NAME + " TEXT NOT NULL,"
            + DESCRIPTION + " TEXT NOT NULL,"
            + DATA_TYPE + " TEXT NOT NULL,"
            + MEDIA_TYPE + " TEXT,"
            + CONSTRAINT + " TEXT,"
            + CREATED + " INTEGER,"
            + MODIFIED + " INTEGER"
            + ");";

}
