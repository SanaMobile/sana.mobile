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
import java.util.UUID;

import org.sana.android.content.Uris;
import org.sana.android.db.ModelWrapper;
import org.sana.android.provider.Patients;
import org.sana.android.provider.Subjects;
import org.sana.android.util.Dates;
import org.sana.api.ILocation;
import org.sana.api.IPatient;
import org.sana.core.Location;
import org.sana.core.Model;
import org.sana.core.Patient;
import org.sana.util.UUIDUtil;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

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

    public boolean getConfirmed(){
        return getBooleanField(Patients.Contract.CONFIRMED);
    }

    public boolean getDobEstimated(){
        return getBooleanField(Patients.Contract.DOB_ESTIMATED);
    }

	/* (non-Javadoc)
	 * @see org.sana.android.db.ModelWrapper#getObject()
	 */
	@Override
	public IPatient getObject() {
        Patient obj = new Patient();
        obj.setUuid(getUuid());
        obj.setCreated(getCreated());
        obj.setModified(getModified());
        obj.setDob(getDob());
        obj.setFamily_name(getFamily_name());
        obj.setGiven_name(getGiven_name());
        obj.setGender(getGender());
        obj.setImage(getImage());
        obj.setLocation((Location) getLocation());
        obj.setSystemId(getSystemId());
        //obj.setDobEstimated(getDobEstimated());
        //obj.setConfirmed(getConfirmed());
		return obj;
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

    /**
     * Gets the value of the system identifier stored in the
     * {@link org.sana.android.provider.Patients.Contract#PATIENT_ID PATIENT_ID} column.
     *
     * @return The value or null.
     */
    public String getSystemId(){
        return getStringField(Patients.Contract.PATIENT_ID);
    }

    /**
     * Convenience method to look up a single Patient by the <code>system_id</code>.
     *
     * @param resolver The resolver which will perform the query.
     * @param systemId The system id to query
     * @return
     */
    public static Patient getOneBySystemId(ContentResolver resolver, String systemId){
        PatientWrapper wrapper = new PatientWrapper(ModelWrapper.getOneByFields(
                Patients.CONTENT_URI,
                resolver,
                new String[]{Patients.Contract.PATIENT_ID},
                new String[]{systemId}
        ));
        Patient obj = null;
        if(wrapper != null)
            try{
                if(wrapper.getCount() == 1) {
                    wrapper.moveToFirst();
                    obj = new Patient();
                    obj = (Patient) wrapper.getObject();
                } else {

                }
            } finally {
                wrapper.close();
            }
        return obj;
    }

    public static Patient get(Context context, Uri uri){
        Patient patient = null;
        switch(Uris.getTypeDescriptor(uri)) {
            case Uris.ITEM_UUID:
            case Uris.ITEM_ID:
                PatientWrapper wrapper = null;
                try {
                    wrapper = new PatientWrapper(
                            context.getContentResolver().query(uri, null, null,
                                    null, null));
                    if (wrapper != null && wrapper.getCount() == 1) {
                        if (wrapper.moveToFirst()) {
                            patient = (Patient) wrapper.getObject();
                        }
                    }
                } finally {
                    if (wrapper != null) {
                        wrapper.close();
                    }
                }
                break;
            case Uris.ITEMS:
            default:
                break;
        }
        return patient;
    }

    public static Uri getOrCreate(Context context, Uri uri, ContentValues values){
        Uri result = Uri.EMPTY;
        switch(Uris.getTypeDescriptor(uri)) {
            case Uris.ITEM_UUID:
            case Uris.ITEM_ID:
                if (exists(context, uri)) {
                    context.getContentResolver().update(uri, values, null, null);
                } else {
                    throw new IllegalArgumentException("Item Uri. Does not exist.");
                }
                break;
            case Uris.ITEMS:
                result = context.getContentResolver().insert(uri, values);
                break;
            default:
                throw new IllegalArgumentException("Invalid Uri.");
        }
        return result;
    }

    public static Uri getOrCreate(Context context, ContentValues values){
        return getOrCreate(context, Subjects.CONTENT_URI, values);
    }

    public static Uri getOrCreate(Context context, Patient mPatient){
        ContentValues cv = new ContentValues();
        String uuid = mPatient.getUuid();
        Uri uri = Patients.CONTENT_URI;
        boolean exists = false;
        if(!TextUtils.isEmpty(uuid)){
            exists = ModelWrapper.exists(context, Uris.withAppendedUuid(uri,
                    uuid));
            if(!exists){
                cv.put(Patients.Contract.UUID, uuid);
            } else {
                uri = Uris.withAppendedUuid(uri,uuid);
            }
        } else {
            uuid = UUIDUtil.generatePatientUUID(mPatient.getSystemId()).toString();
            cv.put(Patients.Contract.UUID, uuid);
        }
        cv.put(Patients.Contract.PATIENT_ID, mPatient.getSystemId());
        cv.put(Patients.Contract.GIVEN_NAME, mPatient.getGiven_name());
        cv.put(Patients.Contract.FAMILY_NAME, mPatient.getFamily_name());
        // Format the date for insert
        cv.put(Patients.Contract.DOB, Dates.toSQL(mPatient.getDob()));
        cv.put(Patients.Contract.GENDER, mPatient.getGender());
        cv.put(Patients.Contract.IMAGE, String.valueOf(mPatient.getImage()));
        //TODO update db and uncomment
        //cv.put(Patients.Contract.CONFIRMED, mPatient.getConfirmed());
        //cv.put(Patients.Contract.DOB_ESTIMATED, mPatient.isDobEstimated());
        if(mPatient.getLocation() != null)
            cv.put(Patients.Contract.LOCATION, mPatient.getLocation().getUuid());
        if(exists){
            context.getContentResolver().update(uri,cv,null,null);
        } else {
            uri = context.getContentResolver().insert(Patients.CONTENT_URI,cv);
        }
        return uri;
    }
}
