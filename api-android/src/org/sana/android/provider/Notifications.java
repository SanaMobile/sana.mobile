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

import org.sana.core.Notification;

import android.net.Uri;

/**
 * This class defines the URI and data fields for the content provider 
 * storing notifications. Notifications are SMS messages sent to the 
 * Android phone. They may contain doctor recommendations or diagnoses.
 * @author Sana Development Team
 */
public final class Notifications{
    
    /**
     * The authority for the notification provider.
     */
    public static final String AUTHORITY = "org.sana.provider";
    
    /**
     * The content:// style URI for this content provider.
     */
    public static final Uri CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/notification");

    /**
     * The MIME type of CONTENT_URI providing a directory of notifications.
     */
    public static final String CONTENT_TYPE = 
    	"vnd.android.cursor.dir/org.sana.notification";

    /**
     * The MIME type of CONTENT_URI subdirectory for a single notification.
     */
    public static final String CONTENT_ITEM_TYPE = 
    	"vnd.android.cursor.item/org.sana.notification";

    /**
     * Default sort order.
     */
    public static final String DEFAULT_SORT_ORDER = "modified DESC";

    private Notifications() {}
    
    /**
     * Contract for the Concept table in the database.
     * 
     * @author Sana Development
     *
     */
    public static interface Contract extends BaseContract<Notification>{
    	/**
    	 * The unique id of the notification.
    	 */
    	public static final String UUID = "uuid";

    	/**
    	 * The procedure the notification is referring to.
    	 */
    	public static final String PROCEDURE_ID = "procedure_id";

    	/**
    	 * The id of the patient the notification is for.
    	 */
    	public static final String PATIENT_ID = "patient_id";

    	/**
    	 * The message body, containing part of the transmitted message. 
    	 * Messages may take more than one notification to transmit.
    	 */
    	public static final String MESSAGE = "message";

    	/**
    	 * The entire message, including header information specifying the 
    	 * patient and encounter the notification is associated with.
    	 */
    	public static final String FULL_MESSAGE = "full_message";

    	/**
    	 * Indicates whether the entire message is downloaded.
    	 */
    	public static final String DOWNLOADED = "downloaded";

    	/**
    	 * The date the notification was created.
    	 */
    	public static final String CREATED = "created";

    	/**
    	 * The date the notification was last modified.
    	 */
    	public static final String MODIFIED = "modified";
    }
}