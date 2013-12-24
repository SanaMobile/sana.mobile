package org.sana.android.db;


import org.sana.android.provider.Encounters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
/**
 * A class for interacting with the encounter, or saved procedure, content 
 * provider.
 * 
 * @author Sana Development Team
 *
 */
public class EncounterDAO {
	
	/**
	 * Fetches an encounter UUID.
	 * 
	 * @param context The application context.
	 * @param encounterUri The Uri of an encounter.
	 * @return
	 */
	public static String getEncounterGuid(Context context, Uri encounterUri) {
		Cursor cursor = null;
		String guid = "";
		try {
			cursor = context.getContentResolver().query(encounterUri, 
					new String [] { Encounters.Contract.UUID }, null, null, 
					null);
			if (cursor != null && cursor.moveToFirst()) {
				guid = cursor.getString(cursor.getColumnIndex(
						Encounters.Contract.UUID));
			}
		} catch (Exception e) {
			e.printStackTrace();
			EventDAO.logException(context, e);
		} finally {
			if (cursor != null)
				cursor.close();
		}
		
		return guid;
	}
}
