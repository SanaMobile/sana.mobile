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
package org.sana.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @author Sana Development
 *
 */
public class UUIDUtil {
	
	public static final String NAMESPACE_OID = "6ba7b812-9dad-11d1-80b4-00c04fd430c8";
	public static final String ENCOUNTER_UUID = "22f73cbc-a66d-3567-a074-607341b35757";
	public static final String OBSERVATION_UUID = "22f73cbc-a66d-3567-a074-607341b35757";
	public static final String OBSERVER_UUID = "14034c9f-09b1-349c-b633-127e6e10357c";
	public static final String SUBJECT_UUID = "12edc2564-46b9-3da0-8156-79ff18456bce";
	
	public static UUID EMPTY = UUID.fromString(NAMESPACE_OID);
			
	static final Pattern PATTERN =Pattern.compile(
			"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
	
	public static boolean isValid(String uuid){
		if(uuid == null)
			return false;
		try{
			UUID.fromString(uuid);
			return true;
		} catch(IllegalArgumentException e) {
			return false;
		}  
	}
	
	public static UUID generate(String urn, String name){
		byte[] bytes = urn.getBytes();
		return UUID.nameUUIDFromBytes(bytes);
	}
	
	
	public static UUID uuid3(UUID uuid, String name){
		
		final byte[] bytes = new byte[16];
		final long msb = uuid.getMostSignificantBits();
		final long lsb = uuid.getLeastSignificantBits();
		
	    for (int i = 0; i < 8; i++) {
	        bytes[i] = (byte) (msb >>> 8 * (7 - i));
	    }
	    for (int i = 8; i < 16; i++) {
	    	bytes[i] = (byte) (lsb >>> 8 * (7 - i));
	    }

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			out.write(bytes);
			out.write(name.getBytes());
			return UUID.nameUUIDFromBytes(out.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			return EMPTY;
		}		
	}
	
	public static UUID generateObserverUUID(String username){
		return UUIDUtil.uuid3(UUID.fromString(OBSERVER_UUID), username);
	}
	
	public static UUID generateObservationUUID(UUID encounter, String id){
		return UUIDUtil.uuid3(encounter, "observation/" + id);
	}	
	
	public static UUID generateEncounterUUID(UUID procedure, String subject,
			String date)
	{
		return UUIDUtil.uuid3(UUIDUtil.uuid3(procedure, subject), date);
	}
	
	public static UUID generatePatientUUID(String systemID){
		return UUIDUtil.uuid3(UUID.fromString(SUBJECT_UUID), systemID);
	}
	
	public static void main(String[] ... args){
	}
	
}
