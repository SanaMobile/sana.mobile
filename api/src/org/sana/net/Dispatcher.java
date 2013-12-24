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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;


/**
 * @author Sana Development
 *
 */
public class Dispatcher {
	
	static final String CHAR_SET = "UTF-8";
	
	public static interface ContentHandler<T>{
		
	}
	
	public <T> T create(String url, Map<String, ?> params) throws MalformedURLException, IOException{
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setDoOutput(true); // Triggers POST.
		connection.setRequestProperty("Accept-Charset", CHAR_SET);
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + CHAR_SET);
		OutputStream output = null;
		try {
		     output = connection.getOutputStream();
		} finally {
		     if (output != null) try { output.close(); } catch (IOException logOrIgnore) {}
		}
		int status = connection.getResponseCode();
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

}
