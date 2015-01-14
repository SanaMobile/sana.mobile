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
package org.sana.android.db;

import org.sana.R;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class helps open, create, and upgrade the database file.
 * 
 * @author Sana Development Team
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	/**
	 * Creates a new instance with the database name and version set based on
	 * the values of {@link R.string#db_name} and {@link R.integer#db_version}.
	 * 
	 * @param context
	 */
    DatabaseHelper(Context context) {
        super(context, context.getString(R.string.db_name), 
        		null, 
        		context.getResources().getInteger(R.integer.cfg_db_version_value));
    }

    /* (non-Javadoc)
     * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
    /**
     * Creates a table for each content provider in the input database.
     * @param db The SQLite database where the tables are stored.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
    	/*
    	ConceptProvider.onCreateDatabase(db);
        ObserverProvider.onCreateDatabase(db);
        ProcedureProvider.onCreateDatabase(db);
        PatientProvider.onCreateDatabase(db);
        EncounterProvider.onCreateDatabase(db);
        ObservationProvider.onCreateDatabase(db);
        
        EventProvider.onCreateDatabase(db);
        NotificationProvider.onCreateDatabase(db);
    	 */
        // Deprecated 
        ImageProvider.onCreateDatabase(db);
        SoundProvider.onCreateDatabase(db);
        BinaryProvider.onCreateDatabase(db);
    }

    /* (non-Javadoc)
     * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
     */
    /**
     * Upgrades the database version for each content provider in the input database.
     * @param db The SQLite database where the tables are stored.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        BinaryProvider.onUpgradeDatabase(db, oldVersion, newVersion);
        ImageProvider.onUpgradeDatabase(db, oldVersion, newVersion);
        SoundProvider.onUpgradeDatabase(db, oldVersion, newVersion);
        /*
        ProcedureProvider.onUpgradeDatabase(db, oldVersion, newVersion);
        EncounterProvider.onUpgradeDatabase(db, oldVersion, newVersion);
        NotificationProvider.onUpgradeDatabase(db, oldVersion, newVersion);
        PatientProvider.onUpgradeDatabase(db, oldVersion, newVersion);
        EventProvider.onUpgradeDatabase(db, oldVersion, newVersion);
        ObservationProvider.onUpgradeDatabase(db, oldVersion, newVersion);
        */
    }
}