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
package org.sana.android.service.impl;

import org.sana.android.content.core.ObservationParcel;
import org.sana.android.content.core.ObservationWrapper;
import org.sana.android.provider.Observations;
import org.sana.android.service.IObservationService;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.RemoteException;

/**
 * Provides restful access to Observation data.
 * 
 * @author Sana Development
 *
 */
public class ObservationService extends Service{
	public static final String TAG = ObservationService.class
			.getSimpleName();

	private final IObservationService.Stub mBinder = new IObservationService.Stub(){

		@Override
		public Uri create(ObservationParcel t) throws RemoteException {
			ContentValues values = new ContentValues();
			return getContentResolver().insert(Observations.CONTENT_URI, values);
		}

		@Override
		public ObservationParcel read(ParcelUuid uuid) throws RemoteException {
			return (ObservationParcel) ObservationWrapper.getOneByUuid(getContentResolver(), uuid.toString());
		}

		@Override
		public boolean update(ObservationParcel t) throws RemoteException {
			Uri uri = Uri.withAppendedPath(Observations.CONTENT_URI, t.getUuid());
			ContentValues values = new ContentValues();
			return getContentResolver().update(uri, values, null, null) == 1;
		}

		@Override
		public boolean delete(ParcelUuid uuid) throws RemoteException {
			Uri uri = Uri.withAppendedPath(Observations.CONTENT_URI, uuid.toString());
			return getContentResolver().delete(uri, null, null) == 1;
		}
		
	};
	
	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
}
