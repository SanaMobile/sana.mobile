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
package org.sana.android.db;

import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.sana.android.provider.BaseContract;
import org.sana.api.IModel;
import org.sana.util.DateUtil;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public abstract class ModelWrapper<T extends IModel> extends CursorWrapper implements ModelIterable<T>, IModel{
	
	public ModelWrapper(Cursor cursor){
		super(cursor);
	}
	
	public Date getDateField(String field){
		try {
			return DateUtil.parseDate(getString(getColumnIndex(field)));
		} catch (ParseException e) {
			throw new IllegalArgumentException();
		}
	}
	
	public int getIntField(String field){
		return getInt(getColumnIndex(field));
	}
	
	public String getStringField(String field){
		return getString(getColumnIndex(field));
	}
	
	/* (non-Javadoc)
	 * @see org.sana.api.IModel#getUuid()
	 */
	@Override
	public String getUuid() {
		return getStringField(BaseContract.UUID);
	}

	/* (non-Javadoc)
	 * @see org.sana.api.IModel#getCreated()
	 */
	@Override
	public Date getCreated() {
		return getDateField(BaseContract.CREATED);
	}
	
	/* (non-Javadoc)
	 * @see org.sana.api.IModel#getModified()
	 */
	@Override
	public Date getModified() {
		return getDateField(BaseContract.MODIFIED);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sana.android.db.ModelIterable#next()
	 */
	public T next() {
		if(moveToNext()){
			try{
				return getObject();
			} catch (Exception e){
				throw new NoSuchElementException("Failed to instantiate object ");
			}
		}
		throw new NoSuchElementException("Wrapped cursor at or past last.");
	}

	/**
	 * Should create a new instance of <T> from the values in the current row.
	 * Note: Only columns declared as part of the projection in the original 
	 * query will be non null in the instance.
	 * @return
	 */
	public abstract T getObject();
	
	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return !isLast();
	}
	
	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException("Removal not supported");
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		moveToFirst();
		return new ProxyIterator<T>(this);
	}
	
	public java.util.List<T> toList(ModelWrapper<T> wrapper){
		java.util.List<T> list = new java.util.ArrayList<T>(wrapper.getCount());
		for(T t:wrapper){
			list.add(t);
		}
		return list;
	}
	
	public static class ProxyIterator<T extends IModel> implements Iterator <T> {
		
		private ModelWrapper<T> modelWrapper;
		
		public ProxyIterator(ModelWrapper<T> proxy){
			this.modelWrapper = proxy;
		}
		
		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return modelWrapper.hasNext();
		}


		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public T next() {
			return modelWrapper.next();
		}


		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException("Removal not supported");
		}
	}
	
	/**
	 * Convenience wrapper which returns a cursor representing a single row 
	 * selected a single column value.
	 * 
	 * @param contentUri The content style Uri to query
	 * @param resolver The resolver which will perform the query.
	 * @param field The field, or column, to select by.
	 * @param value The selection argument or, row value, to select by.
 	 * @return A cursor with a single row.
 	 * @throws IllegalArgumentException if multiple rows are returned.
 	 */
	public static Cursor getOneByField(Uri contentUri, ContentResolver resolver, 
			String field, Object object)
	{
		String selection = field + " = ?"; 
		Cursor cursor = resolver.query(contentUri,null, selection, 
				new String[]{ object.toString() }, null);
		if(cursor != null && cursor.getCount() > 1)
			throw new IllegalArgumentException("Multiple entries found! Expecting one.");
		return cursor;
	}
	
	/**
	 * Convenience wrapper which returns a cursor representing a single row 
	 * selected a single column value.
	 * 
	 * @param contentUri The content style Uri to query
	 * @param resolver The resolver which will perform the query.
	 * @param field The field, or column, to select by.
	 * @param value The selection argument or, row value, to select by.
 	 * @return A cursor with a single row.
 	 * @throws IllegalArgumentException if multiple rows are returned.
 	 */
	public static Cursor getOneByFields(Uri contentUri, ContentResolver resolver, 
			String[] fields, String[] vals)
	{
		Cursor cursor = ModelWrapper.getAllByFields(contentUri, resolver, fields, vals);
		if(cursor != null && cursor.getCount() > 1)
			throw new IllegalArgumentException("Multiple entries found! Expecting one.");
		return cursor;
	}
	
	/**
	 * Convenience wrapper to returns a cursor representing a single row matched
	 * by the uuid value.
	 * 
	 * @param contentUri The content style Uri to query
	 * @param resolver The resolver which will perform the query.
	 * @param uuid The uuid to select by.
 	 * @return A cursor with a single row.
 	 * @throws IllegalArgumentException if multiple rows are returned.
 	 */
	public static Cursor getOneByUuid(Uri contentUri, ContentResolver resolver, 
			String uuid)
	{
		String selection = BaseContract.UUID + " = ?"; 
		Cursor cursor = resolver.query(contentUri,null, selection, 
				new String[]{ uuid }, null);
		if(cursor != null && cursor.getCount() > 1)
			throw new IllegalArgumentException("Non unique id! " +contentUri+"/"+uuid);
		return cursor;
	}
	
	/**
	 * Convenience wrapper which returns a cursor representing zero or more rows
	 * selected a single column value.
	 * 
	 * @param contentUri The content style Uri to query
	 * @param resolver The resolver which will perform the query.
	 * @param field The field, or column, to select by.
	 * @param value The selection argument or, row value, to select by.
 	 * @return A cursor with zero or more rows.
 	 */
	public static Cursor getAllByField(Uri contentUri, ContentResolver resolver, 
			String field, Object object)
	{
		return ModelWrapper.getAllByFieldOrdered(contentUri, resolver, field, object, null);
	}
	
	/**
	 * Convenience wrapper which returns a cursor representing zero or more rows
	 * selected a single column value.
	 * 
	 * @param contentUri The content style Uri to query
	 * @param resolver The resolver which will perform the query.
	 * @param field The field, or column, to select by.
	 * @param value The selection argument or, row value, to select by.
 	 * @return A cursor with zero or more rows.
 	 */
	public static Cursor getAllByFields(Uri contentUri, ContentResolver resolver, 
			String[] fields, String[] vals)
	{
		return ModelWrapper.getAllByFieldsOrdered(contentUri, resolver, fields, vals, null);
	}
	
	/**
	 * Convenience wrapper which returns a cursor representing zero or more rows
	 * selected a single column value and ordered. Passing a null field or object
	 * will bypass any selection.
	 * 
	 * @param contentUri The content style Uri to query
	 * @param resolver The resolver which will perform the query.
	 * @param field The field, or column, to select by.
	 * @param value The selection argument or, row value, to select by.
	 * @paeam order The order to return by.
 	 * @return A cursor with zero or more rows.
 	 */
	public static Cursor getAllByFieldOrdered(Uri contentUri, ContentResolver resolver, 
			String field, Object object, String order)
	{
		String selection = null; 
		String[] selectionArgs = null; 

		if(!TextUtils.isEmpty(field) && object != null){
			selection = field + " = ?";
			selectionArgs = new String[]{ object.toString() } ;
		}
		return resolver.query(contentUri,null, selection, selectionArgs, order);
	}
	
	/**
	 * Convenience wrapper which returns a cursor representing zero or more rows
	 * selected a single column value and ordered. Passing a null field or object
	 * will bypass any selection.
	 * 
	 * @param contentUri The content style Uri to query
	 * @param resolver The resolver which will perform the query.
	 * @param field The field, or column, to select by.
	 * @param value The selection argument or, row value, to select by.
	 * @paeam order The order to return by.
 	 * @return A cursor with zero or more rows.
 	 */
	public static Cursor getAllByFieldsOrdered(Uri contentUri, ContentResolver resolver, 
			String[] fields, String[] vals, String order)
	{
		StringBuilder selection = new StringBuilder();
		int index = 0;
		for(String field:fields){
			if(index > 0)
				selection.append(" AND ");
			selection.append(field + " = ?");
			index++;
		}
		return resolver.query(contentUri, null, selection.toString(), vals, order);
	}
	
	/**
	 * Convenience wrapper to return a cursor which returns all of the entries
	 * ordered by {@link org.sana.android.provider.BaseContract#CREATED CREATED} 
	 * in ascending order, or, oldest first.
	 * 
	 * @param contentUri The content style Uri to query
	 * @param resolver The resolver which will perform the query.
	 * @return A cursor with the result or null.
	 */
	public static Cursor getAllByCreatedAsc(Uri contentUri, ContentResolver resolver)
	{
		return ModelWrapper.getAllByFieldOrdered(contentUri, resolver, null, null, BaseContract.CREATED +" ASC");
	}
	/**
	 * Convenience wrapper to return a cursor which returns all of the entries
	 * ordered by {@link org.sana.android.provider.BaseContract#CREATED CREATED} 
	 * in descending order, or, newest first.
	 *   
	 * @param contentUri The content style Uri to query
	 * @param resolver The resolver which will perform the query.
	 * @return A cursor with the result or null.
	 */
	public static Cursor getAllByCreatedDesc(Uri contentUri, ContentResolver resolver)
	{
		final String order = BaseContract.CREATED +" DESC";
		return resolver.query(contentUri,null, null,null, order);
	}
	
	/**
	 * Convenience wrapper to return a cursor which returns all of the entries
	 * ordered by {@link org.sana.android.provider.BaseContract#MODIFIED MODIFIED} 
	 * in ascending order, or, oldest first.
	 * 
	 * @param contentUri The content style Uri to query
	 * @param resolver The resolver which will perform the query.
	 * @return A cursor with the result or null.
	 */
	public static Cursor getAllByModifiedAsc(Uri contentUri, ContentResolver resolver)
	{
		final String order = BaseContract.MODIFIED +" ASC";
		return resolver.query(contentUri,null, null,null, order);
	}
	
	/**
	 * Convenience wrapper to return a cursor which returns all of the entries
	 * ordered by {@link org.sana.android.provider.BaseContract#MODIFIED MODIFIED} 
	 * in ascending order, or, newest first.
	 * 
	 * @param contentUri The content style Uri to query
	 * @param resolver The resolver which will perform the query.
	 * @return A cursor with the result or null.
	 */
	public static Cursor getAllByModifiedDesc(Uri contentUri, ContentResolver resolver)
	{
		final String order = BaseContract.MODIFIED +" DESC";
		return resolver.query(contentUri,null, null,null, order);
	}
	
	
}