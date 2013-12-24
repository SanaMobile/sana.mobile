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
package org.sana.android.db.impl.test;

import java.util.UUID;

import org.sana.R;
import org.sana.android.db.DatabaseOpenHelper;
import org.sana.android.db.TableHelper;
import org.sana.android.db.impl.ConceptsHelper;
import org.sana.android.db.impl.DatabaseOpenHelperImpl;
import org.sana.android.db.impl.EncounterTasksHelper;
import org.sana.android.db.impl.EncountersHelper;
import org.sana.android.db.impl.EventsHelper;
import org.sana.android.db.impl.InstructionsHelper;
import org.sana.android.db.impl.ModelContentProviderImpl;
import org.sana.android.db.impl.NotificationsHelper;
import org.sana.android.db.impl.ObservationsHelper;
import org.sana.android.db.impl.ObserversHelper;
import org.sana.android.db.impl.ProceduresHelper;
import org.sana.android.db.impl.SubjectsHelper;
import org.sana.android.provider.Concepts;
import org.sana.android.provider.EncounterTasks;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Events;
import org.sana.android.provider.Instructions;
import org.sana.android.provider.Notifications;
import org.sana.android.provider.Observations;
import org.sana.android.provider.Observers;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Subjects;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import android.test.mock.MockContext;
import android.util.Log;

/**
 * Handles the test for the ModelContentProvdierImpl class.
 * 
 * @author Sana Development
 *
 */
public class ModelContentProviderImplTest extends 
	ProviderTestCase2<ModelContentProviderImpl> 
{

	public static final String TAG = ModelContentProviderImplTest.class
			.getSimpleName();
	
	MockContentResolver mMockResolver;
	SQLiteDatabase  mDb;
	TableHelper<?> mHelper;
	DatabaseOpenHelper mOpener = null;
	MockContext mContext;
	
	/**
	 * 
	 */
	public ModelContentProviderImplTest() {
		super(ModelContentProviderImpl.class, ModelContentProviderImpl.AUTHORITY);
	}
	
	@Override
	protected void setUp() throws Exception{
        // Calls the base class implementation of this method.
        super.setUp();
        
        // Gets the resolver for this test.
        mMockResolver = getMockContentResolver();

	}

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        for(String name: getContext().databaseList()){
        	getContext().deleteDatabase(name);
        }
        
    }
    
    /**
     * TEst directly against the SQL CREATE statements in the helpers.
     */
    public void testTableHelperOnCreate(){
		String name = getContext().getString(R.string.db_name);//DATABASE;
		int version = getContext().getResources().getInteger(R.integer.cfg_db_version_value);
		SQLiteDatabase db = getContext().openOrCreateDatabase(name, 0, null);
		
		// Concept table
		db.execSQL(ConceptsHelper.getInstance().onCreate());
		
		// Encounter table
		db.execSQL(EncountersHelper.getInstance().onCreate());
		
		// Events table
		db.execSQL(EventsHelper.getInstance().onCreate());
		
		// Instructions table
		db.execSQL(InstructionsHelper.getInstance().onCreate());

		// Notification table
		db.execSQL(NotificationsHelper.getInstance().onCreate());

		// Observation table
		db.execSQL(ObservationsHelper.getInstance().onCreate());

		// Observer table
		db.execSQL(ObserversHelper.getInstance().onCreate());

		// Procedure table
		db.execSQL(ProceduresHelper.getInstance().onCreate());

		// Subject table
		db.execSQL(SubjectsHelper.getInstance().onCreate());

		// Subject table
		db.execSQL(EncounterTasksHelper.getInstance().onCreate());
		
		db.close();
    }
    

    /**
     * Tests DatabaseOpenHelperImpl onCreate(SQLiteDatabase) directly 
     */
    public void testProvideronCreate(){
		String name = getContext().getString(R.string.db_name);//DATABASE;
		int version = getContext().getResources().getInteger(R.integer.cfg_db_version_value);
		mOpener = new DatabaseOpenHelperImpl(getContext(),name, version);
		SQLiteDatabase db = getContext().openOrCreateDatabase(name, 0, null);
		mOpener.onCreate(db);
		mOpener.close();
    }
    
    /**
     * Tests DatabaseOpenHelperImpl 
     */
    public void testDatabaseOpenHelperCreateOnGetReadable(){
		String name = getContext().getString(R.string.db_name);//DATABASE;
		int version = getContext().getResources().getInteger(R.integer.cfg_db_version_value);
		mOpener = new DatabaseOpenHelperImpl(getContext(),name, version);
		
		// this should invoke the onCreate method		
		mOpener.getReadableDatabase();
		mOpener.close();
    }
    
    /**
     * Tests DatabaseOpenHelperImpl 
     */
    public void testDatabaseOpenHelperCreateOnGetWritable(){
		String name = getContext().getString(R.string.db_name);//DATABASE;
		int version = getContext().getResources().getInteger(R.integer.cfg_db_version_value);
		mOpener = new DatabaseOpenHelperImpl(getContext(),name, version);
		
		// this should invoke the onCreate method		
		mOpener.getWritableDatabase();
		mOpener.close();
    }
    
    
    /**
     * test getType() method
     */
    public void testDatabaseGetType(){
    
    	Uri uri;
    	String uuid = UUID.randomUUID().toString();
    	
    	// Concepts
    	// DIR type
    	uri = Concepts.CONTENT_URI;
    	assertEquals(Concepts.CONTENT_TYPE, mMockResolver.getType(uri));
    	// ITEM type
    	uri = Uri.withAppendedPath(Concepts.CONTENT_URI, "1");
    	assertEquals(Concepts.CONTENT_ITEM_TYPE, mMockResolver.getType(uri));
    	// ITEM type from UUID
    	uri = Uri.withAppendedPath(Concepts.CONTENT_URI, uuid.toString());
    	assertEquals(Concepts.CONTENT_ITEM_TYPE, mMockResolver.getType(uri));
    	
    	// Encounters
    	// DIR type
    	uri = Encounters.CONTENT_URI;
    	assertEquals(Encounters.CONTENT_TYPE, mMockResolver.getType(uri));
    	// ITEM type
    	uri = Uri.withAppendedPath(Encounters.CONTENT_URI, "1");
    	assertEquals(Encounters.CONTENT_ITEM_TYPE, mMockResolver.getType(uri));
    	// ITEM type from UUID
    	uri = Uri.withAppendedPath(Encounters.CONTENT_URI, uuid);
    	assertEquals(Encounters.CONTENT_ITEM_TYPE, mMockResolver.getType(uri));

    	// Events
    	// DIR type
    	uri = Events.CONTENT_URI;
    	assertEquals(Events.CONTENT_TYPE, mMockResolver.getType(uri));
    	// ITEM type
    	uri = Uri.withAppendedPath(Events.CONTENT_URI, "1");
    	assertEquals(Events.CONTENT_ITEM_TYPE, mMockResolver.getType(uri));
    	// ITEM type from UUID
    	uri = Uri.withAppendedPath(Events.CONTENT_URI, uuid);
    	assertEquals(Events.CONTENT_ITEM_TYPE, mMockResolver.getType(uri));
    	
    	// Instructions
    	// DIR type
    	uri = Instructions.CONTENT_URI;
    	assertEquals(Instructions.CONTENT_TYPE, mMockResolver.getType(uri));
    	// ITEM type
    	uri = Uri.withAppendedPath(Instructions.CONTENT_URI, "1");
    	assertEquals(Instructions.CONTENT_ITEM_TYPE, mMockResolver.getType(uri));
    	// ITEM type from UUID
    	uri = Uri.withAppendedPath(Instructions.CONTENT_URI, uuid);
    	assertEquals(Instructions.CONTENT_ITEM_TYPE, mMockResolver.getType(uri));
    	
    	// Notifications
    	// DIR type
    	uri = Notifications.CONTENT_URI;
    	assertEquals(Notifications.CONTENT_TYPE, mMockResolver.getType(uri));
    	// ITEM type
    	uri = Uri.withAppendedPath(Notifications.CONTENT_URI, "1");
    	assertEquals(Notifications.CONTENT_ITEM_TYPE, mMockResolver.getType(uri));
    	// ITEM type from UUID
    	uri = Uri.withAppendedPath(Notifications.CONTENT_URI, uuid);
    	assertEquals(Notifications.CONTENT_ITEM_TYPE, mMockResolver.getType(uri));

    	// Observers
    	// DIR type
    	uri = Observers.CONTENT_URI;
    	assertEquals(Observers.CONTENT_TYPE, mMockResolver.getType(uri));
    	// ITEM type
    	uri = Uri.withAppendedPath(Observers.CONTENT_URI, "1");
    	assertEquals(Observers.CONTENT_ITEM_TYPE, mMockResolver.getType(uri));
    	// ITEM type from UUID
    	uri = Uri.withAppendedPath(Observers.CONTENT_URI, uuid);
    	assertEquals(Observers.CONTENT_ITEM_TYPE, mMockResolver.getType(uri));

    	// Observations
    	uri = Observations.CONTENT_URI;
    	assertEquals(Observations.CONTENT_TYPE, mMockResolver.getType(uri));
    	// ITEM type
    	uri = Uri.withAppendedPath(Observations.CONTENT_URI, "1");
    	assertEquals(Observations.CONTENT_ITEM_TYPE, mMockResolver.getType(uri));
    	// ITEM type from UUID
    	uri = Uri.withAppendedPath(Observations.CONTENT_URI, uuid);
    	assertEquals(Observations.CONTENT_ITEM_TYPE, mMockResolver.getType(uri));
    	
    	// Procedures
    	// DIR type
    	uri = Procedures.CONTENT_URI;
    	assertEquals(Procedures.CONTENT_TYPE, mMockResolver.getType(uri));
    	// ITEM type
    	uri = Uri.withAppendedPath(Procedures.CONTENT_URI, "1");
    	assertEquals(Procedures.CONTENT_ITEM_TYPE, mMockResolver.getType(uri));
    	// ITEM type from UUID
    	uri = Uri.withAppendedPath(Procedures.CONTENT_URI, uuid);
    	assertEquals(Procedures.CONTENT_ITEM_TYPE, mMockResolver.getType(uri));

    	// Subjects
    	// DIR type
    	uri = Subjects.CONTENT_URI;
    	assertEquals(Subjects.CONTENT_TYPE, mMockResolver.getType(uri));
    	// ITEM type
    	uri = Uri.withAppendedPath(Subjects.CONTENT_URI, "1");
    	assertEquals(Subjects.CONTENT_ITEM_TYPE, mMockResolver.getType(uri));
    	// ITEM type from UUID
    	uri = Uri.withAppendedPath(Subjects.CONTENT_URI, uuid);
    	assertEquals(Subjects.CONTENT_ITEM_TYPE, mMockResolver.getType(uri));
    	
    	// EncounterTasks
    	// DIR type
    	uri = EncounterTasks.CONTENT_URI;
    	assertEquals(EncounterTasks.CONTENT_TYPE, mMockResolver.getType(uri));
    	// ITEM type
    	uri = Uri.withAppendedPath(EncounterTasks.CONTENT_URI, "1");
    	assertEquals(EncounterTasks.CONTENT_ITEM_TYPE, mMockResolver.getType(uri));
    	// ITEM type from UUID
    	uri = Uri.withAppendedPath(EncounterTasks.CONTENT_URI, uuid);
    	assertEquals(EncounterTasks.CONTENT_ITEM_TYPE, mMockResolver.getType(uri));
    }
}
