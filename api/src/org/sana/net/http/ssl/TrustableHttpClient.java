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
package org.sana.net.http.ssl;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpProcessor;

/**
 * @author Sana Development
 *
 */
public class TrustableHttpClient extends DefaultHttpClient{

	SchemeRegistry registry;
	
	KeyStore trusted;
	
	/**
	 * @param conman
	 * @param params
	 */
	public TrustableHttpClient(HttpParams params) 
	{
		super(params);
	}	

	/**
	 * 
	 */
	public TrustableHttpClient() 
	{
		super();
	}
	
	public void register(Scheme scheme){
		registry.register(scheme);
	}
	
	@SuppressWarnings({ "deprecation", "deprecation" })
	@Override
	protected ClientConnectionManager createClientConnectionManager() {
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		// Register for port 443 our SSLSocketFactory with our keystore
		// to the ConnectionManager
		try {
			registry.register(new Scheme("https", newSslSocketFactory(), 443));
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		return new SingleClientConnManager(getParams(), registry);
	}
	
	protected SSLSocketFactory newSslSocketFactory() throws 
		KeyManagementException, 
		UnrecoverableKeyException, 
		NoSuchAlgorithmException, 
		KeyStoreException 
	{
		// Pass the keystore to the SSLSocketFactory. The factory is responsible
		// for the verification of the server certificate.
		SSLSocketFactory sf = new SSLSocketFactory(trusted);
		// Hostname verification from certificate
		// http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e506
		sf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
		return sf;
	}
}
