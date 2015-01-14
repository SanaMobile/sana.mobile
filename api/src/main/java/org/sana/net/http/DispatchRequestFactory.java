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

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestFactory;
import org.apache.http.HttpVersion;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.NameValuePair;
import org.apache.http.RequestLine;
import org.apache.http.auth.Credentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicRequestLine;

/**
 * @author Sana Development
 *
 */
public class DispatchRequestFactory implements HttpRequestFactory{

	public static HttpPost generatePost(String url, List<NameValuePair> data){
		UrlEncodedFormEntity entity;
		try {
			entity = (data != null)? new UrlEncodedFormEntity(data, "UTF-8"): null;
			return generatePost(url,entity);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Encoding not supported: "
					+e.getMessage(),e);
		}
	}
	
	public static HttpPost generatePost(String url, HttpEntity entity){
		HttpPost post = new HttpPost(url);
		if(entity != null)
			post.setEntity(entity);
		return post;
	}
	
	
	public DispatchRequestFactory(String hostname, int port, String scheme){
		
	}
	
	public DispatchRequestFactory(){}
	
	
	/*
	 * (non-Javadoc)
	 * @see org.apache.http.HttpRequestFactory#newHttpRequest(org.apache.http.RequestLine)
	 */
	@Override
	public HttpRequest newHttpRequest(RequestLine requestLine)
			throws MethodNotSupportedException {
		String method = requestLine.getMethod();
		String uri = requestLine.getUri();
		return this.newHttpRequest(method, uri);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.http.HttpRequestFactory#newHttpRequest(java.lang.String, java.lang.String)
	 */
	@Override
	public HttpRequest newHttpRequest(String method, String uri)
			throws MethodNotSupportedException {
		RequestLine line = new BasicRequestLine(method, uri, HttpVersion.HTTP_1_1);
		
		HttpUriRequest request = null;
		if(method.equals(HttpGet.METHOD_NAME)){
			request = new HttpGet(uri);
		}else if(method.equals(HttpPost.METHOD_NAME)){
			request = new HttpPost(uri);
		}else if(method.equals(HttpPut.METHOD_NAME)){
			request = new HttpPut(uri);
		}else if(method.equals(HttpPut.METHOD_NAME)){
			request = new HttpDelete(uri);
		}
		return request;
	}
	
	
	
}
