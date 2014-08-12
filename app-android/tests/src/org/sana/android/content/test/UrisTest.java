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

import org.sana.android.content.Uris;
import org.sana.android.provider.Concepts;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Events;
import org.sana.android.provider.Instructions;
import org.sana.android.provider.Notifications;
import org.sana.android.provider.Observations;
import org.sana.android.provider.Observers;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Subjects;

import android.content.ContentUris;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * Tests utility methods of the {@link org.sana.android.content.Uris Uris} class
 * as well as those which handled matching the integer value to content style 
 * Uri mapping for Uri's handled by the ContentProvider's in the Sana API. The 
 * matching tests must validate directory, item and item uuid style Uri's. 
 * 
 * @author Sana Development
 *
 */
public class UrisTest extends AndroidTestCase {
	public static final String TAG = UrisTest.class.getSimpleName();

	Uri uri = Uri.EMPTY;
	String uuid = UUID.randomUUID().toString();
	String baduuid = "abcd-tyu-io4-uuon";

	protected void setUp() throws Exception{
		super.setUp();
		// Rest the local uri field to an empty Uri
		uri = Uri.EMPTY;
		 //Resets the uuid to a valid, randomly generated value.
		uuid = UUID.randomUUID().toString();
	}
	
	public static void logUri(Uri uri){
		Log.d(TAG, String.format(" { 'descriptor'=%d, 'typeDescriptor'=%d, 'contentDescriptor'=%d, 'uri'=%s }",
			Uris.getDescriptor(uri),
			Uris.getTypeDescriptor(uri),
			Uris.getContentDescriptor(uri),
			uri));
	}
	
	public void testConceptDescriptors(){
    	assertEquals(Uris.getDescriptor(Concepts.CONTENT_URI), Uris.CONCEPT_DIR);
    	uri = ContentUris.withAppendedId(Concepts.CONTENT_URI, 1);
    	assertEquals(Uris.getDescriptor(uri), Uris.CONCEPT_ITEM);
    	uri = Uris.withAppendedUuid(Concepts.CONTENT_URI, uuid.toString());
    	assertEquals(Uris.getDescriptor(uri), Uris.CONCEPT_UUID);
	}
	
	public void testEncounterDescriptors(){
		// Test DIR
		uri = Encounters.CONTENT_URI;
		logUri(uri);
    	assertEquals(Uris.getDescriptor(Encounters.CONTENT_URI), Uris.ENCOUNTER_DIR);
    	assertEquals(Uris.ITEMS, Uris.getTypeDescriptor(uri));
    	assertEquals(Uris.ENCOUNTER, Uris.getContentDescriptor(uri));
    	
    	//Test ITEM_ID
    	uri = ContentUris.withAppendedId(Encounters.CONTENT_URI, 1);
		logUri(uri);
    	assertEquals(Uris.getDescriptor(uri), Uris.ENCOUNTER_ITEM);
    	assertEquals(Uris.ITEM_ID, Uris.getTypeDescriptor(uri));
    	assertEquals(Uris.ENCOUNTER, Uris.getContentDescriptor(uri));
    	
    	// Test ITEM UUID
    	uri = Uris.withAppendedUuid(Encounters.CONTENT_URI, uuid.toString());
		logUri(uri);
    	assertEquals(Uris.getDescriptor(uri), Uris.ENCOUNTER_UUID);
    	assertEquals(Uris.ITEM_UUID, Uris.getTypeDescriptor(uri));
    	assertEquals(Uris.ENCOUNTER, Uris.getContentDescriptor(uri));
	}
	
	public void testEventDescriptors(){
    	assertEquals(Uris.getDescriptor(Events.CONTENT_URI), Uris.EVENT_DIR);
    	uri = ContentUris.withAppendedId(Events.CONTENT_URI, 1);
    	assertEquals(Uris.getDescriptor(uri), Uris.EVENT_ITEM);
    	uri = Uris.withAppendedUuid(Events.CONTENT_URI, uuid.toString());
    	assertEquals(Uris.getDescriptor(uri), Uris.EVENT_UUID);
	}
	
	public void testInstructionDescriptors(){
    	assertEquals(Uris.getDescriptor(Instructions.CONTENT_URI), Uris.INSTRUCTION_DIR);
    	uri = ContentUris.withAppendedId(Instructions.CONTENT_URI, 1);
    	assertEquals(Uris.getDescriptor(uri), Uris.INSTRUCTION_ITEM);
    	uri = Uris.withAppendedUuid(Instructions.CONTENT_URI, uuid.toString());
    	assertEquals(Uris.getDescriptor(uri), Uris.INSTRUCTION_UUID);
	}
	
	public void testNotificationDescriptors(){
    	assertEquals(Uris.getDescriptor(Notifications.CONTENT_URI), Uris.NOTIFICATION_DIR);
    	uri = ContentUris.withAppendedId(Notifications.CONTENT_URI, 1);
    	assertEquals(Uris.getDescriptor(uri), Uris.NOTIFICATION_ITEM);
    	uri = Uris.withAppendedUuid(Notifications.CONTENT_URI, uuid.toString());
    	assertEquals(Uris.getDescriptor(uri), Uris.NOTIFICATION_UUID);
	}
	
	public void testObservationDescriptors(){
    	assertEquals(Uris.getDescriptor(Observations.CONTENT_URI), Uris.OBSERVATION_DIR);
    	uri = ContentUris.withAppendedId(Observations.CONTENT_URI, 1);
    	assertEquals(Uris.getDescriptor(uri), Uris.OBSERVATION_ITEM);
    	uri = Uris.withAppendedUuid(Observations.CONTENT_URI, uuid.toString());
    	assertEquals(Uris.getDescriptor(uri), Uris.OBSERVATION_UUID);
	}
	
	public void testObserverDescriptors(){
    	assertEquals(Uris.getDescriptor(Observers.CONTENT_URI), Uris.OBSERVER_DIR);
    	uri = ContentUris.withAppendedId(Observers.CONTENT_URI, 1);
    	assertEquals(Uris.getDescriptor(uri), Uris.OBSERVER_ITEM);
    	uri = Uris.withAppendedUuid(Observers.CONTENT_URI, uuid.toString());
    	assertEquals(Uris.getDescriptor(uri), Uris.OBSERVER_UUID);
	}
	
	public void testProcedureDescriptors(){
    	assertEquals(Uris.getDescriptor(Procedures.CONTENT_URI), Uris.PROCEDURE_DIR);
    	uri = ContentUris.withAppendedId(Procedures.CONTENT_URI, 1);
    	assertEquals(Uris.getDescriptor(uri), Uris.PROCEDURE_ITEM);
    	uri = Uris.withAppendedUuid(Procedures.CONTENT_URI, uuid.toString());
    	assertEquals(Uris.getDescriptor(uri), Uris.PROCEDURE_UUID);
	}
	
	public void testSubjectDescriptors(){
    	assertEquals(Uris.getDescriptor(Subjects.CONTENT_URI), Uris.SUBJECT_DIR);
    	uri = ContentUris.withAppendedId(Subjects.CONTENT_URI, 1);
    	assertEquals(Uris.getDescriptor(uri), Uris.SUBJECT_ITEM);
    	uri = Uris.withAppendedUuid(Subjects.CONTENT_URI, uuid.toString());
    	assertEquals(Uris.getDescriptor(uri), Uris.SUBJECT_UUID);
	}
	
	public void testWithAppendedUuid(){
		String uriString = Observers.CONTENT_URI.toString() +  "/" + uuid.toString();
		Log.i("UrisTest", "str " + uriString);
		Uri uri = Uris.withAppendedUuid(Observers.CONTENT_URI, uuid.toString());
		Log.i("UrisTest", "uri " + uri.toString());
		assertTrue(uri.compareTo(Uri.parse(uriString)) == 0);
	}
}
