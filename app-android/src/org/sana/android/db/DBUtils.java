package org.sana.android.db;

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
	 * Returns the selection statement as
	 *  ( _ID = "uri.getPathSegments().get(1)" ) AND ( selection )
	 * or as the original based on whether the uri match was a dir or item.
	 * Relies on matcher values for *.dir being even integers.
	 * @param uri
	 * @param selection
	 * @return
	 */
	public static String getWhereWithIdOrReturn(Uri uri, int match, String selection){
		String select = selection;
		if((match & 1) != 0)
			select = DatabaseUtilsCompat.concatenateWhere(selection,
					BaseColumns._ID + " = " + uri.getPathSegments().get(1));
		return select;
	}
	
}
