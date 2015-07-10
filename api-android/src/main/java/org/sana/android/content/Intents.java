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

import java.util.HashMap;
import java.util.Map;

import org.sana.android.util.Logf;

import android.content.Intent;
import android.text.TextUtils;
import android.util.SparseArray;

/**
 * 
 * @author Sana Development
 *
 */
public final class Intents{
	
	private static final String TAG = Intents.class.getSimpleName();
	
	protected static final int ACTION_WIDTH = 16;
	
	protected static final int ACTION_MASK = ((1 << ACTION_WIDTH) - 1);	
	
	/** The bit width of the intent descriptor */
	public static final int DESCRIPTOR_WIDTH = 32;
	
    //-------------------------------------------------------------------------
	//	Base action codes
	//-------------------------------------------------------------------------	
	public static final int NULL = -1;
	public static final int MAIN = 1;
	public static final int RUN = 2;
	public static final int INSERT = 4 ;
	public static final int INSERT_OR_EDIT = 8;
	public static final int VIEW = 16;
	public static final int EDIT = 32;
	public static final int DELETE = 64;
	public static final int PICK = 128;
	public static final int ATTACH_DATA = 256;
	public static final int CHOOSER = 512;
	public static final int GET_CONTENT = 512;
	public static final int DIAL = 1024;
	public static final int CALL = 2048;
	public static final int SEND = 4096;
	public static final int SENDTO = 8192;
	public static final int ANSWER = 16384;
	public static final int SYNC =  32768;
	public static final int PICK_ACTIVITY = 65536;
	public static final int SEARCH = 131072;
	public static final int WEB_SEARCH = 262144;
	
    //-------------------------------------------------------------------------
	//	Request Codes
	//-------------------------------------------------------------------------
	/** Intent request code for picking a procedure */
    public static final int PICK_PROCEDURE = PICK | Uris.PROCEDURE_DIR;
    
    /** Intent request code for picking a saved procedure */
    public static final int PICK_ENCOUNTER = PICK | Uris.ENCOUNTER_DIR;
    
    /** Intent request code for picking a notification */
    public static final int PICK_NOTIFICATION = PICK | Uris.NOTIFICATION_DIR;
    
    /** Intent request code to start running a procedure */
    public static final int RUN_PROCEDURE = RUN | Uris.PROCEDURE_DIR;
    
    /** Intent request code to resume running a saved procedure*/
    public static final int RESUME_PROCEDURE = EDIT | Uris.PROCEDURE_DIR;
    
    /** Intent request code to start running a procedure */
    public static final int EXECUTE_ENCOUNTER_TASK = RUN | Uris.ENCOUNTER_TASK_DIR;

    /** INtent request code to view settings */
    //public static final int SETTINGS = PICK | Uris.SETTINGS_DIR;
    
    /** Intent request code for creating a new patient. */
    public static final int CREATE_PATIENT = INSERT | Uris.SUBJECT_DIR;
    
    /** Intent request code for viewing all patients. */
    public static final int PICK_PATIENT = PICK | Uris.SUBJECT_DIR;
    
    /** Intent request code for creating a new patient. */
    //public static final int INSERT_SESSION = INSERT | Uris.SESSION_DIR;
	
    //-------------------------------------------------------------------------
	// Activity action strings 
	//-------------------------------------------------------------------------
	/** Effectively an action to indicate a call to Activity.finish()  */
    public static final String ACTION_RESPOND_SUCCESS = "org.sana.intent.action.RESPOND_SUCCES";
    public static final String ACTION_RESPOND_FAIL = "org.sana.intent.action.RESPOND_FAIL";
    public static final String ACTION_FINISH = "org.sana.android.intent.action.FINISH";
    
    public static final String ACTION_CANCEL = "org.sana.android.intent.action.CANCEL";
    
    public static final String ACTION_OK = "org.sana.android.intent.action.OK";
    
    
    /** Intent action string for picking the activity to run next. */
    public static final String ACTION_PICK_ACTIVITY = "org.sana.android.intent.action.PICK_ACTIVITY";
    
	/** Intent action string for picking a procedure */
    public static final String ACTION_PICK_PROCEDURE = "org.sana.android.intent.action.PICK_PROCEDURE";
    
    /** Intent action string for picking a saved procedure */
    public static final String ACTION_PICK_ENCOUNTER = "org.sana.android.intent.action.PICK_ENCOUNTER";
    
    /** Intent action string for picking a notification */
    public static final String ACTION_PICK_NOTIFICATION = "org.sana.android.intent.action.PICK_NOTIFICATION";
    
    /** Intent action string for picking a saved procedure */
    public static final String ACTION_PICK_OBSERVER = "org.sana.android.intent.action.PICK_OBSERVER";
    
    /** Intent action string to start running a procedure */
    public static final String ACTION_RUN_PROCEDURE = "org.sana.android.intent.action.RUN_PROCEDURE";
    
    /** Intent action string to resume running a saved procedure*/
    public static final String ACTION_RESUME_PROCEDURE = "org.sana.android.intent.action.EDIT_PROCEDURE";
    
    /** Intent action string to view settings */
    public static final String ACTION_SETTINGS = "org.sana.android.intent.action.EDIT_SETTINGS";
    
    /** Intent action string for creating a new patient. */
    public static final String ACTION_INSERT_SUBJECT = "org.sana.android.intent.action.INSERT_SUBJECT";
    
    /** Intent action string for viewing all patients. */
    public static final String ACTION_PICK_SUBJECT = "org.sana.android.intent.action.PICK_SUBJECT";
	
    /** Intent action string for creating a new patient. */
    public static final String ACTION_INSERT_SESSION = "org.sana.android.intent.action.INSERT_SESSION";
    
    /** Intent action string for creating a new patient. */
    public static final String ACTION_VIEW_SESSION = "org.sana.android.intent.action.VIEW_SESSION";
    
    /** Intent action string for creating a new patient. */
    public static final String ACTION_UPDATE_SESSION = "org.sana.android.intent.action.UPDATE_SESSION";
    
    /** Intent action string for creating a new patient. */
    public static final String ACTION_DELETE_SESSION = "org.sana.android.intent.action.DELETE_SESSION";
    
    /** Intent action string for the main activity. */
    public static final String ACTION_MAIN = "org.sana.android.intent.action.MAIN";
    //-------------------------------------------------------------------------
    // CRUD action strings for the dispatcher
    //-------------------------------------------------------------------------
    /** Indicates data has been created and should be pushed upstream as necessary */
    public static final String ACTION_CREATE = "org.sana.intent.action.CREATE";
    /** Indicates data needs to be read or fetched from upstream as necessary */
    public static final String ACTION_READ = "org.sana.intent.action.READ";
    /** Indicates data has been updated and should be pushed upstream as necessary */
    public static final String ACTION_UPDATE = "org.sana.intent.action.UPDATE";
    /** Indicates data has been removed and should be removed upstream as anecessary */
    public static final String ACTION_DELETE = "org.sana.intent.action.DELETE";
    
    /** General action to indicate the action is intended to complete a predefined task */
    public static final String ACTION_EXECUTE_TASK = "org.sana.intent.action.EXECUTE_TASK";

	private static final Map<String, Integer> sActionCodes = new HashMap<String, Integer>(18);
	static{
		sActionCodes.put(Intent.ACTION_MAIN, MAIN);
		sActionCodes.put(Intent.ACTION_RUN,RUN);
		sActionCodes.put(Intent.ACTION_INSERT,INSERT);
		sActionCodes.put(Intent.ACTION_INSERT_OR_EDIT,INSERT_OR_EDIT);
		sActionCodes.put(Intent.ACTION_VIEW, VIEW);
		sActionCodes.put(Intent.ACTION_EDIT,EDIT);
		sActionCodes.put(Intent.ACTION_DELETE,DELETE);
		sActionCodes.put(Intent.ACTION_PICK,PICK);
		sActionCodes.put(Intent.ACTION_ATTACH_DATA,ATTACH_DATA);
		sActionCodes.put(Intent.ACTION_CHOOSER,CHOOSER);
		sActionCodes.put(Intent.ACTION_GET_CONTENT,GET_CONTENT);
		sActionCodes.put(Intent.ACTION_DIAL,DIAL);
		sActionCodes.put(Intent.ACTION_CALL,CALL);
		sActionCodes.put(Intent.ACTION_SEND,SEND);
		sActionCodes.put(Intent.ACTION_SENDTO,SENDTO);
		sActionCodes.put(Intent.ACTION_ANSWER,ANSWER);
		sActionCodes.put(Intent.ACTION_SYNC,SYNC);
		sActionCodes.put(Intents.ACTION_PICK_ACTIVITY,PICK_ACTIVITY);
		sActionCodes.put(Intent.ACTION_SEARCH,SEARCH);
		sActionCodes.put(Intent.ACTION_WEB_SEARCH,WEB_SEARCH);
                // CRUD Actions - these are essentially synonyms for others
		sActionCodes.put(ACTION_CREATE, INSERT);
		sActionCodes.put(ACTION_READ, VIEW);
		sActionCodes.put(ACTION_UPDATE, EDIT);
		sActionCodes.put(ACTION_DELETE, DELETE);
                // Additional sana defined actions
		sActionCodes.put(ACTION_EXECUTE_TASK, MAIN);
	}
    //-------------------------------------------------------------------------
    // Activity category strings 
    //-------------------------------------------------------------------------
    public static final String CATEGORY_AUTHENTICATE = "org.sana.android.intent.category.AUTHENTICATE";
    public static final String CATEGORY_OBSERVABLE = "org.sana.android.intent.category.OBSERVABLE";
    public static final String CATEGORY_LEXICAL = "org.sana.android.intent.category.LEXICAL";
    public static final String CATEGORY_TASK = "org.sana.android.intent.category.TASK";
    public static final String CATEGORY_TASK_COMPLETE = "org.sana.intent.category.TASK_COMPLETE";

    //-------------------------------------------------------------------------
    // Intent extra keys
    //-------------------------------------------------------------------------
    /** The int value of the original request code used when starting an Activity. */ 
    public static final String EXTRA_REQUEST_CODE = "org.sana.android.intent.REQUEST_CODE";

	/** The int value of the original request code used when starting an Activity. */ 
	public static final String EXTRA_TOKEN = "org.sana.android.intent.TOKEN";
	
	/** A string unique key value for an unvalidated session instance. */ 
	public static final String EXTRA_INSTANCE = "org.sana.android.intent.extra.INSTANCE";

	/** A unique string value for a validated session. */  
	public static final String EXTRA_SESSION = "org.sana.android.intent.extra.SESSION";
	
	/** An Intent representing the previous state or request that was sent */
	public static final String EXTRA_REQUEST = "org.sana.android.intent.extra.REQUEST";

	/** A content: Uri holding the current concept */
	public static final String EXTRA_CONCEPT = "org.sana.android.intent.extra.CONCEPT";
	
	/** A content: Uri holding the current encounter */
	public static final String EXTRA_ENCOUNTER = "org.sana.android.intent.extra.ENCOUNTER";
	
	/** A content: Uri holding the current event */
	public static final String EXTRA_EVENT = "org.sana.android.intent.extra.EVENT";
	
	/** A content: Uri holding the current instruction */
	public static final String EXTRA_INSTRUCTION = "org.sana.android.intent.extra.INSTRUCTION";
	
	/** A content: Uri holding the current notification */
	public static final String EXTRA_NOTIFICATION = "org.sana.android.intent.extra.NOTIFICATION";
	
	/** A content: Uri holding the current observation */
	public static final String EXTRA_OBSERVATION = "org.sana.android.intent.extra.OBSERVATION";
	
	/** A content: Uri holding the current observer */
	public static final String EXTRA_OBSERVER = "org.sana.android.intent.extra.OBSERVER";
	
	/** A content: Uri holding the current procedure */
	public static final String EXTRA_PROCEDURE = "org.sana.android.intent.extra.PROCEDURE";
	public static final String EXTRA_PROCEDURE_ID = "org.sana.android.intent" +
			".extra.PROCEDURE_ID";
	
	/** A content: Uri holding the current subject */ 
	public static final String EXTRA_SUBJECT = "org.sana.android.intent.extra.SUBJECT";
	
	/** A content: Uri holding a task */ 
	public static final String EXTRA_TASK = "org.sana.android.intent.extra.TASK";

        /** An array of content Uris holding zero or more tasks */
	public static final String EXTRA_TASKS = "org.sana.android.intent.extra.TASKS";
	
	/** A content: Uri holding the current encounter task */ 
	public static final String EXTRA_TASK_ENCOUNTER = "org.sana.android.intent.extra.task.ENCOUNTER";
	
	/** A content: Uri holding the current observation task */ 
	public static final String EXTRA_TASK_OBSERVATION = "org.sana.android.intent.extra.task.OBSERVATION";

	/** */
	public static final String EXTRA_ON_COMPLETE = "org.sana.android.intent" +
			".extra.ON_COMPLETE";

	public static final String SERVICE_DISPATCH = "org.sana.service.START_DISPATCH";
	


    //-------------------------------------------------------------------------
    // Intent flags
	//-------------------------------------------------------------------------

    /** Indicates message should be placed into the retry queue on failure */
    public static final int FLAG_RETRY_ON_FAIL = 1;
    /** Indicates message should be placed into the retry queue on failure */
    public static final int FLAG_REPLACE = 2;
    /** Set to  indicate notifications should be sent  */
    public static final int FLAG_NOTIFY = 4;
    /** Set to  indicate notifications should be sent when the action of the intent has been started */
    public static final int FLAG_NOTIFY_TASK_INIT = 8;
    /** Set to  indicate notifications should be sent after the action of the intent has completed  */
    public static final int FLAG_NOTIFY_TASK_COMPLETE = 16;

	/**
	 * Copies an intent into a new one including all data, type, extras, and 
	 * categories.
	 * 
	 * @param intent the Intent to copy.
	 * @return A copy of the original Intent.
	 */
	public static Intent copyOf(Intent intent){
		Intent newIntent = new Intent();
		newIntent.fillIn(intent, Intent.FILL_IN_ACTION);
		newIntent.fillIn(intent, Intent.FILL_IN_CATEGORIES);
		newIntent.fillIn(intent, Intent.FILL_IN_DATA);
		if(intent.getExtras() != null)
			newIntent.putExtras(intent.getExtras());
		return newIntent;
	}
	
	/**
	 * Returns an int descriptor code for an intent by checking whether it first 
	 * has a an Intent extra keyed to {@link #NULL} and then by the action String
	 * if set.
	 * 
	 * @param intent The intent to check for a recognized action descriptor.
	 * @return The descriptor or {@link #NULL} if not recognized.
	 * @throws NullPointerException if <code>intent</code> is null.
	 */
	public static int parseActionDescriptor(Intent intent){
		Integer code = intent.getIntExtra(Intents.EXTRA_REQUEST_CODE, Intents.NULL);
		// if the code was explicitly set as part of the extra as in the case
		// where we are preserving the original action as part of a request code
		if(code == Intents.NULL){
			String action = intent.getAction();
			if(!TextUtils.isEmpty(action)){
				code = sActionCodes.get(action);
				Logf.D(TAG,  "getActionDescriptor(Intent)", 
						"'action' : " + action + ", 'code': " + code);
				// indexOf method returns a negative number if not found
				// set code to standard null value of -1 if not found.
				code = (code != null)? code: Intents.NULL;
			} else 
				Logf.D(TAG,  "getActionDescriptor(Intent)", "'action' : null");
		}
		return code;
	}
	
	/**
	 * Returns whether an Intent with an action String can reasonably be 
	 * expected to return a result. If the intent doesn't refer to an Activity
	 * this is meaningless.
	 * 
	 * @param intent
	 * @return true if a data Uri should be returned.
	 */
	public static boolean startForResult(Intent intent){
		final int descriptor = Intents.parseActionDescriptor(intent);
		Logf.D(TAG, "startForResult(Intent)", "descriptor");
		switch(descriptor){
		case PICK:
		case EDIT:
		case INSERT:
		case INSERT_OR_EDIT:
		case GET_CONTENT:
		case PICK_ACTIVITY:
			return true;
		default:
			return false;
		}
	}
	
}