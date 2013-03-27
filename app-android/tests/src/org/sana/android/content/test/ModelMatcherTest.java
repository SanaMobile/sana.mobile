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
package org.sana.android.content.test;

import java.util.UUID;

import org.sana.android.content.ModelMatcher;
import org.sana.android.provider.Concepts;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Events;
import org.sana.android.provider.Instructions;
import org.sana.android.provider.Notifications;
import org.sana.android.provider.Observations;
import org.sana.android.provider.Observers;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Subjects;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * @author Sana Development
 *
 */
public class ModelMatcherTest extends AndroidTestCase {
	public static final String TAG = ModelMatcherTest.class.getSimpleName();
	
    //TODO refactor this out
    /**
     * Test the model matcher results for DIR, ITEM_ID, ITEM_UUID and invalid 
     * uuid types.
     */
    public void testModelMatcher(){
    	ModelMatcher matcher = ModelMatcher.getInstance();
    	String uuid = UUID.randomUUID().toString();
    	String baduuid = "abcd-tyu-io4-uuon";
    	Uri uri = Uri.EMPTY;

    	// Concepts
    	// DIR type
    	uri = Concepts.CONTENT_URI;
    	assertEquals(matcher.match(uri), ModelMatcher.CONCEPT_DIR );
    	// ITEM type
    	uri = Uri.withAppendedPath(Concepts.CONTENT_URI, "1");
    	assertEquals(matcher.match(uri), ModelMatcher.CONCEPT_ITEM );
    	// UUID type
    	uri = Uri.withAppendedPath(Concepts.CONTENT_URI, uuid);
    	assertEquals(matcher.match(uri), ModelMatcher.CONCEPT_UUID );
    	// Fail UUID type
    	uri = Uri.withAppendedPath(Concepts.CONTENT_URI, baduuid);
    	assertEquals(matcher.match(uri), UriMatcher.NO_MATCH );
    	
    	// Encounters
    	// DIR type
    	uri = Encounters.CONTENT_URI;
    	assertEquals(matcher.match(uri), ModelMatcher.ENCOUNTER_DIR );
    	// ITEM type
    	uri = Uri.withAppendedPath(Encounters.CONTENT_URI, "1");
    	assertEquals(matcher.match(uri), ModelMatcher.ENCOUNTER_ITEM);
    	// UUID type
    	uri = Uri.withAppendedPath(Encounters.CONTENT_URI, uuid);
    	assertEquals(matcher.match(uri), ModelMatcher.ENCOUNTER_UUID );
    	// Fail UUID type
    	uri = Uri.withAppendedPath(Encounters.CONTENT_URI, baduuid);
    	assertEquals(matcher.match(uri), UriMatcher.NO_MATCH );

    	// Events
    	// DIR type
    	uri = Events.CONTENT_URI;
    	assertEquals(matcher.match(uri), ModelMatcher.EVENT_DIR );
    	// ITEM type
    	uri = Uri.withAppendedPath(Events.CONTENT_URI, "1");
    	assertEquals(matcher.match(uri), ModelMatcher.EVENT_ITEM );
    	// UUID type
    	uri = Uri.withAppendedPath(Events.CONTENT_URI, uuid);
    	assertEquals(matcher.match(uri), ModelMatcher.EVENT_UUID );
    	// Fail UUID type
    	uri = Uri.withAppendedPath(Events.CONTENT_URI, baduuid);
    	assertEquals(matcher.match(uri), UriMatcher.NO_MATCH );
    	
    	// Instructions
    	// DIR type
    	uri = Instructions.CONTENT_URI;
    	assertEquals(matcher.match(uri), ModelMatcher.INSTRUCTION_DIR );
    	// ITEM type
    	uri = Uri.withAppendedPath(Instructions.CONTENT_URI, "1");
    	assertEquals(matcher.match(uri), ModelMatcher.INSTRUCTION_ITEM );
    	// UUID type
    	uri = Uri.withAppendedPath(Instructions.CONTENT_URI, uuid);
    	assertEquals(matcher.match(uri), ModelMatcher.INSTRUCTION_UUID );
    	// Fail UUID type
    	uri = Uri.withAppendedPath(Instructions.CONTENT_URI, baduuid);
    	assertEquals(matcher.match(uri), UriMatcher.NO_MATCH );
    	
    	// Notifications
    	// DIR type
    	uri = Notifications.CONTENT_URI;
    	assertEquals(matcher.match(uri), ModelMatcher.NOTIFICATION_DIR );
    	// ITEM type
    	uri = Uri.withAppendedPath(Notifications.CONTENT_URI, "1");
    	assertEquals(matcher.match(uri), ModelMatcher.NOTIFICATION_ITEM );
    	// UUID type
    	uri = Uri.withAppendedPath(Notifications.CONTENT_URI, uuid);
    	assertEquals(matcher.match(uri), ModelMatcher.NOTIFICATION_UUID );
    	// Fail UUID type
    	uri = Uri.withAppendedPath(Notifications.CONTENT_URI, baduuid);
    	assertEquals(matcher.match(uri), UriMatcher.NO_MATCH );


    	// Observations
    	uri = Observations.CONTENT_URI;
    	assertEquals(matcher.match(uri), ModelMatcher.OBSERVATION_DIR );
    	// ITEM type
    	uri = Uri.withAppendedPath(Observations.CONTENT_URI, "1");
    	assertEquals(matcher.match(uri), ModelMatcher.OBSERVATION_ITEM );
    	// UUID type
    	uri = Uri.withAppendedPath(Observations.CONTENT_URI, uuid);
    	assertEquals(matcher.match(uri), ModelMatcher.OBSERVATION_UUID );
    	// Fail UUID type
    	uri = Uri.withAppendedPath(Observations.CONTENT_URI, baduuid);
    	assertEquals(matcher.match(uri), UriMatcher.NO_MATCH );
    	
    	// Observers
    	// DIR type
    	uri = Observers.CONTENT_URI;
    	assertEquals(matcher.match(uri), ModelMatcher.OBSERVER_DIR );
    	// ITEM type
    	uri = Uri.withAppendedPath(Observers.CONTENT_URI, "1");
    	assertEquals(matcher.match(uri), ModelMatcher.OBSERVER_ITEM );
    	// UUID type
    	uri = Uri.withAppendedPath(Observers.CONTENT_URI, uuid);
    	assertEquals(matcher.match(uri), ModelMatcher.OBSERVER_UUID );
    	// Fail UUID type
    	uri = Uri.withAppendedPath(Observers.CONTENT_URI, baduuid);
    	assertEquals(matcher.match(uri), UriMatcher.NO_MATCH );

    	
    	// Procedures
    	// DIR type
    	uri = Procedures.CONTENT_URI;
    	assertEquals(matcher.match(uri), ModelMatcher.PROCEDURE_DIR );
    	// ITEM type
    	uri = Uri.withAppendedPath(Procedures.CONTENT_URI, "1");
    	assertEquals(matcher.match(uri), ModelMatcher.PROCEDURE_ITEM );
    	// UUID type
    	uri = Uri.withAppendedPath(Procedures.CONTENT_URI, uuid);
    	assertEquals(matcher.match(uri), ModelMatcher.PROCEDURE_UUID );
    	// Fail UUID type
    	uri = Uri.withAppendedPath(Procedures.CONTENT_URI, baduuid);
    	assertEquals(matcher.match(uri), UriMatcher.NO_MATCH );

    	// Subjects
    	// DIR type
    	uri = Subjects.CONTENT_URI;
    	assertEquals(matcher.match(uri), ModelMatcher.SUBJECT_DIR );
    	// ITEM type
    	uri = Uri.withAppendedPath(Subjects.CONTENT_URI, "1");
    	assertEquals(matcher.match(uri), ModelMatcher.SUBJECT_ITEM );
    	// UUID type
    	uri = Uri.withAppendedPath(Subjects.CONTENT_URI, uuid);
    	assertEquals(matcher.match(uri), ModelMatcher.SUBJECT_UUID );
    	// Fail UUID type
    	uri = Uri.withAppendedPath(Subjects.CONTENT_URI, baduuid);
    	assertEquals(matcher.match(uri), UriMatcher.NO_MATCH );
    }
}
