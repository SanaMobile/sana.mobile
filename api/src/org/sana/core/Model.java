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
package org.sana.core;

import java.util.Date;

import org.sana.api.IModel;

/**
 * The basic implementation of the core behavior of the core objects in the 
 * data model.
 * 
 * @author Sana Development
 *
 */
public abstract class Model implements IModel{
	
	public String uuid;
	
	public Date created;
	
	public Date modified;
	
	public Model(){}
	
	/*
	 * (non-Javadoc)
	 * @see org.sana.api.IModel#getCreated()
	 */
	public Date getCreated() {
		return null;
	}

	/**
	 * Sets the {@link java.util.Date Date} when this object was created.
	 */
	public void setCreated(Date date) {
		this.created = date;
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.sana.api.IModel#getModified()
	 */
	public Date getModified() {
		return modified;
	}

	/**
	 * Sets the {@link java.util.Date Date} when this object was last modified.
	 */
	public void setModified(Date modified) {
		this.modified = modified;
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.sana.api.IModel#getUuid()
	 */
	public String getUuid() {
		return uuid;
	}
	
	/** 
	 * Sets the instance's universally unique identifier
	 * 
	 * @param uuid the new UUID
	 * @throws IllegalArgumentException if the format of the argument does not 
	 * 	conform to {@link #UUID_REGEX}
	 */
	public void setUuid(String uuid) {
		this.uuid = java.util.UUID.fromString(uuid).toString();
	}
	
        @Override        
        public String toString(){
            return String.format("<%s %s>",this.getClass().getSimpleName(),uuid);
        }
}
