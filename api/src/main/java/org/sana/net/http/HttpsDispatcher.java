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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.ContentHandler;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestFactory;
import org.apache.http.HttpResponse;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.RequestLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;
import org.apache.http.util.EntityUtils;

/**
 * @author Sana Development
 *
 */
public class HttpsDispatcher{

	URL url;
	
	public HttpsDispatcher(URL url){
		this.url = url;
	}
	
	private HttpClient client = new DefaultHttpClient();
	private HttpHost host;
	private SyncBasicHttpContext context;
	private HttpRequestFactory factory;
	
	HttpsDispatcher(HttpHost host, HttpContext context){
		this.context = new SyncBasicHttpContext(context);
		this.host = new HttpHost(host);
		this.factory = new HttpRequestFactory(){

			@Override
			public HttpRequest newHttpRequest(RequestLine requestLine)
					throws MethodNotSupportedException {
				String method = requestLine.getMethod();
				String uri = requestLine.getUri();
				return this.newHttpRequest(method, uri);
			}

			@Override
			public HttpRequest newHttpRequest(String method, String uri)
					throws MethodNotSupportedException {
				HttpUriRequest request = null;
				if(method.equals(HttpGet.METHOD_NAME)){
					request = new HttpGet();
				}else if(method.equals(HttpPost.METHOD_NAME)){
					request = new HttpPost();
				}else if(method.equals(HttpPut.METHOD_NAME)){
					request = new HttpPut();
				}else if(method.equals(HttpPut.METHOD_NAME)){
					request = new HttpDelete();
				}
				return request;
			}
		};
		
	}
	
	public static HttpsDispatcher getInstance(String uri, Credentials credentials){
		
		HttpsDispatcher dispatcher = new HttpsDispatcher(new HttpHost(uri), 
				new BasicHttpContext());
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		AuthScope authScope = new AuthScope(dispatcher.host.getHostName(), dispatcher.host.getPort());
		credsProvider.setCredentials(authScope,credentials);
		((AbstractHttpClient) dispatcher.client).getCredentialsProvider().setCredentials(
		        authScope, credentials);
		return dispatcher;
	}
	
	static class HttpTask<T> extends Thread{
		
		private final HttpClient client;
	    private final HttpContext context;
	    private final HttpUriRequest request;
	    private final ResponseHandler<T> handler;
	    
	    public HttpTask(HttpClient httpClient, HttpUriRequest request, ResponseHandler<T> handler) {
	        client = httpClient;
	        this.context = new BasicHttpContext();
	        this.request = request;
	        this.handler = handler;
	    }
	    
		@Override
		public void run() {
			try{
				HttpResponse httpResponse = client.execute(request,context);
				HttpEntity entity = httpResponse.getEntity();
				if(entity != null)
					handler.handleResponse(httpResponse);
				EntityUtils.consume(entity);
			} catch(Exception e){
				request.abort();
			}
		}
	}
}
