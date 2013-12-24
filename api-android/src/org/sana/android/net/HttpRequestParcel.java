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

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.RequestLine;
import org.apache.http.client.methods.HttpUriRequest;
import org.w3c.dom.Entity;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Sana Development
 *
 */
public class HttpRequestParcel implements Parcelable {

	String uri;
	String method;
	Bundle headers;
	Bundle params;
	String entity;
	Bundle files;
	
	
	
	public HttpRequestParcel(HttpUriRequest request){
		RequestLine line = request.getRequestLine();
		uri = line.getUri();
		method = line.getMethod();
		headers = new Bundle();
		for(Header header:request.getAllHeaders()){
			headers.putString(header.getName(), header.getValue());
		}
		params = new Bundle();
		entity = null;
		files = new Bundle();
	}
			
	public HttpRequestParcel(Parcel in){
		uri = in.readString();
		method = in.readString();
		headers = in.readBundle();
		params = in.readBundle();
		entity = in.readString();
		files = in.readBundle();
	}
	
	
	/* (non-Javadoc)
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(uri);
		dest.writeString(method);
		dest.writeBundle(headers);
		dest.writeBundle(params);
		dest.writeString(entity);
		dest.writeBundle(files);

	}
	
	public static final Creator<HttpRequestParcel> CREATOR = 
			new Creator<HttpRequestParcel>(){

				@Override
				public HttpRequestParcel createFromParcel(Parcel source) {
					return new HttpRequestParcel(source);
				}

				@Override
				public HttpRequestParcel[] newArray(int size) {
					return new HttpRequestParcel[size];
				}
		
	};

}
