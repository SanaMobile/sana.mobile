package org.sana.android.db;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import org.sana.R;
import org.sana.android.content.BasicContentProvider;
import org.sana.android.provider.Concepts;

public class ConceptProvider extends BasicContentProvider {

	static final String TAG = ConceptProvider.class.getSimpleName(); 
	private static final String TABLE = "concepts";
	public static final String DEFAULT_SORT_ORDER = Concepts.Contract.NAME 
			+ " ASC";
	private static final int ITEMS = 0;
	private static final int ITEM_ID = 1;
	
    
    private static final Map<String,String> sProjMap = new HashMap<String, String>();
    private static final UriMatcher mUriMatcher = 
    		new UriMatcher(UriMatcher.NO_MATCH);
    static{
    	mUriMatcher.addURI(Concepts.AUTHORITY, "concept/", ITEMS);
    	mUriMatcher.addURI(Concepts.AUTHORITY, "concept/#", ITEM_ID);
    }
    /*
    private class ConceptOpenHelper extends DatabaseOpenHelper{

		protected ConceptOpenHelper(Context arg0, String arg1, int arg2) {
			super(arg0, arg1, arg2, null);
		}
		@Override
		public void onCreate(SQLiteDatabase db){
			super.onCreate(db);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db, String[] columns) {
			super.onCreate(db, new String[]{ 
		            Concepts.Contract.NAME 			+ " TEXT NOT NULL",
		            Concepts.Contract.DISPLAY_NAME 	+ " TEXT NOT NULL",
		            Concepts.Contract.DESCRIPTION 	+ " TEXT NOT NULL",
		            Concepts.Contract.DATA_TYPE 	+ " TEXT NOT NULL",
		            Concepts.Contract.MEDIA_TYPE 	+ " TEXT",
		            Concepts.Contract.CONSTRAINT 	+ " TEXT"});
		}
		
		@Override
		public String onSort(Uri uri) {
			return DEFAULT_SORT_ORDER;
		}
		
		@Override
		public String getType(Uri uri) {
			switch(mUriMatcher.match(uri)){
			case(ITEMS):
				return Concepts.CONTENT_TYPE;
			case(ITEM_ID):
				return Concepts.CONTENT_ITEM_TYPE;
			default:
				throw new IllegalArgumentException("Invalid Uri for provider");
			}
		}
		
		@Override
		public String getTable(Uri uri) {
			return TABLE;
		}
		
		@Override
		public String getFileColumn(Uri uri) {
			// TODO Auto-generated method stub
			return null;
		}
    }
    */
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(Uri,ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d(TAG, "insert(" + uri.toString() +", N = " 
        	+ String.valueOf((values == null)?0:values.size()) + " values.)");
		
		if(values.containsKey(Concepts.Contract.MEDIA_TYPE) == false) {
            values.put(Concepts.Contract.MEDIA_TYPE, "text/plain");
        }

		return null;//super.insert(uri, values);
	}


	/* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
        Log.d(TAG, ".onCreate();");
        /*
        setHelper(new ConceptOpenHelper(getContext(), 
        		getContext().getString(R.string.db_name),
        		getContext().getResources().getInteger(R.integer.db_version)));
		*/
		return true;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(Uri,ContentValues,String,String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
        Log.d(TAG, ".update(" + uri.toString() +");");
		return 0;//super.update(uri, values, selection, selectionArgs);
	}
	
    private static final String CREATE_TABLE  =
    		"CREATE TABLE " + TABLE + " ("
    		+ Concepts.Contract._ID 			+ " INTEGER PRIMARY KEY,"
    		+ Concepts.Contract.UUID 			+ " TEXT,"
            + Concepts.Contract.NAME 			+ " INTEGER NOT NULL,"
            + Concepts.Contract.DISPLAY_NAME 	+ " TEXT NOT NULL,"
            + Concepts.Contract.DESCRIPTION 	+ " TEXT NOT NULL,"
            + Concepts.Contract.DATA_TYPE 	+ " TEXT NOT NULL,"
            + Concepts.Contract.MEDIA_TYPE 	+ " TEXT,"
            + Concepts.Contract.CONSTRAINT 	+ " TEXT,"
            + Concepts.Contract.CREATED 		+ " TEXT,"
            + Concepts.Contract.MODIFIED 		+ " TEXT"
            + ");";
    
    public static void onCreateDatabase(SQLiteDatabase db){
        Log.i(TAG, "onCreateDatabase() => Executing CREATE for table: " 
        		+ ConceptProvider.TABLE);
    	db.execSQL(CREATE_TABLE);
        Log.i(TAG, "onCreateDatabase() => Completed executing CREATE for table: " 
        		+ ConceptProvider.TABLE);
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
}
