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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import org.sana.android.db.TableHelper;
import org.sana.android.provider.EncounterTasks.Contract;
import org.sana.api.IModel;
import org.sana.api.task.EncounterTask;
import org.sana.api.task.Status;

import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;
import android.text.TextUtils;

/**
 * @author Sana Development
 *
 */
public class EncounterTasksHelper extends TableHelper<EncounterTask>{

    static final SimpleDateFormat sdf = new SimpleDateFormat(IModel.DATE_FORMAT, 
			Locale.US);
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
	
	private EncounterTasksHelper(){
		super(EncounterTask.class);
	}
	

	/* (non-Javadoc)
	 * @see org.sana.android.db.InsertHelper#onInsert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public ContentValues onInsert(ContentValues values) {
		ContentValues vals = new ContentValues();
		//vals.put(Contract.UUID, UUID.randomUUID().toString());
        //vals.put(Contract.STATUS, Status.ACCEPTED.toString());
        vals.put( Contract.OBSERVER, "");
        vals.put(Contract.ENCOUNTER, "");
        vals.put( Contract.PROCEDURE, "");
        vals.put( Contract.SUBJECT, "");
        String dueStr = values.getAsString(Contract.DUE_DATE);
		Date dueDate = new Date();
		try {
			dueDate = (dueStr != null)? sdf.parse(dueStr): new Date();

            Date checkDate;
        
            String completed = values.getAsString(Contract.COMPLETED);
            if(!TextUtils.isEmpty(completed)){
                checkDate = sdf.parse(completed);
                vals.put( Contract.COMPLETED, sdf.format(completed));
            }
            String started = values.getAsString(Contract.STARTED);
            if(!TextUtils.isEmpty(started)){
                checkDate = sdf.parse(started);
                vals.put( Contract.STARTED, sdf.format(started));
            }
        } catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
        vals.put( Contract.DUE_DATE, sdf.format(dueDate));
        
        vals.putAll(values);
		return super.onInsert(vals);
	}

	/* (non-Javadoc)
	 * @see org.sana.android.db.UpdateHelper#onUpdate(java.lang.String, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public ContentValues onUpdate(Uri uri, ContentValues values) {
		/*
		ContentValues vals = new ContentValues();
        vals.put( Contract.OBSERVER, "");
        vals.put(Contract.STATUS, "");
        vals.put(Contract.SUBJECT, "");
        vals.put( Contract.PROCEDURE, "");
        vals.put(Contract.ENCOUNTER, "");
        String dueStr = values.getAsString(Contract.DUE_DATE);
		Date dueDate = new Date();
		try {
			dueDate = (dueStr != null)? sdf.parse(dueStr): new Date();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        vals.put( Contract.DUE_DATE, sdf.format(dueDate));
        vals.putAll(values);
        */
		return super.onUpdate(uri, values);
	}

	/* (non-Javadoc)
	 * @see org.sana.android.db.CreateHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public String onCreate() {
		Log.i(TAG, "onCreate()");
		return "CREATE TABLE " + getTable() + " ("
				+ Contract._ID + " INTEGER PRIMARY KEY,"
				+ Contract.UUID + " TEXT, "
				+ Contract.OBSERVER + " TEXT, "
				+ Contract.STATUS + " TEXT, "
				+ Contract.DUE_DATE + " DATE, "
				+ Contract.COMPLETED + " DATE, "
				+ Contract.STARTED + " DATE, "
				+ Contract.ENCOUNTER + " TEXT, "
				+ Contract.PROCEDURE + " TEXT, "
				+ Contract.SUBJECT + " TEXT, "
                + Contract.CREATED + " INTEGER,"
                + Contract.MODIFIED + " INTEGER"
				+ ");";
		
	}

	/* (non-Javadoc)
	 * @see org.sana.android.db.UpgradeHelper#onUpgrade(int, int)
	 */
	@Override
	public String onUpgrade(int oldVersion, int newVersion) {
		if(oldVersion < newVersion){
			return "DROP TABLE " + getTable() +"; " + onCreate();
		}
		return null;
	}
	

	private static final EncounterTasksHelper HELPER = new EncounterTasksHelper();
	
	public static EncounterTasksHelper getInstance(){
		return HELPER;
	}
}
