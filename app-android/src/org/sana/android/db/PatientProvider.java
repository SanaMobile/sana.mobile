package org.sana.android.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import org.sana.android.content.FileContentProvider;
import org.sana.android.db.SanaDB.DatabaseHelper;
import org.sana.android.provider.Patients;
import android.content.ContentUris;
import android.content.ContentValues;
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
 * Used to store data for patient authentication, birthdates are stored as 
 * integers with the year then month then day appended together, i.e. 19880922 
 * corresponds to September 22, 1988
 * 
 * @author Sana Development Team
 */
public class PatientProvider extends FileContentProvider implements Patients.Contract{

	private static final String TAG = PatientProvider.class.getSimpleName();

	private static final String TABLE = "patients";

	private static final int ITEMS = 1;
	private static final int ITEM_ID = 2;

	private DatabaseHelper mOpenHelper;
	private static final UriMatcher sUriMatcher;
	private static HashMap<String,String> sPatientProjectionMap;

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
		qb.setTables(TABLE);

		switch(sUriMatcher.match(uri)) {
		case ITEMS:    
			break;
		case ITEM_ID:
			qb.appendWhere(_ID + "=" 
					+ uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		String orderBy;
		if(TextUtils.isEmpty(sortOrder)) {
			orderBy = Patients.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}
		
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, 
				null, sortOrder);
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
			count = db.update(TABLE, values, selection, 
					selectionArgs);
			break;
		case ITEM_ID:
			String patientId = uri.getPathSegments().get(1);
			count = db.update(TABLE, values, _ID 
					+ "=" + patientId + (!TextUtils.isEmpty(selection) 
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
		case ITEMS:
			count = db.delete(TABLE, selection, selectionArgs);
			break;
		case ITEM_ID:
			String patientId = uri.getPathSegments().get(1); 
			count = db.delete(TABLE, _ID + "=" 
					+ patientId + (!TextUtils.isEmpty(selection) ? " AND (" 
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
		Log.i(TAG,"starting insert method");
		if (sUriMatcher.match(uri) != ITEMS) {
			Log.i(TAG, "Throwing IllegalArgumentException");
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if(initialValues != null) {
			Log.i(TAG,"do we get to this point?");
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		Long now = Long.valueOf(System.currentTimeMillis());

		if(values.containsKey(GIVEN_NAME) == false) {
			values.put(GIVEN_NAME, "");
		}
		
		if(values.containsKey(FAMILY_NAME) == false) {
			values.put(FAMILY_NAME, "");
		}

		if(values.containsKey(DOB) == false) {
			values.put(DOB, "");
		}

		if(values.containsKey(PATIENT_ID) == false) {
			values.put(PATIENT_ID, now);
		}
		
		if(values.containsKey(GENDER) == false) {
			values.put(GENDER, "");
		}
 
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
 
		try {
			long rowId = db.insertOrThrow(TABLE, 
					GIVEN_NAME, values);
			if(rowId > 0) {
				Uri patientUri = ContentUris.withAppendedId(
						Patients.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(patientUri, 
						null);
				return patientUri;
			}
		} catch (Exception e) {
			Log.i(TAG,e.getClass().toString());
			Log.i(TAG, e.getMessage());
		}
 
		throw new SQLException("Failed to insert row into " + uri);
	}

    /** {@inheritDoc} */
	@Override
	public String getType(Uri uri) {
		Log.i(TAG, "getType(uri="+uri.toString()+")");
		switch(sUriMatcher.match(uri)) {
		case ITEMS:
			return Patients.CONTENT_TYPE;
		case ITEM_ID:
			return Patients.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}
	
	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException{
		return openFileHelper(uri,mode);
	}

    /**
     * Creates the table.
     * @param db the database to create the table in.
     */
	public static void onCreateDatabase(SQLiteDatabase db) {
		Log.i(TAG, "Creating Patient Data Table");
		db.execSQL("CREATE TABLE " + TABLE + " ("
				+ _ID + " INTEGER PRIMARY KEY,"
				+ PATIENT_ID + " TEXT,"
				+ GIVEN_NAME + " TEXT,"
				+ FAMILY_NAME + " TEXT,"
				+ GENDER + " TEXT,"
				+ IMAGE + " TEXT,"
				+ STATE + " INTEGER DEFAULT '-1',"
				+ DOB + " DATE"
				+ ");");
		Log.i(TAG, "Finished Creating Patient Data TAble");
		
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
        	String sql = "ALTER TABLE " + TABLE +
    			" ADD COLUMN " + IMAGE + " TEXT";
        	db.execSQL(sql);
        }
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(SanaDB.PATIENT_AUTHORITY, "patients", ITEMS);
		sUriMatcher.addURI(SanaDB.PATIENT_AUTHORITY, "patients/#", ITEM_ID);

		sPatientProjectionMap = new HashMap<String, String>();
		sPatientProjectionMap.put(_ID, _ID);
		sPatientProjectionMap.put(PATIENT_ID, PATIENT_ID);
		sPatientProjectionMap.put(GIVEN_NAME, GIVEN_NAME);
		sPatientProjectionMap.put(FAMILY_NAME, FAMILY_NAME);
		sPatientProjectionMap.put(DOB, DOB);
		sPatientProjectionMap.put(GENDER, GENDER);
		sPatientProjectionMap.put(IMAGE, IMAGE);
	}

	@Override
	protected File insertFileHelper(Uri uri, ContentValues values)
			throws FileNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getFileColumn() {
		return IMAGE;
	}
}
