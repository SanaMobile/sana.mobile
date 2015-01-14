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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.Authenticator;
import java.net.ContentHandler;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestFactory;
import org.apache.http.HttpResponse;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.RequestLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.sana.net.Response;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * @author Sana Development
 *
 */
public class HttpDispatcher{

	public static class JSONHandler<T> extends ContentHandler implements ResponseHandler<T>{

		/* (non-Javadoc)
		 * @see java.net.ContentHandler#getContent(java.net.URLConnection)
		 */
		@Override
		public T getContent(URLConnection connection) throws IOException {
			
			StringBuilder response  = new StringBuilder();
			String contentType = connection.getHeaderField("Content-Type");
			
			String charset = null;
			for (String param : contentType.replace(" ", "").split(";")) {
			    if (param.startsWith("charset=")) {
			        charset = param.split("=", 2)[1];
			        break;
			    }
			}

			if (charset != null) {
			    BufferedReader reader = null;
			    try {
			        reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), charset));
			        for (String line; (line = reader.readLine()) != null;) {
			            response.append(line);
			        }
			    } finally {
			        if (reader != null) try { reader.close(); } catch (IOException logOrIgnore) {}
			    }
			} else {
			    // It's likely binary content, use InputStream/OutputStream.
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.apache.http.client.ResponseHandler#handleResponse(org.apache.http.HttpResponse)
		 */
		@Override
		public T handleResponse(HttpResponse response)
				throws ClientProtocolException, IOException {
			
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(response.getEntity());
			EntityUtils.consume(response.getEntity());
			
			Type type = new TypeToken<Response<T>>(){}.getType();
			T t = new Gson().fromJson(responseString, type);
			return t;
		}
		
	}
	
	public static class FileHandler extends ContentHandler implements ResponseHandler<URI>{
		
		String uri;
		
		public FileHandler(String uri){
			this.uri = uri;
		}
		
		/* (non-Javadoc)
		 * @see java.net.ContentHandler#getContent(java.net.URLConnection)
		 */
		@Override
		public Object getContent(URLConnection connection) throws IOException {
			String contentType = connection.getHeaderField("Content-Type");
			
			String charset = null;
			for (String param : contentType.replace(" ", "").split(";")) {
			    if (param.startsWith("charset=")) {
			        charset = param.split("=", 2)[1];
			        break;
			    }
			}

            int bytesRead = -1;
            byte[] buffer = new byte[1024];
            
			InputStream in = connection.getInputStream();
			FileOutputStream out = new FileOutputStream(uri);
			try{
				while ((bytesRead = in.read(buffer)) != -1) {
	                out.write(buffer, 0, bytesRead);
	            }
			} finally {
				in.close();
				out.close();
			}
			return URI.create(uri);
		}

		/* (non-Javadoc)
		 * @see org.apache.http.client.ResponseHandler#handleResponse(org.apache.http.HttpResponse)
		 */
		@Override
		public URI handleResponse(HttpResponse response)
				throws ClientProtocolException, IOException {
			
			return null;
		}
		
	}
	
	public static class StringHandler implements ResponseHandler<String>{

		/* (non-Javadoc)
		 * @see org.apache.http.client.ResponseHandler#handleResponse(org.apache.http.HttpResponse)
		 */
		@Override
		public String handleResponse(HttpResponse response)
				throws ClientProtocolException, IOException {
			
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(response.getEntity());
			EntityUtils.consume(response.getEntity());
			return responseString;
		}
		
	}
	
	private DefaultHttpClient client;
	private HttpHost host;
	private SyncBasicHttpContext context;
	
	public HttpDispatcher(String host){
		this(new HttpHost(host));
	}
	
	HttpDispatcher(HttpHost host){
		this(host, new BasicHttpContext());
	}
	
	HttpDispatcher(HttpHost host, HttpContext context){
		this.context = new SyncBasicHttpContext(context);
		this.host = new HttpHost(host);
		
	}
	
	public static HttpDispatcher getInstance(String uri, Credentials credentials){
		
		HttpDispatcher dispatcher = new HttpDispatcher(new HttpHost(uri), 
				new BasicHttpContext());
		
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		
		AuthScope authScope = new AuthScope(
				dispatcher.host.getHostName(), 
				dispatcher.host.getPort());
		
		credsProvider.setCredentials(authScope,credentials);
		
		((DefaultHttpClient) dispatcher.client).getCredentialsProvider().setCredentials(
		        authScope, credentials);
		return dispatcher;
	}
	
	public static HttpDispatcher getInstance(String host, int port, String scheme){
		HttpDispatcher newInstance =  new HttpDispatcher(
				new HttpHost(host,port,scheme));
		return newInstance;
	}

	public static <T> T read(String url, ResponseHandler<T> handler){
		try {
			URL uri = new URL(url);
			HttpDispatcher dispatcher = getInstance(uri.getHost(), uri.getPort(), uri.getProtocol());
			HttpRequestFactory factory = new DispatchRequestFactory();
			HttpUriRequest request;
			request = (HttpUriRequest) factory.newHttpRequest(HttpGet.METHOD_NAME, url);
			return dispatcher.execute(request, handler);
		} catch (MethodNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String read(String url){
		return read(url, new StringHandler());
	}
	
	/**
	 * 
	 * @param request
	 * @param handler
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public <T> T execute(HttpUriRequest request, ResponseHandler<T> handler) throws ClientProtocolException, IOException{
		return handler.handleResponse(client.execute(request));
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public String execute(HttpUriRequest request) throws ClientProtocolException, IOException{
		String response = this.execute(request, new StringHandler());
		return response;
	}
	
}
