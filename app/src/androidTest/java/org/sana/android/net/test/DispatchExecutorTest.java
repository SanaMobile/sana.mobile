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
package org.sana.android.net.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.sana.net.http.HttpDispatcher;
import org.sana.net.http.HttpTaskFactory;

import android.test.AndroidTestCase;

/**
 * @author Sana Development
 *
 */
public class DispatchExecutorTest extends AndroidTestCase {

	public static final String TAG = DispatchExecutorTest.class.getSimpleName();
	
	
	public void testGet(){
		String uri = "https://demo.sana.csail.mit.edu/mds";
		String result = HttpDispatcher.read(uri);
		//Log.i(TAG, result.toString());
	}
	
	public void testPost(){
		String uri = "https://demo.sana.csail.mit.edu/mds/json/validate/credentials";
		List<NameValuePair> forms = new ArrayList<NameValuePair>();
		forms.add(new BasicNameValuePair("username", "admin"));
		forms.add(new BasicNameValuePair("password", "Sanamobile1"));
		
		
		DefaultHttpClient client = (DefaultHttpClient) HttpTaskFactory.CLIENT_FACTORY.produce();
		
		//MDSResult result = DispatchExecutor.executePost(uri,forms);
		//Log.i(TAG, result.toString());
		
	}
	
	
}
