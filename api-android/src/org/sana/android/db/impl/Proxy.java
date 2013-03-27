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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.sana.android.db.IProxy;
import org.sana.api.Datatype;
import org.sana.api.IModel;

import android.database.Cursor;

public abstract class Proxy<T extends IModel> implements IProxy<T>{
	
	private Cursor cursor;
	private Class<T> klazz;
	
	public Proxy(Cursor cursor, Class<T> klazz){
		this.cursor = cursor;
	}
	
	
	public Cursor getRawCursor(){
		return cursor;
	}

	public T next() {
		if(cursor.moveToNext()){
			return null;
		}
		throw new NoSuchElementException();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return !cursor.isLast();
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public <K> K getField(String field){
		int column = cursor.getColumnIndex(field);
		switch(Datatype.valueOf(field)){
			
		}
		//klazz.getDeclaredField(field).getClass();
		return null;
		
	}
	
	public static final class ProxyIterator<T extends IModel> implements Iterator <Proxy<T>> {
		
		private Proxy<T> proxy;
		
		public ProxyIterator(Proxy<T> proxy){
			this.proxy = proxy;
		}
		
		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return proxy.hasNext();
		}


		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public Proxy<T> next() {
			// TODO Auto-generated method stub
			if(proxy.hasNext()){
				return proxy;
			}
			throw new NoSuchElementException();
		}


		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			
		}
	}
	
}