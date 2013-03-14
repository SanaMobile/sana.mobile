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
package org.sana.android.provider;

import org.sana.*;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.net.Uri;

/**
 * @author Sana Development
 *
 */
public class Models {
	public static final String TAG = Models.class.getSimpleName();
	
	public static final String AUTHORITY = "org.sana.provider";
	
	public static final Uri CONTENT_URI = Uri.fromParts(
			ContentResolver.SCHEME_CONTENT, AUTHORITY, null);
	
	public static final Class<?>[] MODELS = new Class<?>[]{
		Concept.class,
		Encounter.class,
		Event.class,
		Instruction.class,
		Notification.class,
		Observation.class,
		Observer.class,
		Procedure.class,
		Subject.class,
	};
	
	// match types
	public static final int ITEMS = 0;
	public static final int ITEM_ID = 1;
	
	// object classes
	public static final int CONCEPT = 1;
	public static final int ENCOUNTER = 2;
	public static final int EVENT = 3;
	public static final int INSTRUCTION = 4;
	public static final int NOTIFICATION = 5;
	public static final int OBSERVATION = 6;
	public static final int OBSERVER = 7;
	public static final int PROCEDURE = 8;
	public static final int RELATIONSHIP = 9;
	public static final int SUBJECT = 10;
	
	
	public static final int code(int objectClass, int matchType){
		return objectClass << 3 + matchType;
	}
	
	public static final String dirPath(Class<?> klazz){
		return klazz.getSimpleName().toLowerCase() + "/";
	}
	
	public static final String itemPath(Class<?> klazz){
		return klazz.getSimpleName().toLowerCase() + "/#";
	}
	
	public static final ModelUriHelper URI_MATCHER = new ModelUriHelper();
	static{
		URI_MATCHER.addURI(AUTHORITY, "concept/", code(CONCEPT,ITEMS));
		URI_MATCHER.addURI(AUTHORITY, "concept/#", code(CONCEPT,ITEM_ID));
		URI_MATCHER.addURI(AUTHORITY, "encounter/", code(ENCOUNTER,ITEMS));
		URI_MATCHER.addURI(AUTHORITY, "encounter/#", code(ENCOUNTER,ITEM_ID));
		URI_MATCHER.addURI(AUTHORITY, "event/", code(EVENT,ITEMS));
		URI_MATCHER.addURI(AUTHORITY, "event/#", code(EVENT,ITEM_ID));
		URI_MATCHER.addURI(AUTHORITY, "instruction/", code(INSTRUCTION,ITEMS));
		URI_MATCHER.addURI(AUTHORITY, "instruction/#", code(INSTRUCTION,ITEM_ID));
		URI_MATCHER.addURI(AUTHORITY, "notification/", code(NOTIFICATION,ITEMS));
		URI_MATCHER.addURI(AUTHORITY, "notification/#", code(NOTIFICATION,ITEM_ID));
		URI_MATCHER.addURI(AUTHORITY, "observation/", code(OBSERVATION,ITEMS));
		URI_MATCHER.addURI(AUTHORITY, "observation/#", code(OBSERVATION,ITEM_ID));
		URI_MATCHER.addURI(AUTHORITY, "observer/", code(OBSERVER,ITEMS));
		URI_MATCHER.addURI(AUTHORITY, "observer/#", code(OBSERVER,ITEM_ID));
		URI_MATCHER.addURI(AUTHORITY, "procedure/", code(PROCEDURE,ITEMS));
		URI_MATCHER.addURI(AUTHORITY, "procedure/#", code(PROCEDURE,ITEM_ID));
		
	}
	
	public static class ModelUriHelper extends UriMatcher{

		/**
		 * Creates a new instance with the root node of the tree set to 
		 * {@link android.content.UriMatcher#NO_MATCH UriMatcher.NO_MATCH} 
		 */
		public ModelUriHelper(){
			this(UriMatcher.NO_MATCH);
		}
		
		/**
		 * Creates a new instance and sets the root node.
		 * @param code
		 */
		public ModelUriHelper(int code) {
			super(code);
		}

		public final Uri getUri(Class<?> klazz){
			return Uri.fromParts(ContentResolver.SCHEME_CONTENT, AUTHORITY
					+ "/" + klazz.getSimpleName(), null);
		}
		
	}
}
