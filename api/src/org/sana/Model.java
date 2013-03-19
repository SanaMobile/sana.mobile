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
package org.sana;

import java.util.Date;

/**
 * 
 * 
 * @author Sana Development
 *
 */
public interface Model {

	/** The format which will be used for persisting Date objects */
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/** The regular expression for validating uuid Strings */ 
	public static final String UUID_REGEX = 
		"[a-f0-9]{8}-[a-f0-9]{4}-[1-5][a-f0-9]{3}-[89ab][a-f0-9]{3}-[a-f0-9]{12}";

	/** A universally unique identifier */
	public static final String UUID = "uuid";

	/** The date when the instance was created */
	public static final String CREATED = "created";
	
	/** The date when the instance was last modified */
	public static final String MODIFIED = "modified";
	
	/** Returns a universally unique identifier */
	String getUuid();
	
	/** 
	 * Sets the instance's universally unique identifier
	 * 
	 * @param uuid the new UUID
	 * @throws IllegalArgumentException if the format of the argument does not 
	 * 	conform to {@link #UUID_REGEX}
	 */
	void setUuid(String uuid) throws IllegalArgumentException;

	/**
	 * Returns a {@link java.util.Date Date} when this object was created.
	 * @return a Date object.
	 */
	Date getCreated();
	
	/**
	 * Sets the {@link java.util.Date Date} when this object was created.
	 */
	void setCreated(Date date);
	
	
	/**
	 * Returns a {@link java.util.Date Date} when this object was last modified.
	 * @return a Date object.
	 */
	Date getModified();

	/**
	 * Sets the {@link java.util.Date Date} when this object was last modified.
	 */
	void setModified(Date modified);

	
}
