package org.sana.android.db;

import java.util.ArrayList;
import java.util.List;

import org.sana.android.provider.BaseContract;

import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.database.DatabaseUtilsCompat;

/**
 * Collection of database helper methods.
 * 
 * @author Sana Development
 *
 */
public final class DBUtils {

	private DBUtils(){}
	/**
	 * Returns the selection statement as:<br/>
	 *  ( uuid = "uri.getLastPathSegment()" ) AND ( selection )
	 * 
	 * @param uri
	 * @param whereClause
	 * @return
	 */
	public static String getWhereClauseWithUUID(Uri uri, String whereClause){
		String select = whereClause;
		select = DatabaseUtilsCompat.concatenateWhere(whereClause,
					BaseContract.UUID + " = " + uri.getLastPathSegment());
		return select;
	}
	
	/**
	 * Returns the selection statement as
	 *  ( _ID = "uri.getPathSegments().get(1)" ) AND ( selection )
	 * or as the original based on whether the uri match was a dir or item.
	 * Relies on matcher values for *.dir being even integers.
	 * @param uri
	 * @param whereClause
	 * @return
	 */
	public static String getWhereClauseWithID(Uri uri, String whereClause){
		return DatabaseUtilsCompat.concatenateWhere(whereClause,
					BaseColumns._ID + " = " + uri.getLastPathSegment());
	}
	
	/**
	 * Returns the selection statement as
	 *  ( _ID = "uri.getPathSegments().get(1)" ) AND ( selection )
	 * or as the original based on whether the uri match was a dir or item.
	 * Relies on matcher values for *.dir being even integers.
	 * @param uri
	 * @param whereClause
	 * @return
	 */
	public static String getWhereClause(Uri uri, int match, String whereClause){
		String select = whereClause;
		if((match & 1) != 0)
			select = DatabaseUtilsCompat.concatenateWhere(whereClause,
					BaseColumns._ID + " = " + uri.getPathSegments().get(1));
		return select;
	}
	
	/**
	 * Constructs a SQL WHERE clause with {@link android.provider.BaseColumns#_ID _ID}
	 * prepended.
	 *  
	 * @param id The value of the _ID column
	 * @param wherClause
	 * @return
	 */
	public static String getWhereClause(long id, String wherClause){
		return DatabaseUtilsCompat.concatenateWhere(
				BaseColumns._ID + " = " + id, wherClause);
	}
	
	public static List<String> dumpStringColumn(Cursor cursor, int column){
		List<String> list = new ArrayList<String>();
		while(cursor.moveToNext()){
			list.add(cursor.getString(column));
		}
		return list;
	}
}
