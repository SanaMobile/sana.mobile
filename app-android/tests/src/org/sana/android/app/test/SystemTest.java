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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.sana.android.content.Intents;
import org.sana.android.provider.Concepts;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Events;
import org.sana.android.provider.Instructions;
import org.sana.android.provider.Notifications;
import org.sana.android.provider.Observations;
import org.sana.android.provider.Observers;
import org.sana.android.provider.Patients;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Subjects;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.test.AndroidTestCase;

/**
 * @author Sana Development
 *
 */
public class SystemTest extends AndroidTestCase {

	static final String[] sCrud = new String[]{
		Intent.ACTION_INSERT,
		Intent.ACTION_INSERT_OR_EDIT,
		Intent.ACTION_VIEW,
		Intent.ACTION_EDIT,
		Intent.ACTION_DELETE,
		Intent.ACTION_PICK,
	};
	
	static final String[] sActions = new String[]{
		Intent.ACTION_MAIN,
		Intent.ACTION_RUN,
		Intent.ACTION_PICK_ACTIVITY,
	};
	
	static Uri[] sModels = new Uri[]{
		Concepts.CONTENT_URI,
		Encounters.CONTENT_URI,
		Events.CONTENT_URI,
		Instructions.CONTENT_URI,
		Notifications.CONTENT_URI,
		Observations.CONTENT_URI,
		Observers.CONTENT_URI,
		Procedures.CONTENT_URI,
		Subjects.CONTENT_URI,
		Patients.CONTENT_URI
		
	};
	
	
	public void testWriteIntents(){
		/*
		 * 		assertEquals(Intents.parseActionDescriptor(new Intent(Intent.ACTION_MAIN)), Intents.MAIN);
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
		 */
		PrintWriter pw;
		BufferedOutputStream os;
		File ext = new File(Environment.getExternalStorageDirectory(),"intents.txt");
		try {
			pw = new PrintWriter(ext);
			for(String action:sActions){
				pw.println(new Intent(action).toUri(Intent.URI_INTENT_SCHEME));
			}
			for(Uri uri:sModels){
				Intent intent = new Intent();
				intent.setData(uri);
				for(String crud:sCrud){
					intent.setAction(crud);
					pw.println(intent.toUri(Intent.URI_INTENT_SCHEME));
				}
			}
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
}
