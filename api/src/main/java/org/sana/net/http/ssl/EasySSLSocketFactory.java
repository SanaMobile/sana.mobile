package org.sana.net.http.ssl;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSchemeSocketFactory;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * This socket factory will create ssl socket that accepts self signed
 * certificate
 * 
 * @author olamy
 * @version $Id: EasySSLSocketFactory.java 765355 2009-04-15 20:59:07Z evenisse
 *          $
 * @since 1.2.3
 */
public class EasySSLSocketFactory implements SocketFactory,
		LayeredSocketFactory {

	private SSLContext sslcontext = null;
	private final TrustManager trustManager;
	
	private static TrustManager TRUST_ALL = new X509TrustManager() {

		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			// do nothing
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			// do nothing
		}

	};
	
	private static SSLContext createEasySSLContext(TrustManager manager) throws IOException {
		try {
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new TrustManager[] { manager }, null);
			return context;
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	// Returns a context that will trust all certs
    private static SSLContext createEasySSLContext() throws IOException {
        try {
                SSLContext context = SSLContext.getInstance("TLS");
                
                // Create a trust manager that does not validate certificate chains     
                context.init(null, new TrustManager[]{ new EasyX509TrustManager(null) } , new SecureRandom ());
                return context;
        } catch (Exception e) {
                throw new IOException(e.getMessage());
        }
    }
	
	private SSLContext getSSLContext() throws IOException {
		if (this.sslcontext == null) {
			if(trustManager == null)
				this.sslcontext = createEasySSLContext();
			else
				this.sslcontext = createEasySSLContext(trustManager);
		}
		return this.sslcontext;
	}
	
	
	public EasySSLSocketFactory(){
		this(null);
	}
	
	public EasySSLSocketFactory(TrustManager trustManager){
		this.trustManager = trustManager;
	}	
	/**
	 * @see org.apache.http.conn.scheme.SocketFactory#connectSocket(java.net.Socket,
	 *      java.lang.String, int, java.net.InetAddress, int,
	 *      org.apache.http.params.HttpParams)
	 */
	public Socket connectSocket(Socket sock, String host, int port,
			InetAddress localAddress, int localPort, HttpParams params)
			throws IOException, UnknownHostException, ConnectTimeoutException {
		int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
		int soTimeout = HttpConnectionParams.getSoTimeout(params);

		InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
		SSLSocket sslsock = (SSLSocket) ((sock != null) ? sock : createSocket());

		if ((localAddress != null) || (localPort > 0)) {
			// we need to bind explicitly
			if (localPort < 0) {
				localPort = 0; // indicates "any"
			}
			InetSocketAddress isa = new InetSocketAddress(localAddress,
					localPort);
			sslsock.bind(isa);
		}

		sslsock.connect(remoteAddress, connTimeout);
		sslsock.setSoTimeout(soTimeout);
		return sslsock;

	}

	/**
	 * @see org.apache.http.conn.scheme.SocketFactory#createSocket()
	 */
	public Socket createSocket() throws IOException {
		return getSSLContext().getSocketFactory().createSocket();
	}

	/**
	 * @see org.apache.http.conn.scheme.SocketFactory#isSecure(java.net.Socket)
	 */
	public boolean isSecure(Socket socket) throws IllegalArgumentException {
		return true;
	}

	/**
	 * @see org.apache.http.conn.scheme.LayeredSocketFactory#createSocket(java.net.Socket,
	 *      java.lang.String, int, boolean)
	 */
	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException, UnknownHostException {
		return getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
	}

	// -------------------------------------------------------------------
	// javadoc in org.apache.http.conn.scheme.SocketFactory says :
	// Both Object.equals() and Object.hashCode() must be overridden
	// for the correct operation of some connection managers
	// -------------------------------------------------------------------

	public boolean equals(Object obj) {
		return ((obj != null) && obj.getClass().equals(
				EasySSLSocketFactory.class));
	}

	public int hashCode() {
		return EasySSLSocketFactory.class.hashCode();
	}

	/* (non-Javadoc)
	 * @see org.apache.http.conn.scheme.SchemeSocketFactory#connectSocket(java.net.Socket, java.net.InetSocketAddress, java.net.InetSocketAddress, org.apache.http.params.HttpParams)
	 */
	//@Override
	public Socket connectSocket(Socket arg0, InetSocketAddress arg1,
			InetSocketAddress arg2, HttpParams arg3) throws IOException,
			UnknownHostException, ConnectTimeoutException {
		return connectSocket(arg0, arg1.getHostName(), arg1.getPort(), arg2.getAddress(), arg2.getPort(), arg3);
	}

	/* (non-Javadoc)
	 * @see org.apache.http.conn.scheme.SchemeSocketFactory#createSocket(org.apache.http.params.HttpParams)
	 */
	//@Override
	public Socket createSocket(HttpParams arg0) throws IOException {
		return createSocket();
	}

	/* (non-Javadoc)
	 * @see org.apache.http.conn.scheme.LayeredSchemeSocketFactory#createLayeredSocket(java.net.Socket, java.lang.String, int, boolean)
	 */
	//@Override
	public Socket createLayeredSocket(Socket arg0, String arg1, int arg2,
			boolean arg3) throws IOException, UnknownHostException {
		return this.createSocket(arg0,arg1,arg2,arg3);
	}

}
