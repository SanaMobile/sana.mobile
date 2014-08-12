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

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.sana.android.db.TableHelper;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Patients;
import org.sana.core.Encounter;
import org.sana.util.UUIDUtil;
/**
 * A database table helper for a table of encounters.
 * 
 * @author Sana Development
 *
 */
public class EncountersHelper extends TableHelper<Encounter>{
    public static final String TAG = EncountersHelper.class.getSimpleName();

    public static final String SELECT_COMPOUND = "SELECT"
            + "encountertask._id AS encountertask_id,"
            + "encountertask.uuid AS encountertask_uuid,"
            + "encountertask.due_on AS ,"
            + "encountertask.modified AS modified,"
            + "patient._id AS patient_id,"
            + "patient.uuid AS patient_uuid,"
            + "patient.given_name AS patient_given_name,"
            + "patient.family_name AS patient_family_name,"
            + "procedure._id AS procedure_id,"
            + "procedure.uuid AS procedure_uuid,"
            + "procedure.title AS procedure_title"
            + " FROM"
            + " encountertask"
            + " LEFT JOIN patient ON encountertask.patient = patient.uuid"
            + " LEFT JOIN procedure ON encountertask.procedure = procedure.uuid";
    
    static final Map<String, String> sProjectionMap = new HashMap<String, String>();
    
    static{
        sProjectionMap.put(Encounters.Contract.STATE, Encounters.Contract.STATE);
        
    }
    
    private static final EncountersHelper HELPER = new EncountersHelper();
    
    /**
     * Gets the singleton instance of this class.
     * 
     * @return An instance of this class.
     */
    public static EncountersHelper getInstance(){
        return HELPER;
    }
    
    protected EncountersHelper(){
        super(Encounter.class);
    }
    
    /* (non-Javadoc)
     * @see org.sana.android.db.InsertHelper#onInsert(android.net.Uri, android.content.ContentValues)
     */
    @Override
    public ContentValues onInsert(ContentValues values) {
        
        if(values.containsKey(Encounters.Contract.STATE)== false){
            values.put(Encounters.Contract.STATE, "");
        }
        
        if(values.containsKey(Encounters.Contract.FINISHED) == false) {
            values.put(Encounters.Contract.FINISHED, false);
        }
        
        if(values.containsKey(Encounters.Contract.UPLOADED) == false) {
            values.put(Encounters.Contract.UPLOADED, false);
        }
        
        if(values.containsKey(Encounters.Contract.UPLOAD_STATUS) == false) {
            values.put(Encounters.Contract.UPLOAD_STATUS, -1);
        }
        
        if(values.containsKey(Encounters.Contract.UPLOAD_QUEUE) == false) {
            values.put(Encounters.Contract.UPLOAD_QUEUE, -1);
        }
        return super.onInsert(values);
    }

    /* (non-Javadoc)
     * @see org.sana.android.db.UpdateHelper#onUpdate(java.lang.String, android.content.ContentValues, java.lang.String, java.lang.String[])
     */
    @Override
    public ContentValues onUpdate(Uri uri, ContentValues values) {
        return super.onUpdate(uri, values);
    }

    /* (non-Javadoc)
     * @see org.sana.android.db.CreateHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
    @Override
    public String onCreate() {
        Log.i(TAG, "onCreate()");
        return "CREATE TABLE " + getTable() + " ("
                + Encounters.Contract._ID + " INTEGER PRIMARY KEY,"
                + Encounters.Contract.UUID + " TEXT,"
                + Encounters.Contract.PROCEDURE + " TEXT NOT NULL,"
                + Encounters.Contract.SUBJECT + " TEXT NOT NULL,"
                + Encounters.Contract.OBSERVER + " TEXT NOT NULL,"
                + Encounters.Contract.STATE + " TEXT,"
                + Encounters.Contract.FINISHED + " INTEGER,"
                + Encounters.Contract.UPLOADED + " INTEGER,"
                + Encounters.Contract.UPLOAD_STATUS + " INTEGER,"
                + Encounters.Contract.UPLOAD_QUEUE + " INTEGER,"
                + Encounters.Contract.CREATED + " TEXT,"
                + Encounters.Contract.MODIFIED + " TEXT"
                + ");";
    }

    /* (non-Javadoc)
     * @see org.sana.android.db.UpgradeHelper#onUpgrade(int, int)
     */
    @Override
    public String onUpgrade(int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.sana.android.db.SortHelper#onSort(android.net.Uri)
     */
    @Override
    public String onSort(Uri uri) {
        return Encounters.Contract.CREATED + " DESC";
    }
    
    /*
    @Override
    public Cursor onQuery(SQLiteDatabase db, String[] projection, 
            String selection, String[] selectionArgs, String sortOrder){
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        if(TextUtils.isEmpty(selection)){
            String[] tables = new String[] { getTable(), SubjectsHelper.getInstance().getTable()};
            selection = String.format("%s LEFT OUTER JOIN %s ON %s = %s",
                    tables[0], tables[1], Encounters.Contract.SUBJECT, Patients.Contract.UUID);
            qb.setTables(tables[0]+","+tables[1]);
        } else {
            qb.setTables(getTable());
        }
        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null,null, sortOrder);
        return cursor;
    }
    */
}
