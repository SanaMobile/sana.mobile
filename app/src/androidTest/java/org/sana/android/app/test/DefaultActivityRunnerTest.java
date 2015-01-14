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
package org.sana.android.app.test;

import java.util.UUID;

import org.sana.android.app.DefaultActivityRunner;
import org.sana.android.content.Intents;
import org.sana.android.content.Uris;
import org.sana.android.provider.Observers;
import org.sana.android.provider.Subjects;

import android.content.Intent;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * @author Sana Development
 *
 */
public class DefaultActivityRunnerTest extends AndroidTestCase {

	DefaultActivityRunner runner;
	Intent request;
	Intent response = null;
	String uuid = null;
	
	@Override
	protected void setUp() throws Exception{
		super.setUp();
		request = new Intent(Intent.ACTION_MAIN);
		runner = new DefaultActivityRunner(request);
		response = null;
		uuid = UUID.randomUUID().toString();
	}
	
	private void assertEqualActionAndItemType(Intent response, String action, Uri uri){
		assertEquals(action,response.getAction());
		assertEquals(Uris.getDescriptor(uri), Uris.getDescriptor(response.getData()));
	}
	
	public void testDefaultConstructor(){
		runner = new DefaultActivityRunner();		
	}
	
	public void testNextActionIsPickObserver(){
		Intent next = runner.next(null);
		assertEqualActionAndItemType(next, Intent.ACTION_PICK, Observers.CONTENT_URI);
	}

	public void testNextActionPickObserverThenCancel(){
		// new runner initialized to MAIN should go into observer pick on null
		runner.next(null);
		// Cancel back from the observer pick should be the the back button
		// from the auth screen which should finish
		assertEquals(runner.next(null).getAction(), Intents.ACTION_FINISH);
	}
	
	public void testNextActionIsPickObserverThenOk(){
		Intent next = runner.next(null);	
		response = new Intent();
		response.setDataAndType(Uris.withAppendedUuid(Observers.CONTENT_URI, uuid), 
				Observers.CONTENT_ITEM_TYPE);
		next = runner.next(response);
		assertEquals(Intents.ACTION_PICK_ACTIVITY, next.getAction());	
	}
	
	
	
	public void testNextActionIsPickSubjectThenCancel(){
		Intent next = runner.next(null);	
		response = new Intent();
		response.setDataAndType(Uris.withAppendedUuid(Observers.CONTENT_URI, uuid), 
				Observers.CONTENT_ITEM_TYPE);
		next = runner.next(response);
		// at pick activity response pick subject
		response.setAction(Intent.ACTION_PICK);
		response.setDataAndType(Subjects.CONTENT_URI, Subjects.CONTENT_TYPE);
		next = runner.next(response);
		assertEqualActionAndItemType(next, Intent.ACTION_PICK, Subjects.CONTENT_URI);
		next = runner.next(null);
		assertEquals(Intents.ACTION_PICK_ACTIVITY, next.getAction());	
	}

	public void testNextActionIsPickSubjectThenOk(){
		Intent next = runner.next(null);	
		response = new Intent();
		response.setDataAndType(Uris.withAppendedUuid(Observers.CONTENT_URI, uuid), 
				Observers.CONTENT_ITEM_TYPE);
		next = runner.next(response);
		
		// at pick activity response pick subject
		response.setAction(Intent.ACTION_PICK);
		response.setDataAndType(Subjects.CONTENT_URI, Subjects.CONTENT_TYPE);
		next = runner.next(response);
		assertEqualActionAndItemType(next, Intent.ACTION_PICK, Subjects.CONTENT_URI);
		
		// mimic that we received subject data
		response = new Intent();
		response.setDataAndType(Uris.withAppendedUuid(Subjects.CONTENT_URI, uuid), 
				Subjects.CONTENT_ITEM_TYPE);
		// Should fall back to activity picker
		next = runner.next(response);
		assertEquals(Intents.ACTION_PICK_ACTIVITY, next.getAction());
	}
	
	public void testNextActionIsPickProcedure(){
		Intent next = runner.next(response);
		
	}
	
	public void testNextActionIsPickActivity(){
		Intent next = runner.next(response);
		
	}
	
	public void testNextActionIsIntentWithAction(){
		Intent next = runner.next(response);
		
	}
	
}
