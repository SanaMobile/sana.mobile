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
package org.sana.android.net;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

import android.os.AsyncTask;
import android.util.Log;

/**
 * @author Sana Development
 * @param <T>
 *
 */
public class HttpTask extends AsyncTask<HttpUriRequest,Integer,MDSResult>{
	public static final String TAG = HttpTask.class.getSimpleName();
	
	public static final int FAIL = 0;
	public static final int SUCCEED = 1;
	public static final int NO_SERVICE = 2;
	
	NetworkTaskListener<MDSResult> listener = null;
	
	public HttpTask(NetworkTaskListener<MDSResult> listener){
		this.listener = listener;
	}
	
	public void setListener(NetworkTaskListener<MDSResult> listener){
		this.listener = listener;
	}
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected MDSResult doInBackground(HttpUriRequest... params) {
		HttpUriRequest method = params[0];
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 1000);
		HttpConnectionParams.setSoTimeout(httpParams, 1000);
		HttpClient client = new DefaultHttpClient(httpParams);
		MDSResult response = MDSResult.NOSERVICE;
		HttpResponse httpResponse = null;
		String responseString = null;
		try {
			Log.i(TAG, "About to execute request...");
			httpResponse = client.execute(method);
			responseString = EntityUtils.toString(httpResponse.getEntity());
			Log.i(TAG, "Received from MDS:" + responseString.length()+" chars");
			Log.d(TAG, "    " + responseString);
			Gson gson = new Gson();
			response = gson.fromJson(responseString, MDSResult.class);
		} catch (ClientProtocolException e) {
			Log.e(TAG, "ClientProtocolException: " + e.getMessage());
			e.printStackTrace();
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
		return response;
	}
	
	protected void onPostExecute(MDSResult result){
		if(listener != null)
			listener.onTaskComplete(result);
	}
}
