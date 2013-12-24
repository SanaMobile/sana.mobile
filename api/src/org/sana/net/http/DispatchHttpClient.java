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
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.HttpParams;

public class DispatchHttpClient extends DefaultHttpClient{
	
	final File keystore;
	final String keypass;
	
	public DispatchHttpClient(String keystore, String keypass){
		this(new File(keystore),keypass);
	}
	
	public DispatchHttpClient(File keystore, String keypass){
		this.keystore = keystore;
		this.keypass = keypass;
	}
	
	public DispatchHttpClient(File keystore, String keypass, 
			ClientConnectionManager manager, HttpParams params){
		super(manager,params);
		this.keystore = keystore;
		this.keypass = keypass;
	}
	
	@Override protected ClientConnectionManager createClientConnectionManager() {
	    SchemeRegistry registry = new SchemeRegistry();
	    registry.register(
	        new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	    registry.register(new Scheme("https", newSslSocketFactory(), 443));
	    return new SingleClientConnManager(getParams(), registry);
	  }

	private SSLSocketFactory newSslSocketFactory() {
		try {
			KeyStore trusted = KeyStore.getInstance("BKS");
			InputStream in = new FileInputStream(keystore);
			try {
				trusted.load(in, keypass.toCharArray());
			} finally {
				in.close();
			}
			return new SSLSocketFactory(trusted);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
  }
}