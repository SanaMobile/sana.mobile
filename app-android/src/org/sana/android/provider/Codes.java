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
package org.sana.android.provider;

/**
 * Integer codes for core objects. The convention is to construct codes for 
 * actual use as
 * 
 * 	[object byte][authority byte][content byte]
 * 
 * @author Sana Development
 *
 */
public final class Codes {
	public static final String TAG = Codes.class.getSimpleName();
	
	// authorities
	public static final int LEXICON = 0;
	public static final int OBSERVABLE = 1;
	public static final int MESSAGING = 2;
	
	// Lexicon codes
	public static final int CONCEPTS = 0;
	public static final int INSTRUCTION = 1;
	public static final int PROCEDURE = 2;
	
	// Observable codes
	public static final int ENCOUNTERS = 0;
	public static final int OBSERVATIONS = 1;
	public static final int OBSERVERS = 2;
	public static final int SUBJECTS = 3;
	
	// Messaging codes
	public static final int EVENTS = 0;
	public static final int NOTIFICATIONS = 1;
	
	
	public static final int build(int authority, int content, int item){
		
		return item << 6 | authority << 3 | item;
	}
	
	
	public static final int[] bits(int code){
		return new int[]{
				code ^ 64,
				code ^ 8,
				code 
		};
	}
}
