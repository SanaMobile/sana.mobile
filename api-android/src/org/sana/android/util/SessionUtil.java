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
package org.sana.android.util;

import java.util.UUID;

import org.sana.util.UUIDUtil;

import android.text.TextUtils;

/**
 * @author Sana Development
 *
 */
public class SessionUtil {
	public static final String TAG = SessionUtil.class.getSimpleName();
	
	public static final UUID INVALID = UUIDUtil.uuid3(UUIDUtil.EMPTY, "invalid");
	public static final UUID INVALID_PASSWORD = UUIDUtil.uuid3(UUIDUtil.EMPTY, "password");
	public static final UUID VALID = UUIDUtil.uuid3(UUIDUtil.EMPTY, "valid");

	public static final int INDETERMINATE = -1;
	public static final int FAIL_INVALID = 0;
	public static final int SUCCESS_LOCAL = 1;
	public static final int SUCCESS_NETWORK = 2;
	
	/**
	 * Returns true if the key is not empty, null or "", and the key does not 
	 * equal the String value of {@link #INVALID} or {@link #INVALID_PASSWORD}. 
	 * 
	 * @param sessionKey The key String to validate
	 * @return true if the key is valid
	 */
	public static boolean isValidKey(String sessionKey){
		return !TextUtils.isEmpty(sessionKey) && 
				!(sessionKey.equals(INVALID.toString()) || sessionKey.equals(INVALID_PASSWORD.toString()));
	}
	
	
	public static String generateSessionKey(String username){
		return UUIDUtil.generateObserverUUID(username).toString();
	}
}
