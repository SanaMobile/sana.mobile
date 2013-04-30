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
package org.sana.android.content;


import java.util.UUID;

import org.sana.android.db.UriHelper;
import org.sana.android.provider.Concepts;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Events;
import org.sana.android.provider.Instructions;
import org.sana.android.provider.Models;
import org.sana.android.provider.Notifications;
import org.sana.android.provider.Observations;
import org.sana.android.provider.Observers;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Subjects;

import android.content.UriMatcher;
import android.net.Uri;

/**
 * UriMatcher wrapper which provides additional content information based on 
 * Uri match values. 
 * 
 * @author Sana Development
 *
 */
public class ModelMatcher implements UriHelper {
	
	public static final String TAG = ModelMatcher.class.getSimpleName();
	private static ModelMatcher MATCHER = new ModelMatcher();
	
	public static final String DIR_FORMAT = "%s/";
	public static final String ID_FORMAT = "%s/*";
	public static final String UUID_FORMAT = "%s/#";
	
	private static final int CONTENT = 2;
	private static final int OBJECT = 6;
	
	public static final int ITEMS = 0;
	public static final int ITEM_ID = 1;
	public static final int ITEM_UUID = 3;
	
	// object codes
	public static final class Code{
		private Code(){}
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
	}
	
	
	// dir match codes OBJECT << CONTENT | ITEMS
	public static final int CONCEPT_DIR = Code.CONCEPT << CONTENT | ITEMS;
	public static final int ENCOUNTER_DIR = Code.ENCOUNTER << CONTENT | ITEMS;
	public static final int EVENT_DIR = Code.EVENT << CONTENT | ITEMS;
	public static final int INSTRUCTION_DIR = Code.INSTRUCTION << CONTENT | ITEMS;
	public static final int NOTIFICATION_DIR = Code.NOTIFICATION << CONTENT | ITEMS;
	public static final int OBSERVATION_DIR = Code.OBSERVATION << CONTENT | ITEMS;
	public static final int OBSERVER_DIR = Code.OBSERVER << CONTENT | ITEMS;
	public static final int PROCEDURE_DIR = Code.PROCEDURE << CONTENT | ITEMS;
	public static final int RELATIONSHIP_DIR = Code.RELATIONSHIP << CONTENT | ITEMS;
	public static final int SUBJECT_DIR = Code.SUBJECT << CONTENT | ITEMS;
	
	// item match codes OBJECT << CONTENT | ITEM_ID
	public static final int CONCEPT_ITEM = Code.CONCEPT << CONTENT | ITEM_ID;
	public static final int ENCOUNTER_ITEM = Code.ENCOUNTER << CONTENT | ITEM_ID;
	public static final int EVENT_ITEM = Code.EVENT << CONTENT | ITEM_ID;
	public static final int INSTRUCTION_ITEM = Code.INSTRUCTION << CONTENT | ITEM_ID;
	public static final int NOTIFICATION_ITEM = Code.NOTIFICATION << CONTENT | ITEM_ID;
	public static final int OBSERVATION_ITEM = Code.OBSERVATION << CONTENT | ITEM_ID;
	public static final int OBSERVER_ITEM = Code.OBSERVER << CONTENT | ITEM_ID;
	public static final int PROCEDURE_ITEM = Code.PROCEDURE << CONTENT | ITEM_ID;
	public static final int RELATIONSHIP_ITEM = Code.RELATIONSHIP << CONTENT | ITEM_ID;
	public static final int SUBJECT_ITEM = Code.SUBJECT << CONTENT | ITEM_ID;
	
	// item match codes OBJECT << CONTENT | ITEM_UUID
	public static final int CONCEPT_UUID = Code.CONCEPT << CONTENT | ITEM_UUID;
	public static final int ENCOUNTER_UUID = Code.ENCOUNTER << CONTENT | ITEM_UUID;
	public static final int EVENT_UUID = Code.EVENT << CONTENT | ITEM_UUID;
	public static final int INSTRUCTION_UUID = Code.INSTRUCTION << CONTENT | ITEM_UUID;
	public static final int NOTIFICATION_UUID = Code.NOTIFICATION << CONTENT | ITEM_UUID;
	public static final int OBSERVATION_UUID = Code.OBSERVATION << CONTENT | ITEM_UUID;
	public static final int OBSERVER_UUID = Code.OBSERVER << CONTENT | ITEM_UUID;
	public static final int PROCEDURE_UUID = Code.PROCEDURE << CONTENT | ITEM_UUID;
	public static final int RELATIONSHIP_UUID = Code.RELATIONSHIP << CONTENT | ITEM_UUID;
	public static final int SUBJECT_UUID = Code.SUBJECT << CONTENT | ITEM_UUID;
	
	private static final UriMatcher mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static{
		mMatcher.addURI(Models.AUTHORITY, "concept/", CONCEPT_DIR);
		mMatcher.addURI(Models.AUTHORITY, "concept/#", CONCEPT_ITEM);
		mMatcher.addURI(Models.AUTHORITY, "concept/*", CONCEPT_UUID);
		mMatcher.addURI(Models.AUTHORITY, "encounter/", ENCOUNTER_DIR);
		mMatcher.addURI(Models.AUTHORITY, "encounter/#", ENCOUNTER_ITEM);
		mMatcher.addURI(Models.AUTHORITY, "encounter/*", ENCOUNTER_UUID);
		mMatcher.addURI(Models.AUTHORITY, "event/", EVENT_DIR);
		mMatcher.addURI(Models.AUTHORITY, "event/#", EVENT_ITEM);
		mMatcher.addURI(Models.AUTHORITY, "event/*", EVENT_UUID);
		mMatcher.addURI(Models.AUTHORITY, "instruction/", INSTRUCTION_DIR);
		mMatcher.addURI(Models.AUTHORITY, "instruction/#", INSTRUCTION_ITEM);
		mMatcher.addURI(Models.AUTHORITY, "instruction/*", INSTRUCTION_UUID);
		mMatcher.addURI(Models.AUTHORITY, "notification/", NOTIFICATION_DIR);
		mMatcher.addURI(Models.AUTHORITY, "notification/#", NOTIFICATION_ITEM);
		mMatcher.addURI(Models.AUTHORITY, "notification/*", NOTIFICATION_UUID);
		mMatcher.addURI(Models.AUTHORITY, "observation/", OBSERVATION_DIR);
		mMatcher.addURI(Models.AUTHORITY, "observation/#", OBSERVATION_ITEM);
		mMatcher.addURI(Models.AUTHORITY, "observation/*", OBSERVATION_UUID);
		mMatcher.addURI(Models.AUTHORITY, "observer/", OBSERVER_DIR);
		mMatcher.addURI(Models.AUTHORITY, "observer/#", OBSERVER_ITEM);
		mMatcher.addURI(Models.AUTHORITY, "observer/*", OBSERVER_UUID);
		mMatcher.addURI(Models.AUTHORITY, "procedure/", PROCEDURE_DIR);
		mMatcher.addURI(Models.AUTHORITY, "procedure/#", PROCEDURE_ITEM);
		mMatcher.addURI(Models.AUTHORITY, "procedure/*", PROCEDURE_UUID);
		mMatcher.addURI(Models.AUTHORITY, "subject/", SUBJECT_DIR);
		mMatcher.addURI(Models.AUTHORITY, "subject/#", SUBJECT_ITEM);
		mMatcher.addURI(Models.AUTHORITY, "subject/*", SUBJECT_UUID);
		mMatcher.addURI(Models.AUTHORITY, "patient/", SUBJECT_DIR);
		mMatcher.addURI(Models.AUTHORITY, "patient/#", SUBJECT_ITEM);
		mMatcher.addURI(Models.AUTHORITY, "patient/*", SUBJECT_UUID);
		
	}
	
	private ModelMatcher() {}
	
	/*
	 * (non-Javadoc)
	 * @see org.sana.android.db.UriHelper#match(android.net.Uri)
	 */
	public int match(Uri uri){
		int match = mMatcher.match(uri);
		if(match != UriMatcher.NO_MATCH && matchContent(uri) == ITEM_UUID)
			try{
				UUID.fromString(uri.getLastPathSegment());
			} catch(Exception e){
				match = UriMatcher.NO_MATCH; 
			}
		return match;
	}
	
	/* (non-Javadoc)
	 * @see org.sana.android.db.UriHelper#getType(android.net.Uri)
	 */
	/**
	 * Will return an ITEM type for item or uuid matches-i.e. path matching 
	 * {@literal value/#} and {@literal value/*}. respectively 
	 */
	@Override
	public String getType(Uri uri) {
		switch (match(uri)){
		case CONCEPT_DIR:
			return Concepts.CONTENT_TYPE;
		case CONCEPT_UUID:
		case CONCEPT_ITEM:
			return Concepts.CONTENT_ITEM_TYPE;
		case ENCOUNTER_DIR:
			return Encounters.CONTENT_TYPE;
		case ENCOUNTER_UUID:
		case ENCOUNTER_ITEM:
			return Encounters.CONTENT_ITEM_TYPE;
		case EVENT_DIR:
			return Events.CONTENT_TYPE;
		case EVENT_UUID:
		case EVENT_ITEM:
			return Events.CONTENT_ITEM_TYPE;
		case INSTRUCTION_DIR:
			return Instructions.CONTENT_TYPE;
		case INSTRUCTION_UUID:
		case INSTRUCTION_ITEM:
			return Instructions.CONTENT_ITEM_TYPE;
		case NOTIFICATION_DIR:
			return Notifications.CONTENT_TYPE;
		case NOTIFICATION_UUID:
		case NOTIFICATION_ITEM:
			return Notifications.CONTENT_ITEM_TYPE;
		case OBSERVATION_DIR:
			return Observations.CONTENT_TYPE;
		case OBSERVATION_UUID:
		case OBSERVATION_ITEM:
			return Observations.CONTENT_ITEM_TYPE;
		case OBSERVER_DIR:
			return Observers.CONTENT_TYPE;
		case OBSERVER_UUID:
		case OBSERVER_ITEM:
			return Observers.CONTENT_ITEM_TYPE;
		case PROCEDURE_DIR:
			return Procedures.CONTENT_TYPE;
		case PROCEDURE_UUID:
		case PROCEDURE_ITEM:
			return Procedures.CONTENT_ITEM_TYPE;
		case SUBJECT_DIR:
			return Subjects.CONTENT_TYPE;
		case SUBJECT_UUID:
		case SUBJECT_ITEM:
			return Subjects.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Invalid uri. No match");
		}
	}
	
	/**
	 * Returns the first two bits of the match code which correspond to the
	 * content type, dir or item. 
	 * 
	 * @param uri The Uri to match against.
	 * @return {@link #ITEMS} for a dir match, or {@link #ITEM_ID} for a single
	 * 	item match.
	 */
	public int matchContent(Uri uri){
		return mMatcher.match(uri) & 3;
	}
	
	/**
	 * Returns the object match code which corresponds to the static fields
	 * of {@link #Code}. 
	 * 
	 * @param uri The Uri to match against
	 * @return An integer match code
	 */
	public int matchObject(Uri uri) {
		return mMatcher.match(uri) >> CONTENT;
	}
	
	public int code(int objectCode, int itemCode) {
		return objectCode << CONTENT | itemCode ;
	}
	
	public UUID parseUUID(Uri uri){
		return UUID.fromString(uri.getLastPathSegment());
	}
	
	/**
	 * @return
	 */
	public static final ModelMatcher getInstance(){
		return MATCHER;
	}

	
	
}
