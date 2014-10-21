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
package org.sana.android.db.impl;

import org.sana.R;
import org.sana.android.content.ModelContentProvider;
import org.sana.android.db.DatabaseManager;

import android.util.Log;

/**
 * Concrete implementation of the {@link org.sana.android.db.ModelContentProvider 
 * ModelContentProvider} class.
 * 
 * @author Sana Development
 *
 */
public class ModelContentProviderImpl extends ModelContentProvider {
	public static final String TAG = ModelContentProviderImpl.class.getSimpleName();
	
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
		Log.i(TAG, "onCreate() called");
		String name = getContext().getString(R.string.db_name);
		int version = getContext().getResources().getInteger(R.integer.cfg_db_version_value);
		Log.i(TAG, "onCreate(). version:" + version);
		mOpener = DatabaseOpenHelperImpl.getInstance(getContext().getApplicationContext(),name, version);
		DatabaseManager.initializeInstance(mOpener);
		mManager = DatabaseManager.getInstance();
		return true;
	}
}
