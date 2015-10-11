package org.sana.android.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.sana.android.content.ExifUtil;
import org.sana.android.db.SanaDB.ImageSQLFormat;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

/**
 * Content provider for image content.
 *
 * @author Sana Development Team
 *
 */
public class ImageProvider extends ContentProvider {

    private static final String TAG = "ImageProvider";

    public static final String VIEW_PARAMETER = "view";
    public static final String THUMBNAIL_VIEW = "thumb";

    private static final String IMAGE_TABLE_NAME = "images";
    public static final String IMAGE_BUCKET_NAME = "/sdcard/dcim/sana/";

    private static final int IMAGES = 1;
    private static final int IMAGE_ID = 2;

    private DatabaseHelper mOpenHelper;
    private static final UriMatcher sUriMatcher;
    private static HashMap<String,String> sImageProjectionMap;

    /** {@inheritDoc} */
    @Override
    public boolean onCreate() {
        Log.i(TAG, "onCreate()");
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

	/**
	 * Return a thumbnail Uri for a given ImageProvider item Uri. The returned
	 * Uri will still be compatible with all ContentProvider methods and refers
	 * to the same item that imageUri refers to.
	 */
    public static Uri getThumbUri(Uri imageUri) {
    	Uri.Builder thumbBuilder = imageUri.buildUpon();
    	thumbBuilder.appendQueryParameter(VIEW_PARAMETER, THUMBNAIL_VIEW);
        return thumbBuilder.build();
    }

    public static void correctOrientation(Context context, Uri uri){
        Log.i(TAG, "correctOrientation()");
        List<String> segments = uri.getPathSegments();


        String imagePath = buildFilenameFromUri(uri);

        // Invalid URI
        if (TextUtils.isEmpty(imagePath)){
            Log.w(TAG, "Invalid image uri");
            return;
        }

        File src = new File(imagePath);
        if(src.exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            try {
                OutputStream os =context.getContentResolver().openOutputStream
                        (uri);
                bitmap = ExifUtil.rotateBitmap(src, bitmap);
                boolean saved = bitmap.compress(Bitmap.CompressFormat.JPEG,
                        100, os);
                Log.d(TAG, "Compressed rotated bitmap: saved=" + saved);
                os.close();
                bitmap.recycle();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Log.e(TAG, "File not found: " + src);
        }
    }

    private static String basePath() {
    	return "/data/data/org.sana.android/files/";
    }

    private static String buildImageFilenameFromId(String imageId) {
    	return basePath() + imageId +".jpg";
    }

    private static String buildThumbnailFilenameFromId(String imageId) {
    	return basePath() + "thumb_" + imageId + ".jpg";
    }

    private static String buildFilenameFromUri(Uri uri) {
    	List<String> segments = uri.getPathSegments();

    	// Invalid URI
    	if (segments.size() != 2)
    		return "";

    	String imageId = segments.get(1);
    	String viewName = uri.getQueryParameter(VIEW_PARAMETER);

    	if (THUMBNAIL_VIEW.equals(viewName)) {
    		return ImageProvider.buildThumbnailFilenameFromId(imageId);
    	} else { // default to image view
    		return buildImageFilenameFromId(imageId);
    	}
    }

    private boolean deleteFile(String imageId) {
    	String filename = buildImageFilenameFromId(imageId);
    	File f = new File(filename);
    	boolean result = f.delete();
    	Log.i(TAG, "Deleting file for id " + imageId + " : " + filename + " "
    			+ (result ? "succeeded" : "failed"));
    	filename = buildThumbnailFilenameFromId(imageId);
    	f = new File(filename);
    	boolean thumbResult = f.delete();
    	Log.i(TAG, "Deleting thumbnail for id " + imageId + " : " + filename
    			+ " " + (thumbResult ? "succeeded" : "failed"));
    	return result && thumbResult;
    }

    private boolean deleteFile(Uri uri) {
    	List<String> segments = uri.getPathSegments();

    	// Invalid URI
    	if (segments.size() != 1)
    		return true;

    	String imageId = segments.get(1);
    	return deleteFile(imageId);
    }

    /** {@inheritDoc} */
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws
    	FileNotFoundException
    {
        Log.i(TAG, "openFile()");
    	String filename = buildFilenameFromUri(uri);
        Log.d(TAG, "...filename='" + filename + "', mode: " + mode);
        File f = new File(filename);

        //Hack to get image to write to database

        int m = ParcelFileDescriptor.MODE_READ_ONLY;
        if ("w".equals(mode)) {
        	m = ParcelFileDescriptor.MODE_WRITE_ONLY |
        							ParcelFileDescriptor.MODE_CREATE;
        } else if("rw".equals(mode) || "rwt".equals(mode)) {
        	m = ParcelFileDescriptor.MODE_READ_WRITE;
        }
        return ParcelFileDescriptor.open(f,m);
    }

    /** {@inheritDoc} */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
                Log.i(TAG, "query() uri="+uri.toString() + " projection="
        		+ ((projection == null)?"":TextUtils.join(",",projection))
        		+ ", selection="+selection
        		+ ", selectionArgs=" + ((selectionArgs == null)?"":TextUtils.join(",",selectionArgs)));

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(IMAGE_TABLE_NAME);

        switch(sUriMatcher.match(uri)) {
        case IMAGES:
            break;
        case IMAGE_ID:
            qb.appendWhere(ImageSQLFormat._ID + "="
            		+ uri.getPathSegments().get(1));
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        String orderBy;
        if(TextUtils.isEmpty(sortOrder)) {
            orderBy = ImageSQLFormat.DEFAULT_SORT_ORDER;
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
        case IMAGES:
            count = db.update(IMAGE_TABLE_NAME, values, selection,
            		selectionArgs);
            break;

        case IMAGE_ID:
            String procedureId = uri.getPathSegments().get(1);
            count = db.update(IMAGE_TABLE_NAME, values, ImageSQLFormat._ID + "="
            		+ procedureId + (!TextUtils.isEmpty(selection) ? " AND ("
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
    	Log.i(TAG, "delete(): uri="+ uri+", selection="+selection);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case IMAGES:
        	LinkedList<String> idList = new LinkedList<String>();
        	Cursor c = query(ImageSQLFormat.CONTENT_URI,
        			new String[] { ImageSQLFormat._ID }, selection,
        			selectionArgs, null);
        	if(c.moveToFirst()) {
        		while(!c.isAfterLast()) {
        			String id = c.getString(c.getColumnIndex(
        					ImageSQLFormat._ID));
        			idList.add(id);
        			c.moveToNext();
        		}
        	}
        	c.deactivate();

            count = db.delete(IMAGE_TABLE_NAME, selection, selectionArgs);

            for(String id : idList) {
            	deleteFile(id);
            }
            break;
        case IMAGE_ID:
            String imageId = uri.getPathSegments().get(1);
            count = db.delete(IMAGE_TABLE_NAME, ImageSQLFormat._ID + "="
            		+ imageId + (!TextUtils.isEmpty(selection) ? " AND ("
            				+ selection + ")" : ""), selectionArgs);
            deleteFile(uri);
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
        if (sUriMatcher.match(uri) != IMAGES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if(initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis());

        if(values.containsKey(ImageSQLFormat.CREATED_DATE) == false) {
            values.put(ImageSQLFormat.CREATED_DATE, now);
        }

        if(values.containsKey(ImageSQLFormat.MODIFIED_DATE) == false) {
            values.put(ImageSQLFormat.MODIFIED_DATE, now);
        }

        if(values.containsKey(ImageSQLFormat.ENCOUNTER_ID) == false) {
            values.put(ImageSQLFormat.ENCOUNTER_ID, "");
        }

        if(values.containsKey(ImageSQLFormat.ELEMENT_ID) == false) {
            values.put(ImageSQLFormat.ELEMENT_ID, "");
        }

        if(values.containsKey(ImageSQLFormat.FILE_URI) == false) {
            values.put(ImageSQLFormat.FILE_URI, "");
        }

        if(values.containsKey(ImageSQLFormat.FILE_VALID) == false) {
            values.put(ImageSQLFormat.FILE_VALID, false);
        }

        if(values.containsKey(ImageSQLFormat.FILE_SIZE) == false) {
            values.put(ImageSQLFormat.FILE_SIZE, 0);
        }

        if(values.containsKey(ImageSQLFormat.UPLOAD_PROGRESS) == false) {
            values.put(ImageSQLFormat.UPLOAD_PROGRESS, 0);
        }

        if(values.containsKey(ImageSQLFormat.UPLOADED) == false) {
            values.put(ImageSQLFormat.UPLOADED, false);
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(IMAGE_TABLE_NAME,
        		ImageSQLFormat.ENCOUNTER_ID, values);
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
            Uri noteUri = ContentUris.withAppendedId(
            		ImageSQLFormat.CONTENT_URI, rowId);
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
        case IMAGES:
            return ImageSQLFormat.CONTENT_TYPE;
        case IMAGE_ID:
            return ImageSQLFormat.CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    /**
     * Creates the table.
     * @param db the database to create the table in.
     */
    public static void onCreateDatabase(SQLiteDatabase db) {
        Log.i(TAG, "Creating Image Table");
        db.execSQL("CREATE TABLE " + IMAGE_TABLE_NAME + " ("
                + ImageSQLFormat._ID + " INTEGER PRIMARY KEY,"
                + ImageSQLFormat.ENCOUNTER_ID + " TEXT,"
                + ImageSQLFormat.ELEMENT_ID + " TEXT,"
                + ImageSQLFormat.FILE_URI + " TEXT,"
                + ImageSQLFormat.FILE_VALID + " INTEGER,"
                + ImageSQLFormat.FILE_SIZE + " INTEGER,"
                + ImageSQLFormat.UPLOAD_PROGRESS + " INTEGER,"
                + ImageSQLFormat.UPLOADED + " INTEGER,"
                + ImageSQLFormat.CREATED_DATE + " INTEGER,"
                + ImageSQLFormat.MODIFIED_DATE + " INTEGER"
                + ");");
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
        }
    }


    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(SanaDB.IMAGE_AUTHORITY, "images", IMAGES);
        sUriMatcher.addURI(SanaDB.IMAGE_AUTHORITY, "images/#", IMAGE_ID);

        sImageProjectionMap = new HashMap<String, String>();
        sImageProjectionMap.put(ImageSQLFormat._ID, ImageSQLFormat._ID);
        sImageProjectionMap.put(ImageSQLFormat.ENCOUNTER_ID, ImageSQLFormat.ENCOUNTER_ID);
        sImageProjectionMap.put(ImageSQLFormat.ELEMENT_ID, ImageSQLFormat.ELEMENT_ID);
        sImageProjectionMap.put(ImageSQLFormat.FILE_URI, ImageSQLFormat.FILE_URI);
        sImageProjectionMap.put(ImageSQLFormat.FILE_VALID, ImageSQLFormat.FILE_VALID);
        sImageProjectionMap.put(ImageSQLFormat.FILE_SIZE, ImageSQLFormat.FILE_SIZE);
        sImageProjectionMap.put(ImageSQLFormat.UPLOAD_PROGRESS, ImageSQLFormat.UPLOAD_PROGRESS);
        sImageProjectionMap.put(ImageSQLFormat.UPLOADED, ImageSQLFormat.UPLOADED);
        sImageProjectionMap.put(ImageSQLFormat.CREATED_DATE, ImageSQLFormat.CREATED_DATE);
        sImageProjectionMap.put(ImageSQLFormat.MODIFIED_DATE, ImageSQLFormat.MODIFIED_DATE);
    }

}
