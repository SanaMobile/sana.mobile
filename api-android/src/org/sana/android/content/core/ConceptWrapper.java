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
package org.sana.android.content.core;

import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;

import org.sana.android.db.ModelWrapper;
import org.sana.android.provider.Concepts;
import org.sana.api.IConcept;
import org.sana.util.DateUtil;

import android.content.ContentResolver;
import android.database.Cursor;

/**
 * @author Sana Development
 *
 */
public class ConceptWrapper extends ModelWrapper<IConcept> implements IConcept{
	
	public static final String TAG = ConceptWrapper.class.getSimpleName();
	
	/**
	 * @param cursor
	 */
	public ConceptWrapper(Cursor cursor) {
		super(cursor);
	}

	/* (non-Javadoc)
	 * @see org.sana.android.db.impl.Proxy#nextObject()
	 */
	@Override
	public IConcept getObject() {
		return this;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<IConcept> iterator() {
		moveToFirst();
		return new ProxyIterator<IConcept>(this);
	}

	/* (non-Javadoc)
	 * @see org.sana.api.IConcept#getConstraints()
	 */
	@Override
	public String getConstraints() {
		return getString(getColumnIndex(Concepts.Contract.CONSTRAINT));
	}

	/* (non-Javadoc)
	 * @see org.sana.api.IConcept#getDatatype()
	 */
	@Override
	public String getDatatype() {
		return getString(getColumnIndex(Concepts.Contract.DATA_TYPE));
	}

	/* (non-Javadoc)
	 * @see org.sana.api.IConcept#getName()
	 */
	@Override
	public String getName() {
		return getString(getColumnIndex(Concepts.Contract.NAME));
	}

	/* (non-Javadoc)
	 * @see org.sana.api.IConcept#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return getString(getColumnIndex(Concepts.Contract.DISPLAY_NAME));
	}

	/* (non-Javadoc)
	 * @see org.sana.api.IConcept#getDescription()
	 */
	@Override
	public String getDescription() {
		return getString(getColumnIndex(Concepts.Contract.DESCRIPTION));
	}

	/* (non-Javadoc)
	 * @see org.sana.api.IConcept#getMediatype()
	 */
	@Override
	public String getMediatype() {
		return getString(getColumnIndex(Concepts.Contract.MEDIA_TYPE));
	}
	
	public static ConceptWrapper getAll(){
		return null;
	}
	
	public static ConceptWrapper getAllByDisplayName(){
		return null;
		
	}
	/**
	 * Convenience wrapper to returns a Concept representing a single row matched
	 * by the uuid value.
	 * 
	 * @param resolver The resolver which will perform the query.
	 * @param uuid The uuid to select by.
 	 * @return A cursor with a single row.
 	 * @throws IllegalArgumentException if multiple objects are returned.
 	 */
	public static IConcept getOneByUuid(ContentResolver resolver, String uuid){
		ConceptWrapper wrapper = new ConceptWrapper(ModelWrapper.getOneByUuid(
				Concepts.CONTENT_URI,resolver, uuid));
		IConcept object = null;
		if(wrapper != null)
		try{ 
			object = wrapper.next();
		} finally {
			wrapper.close();
		}
		return object;
	}
	
	/**
	 * Convenience wrapper to return a wrapped cursor which references all of 
	 * the Concept entries ordered by {@link org.sana.android.provider.BaseContract#CREATED CREATED} 
	 * in ascending order, or, oldest first.
	 * 
	 * @param resolver The resolver which will perform the query.
	 * @return A cursor with the result or null.
	 */
	public static ConceptWrapper getAllByCreatedAsc(ContentResolver resolver)
	{		
		return new ConceptWrapper(ModelWrapper.getAllByCreatedAsc(
				Concepts.CONTENT_URI,resolver));
	}
	
	/**
	 * Convenience wrapper to return a wrapped cursor which references all of 
	 * the Concept entries ordered by {@link org.sana.android.provider.BaseContract#CREATED CREATED} 
	 * in descending order, or, newest first.
	 *   
	 * @param resolver The resolver which will perform the query.
	 * @return A cursor with the result or null.
	 */
	public static ConceptWrapper getAllByCreatedDesc(ContentResolver resolver)
	{		
		return new ConceptWrapper(ModelWrapper.getAllByCreatedDesc(
				Concepts.CONTENT_URI,resolver));
	}
	
	/**
	 * Convenience wrapper to return a wrapped cursor which references all of 
	 * the Concept entriesordered by {@link org.sana.android.provider.BaseContract#MODIFIED MODIFIED} 
	 * in ascending order, or, oldest first.
	 * 
	 * @param resolver The resolver which will perform the query.
	 * @return A cursor with the result or null.
	 */
	public static ConceptWrapper getAllByModifiedAsc(ContentResolver resolver)
	{		
		return new ConceptWrapper(ModelWrapper.getAllByModifiedAsc(
				Concepts.CONTENT_URI,resolver));
	}
	
	/**
	 * Convenience wrapper to return a wrapped cursor which references all of 
	 * the Concept entriesordered by {@link org.sana.android.provider.BaseContract#MODIFIED MODIFIED} 
	 * in descending order, or, newest first.
	 * 
	 * @param resolver The resolver which will perform the query.
	 * @return A cursor with the result or null.
	 */
	public static ConceptWrapper getAllByModifiedDesc(ContentResolver resolver)
	{		
		return new ConceptWrapper(ModelWrapper.getAllByModifiedDesc(
				Concepts.CONTENT_URI,resolver));
	}
}
