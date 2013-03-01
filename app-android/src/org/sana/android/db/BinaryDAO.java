package org.sana.android.db;

import org.sana.android.db.SanaDB.BinarySQLFormat;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Data access object for mapping binary objects to encounter elements, i.e.
 * observations.
 * 
 * @author Sana Development Team
 *
 */
public class BinaryDAO {
	
	private static final String TAG = BinaryDAO.class.getSimpleName();
	public static final String DEFAULT_MIME = "application/octet-stream";
	
	/**
	 * Removes the entry for a binary object and .
	 * @param cr
	 * @param uri
	 * @return
	 */
	public static int delete(ContentResolver cr, Uri uri)
	{
		int result = 0;
		result = cr.delete(uri, null, null);
		Log.d(TAG, "Result: "+ result + ", deleted: " + uri);
		return result;
	}
	
	/**
	 * Removes the file entry only for a binary object in this table.
	 * 
	 * @param cr
	 * @param answer
	 * @return
	 */
	public static int deleteFile(ContentResolver cr, Uri uri)
	{
		int result = 0;
		Uri fUri = queryFile(cr,uri);
		update(cr, uri, null);
		result = cr.delete(fUri,null,null);
		Log.d(TAG, "Result: "+ result + ", deleted: " + fUri);
		return result;
	}
	
	/**
	 * Inserts a new record for a binary object.
	 * 
	 * @param cr A content resolver
	 * @param encounterId The encounter identifier
	 * @param elementId The encounter element identifier
	 * @param fileUri For locating the binary object
	 * @return A Uri for locating the new entry or null if unsuccessful.
	 */
	public static Uri insert(ContentResolver cr, String encounterId, 
			String elementId, Uri fileUri, String mime)
	{
		Uri result = null;
		ContentValues values = new ContentValues();
		values.put(BinarySQLFormat.ENCOUNTER_ID, encounterId);
		values.put(BinarySQLFormat.ELEMENT_ID, elementId);
		values.put(BinarySQLFormat.CONTENT, fileUri.toString());
		values.put(BinarySQLFormat.MIME, mime);
		result = cr.insert(BinarySQLFormat.CONTENT_URI, values);

		Log.d(TAG, "Result: " + result);
		return result;
	}
	
	/**
	 * Inserts a new record for a binary object with no file uri or mime type
	 * set.
	 * 
	 * @param cr A content resolver
	 * @param encounterId The encounter identifier
	 * @param elementId The encounter element identifier
	 * @return A Uri for locating the new entry or null if unsuccessful.
	 */
	public static Uri insert(ContentResolver cr, String encounterId, 
			String elementId)
	{
		Uri result = null;
		ContentValues values = new ContentValues();
		values.put(BinarySQLFormat.ENCOUNTER_ID, encounterId);
		values.put(BinarySQLFormat.ELEMENT_ID, elementId);
		result = cr.insert(BinarySQLFormat.CONTENT_URI, values);

		Log.d(TAG, "Result: " + result);
		return result;
	}
	
	/**
	 * Returns the Uri for the row.
	 * 
	 * @param cr A content resolver
	 * @param answerUri The row to query
	 * @return The Uri or null.
	 */
	public static Uri query(ContentResolver cr, String encounter, 
			String element)
	{
		Uri result = null;
		String[] projection = BinaryProvider.PROJ_ID;
		String selection = BinaryProvider.OBS_WHERE;
		String[] selArgs = new String[]{ encounter, element };
		Cursor c = null;
		try{
			c = cr.query(BinarySQLFormat.CONTENT_URI, projection, selection,
					selArgs, null);
			if(c.moveToFirst()){
				result = Uri.parse(c.getString(
						c.getColumnIndex(BinarySQLFormat.CONTENT)));
			}	
		} catch(Exception e){
			Log.e(TAG, e.toString());
		} finally {
			if(c != null)
				c.close();
		}
		Log.d(TAG, "Result: "+ result + ", For(encounter,element): (" 
				+ encounter + "," + element +")");
		return result;
	}
	
	/**
	 * Returns the Uri for the binary file object.
	 * 
	 * @param cr A content resolver
	 * @param uri The row to query
	 * @return The Uri or null.
	 */
	public static Uri queryFile(ContentResolver cr, Uri uri){
		Uri result = null;
		String[] projection = BinaryProvider.PROJ_ITEM_CONTENT;
		Cursor c = null;
		try{
			c = cr.query(uri, projection,null,null,null);
			if(c.moveToFirst()){
				result = Uri.parse(c.getString(
						c.getColumnIndex(BinarySQLFormat.CONTENT)));
			}	
		} catch(Exception e){
			Log.e(TAG, e.toString());
		} finally {
			if(c != null)
				c.close();
		}
		Log.d(TAG, "Result: "+ result + ", From: " + uri);
		return result;
	}
	
	/**
	 * Updates the file Uri string for the row stored as an answer.
	 * 
	 * @param cr A content resolver
	 * @param answerUri The row to update
	 * @param fileUri The new Uri for the binary object.
	 * @return 1 if successful, otherwise 0.
	 */
	public static Uri updateOrCreate(ContentResolver cr, String encounterId, 
			String elementId, Uri fileUri, String mime)
	{
		int result = 0;
		Uri uri = query(cr, encounterId, elementId);
		mime = (TextUtils.isEmpty(mime))? BinaryDAO.DEFAULT_MIME: mime;
		if(uri == null){
			uri = BinaryDAO.insert(cr, encounterId, elementId, fileUri, mime);
		} else {
			String newFile = (fileUri != null)? fileUri.toString(): "";
			ContentValues values = new ContentValues();
			values.put(BinarySQLFormat.CONTENT, newFile);
			values.put(BinarySQLFormat.MIME,mime);
			result = cr.update(uri, values, null, null);
		}
		Log.d(TAG, "Result: "+ result + ", Updated: " + uri 
				+ ", with: " + fileUri);
		return uri;
	}
	
	/**
	 * Updates the file Uri string for the row stored as an answer.
	 * 
	 * @param cr A content resolver
	 * @param uri The row to update
	 * @param fileUri The new Uri for the binary object.
	 * @return 1 if successful, otherwise 0.
	 */
	public static int update(ContentResolver cr, Uri uri, Uri fileUri)
	{
		int result = 0;
		String newFile = (fileUri != null)? fileUri.toString(): "";
		ContentValues values = new ContentValues();
		values.put(BinarySQLFormat.CONTENT, newFile);
		result = cr.update(uri, values, null, null);
		Log.d(TAG, "Result: "+ result + ", Updated: " + uri 
				+ ", with: " + fileUri);
		return result;
	}
	
	/**
	 * Returns a unique identifier for the entry mapped to the Uri
	 * 
	 * @param uri The entry to get a UUID for.
	 * @return The uuid or null
	 */
	public static String getUUID(Uri uri){
		if(uri != null)
			return uri.getPathSegments().get(1);
		else
			return null;
	}
	
	public static Uri obsUri(Uri encounter, String element){
		Uri.Builder result = encounter.buildUpon();
		result.appendPath(element);
		return result.build();
	}
}
