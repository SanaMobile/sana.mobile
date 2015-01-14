/**
 * Copyright (c) 2013, Sana
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sana nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL Sana BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.sana.android.db.impl;

import org.sana.android.db.BinaryProvider;
import org.sana.android.db.DatabaseOpenHelper;
import org.sana.android.db.ImageProvider;
import org.sana.android.db.SoundProvider;
import org.sana.android.db.TableHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * @author Sana Development
 *
 */
public class DatabaseOpenHelperImpl extends DatabaseOpenHelper{
	
	public static final String TAG = DatabaseOpenHelperImpl.class
			.getSimpleName();
	
	private static DatabaseOpenHelperImpl instance = null;
	
	public static synchronized DatabaseOpenHelperImpl getInstance(Context context, String name, int version){

        if (instance == null)
            instance = new DatabaseOpenHelperImpl(context, name, version);
        return instance;
    }
	
	/**
	 * @param context
	 * @param name
	 * @param version
	 */
	public DatabaseOpenHelperImpl(Context context, String name, int version) {
		super(context, name, version);
		
	}
	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "onCreate()");
		String[] create = new String[]{ 
				ConceptsHelper.getInstance().onCreate(),
				EncountersHelper.getInstance().onCreate(),
				EncounterTasksHelper.getInstance().onCreate(),
				EventsHelper.getInstance().onCreate(),
				InstructionsHelper.getInstance().onCreate(),
				NotificationsHelper.getInstance().onCreate(),
				ObservationsHelper.getInstance().onCreate(),
				ObserversHelper.getInstance().onCreate(),
				ProceduresHelper.getInstance().onCreate(),
				SubjectsHelper.getInstance().onCreate()
		};
		//db.acquireReference();
		for(String sql:create){
			db.execSQL(sql);
		}
        // Deprecated 
        ImageProvider.onCreateDatabase(db);
        SoundProvider.onCreateDatabase(db);
        BinaryProvider.onCreateDatabase(db);
		//db.releaseReference();
	}
	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG, String.format("onUpgrade(int,int) -> (%d, %d)",oldVersion, newVersion));
		// No bump in version - return quietly
		if(newVersion > oldVersion){
			TableHelper<?>[] helpers = new TableHelper<?>[]{
				ConceptsHelper.getInstance(),
				EncountersHelper.getInstance(),
				EncounterTasksHelper.getInstance(),
				EventsHelper.getInstance(),
				InstructionsHelper.getInstance(),
				NotificationsHelper.getInstance(),
				ObservationsHelper.getInstance(),
				ObserversHelper.getInstance(),
				ProceduresHelper.getInstance(),
				SubjectsHelper.getInstance() };

			
			for(TableHelper<?> helper:helpers){
				if(oldVersion <= 2){
					db.execSQL("DROP TABLE " + helper.getTable() + " IF EXISTS;");
					db.execSQL(helper.onCreate());
				} else {
					String sql = helper.onUpgrade(oldVersion, newVersion);
					Log.i(TAG, String.format("onUpgrade(int,int)", ((sql == null)?"NULL":sql)));
					if(sql != null)
						db.execSQL(helper.onUpgrade(oldVersion, newVersion));
				}
			}
		}
	}
}
