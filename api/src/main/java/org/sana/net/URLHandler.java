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
package org.sana.net;

import java.io.IOException;
import java.net.ContentHandler;
import java.net.ContentHandlerFactory;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Map;

/**
 * @author Sana Development
 *
 */
public class URLHandler<T> extends URLStreamHandler{

	/**
	 * 
	 * @author Sana Development
	 *
	 */
	public static final class URLHandlerFactory implements ContentHandlerFactory, URLStreamHandlerFactory{

		Map<String, ContentHandler> contentHandlers;
		Map<String, URLStreamHandler> streamHandlers;
		
		private enum Protocol{
			FILE,
			HTTP,
			HTTPS,
			FTP,
			SMTP
			
		}
		
		private enum Method{
			CREATE,
			READ,
			UPDATE,
			DELETE
		}
		

		/* (non-Javadoc)
		 * @see java.net.URLStreamHandlerFactory#createURLStreamHandler(java.lang.String)
		 */
		@Override
		public URLStreamHandler createURLStreamHandler(String protocol) {
			// TODO Auto-generated method stub
			Protocol p = Protocol.valueOf(protocol.toUpperCase());
			switch(p){
			case FILE:
				break;
			case HTTP:
				
				break;
			case HTTPS:
				break;
			case FTP:
				break;
			case SMTP:
				break;
			default:
				throw new IllegalArgumentException();
			}
			
			return null;
		}

		/* (non-Javadoc)
		 * @see java.net.ContentHandlerFactory#createContentHandler(java.lang.String)
		 */
		@Override
		public ContentHandler createContentHandler(String mimetype) {
			
			return null;
		}
		
		
		public <K> URLHandler<K> createStreamHandler(String protocol, String method){
			
			return null;
		}
		
		public <K> URLHandler<K> createContentHandler(String protocol, String method, String contentType){
				
			return null;
		}
		
		public static URLHandlerFactory getInstance(){
			return new 
					URLHandlerFactory();
		}
		
	}
	
	public static URLHandlerFactory FACTORY = URLHandlerFactory.getInstance();
	
	/* (non-Javadoc)
	 * @see java.net.URLStreamHandler#openConnection(java.net.URL)
	 */
	@Override
	protected URLConnection openConnection(URL url) throws IOException {
		String scheme = url.getProtocol();
		
		return null;
	}
	
	
	public static <K> K create(URL url, K object){
		URLHandler<K> handler = FACTORY.createStreamHandler(url.getProtocol(), 
				org.sana.net.URLHandler.URLHandlerFactory.Method.CREATE.toString());
		
		return null;
	}
	
	public static <K> K read(URL url){
		URLHandler<K> handler = FACTORY.createStreamHandler(url.getProtocol(), 
				org.sana.net.URLHandler.URLHandlerFactory.Method.READ.toString());
		return null;
	}
	
	public static <K> K update(URL url, K object){
		URLHandler<K> handler = FACTORY.createStreamHandler(url.getProtocol(), 
				org.sana.net.URLHandler.URLHandlerFactory.Method.UPDATE.toString());
		return null;
	}
	
	public static <K> K delete(URL url){
		URLHandler<K> handler = FACTORY.createStreamHandler(url.getProtocol(), 
				org.sana.net.URLHandler.URLHandlerFactory.Method.DELETE.toString());
		return null;
	}
}
