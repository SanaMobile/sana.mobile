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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.sana.net.Response;
import org.sana.net.http.HttpTaskFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.ResultReceiver;
import android.util.Log;

/**
 * @author Sana Development
 * @param <T>
 *
 */
public class HttpTask<T> extends AsyncTask<HttpUriRequest,Integer, Response<T>>{
	public static final String TAG = HttpTask.class.getSimpleName();
	
	public static final int FAIL = 0;
	public static final int SUCCEED = 1;
	public static final int NO_SERVICE = 2;
	
	private final int timeout;
	
	NetworkTaskListener<Response<T>> listener = null;
	Messenger handler = null;
	Message message = null;
	public HttpTask(){
		this(null,-1);
	}
	
	public HttpTask(NetworkTaskListener<Response<T>> listener){
		this(listener, -1);
	}
	
	public HttpTask(NetworkTaskListener<Response<T>> listener, int timeout){
		this.listener = listener;
		this.timeout = timeout;
	}
	
	public void setListener(NetworkTaskListener<Response<T>> listener){
		this.listener = listener;
	}
	
	public void setHandler(Messenger messenger){
		this.handler = messenger;
	}
	
	public void setReplyTo(Message message){
		this.message = Message.obtain(message);
	}
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Response<T> doInBackground(HttpUriRequest... params) {
		HttpUriRequest method = params[0];
		HttpClient client = HttpTaskFactory.CLIENT_FACTORY.produce();
		HttpParams httpParams = client.getParams();
		if(timeout > 0){
			HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
			HttpConnectionParams.setSoTimeout(httpParams, timeout);
		}
		
		HttpResponse httpResponse = null;
		String responseString = null;
		Response<T> response = Response.empty();
		try {
			Log.i(TAG, "doInBackground(): About to execute request...");
			httpResponse = client.execute(method);
			responseString = EntityUtils.toString(httpResponse.getEntity());
			Log.d(TAG, String.format("Response. \n...code: %d\n...chars: %d",
					httpResponse.getStatusLine().getStatusCode(),
					responseString.length()));
			Log.d(TAG, "Received from MDS:" + responseString);
			if(message != null){
				message.obj = responseString;
				message.sendToTarget();
			} else {
				Type type = new TypeToken<Response<T>>(){}.getType();
				response = new Gson().fromJson(responseString, type);
			}
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
		return response;
	}
	
	
	protected void onPostExecute(Response<T> result){
		if(listener != null){
			listener.onTaskComplete(result);
		}
	}

}
