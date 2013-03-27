/**
 * Copyright (c) 2013, Sana
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
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
package org.sana.android.db.test;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import android.util.Log;

/**
 * @author Sana Development
 * 
 * @param <T>
 *
 */
public abstract class ModelProviderTest<T extends ContentProvider> extends ProviderTestCase2<T> {

	static final String TAG = ModelProviderTest.class.getSimpleName();
	
	MockContentResolver mMockResolver;
	SQLiteDatabase  mDb;
	/**
	 * @param providerClass
	 * @param providerAuthority
	 */
	protected ModelProviderTest(Class<T> providerClass, String providerAuthority) {
		super(providerClass, providerAuthority);
		// TODO Auto-generated constructor stub
	}

	
	
	@Override
	protected void setUp() throws Exception{
        // Calls the base class implementation of this method.
        super.setUp();
        
        // Gets the resolver for this test.
        mMockResolver = getMockContentResolver();

	}

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
	
	protected abstract Uri getContentUri();
	
	protected abstract String getDirType();

	protected abstract String getItemType();
	
	protected abstract ContentValues getValues();

	protected abstract ContentValues getUpdateValues();
	
	public void testUriAndGetType() {
		Log.d(TAG, ".testUriAndGetType()");
        // Tests the MIME type for the notes table URI.
        String mimeType = mMockResolver.getType(getContentUri());
        assertEquals(getDirType(), mimeType);

        Uri uri = ContentUris.withAppendedId(getContentUri(), 1);

        // Gets the note ID URI MIME type.
        mimeType = mMockResolver.getType(uri);
        assertEquals(getItemType(), mimeType);

        // Tests an invalid URI. This should throw an IllegalArgumentException.
        mimeType = mMockResolver.getType(Uri.withAppendedPath(getContentUri(), "invalid"));
    }
	
	public void testInsertDelete(){
		Log.d(TAG, ".testInsertDelete()");
		ContentValues values = getValues();
		// insert
		Uri uri = insert(values);
		
		// test that we can remove it
		delete(uri);
	}
	

	public void testInsertQueryDelete(){
		Log.d(TAG, ".testInsertQueryDelete()");
		ContentValues values = getValues();
		// insert
		Uri uri = insert(values);
		
		// query the insert Uri and check returned values
		query(uri, values);
		
		// test that we can remove it
		delete(uri);
	}
	
	public void testInsertUpdateDelete(){
		Log.d(TAG, ".testInsertUpdateDelete()");
		ContentValues values = getValues();
		// insert
		Uri uri = insert(values);
		
		// query the insert Uri and check returned values
		query(uri, values);
		
		// update the concept
		values = getUpdateValues();
		update(uri,values);
		
		// test that we can remove it
		delete(uri);
	}
	
	/**
	 * 
	 */
	public Uri insert(ContentValues values){
		return mMockResolver.insert(getContentUri(), values);
	}
	
	/**
	 * 
	 */
	public void update(Uri uri, ContentValues values){
		int result = mMockResolver.update(uri, values, null,null);
		assert(result == 1);
	}
	
	/**
	 * 
	 */
	public void query(Uri uri, ContentValues values){
		Cursor cursor = mMockResolver.query(uri, null, null, null,null);
		assert(cursor != null);
		try{
			if(cursor.moveToFirst()){
				Set<Entry<String, Object>> s=values.valueSet();
				Iterator itr = s.iterator();
				while(itr.hasNext()){
					Map.Entry me = (Map.Entry)itr.next(); 
					String key = me.getKey().toString();
					Object value =  me.getValue();
					int col = cursor.getColumnIndex(key);
					assert(values.get(key).toString().compareTo(
							cursor.getString(col)) == 0);
				}
			} else {
				assert(false);
			}
		}finally {
			cursor.close();
		}
			
	}
	
	public void delete(Uri uri){
		int result = mMockResolver.delete(uri, null,null);
		assert(result == 1);
	}
	
	public void testInsertFail(){}
	
	public void testUpdateFail(){}
	
	public void testQueryFail(){}
	
	public void testDeleteFail(){}
	
}
