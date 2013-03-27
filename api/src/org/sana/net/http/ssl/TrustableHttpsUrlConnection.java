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

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManagerFactory;

/**
 * @author Sana Development
 *
 */
public class TrustableHttpsUrlConnection extends HttpsURLConnection{

	/**
	 * @param arg0
	 */
	protected TrustableHttpsUrlConnection(URL arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see javax.net.ssl.HttpsURLConnection#getCipherSuite()
	 */
	@Override
	public String getCipherSuite() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.net.ssl.HttpsURLConnection#getLocalCertificates()
	 */
	@Override
	public Certificate[] getLocalCertificates() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.net.ssl.HttpsURLConnection#getServerCertificates()
	 */
	@Override
	public Certificate[] getServerCertificates()
			throws SSLPeerUnverifiedException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see java.net.HttpURLConnection#disconnect()
	 */
	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.net.HttpURLConnection#usingProxy()
	 */
	@Override
	public boolean usingProxy() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see java.net.URLConnection#connect()
	 */
	@Override
	public void connect() throws IOException {
		// TODO Auto-generated method stub
		
	}

	public static TrustableHttpsUrlConnection getInstance(URL url, 
			KeyStore trusted, String algorithm) throws KeyStoreException, 
			NoSuchAlgorithmException, KeyManagementException, IOException
	{
		TrustableHttpsUrlConnection connection = null;
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
		tmf.init(trusted);

		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, tmf.getTrustManagers(), null);
		connection = (TrustableHttpsUrlConnection) url.openConnection();
		connection.setSSLSocketFactory(context.getSocketFactory());
		return connection;
	}
	
}
