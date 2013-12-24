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

import java.io.File;

import org.sana.android.content.Intents;
import org.sana.android.provider.Subjects;

import android.content.Intent;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Tests utility methods of the {@link org.sana.android.content.Intents Intents}
 * class as well as those which handled matching the integer value to Intent
 * action and data mappings for Intents used in the application. 
 * 
 * @author Sana Development
 *
 */
public class IntentsTest extends AndroidTestCase {
	
	//-------------------------------------------------------------------------
	// Sana explicit activity action strings 
	//-------------------------------------------------------------------------
	static final String[] actions = new String[]{ 	
		Intents.ACTION_FINISH,
		Intents.ACTION_PICK_ACTIVITY,
		Intents.ACTION_PICK_PROCEDURE,
		Intents.ACTION_PICK_ENCOUNTER,
		Intents.ACTION_PICK_NOTIFICATION,
		Intents.ACTION_PICK_OBSERVER,
		Intents.ACTION_RUN_PROCEDURE,
		Intents.ACTION_RESUME_PROCEDURE,
		Intents.ACTION_SETTINGS,
		Intents.ACTION_INSERT_SUBJECT,
		Intents.ACTION_PICK_SUBJECT,
		Intents.ACTION_INSERT_SESSION,
		Intents.ACTION_VIEW_SESSION,
		Intents.ACTION_UPDATE_SESSION,
		Intents.ACTION_DELETE_SESSION,
		Intents.ACTION_MAIN }; 
	
	public void testParseActionDescriptor(){
		assertEquals(Intents.parseActionDescriptor(new Intent()), Intents.NULL);
		assertEquals(Intents.parseActionDescriptor(new Intent(Intent.ACTION_MAIN)), Intents.MAIN);
		assertEquals(Intents.parseActionDescriptor(new Intent(Intent.ACTION_RUN)), Intents.RUN);
		assertEquals(Intents.parseActionDescriptor(new Intent(Intent.ACTION_INSERT)), Intents.INSERT);
		assertEquals(Intents.parseActionDescriptor(new Intent(Intent.ACTION_INSERT_OR_EDIT)), Intents.INSERT_OR_EDIT);
		assertEquals(Intents.parseActionDescriptor(new Intent(Intent.ACTION_VIEW)), Intents.VIEW);
		assertEquals(Intents.parseActionDescriptor(new Intent(Intent.ACTION_EDIT)), Intents.EDIT);
		assertEquals(Intents.parseActionDescriptor(new Intent(Intent.ACTION_DELETE)), Intents.DELETE);
		assertEquals(Intents.parseActionDescriptor(new Intent(Intent.ACTION_PICK)), Intents.PICK);
		assertEquals(Intents.parseActionDescriptor(new Intent(Intent.ACTION_ATTACH_DATA)), Intents.ATTACH_DATA);
		assertEquals(Intents.parseActionDescriptor(new Intent(Intent.ACTION_CHOOSER)), Intents.CHOOSER);
		assertEquals(Intents.parseActionDescriptor(new Intent(Intent.ACTION_GET_CONTENT)), Intents.GET_CONTENT);
		assertEquals(Intents.parseActionDescriptor(new Intent(Intent.ACTION_DIAL)), Intents.DIAL);
		assertEquals(Intents.parseActionDescriptor(new Intent(Intent.ACTION_CALL)), Intents.CALL);
		assertEquals(Intents.parseActionDescriptor(new Intent(Intent.ACTION_SEND)), Intents.SEND);
		assertEquals(Intents.parseActionDescriptor(new Intent(Intent.ACTION_SENDTO)), Intents.SENDTO);
		assertEquals(Intents.parseActionDescriptor(new Intent(Intent.ACTION_ANSWER)), Intents.ANSWER);
		assertEquals(Intents.parseActionDescriptor(new Intent(Intent.ACTION_SYNC)), Intents.SYNC);
		assertEquals(Intents.parseActionDescriptor(new Intent(Intents.ACTION_PICK_ACTIVITY)), Intents.PICK_ACTIVITY);
		assertEquals(Intents.parseActionDescriptor(new Intent(Intent.ACTION_SEARCH)), Intents.SEARCH);
		assertEquals(Intents.parseActionDescriptor(new Intent(Intent.ACTION_WEB_SEARCH)), Intents.WEB_SEARCH);
	}
	
	/**
	 * Creates an identical intents and then checks that it is equivalent to an
	 * Intent generated by {@link org.sana.android.content.Intents#copyOf(Intent) copyOf(Intent)}. 
	 */
	public void testCopyOf(){
		// Non-exhaustive list of extras
		// We are willing to assume Intent.putExtras(Bundle) works for all if 
		// it works for two basic extra types
		
		// fake file to check extra parcel
		Uri stream = Uri.fromFile(new File("/mnt/sdcard/test.xml"));
		// text value to check string extra
		String val = "value";
		
		// create an Intent
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_DEFAULT)
			.setDataAndType(Subjects.CONTENT_URI, Subjects.CONTENT_TYPE)
			.putExtra(Intent.EXTRA_TEXT, val)
			.putExtra(Intent.EXTRA_STREAM, stream);
		
		// create a copy using copyOf(Intent)
		Intent copyIntent = Intents.copyOf(intent);
		
		// check equality
		assertEquals(intent.getAction(), copyIntent.getAction());
		assertEquals(intent.getData(), copyIntent.getData());
		assertEquals(intent.getType(), copyIntent.getType());
		assertEquals(intent.getExtras().getString(Intent.EXTRA_TEXT), copyIntent.getExtras().getString(Intent.EXTRA_TEXT));
		assertEquals(intent.getExtras().getParcelable(Intent.EXTRA_STREAM), copyIntent.getExtras().getParcelable(Intent.EXTRA_STREAM));
	}
	
	/**
	 * Checks return values for: 
	 * 	PICK,EDIT, INSERT, INSERT_OR_EDIT, GET_CONTENT, PICK_ACTIVITY
	 */
	public void testStartForResultIsTrue(){
		assertTrue(Intents.startForResult(new Intent(Intent.ACTION_PICK)));
		assertTrue(Intents.startForResult(new Intent(Intent.ACTION_EDIT)));
		assertTrue(Intents.startForResult(new Intent(Intent.ACTION_INSERT)));
		assertTrue(Intents.startForResult(new Intent(Intent.ACTION_INSERT_OR_EDIT)));
		assertTrue(Intents.startForResult(new Intent(Intent.ACTION_GET_CONTENT)));
		assertTrue(Intents.startForResult(new Intent(Intents.ACTION_PICK_ACTIVITY)));
	}


	
	/**
	 * Non-exhaustive false return value check for actions:
	 * empty String, VIEW, RUN, MAIN 
	 */
	public void testStartForResultIsFalse(){
		assertFalse(Intents.startForResult(new Intent()));
		assertFalse(Intents.startForResult(new Intent(Intent.ACTION_MAIN)));
		assertFalse(Intents.startForResult(new Intent(Intent.ACTION_VIEW)));
		assertFalse(Intents.startForResult(new Intent(Intent.ACTION_RUN)));
	}
	
	public void testReadFromFile(){
		
	}
	
}
