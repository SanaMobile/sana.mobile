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

import java.net.URI;
import java.util.Date;

import org.sana.android.db.ModelWrapper;
import org.sana.android.provider.Patients;
import org.sana.api.ILocation;
import org.sana.api.IPatient;
import org.sana.core.Location;

import android.database.Cursor;

/**
 * @author Sana Development
 *
 */
public class PatientWrapper extends ModelWrapper<IPatient> implements IPatient {
	public static final String TAG = PatientWrapper.class.getSimpleName();

	/**
	 * @param cursor
	 */
	public PatientWrapper(Cursor cursor) {
		super(cursor);
	}

	/* (non-Javadoc)
	 * @see org.sana.api.IPatient#getGiven_name()
	 */
	@Override
	public String getGiven_name() {
		return getStringField(Patients.Contract.GIVEN_NAME);
	}

	/* (non-Javadoc)
	 * @see org.sana.api.IPatient#getFamily_name()
	 */
	@Override
	public String getFamily_name() {
		return getStringField(Patients.Contract.FAMILY_NAME);
	}

	/* (non-Javadoc)
	 * @see org.sana.api.IPatient#getDob()
	 */
	@Override
	public Date getDob() {
		return getDateField(Patients.Contract.DOB);
	}

	/* (non-Javadoc)
	 * @see org.sana.api.IPatient#getGender()
	 */
	@Override
	public String getGender() {
		return getStringField(Patients.Contract.GENDER);
	}

	/* (non-Javadoc)
	 * @see org.sana.api.IPatient#getImage()
	 */
	@Override
	public URI getImage() {
		try{
			return URI.create(getStringField(Patients.Contract.IMAGE));
		} catch(Exception e){
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sana.android.db.ModelWrapper#getObject()
	 */
	@Override
	public IPatient getObject() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.sana.api.IPatient#getLocation()
	 */
	@Override
	public ILocation getLocation() {
		Location location = new Location();
		location.setName(getStringField(Patients.Contract.LOCATION));
		return location;
	}
}
