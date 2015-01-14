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
package org.sana.android.task;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.sana.R;
import org.sana.net.Response;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

/**
 * @author Sana Development
 *
 */
public class NetHandler<T> extends Handler {
	public static final String TAG = NetHandler.class.getSimpleName();
	
	public static final String HEADERS = "headers";
	public static final String PARAMS = "params";
	public static final String FILES = "files";
	public static final String URL = "url";
		
	int timeout = 0;
	
	@Override
	public void handleMessage(Message msg){
		HttpUriRequest method = null;
		Intent request = (Intent) msg.obj;
		Uri u = request.getData();
		try {
			URI uri = URIUtils.createURI(u.getScheme(), 
					u.getHost(), 
					u.getPort(),
					u.getPath(), 
					u.getQuery(),
					u.getFragment());
		
		//data.getBundle(key);
		HttpClient client = new DefaultHttpClient();
		HttpParams httpParams = client.getParams();
		if(timeout > 0){
			HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
			HttpConnectionParams.setSoTimeout(httpParams, timeout);
		}
		
		HttpResponse httpResponse = null;
		String responseString = null;
		Response<T> response = new Response<T>();
			Log.i(TAG, "doInBackground(): About to execute request...");
			httpResponse = client.execute(method);
			responseString = EntityUtils.toString(httpResponse.getEntity());
			Log.d(TAG, String.format("Response. \n...code: %d\n...chars: %d",
					httpResponse.getStatusLine().getStatusCode(),
					responseString.length()));
			//Log.d(TAG, "Received from MDS:" + responseString);
			
			Type type = new TypeToken<Response<T>>(){}.getType();
			response = new Gson().fromJson(responseString, type);
		} catch (ClientProtocolException e) {
			Log.e(TAG, "ClientProtocolException: " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
			e.printStackTrace();
		} catch (ParseException e) {
			Log.e(TAG, "ParseException: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e){
			Log.e(TAG, "Exception: " + e.getMessage());
			e.printStackTrace();
		}
		//return response;
	}
}
