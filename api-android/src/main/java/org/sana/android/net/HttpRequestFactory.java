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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.message.BasicNameValuePair;

/**
 * Factory class which generates Http requests that can be sent to the 
 * dispatch server.
 * 
 * @author Sana Development
 *
 */
public final class HttpRequestFactory {
	public static final String TAG = HttpRequestFactory.class.getSimpleName();

	static final String SCHEME = "http";

	/**
	 * 
	 * @param host
	 * @param port
	 * @param path
	 * @param postData
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws URISyntaxException
	 */
	public static HttpPost getPostRequest(String host, int port, String path, 
		List<NameValuePair> postData) throws IllegalArgumentException
	{
		return getPostRequest(SCHEME, host,port, path, postData);
	}

	/**
	 * 
	 * @param scheme
	 * @param host
	 * @param port
	 * @param path
	 * @param postData
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static HttpPost getPostRequest(String scheme, String host, int port,
			String path, List<NameValuePair> postData) throws IllegalArgumentException
	{
		try{
			URI uri = URIUtils.createURI(scheme, host, port, path,null,null);
			HttpPost post = new HttpPost(uri);
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postData, "UTF-8");
			post.setEntity(entity);
			return post;
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public static HttpPost getPostRequest(URI uri, List<NameValuePair> postData) throws IllegalArgumentException
	{
		try{
			HttpPost post = new HttpPost(uri);
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postData, "UTF-8");
			post.setEntity(entity);
			return post;
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * 
	 * @param host
	 * @param port
	 * @param path
	 * @param queryParams
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static HttpGet getHttpGetRequest(String host, int port, 
			String path,  List<NameValuePair> queryParams, Header header)
	{
		return 	HttpRequestFactory.getHttpGetRequest(SCHEME,  host, port, path, queryParams, header);
	}

	/**
	 * 
	 * @param scheme
	 * @param host
	 * @param port
	 * @param path
	 * @param queryParams
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static HttpGet getHttpGetRequest(String scheme, String host, int port, 
			String path,  List<NameValuePair> queryParams, Header header)
	{
		try{
			URI uri = URIUtils.createURI(scheme, host, port, path,
					URLEncodedUtils.format(queryParams, "UTF-8"), null);
			HttpGet get = new HttpGet(uri);
			get.addHeader(header);
			return get;
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static HttpGet getScheduledEncounters(String scheme, String host, int port, 
			String username, String password) throws IllegalArgumentException
	{
		List<NameValuePair> qParams = new ArrayList<NameValuePair>();
		qParams.add(new BasicNameValuePair("username", username));
		HttpGet httpGet = getHttpGetRequest(SCHEME, host, port, "mds/scheduling/encounters/",
				qParams,
				BasicScheme.authenticate(
				new UsernamePasswordCredentials(username, password),
				"UTF-8", false));
		return httpGet;
	}
}
