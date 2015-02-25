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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.sana.android.provider.Concepts;
import org.sana.android.provider.EncounterTasks;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Events;
import org.sana.android.provider.Instructions;
import org.sana.android.provider.Models;
import org.sana.android.provider.Notifications;
import org.sana.android.provider.ObservationTasks;
import org.sana.android.provider.Observations;
import org.sana.android.provider.Observers;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Subjects;
import org.sana.util.UUIDUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
/**
 * a container of Uri descriptor values and related constants. The static 
 * methods of this class are primarily intended for consistent, application-wide
 * Uri matching.
 * 
 * @author Sana Development
 *
 */
public final class Uris {
 
	private static final String SCHEME_CONTENT = "content";
	
    public static final int NO_MATCH = -1;
    public static final int MATCH_ALL = 0;
    public static final int MATCH_TYPE = 1;
    public static final int MATCH_CONTENT = 2;
    public static final int MATCH_PACKAGE = 4;
    
	public static final int ITEMS = 1;
	public static final int ITEM_ID = 2;
	public static final int ITEM_UUID = 4;
	public static final int ITEM_RELATED = 8;
    public static final int ITEM_FILE = 16;

	public static final int TYPE_WIDTH = 8;
	public static final int TYPE_SHIFT = 0;
	private static final int TYPE_MASK = (1 << TYPE_WIDTH) -1; 
	
	public static final int CONTENT_WIDTH = 16;
	public static final int CONTENT_SHIFT = TYPE_WIDTH;
	private static final int CONTENT_MASK = ((1 << CONTENT_WIDTH) - 1) << CONTENT_SHIFT; 

        public static final int PACKAGE_WIDTH = 4;
	public static final int PACKAGE_SHIFT = CONTENT_WIDTH + CONTENT_SHIFT;
	private static final int PACKAGE_MASK = ((1 << PACKAGE_WIDTH) - 1) << PACKAGE_SHIFT; 
    
	
	public static final int DESCRIPTOR_WIDTH = PACKAGE_WIDTH + CONTENT_WIDTH + TYPE_WIDTH;
	public static final int NULL = UriMatcher.NO_MATCH;
	

	//--------------------------------------------------------------------------
	// Application, i.e. Package codes
	//--------------------------------------------------------------------------
	public static final String PACKAGE_AUTHORITY = "org.sana";
	public static final int PACKAGE_DIR = 0x000000001;
	/*
	public static final Uri INTENT_URI = buildContentUri(PACKAGE_AUTHORITY, "intent");

	/** Uri which identifies the application settings *
	public static final Uri SETTINGS_URI = buildContentUri(PACKAGE_AUTHORITY, "settings");
	
	/** Uri which identifies session activity *
	public static final Uri SESSION_URI =buildContentUri(PACKAGE_AUTHORITY, "session");
	
	
	public static final int PACKAGE = 1;
	public static final int SESSION = 2 << (DESCRIPTOR_WIDTH  - PACKAGE_SHIFT);
	public static final int SETTINGS = 4 << (DESCRIPTOR_WIDTH  - PACKAGE_SHIFT);

	public static final int PACKAGE_DIR = PACKAGE | ITEMS;
	public static final int SESSION_DIR = SESSION | ITEMS;
	public static final int SETTINGS_DIR = SETTINGS | ITEMS;

	public static final int SESSION_ITEM = SESSION | ITEM_ID;
	public static final int SETTINGS_ITEM = SETTINGS | ITEM_ID;
	
	public static final int SESSION_UUID = SESSION | ITEM_UUID;
	public static final int SETTINGS_UUID = SETTINGS | ITEM_UUID;
	
	static interface Sessions{
		static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.sana.session";
		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.sana.session";
	}
	
	static interface Settings{
		static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.sana.setting";
		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.sana.setting";
	}
	*/
	//--------------------------------------------------------------------------
	// Model codes
	//--------------------------------------------------------------------------
	public static final int CONCEPT = 1 << CONTENT_SHIFT;
	public static final int ENCOUNTER = 2 << CONTENT_SHIFT;
	public static final int EVENT = 4 << CONTENT_SHIFT;
	public static final int INSTRUCTION = 8 << CONTENT_SHIFT;
	public static final int NOTIFICATION = 6 << CONTENT_SHIFT;
	public static final int OBSERVATION = 32 << CONTENT_SHIFT;
	public static final int OBSERVER = 64 << CONTENT_SHIFT;
	public static final int PROCEDURE = 128 << CONTENT_SHIFT;
	public static final int RELATIONSHIP = 256 << CONTENT_SHIFT;
	public static final int SUBJECT = 512 << CONTENT_SHIFT;
	public static final int ENCOUNTER_TASK = 1024 << CONTENT_SHIFT;
	public static final int OBSERVATION_TASK = 2048 << CONTENT_SHIFT;
	
	// dir match codes OBJECT | ITEMS
	public static final int CONCEPT_DIR = CONCEPT | ITEMS;
	public static final int ENCOUNTER_DIR = ENCOUNTER | ITEMS;
	public static final int EVENT_DIR = EVENT | ITEMS;
	public static final int INSTRUCTION_DIR = INSTRUCTION | ITEMS;
	public static final int NOTIFICATION_DIR = NOTIFICATION | ITEMS;
	public static final int OBSERVATION_DIR = OBSERVATION | ITEMS;
	public static final int OBSERVER_DIR = OBSERVER | ITEMS;
	public static final int PROCEDURE_DIR = PROCEDURE | ITEMS;
	public static final int RELATIONSHIP_DIR = RELATIONSHIP | ITEMS;
	public static final int SUBJECT_DIR = SUBJECT | ITEMS;
	public static final int ENCOUNTER_TASK_DIR = ENCOUNTER_TASK | ITEMS;
	public static final int OBSERVATION_TASK_DIR = OBSERVATION_TASK | ITEMS;
	
	// item match codes OBJECT | ITEM_ID
	public static final int CONCEPT_ITEM = CONCEPT | ITEM_ID;
	public static final int ENCOUNTER_ITEM = ENCOUNTER | ITEM_ID;
	public static final int EVENT_ITEM = EVENT | ITEM_ID;
	public static final int INSTRUCTION_ITEM = INSTRUCTION | ITEM_ID;
	public static final int NOTIFICATION_ITEM = NOTIFICATION | ITEM_ID;
	public static final int OBSERVATION_ITEM = OBSERVATION | ITEM_ID;
	public static final int OBSERVER_ITEM = OBSERVER | ITEM_ID;
	public static final int PROCEDURE_ITEM = PROCEDURE | ITEM_ID;
	public static final int RELATIONSHIP_ITEM = RELATIONSHIP | ITEM_ID;
	public static final int SUBJECT_ITEM = SUBJECT | ITEM_ID;
	public static final int ENCOUNTER_TASK_ITEM = ENCOUNTER_TASK | ITEM_ID;
	public static final int OBSERVATION_TASK_ITEM = OBSERVATION_TASK | ITEM_ID;
	
	// item match codes OBJECT | ITEM_UUID
	public static final int CONCEPT_UUID = CONCEPT | ITEM_UUID;
	public static final int ENCOUNTER_UUID = ENCOUNTER | ITEM_UUID;
	public static final int EVENT_UUID = EVENT | ITEM_UUID;
	public static final int INSTRUCTION_UUID = INSTRUCTION | ITEM_UUID;
	public static final int NOTIFICATION_UUID = NOTIFICATION | ITEM_UUID;
	public static final int OBSERVATION_UUID = OBSERVATION | ITEM_UUID;
	public static final int OBSERVER_UUID = OBSERVER | ITEM_UUID;
	public static final int PROCEDURE_UUID = PROCEDURE | ITEM_UUID;
	public static final int RELATIONSHIP_UUID = RELATIONSHIP | ITEM_UUID;
	public static final int SUBJECT_UUID = SUBJECT | ITEM_UUID;
	public static final int ENCOUNTER_TASK_UUID = ENCOUNTER_TASK | ITEM_UUID;
	public static final int OBSERVATION_TASK_UUID = OBSERVATION_TASK | ITEM_UUID;
	
	// Matcher for mapping the Uri to code mappings 
	private static final UriMatcher mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static{
		/*
		// admin mappings
		mMatcher.addURI(PACKAGE_AUTHORITY, "/", PACKAGE_DIR);
		mMatcher.addURI(PACKAGE_AUTHORITY, "session/", SESSION_DIR);
		mMatcher.addURI(PACKAGE_AUTHORITY, "session/#", SESSION_ITEM);
		mMatcher.addURI(PACKAGE_AUTHORITY, "session/*", SESSION_UUID);
		mMatcher.addURI(PACKAGE_AUTHORITY, "settings/", SETTINGS_DIR);
		mMatcher.addURI(PACKAGE_AUTHORITY, "settings/#", SETTINGS_ITEM);
		mMatcher.addURI(PACKAGE_AUTHORITY, "settings/*", SETTINGS_UUID);
		*/
		mMatcher.addURI(Models.AUTHORITY, "/", PACKAGE_DIR);
		mMatcher.addURI(Models.AUTHORITY, "core/concept/", CONCEPT_DIR);
		mMatcher.addURI(Models.AUTHORITY, "core/concept/#", CONCEPT_ITEM);
		mMatcher.addURI(Models.AUTHORITY, "core/concept/*", CONCEPT_UUID);
		mMatcher.addURI(Models.AUTHORITY, "core/encounter/", ENCOUNTER_DIR);
		mMatcher.addURI(Models.AUTHORITY, "core/encounter/#", ENCOUNTER_ITEM);
		mMatcher.addURI(Models.AUTHORITY, "core/encounter/*", ENCOUNTER_UUID);
		mMatcher.addURI(Models.AUTHORITY, "core/event/", EVENT_DIR);
		mMatcher.addURI(Models.AUTHORITY, "core/event/#", EVENT_ITEM);
		mMatcher.addURI(Models.AUTHORITY, "core/event/*", EVENT_UUID);
		mMatcher.addURI(Models.AUTHORITY, "core/instruction/", INSTRUCTION_DIR);
		mMatcher.addURI(Models.AUTHORITY, "core/instruction/#", INSTRUCTION_ITEM);
		mMatcher.addURI(Models.AUTHORITY, "core/instruction/*", INSTRUCTION_UUID);
		mMatcher.addURI(Models.AUTHORITY, "core/notification/", NOTIFICATION_DIR);
		mMatcher.addURI(Models.AUTHORITY, "core/notification/#", NOTIFICATION_ITEM);
		mMatcher.addURI(Models.AUTHORITY, "core/notification/*", NOTIFICATION_UUID);
		mMatcher.addURI(Models.AUTHORITY, "core/observation/", OBSERVATION_DIR);
		mMatcher.addURI(Models.AUTHORITY, "core/observation/#", OBSERVATION_ITEM);
		mMatcher.addURI(Models.AUTHORITY, "core/observation/*", OBSERVATION_UUID);
		mMatcher.addURI(Models.AUTHORITY, "core/observer/", OBSERVER_DIR);
		mMatcher.addURI(Models.AUTHORITY, "core/observer/#", OBSERVER_ITEM);
		mMatcher.addURI(Models.AUTHORITY, "core/observer/*", OBSERVER_UUID);
		mMatcher.addURI(Models.AUTHORITY, "core/procedure/", PROCEDURE_DIR);
		mMatcher.addURI(Models.AUTHORITY, "core/procedure/#", PROCEDURE_ITEM);
		mMatcher.addURI(Models.AUTHORITY, "core/procedure/*", PROCEDURE_UUID);
		mMatcher.addURI(Models.AUTHORITY, "core/subject/", SUBJECT_DIR);
		mMatcher.addURI(Models.AUTHORITY, "core/subject/#", SUBJECT_ITEM);
		mMatcher.addURI(Models.AUTHORITY, "core/subject/*", SUBJECT_UUID);
		mMatcher.addURI(Models.AUTHORITY, "core/patient/", SUBJECT_DIR);
		mMatcher.addURI(Models.AUTHORITY, "core/patient/#", SUBJECT_ITEM);
		mMatcher.addURI(Models.AUTHORITY, "core/patient/*", SUBJECT_UUID);

		mMatcher.addURI(Models.AUTHORITY, "tasks/encounter/", ENCOUNTER_TASK_DIR);
		mMatcher.addURI(Models.AUTHORITY, "tasks/encounter/#", ENCOUNTER_TASK_ITEM);
		mMatcher.addURI(Models.AUTHORITY, "tasks/encounter/*", ENCOUNTER_TASK_UUID);
		mMatcher.addURI(Models.AUTHORITY, "tasks/observation/", OBSERVATION_TASK_DIR);
		mMatcher.addURI(Models.AUTHORITY, "tasks/observation/#", OBSERVATION_TASK_ITEM);
		mMatcher.addURI(Models.AUTHORITY, "tasks/observation/*", OBSERVATION_TASK_UUID);
		
	}
	
	/**
	 * Returns an int value describing the content and type represented by the
	 * Uri
	 * 
	 * @param uri The Uri to check.
	 * @return a value greater than 0 if the Uri was recognized or the value of
	 * 	{@link android.content.UriMatcher#NO_MATCH UriMatcher.NO_MATCH}.
	 * @throws IllegalArgumentException if the descriptor can not be determined
	 * 	or the UUID provided as a path segment is invalid.
	 */
	public static int getDescriptor(Uri uri){
		int result = mMatcher.match((uri !=null)? uri: Uri.EMPTY);
		if(result > -1){
			if(((result & TYPE_MASK) >> TYPE_SHIFT) == Uris.ITEM_UUID){
				String uuid = uri.getLastPathSegment();
				if(!UUIDUtil.isValid(uuid))
					throw new IllegalArgumentException("Invalid uuid format");
			}
		}
		return result;
	}
	
	/**
	 * Returns the content object class descriptor for the Uri.
	 * 
	 * @param uri
	 * @return
	 * @throws IllegalArgumentException if the descriptor can not be determined.
	 */
	public static int getContentDescriptor(Uri uri){
		int d = getDescriptor(uri) & CONTENT_MASK;
		return (d > -1)? d: UriMatcher.NO_MATCH;
	}
	
	/**
	 * The content type descriptor. If matched, it will return one of 
	 * ITEMS, ITEMS_ID, or ITEM_UUID
	 * 
	 * @param uri
	 * @return
	 * @throws IllegalArgumentException if the descriptor can not be determined.
	 */
	public static int getTypeDescriptor(Uri uri){
		int d = getDescriptor(uri) & TYPE_MASK;
		return (d > -1)? d: UriMatcher.NO_MATCH;
	}
	
	/**
	 * Builds a hierarchical Uri.
	 * 
	 * @param scheme
	 * @param authority
	 * @param path
	 * @return
	 */
    public static Uri buildUri(String scheme, String authority, String path){
        Uri uri = Uri.parse(scheme + "://");
        Uri.Builder builder = uri.buildUpon();
        builder.scheme(scheme).authority(authority);
        for(String s:path.split("/")){
            if(!TextUtils.isEmpty(s)){
                builder.appendPath(path);
            }
        }
        return builder.build();
    }
	
	/**
	 * Builds a hierarchical content style Uri.
	 *  
	 * @param authority The authority String 
	 * @param path
	 * @return
	 */
	public static Uri buildContentUri(String authority, String path){
		return buildUri(SCHEME_CONTENT,authority, path );
	}
	
	/**
	 * Parses the last path segment. This method performs no validation on the 
	 * format.
	 * 
	 * @param uri
	 * @return
	 */
	public static String parseUUID(Uri uri){
		return uri.getLastPathSegment();
	}
	
	/**
	 * Appends a uuid String as the last path segment. This method performs no
	 * validation on the format.
	 * 
	 * @param uri
	 * @param uuid
	 * @return
	 */
    public static Uri withAppendedUuid(Uri uri, String uuid){
        // verify that the Uri is valid
        if(Uris.isEmpty(uri))
            throw new NullPointerException("Empty uri. Can not append UUID");
        Uri result = Uri.parse(uri.toString() + "/" + uuid);
        return result;
    }


	/**
	 * Will return the mime type represented by the Uri. For item or uuid 
	 * matches-i.e. path matching {@literal value/#} and {@literal value/*} 
	 * respectively, it will return a type beginning with 
	 * "vnd.android.cursor.item", otherwise, a type beginning with 
	 * "vnd.android.cursor.dir" will be returned.
	 *
	 * @param uri
	 * @return The mime type for the Uri
	 * @throws IllegalArgumentException if the mime type can not be determined.
	 */
	public static String getType(Uri uri) {
		switch (getDescriptor(uri)){
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
		case ENCOUNTER_TASK_DIR:
			return EncounterTasks.CONTENT_TYPE;
		case ENCOUNTER_TASK_UUID:
		case ENCOUNTER_TASK_ITEM:
			return EncounterTasks.CONTENT_ITEM_TYPE;
		case OBSERVATION_TASK_DIR:
			return ObservationTasks.CONTENT_TYPE;
		case OBSERVATION_TASK_UUID:
		case OBSERVATION_TASK_ITEM:
			return ObservationTasks.CONTENT_ITEM_TYPE;
		case PACKAGE_DIR:
			return "application/vnd.android.package-archive";
		default:
			throw new IllegalArgumentException("Invalid uri. No match");
		}
	}
	
	/**
	 * Returns whether a Uri is a content directory type. The mime types for 
	 * directory uris should typically start with ""vnd.android.cursor.dir".
	 * 
	 * @param uri the Uri to check
	 * @return true if the Uri may refer to multiple objects
	 * @throws IllegalArgumentException if the type can not be determined.
	 */
	public static boolean isDirType(Uri uri){
		return (getContentDescriptor(uri) & TYPE_MASK) == ITEMS;
	}
	
	/**
	 * Returns true if the Uri refers to a single item type. The mime types for 
	 * single item uris should should start with "vnd.android.cursor.item".
	 * 
	 * @param uri the Uri to check
	 * @return true if the Uri may refer to multiple objects
	 * @throws IllegalArgumentException if the type can not be determined.
	 */
	public static boolean isItemType(Uri uri){
		int val = getContentDescriptor(uri) & TYPE_MASK;
		return (val == ITEM_ID) || (val == ITEM_UUID);
	}
	
	public static boolean isPackage(Uri uri){
		return (uri.getScheme().equals("package"));
	}
	
	/**
	 * Returns true if the Uri is null or equal to Uri.EMPTY 
	 * @param uri
	 * @return
	 */
	public static boolean isEmpty(Uri uri){
		return (uri != null && !uri.equals(Uri.EMPTY))? false: true;
	}
	

    public static boolean isTask(Uri uri){
        int d = getTypeDescriptor(uri);
        if(d == UriMatcher.NO_MATCH) return false;
        return ((d == ENCOUNTER_TASK) ||
                (d == OBSERVATION_TASK))? true: false; 
    }

    public static final boolean filterEquals(Uri from, Uri to, int flags){
        boolean result = false;
        if(flags == MATCH_ALL){
            result = (getDescriptor(from) == getDescriptor(to));
        } else {
            // TODO implement this
        }
        return result;
    }

    public static final boolean filterEquals(Uri from, Uri to){
        return filterEquals(from,to, MATCH_ALL);
    }
	/**
	 * Copy constructor utility.
	 * 
	 * @param uri The uri to copy.
	 * @return A copy of the input parameter or Uri.EMPTY;
	 */
    public static Uri copyInstance(Uri uri){
    	return (uri == null)? Uri.EMPTY: Uri.parse(uri.toString());
    }
    
    public static Uri iriToUri(Uri iri){
    	Uri result = Uri.EMPTY;
    	
    	return result;
    }
    
    /**
     * Utility function to return a Uri whose path can be used for web based 
     * services. POST and PUT methods will remove the trailing ID or UUID if
     * present.
     * @param iri 
     * @param method
     * @return
     */
    public static Uri iriToUri(Uri iri, String method){
        Uri result = Uri.EMPTY;
        
        return result;
    }
    
    /**
     * Converts Android "content" style resource identifiers to URIs to use with the
     * new MDS REST API. Only works for those objects whose path components in the new 
     * REST API are consistent with MDS.
     * 
     * @param uri The internal resource identifier to convert.
     * @param scheme The scheme to use for the conversion
     * @param host The mds host
     * @param port The mds port to talk to.
     * @param rootPath Additional 
     * @return
     * @throws MalformedURLException 
     * @throws URISyntaxException 
     */
	public static URI iriToURI(Uri uri, String scheme, String host, int port, String rootPath) 
			throws MalformedURLException, URISyntaxException
	{
		String path = String.format("%s%s", rootPath, uri.getPath());
                if(!path.endsWith("/"))
                    path = path + "/";
		String query = uri.getEncodedQuery();
		URL url = null;
		if(!TextUtils.isEmpty(query)){
			path = String.format("%s%s?%s", path,query);
			url = new URL(scheme, host, port, path);
		} else {String.format("%s%s", rootPath, uri.getPath());
			url = new URL(scheme, host, port, path);
		}
		URI u  = url.toURI();
		return u;
	}

}
