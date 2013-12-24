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

/**
 * @author Sana Development
 *
 */
public class URLResponse<T> {
	
	public static final String SUCCESS = "SUCCESS";
	public static final String FAILURE = "FAILURE";
	public static final String UNKNOWN = "UNKNOWN";
	
	private String status;
	private int code;
	private T message;
	
	/**
	 * A new MDSResult with status of "UNKNOWN", code value of 
	 * {@link Code#UNKNOWn}, and null message.
	 */
	public URLResponse() {
		this(UNKNOWN, 0, null);
	}
	
	public URLResponse(String status, int code, T message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}
	
	/**
	 * Whether the result is successful.
	 * @return true if <code>code >= 200</code> < <code>code < 400 </code>
	 */
	public boolean succeeded() {
		return (200 <= code) && (code < 400);
	}

	/**
	 * Whether the result is a failure.
	 * @return true if <code>code < 200</code> or <code>code >= 400 </code>
	 */
	public boolean failed() {
		return (code < 200) || (code >= 400);
	}
	
	/**
	 * The result message body.
	 * @return 
	 */
	public T getMessage() {
		return message;
	}
	
	public void setMessage(T message){
		this.message = message;
	}
	
	/**
	 * The status code.
	 * @return
	 */
	public int getCode() {
		return code;
	}
	
	public void setCode(int code){
		this.code = code;
	}
	
	public String getStatus(){
		return this.status;
	}

	public void setStatus(String status){
		this.status = status;
	}
	
	public static <K> URLResponse<K> empty(){
		URLResponse<K> e = new URLResponse<K>();
		return e;
		
	}
	public static final URLResponse<Void> NOSERVICE = new URLResponse<Void>();
}
