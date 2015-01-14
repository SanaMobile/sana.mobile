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
package org.sana.net.http;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;

import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestFactory;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.RequestLine;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

/**
 * @author Sana Development
 *
 */
public class HttpRequestBuilder{
	
	
	/* Basic implementation of a REquestFactory */
	private static RequestFactory FACTORY = new RequestFactory(){
		@Override
		public HttpUriRequest produceCreate(URI uri, Map<String, ?> formData,
				Map<String, URI> files) throws UnsupportedEncodingException {
			HttpPost request = new HttpPost(uri);
			MultipartEntity entity = new MultipartEntity();
			if(files != null){
				for(String key:files.keySet()){
					URI value = files.get(key);
					entity.addPart(key, new FileBody(new File(value)));
				}
			}
			if(formData != null){
				for(String key:formData.keySet()){
					Object value = formData.get(key);
					entity.addPart(key, new StringBody(String.valueOf(value)));
				}
			}
			request.setEntity(entity);
			return request;
		}
		
		@Override
		public HttpUriRequest produceRead(URI uri) {
			return new HttpGet(uri);
		}
		@Override
		public HttpUriRequest produceUpdate(URI uri, Map<String, ?> formData,
				Map<String, URI> files) throws UnsupportedEncodingException {
			HttpPut request = new HttpPut(uri);
			MultipartEntity entity = new MultipartEntity();
			if(files != null){
				for(String key:files.keySet()){
					URI value = files.get(key);
					entity.addPart(key, new FileBody(new File(value)));
				}
			}
			if(formData != null){
				for(String key:formData.keySet()){
					Object value = formData.get(key);
					entity.addPart(key, new StringBody(String.valueOf(value)));
				}
			}
			request.setEntity(entity);
			return request;
		}
		@Override
		public HttpUriRequest produceDelete(URI uri){
			return new HttpDelete(uri);
		}
	};
	
	/**
	 * 
	 */
	private HttpRequestBuilder() {}

	public static RequestFactory getDefaultBuilder(){
		return FACTORY;
	}

	/* (non-Javadoc)
	 * @see org.apache.http.HttpRequestFactory#newHttpRequest(org.apache.http.RequestLine)
	 */
	public HttpRequest newHttpRequest(RequestLine requestLine)
			throws MethodNotSupportedException {
		String method = requestLine.getMethod();
		String uri = requestLine.getUri();
		return newHttpRequest(method, uri);
	}

	/* (non-Javadoc)
	 * @see org.apache.http.HttpRequestFactory#newHttpRequest(java.lang.String, java.lang.String)
	 */
	public HttpRequest newHttpRequest(String method, String uri)
			throws MethodNotSupportedException {
		HttpUriRequest request = null;
		URI host = URI.create(uri);
		if(method.equals(HttpGet.METHOD_NAME)){
			request = new HttpGet(host);
		}else if(method.equals(HttpPost.METHOD_NAME)){
			request = new HttpPost(host);
		}else if(method.equals(HttpPut.METHOD_NAME)){
			request = new HttpPut(host);
		}else if(method.equals(HttpPut.METHOD_NAME)){
			request = new HttpDelete(host);
		}
		return request;
	}
	
}
