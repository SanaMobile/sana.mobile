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


import org.sana.core.Encounter;

import android.net.Uri;

/**
 * This class defines the URI and data fields for the content provider 
 * storing the text for a completed procedure form, referred to as a saved 
 * procedure.
 * 
 * @author Sana Development Team
 */
public final class Encounters {
    
    /** The authority for the encounter provider. */
	public static final String AUTHORITY = "org.sana.provider";
    
    /** The content:// style URI for encounters.
     */
    public static final Uri CONTENT_URI = Uri.parse("content://"
            + Encounters.AUTHORITY + "/core/encounter");

    /**
     * The MIME type of CONTENT_URI providing a directory of saved procedures.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.sana.encounter";

    /**
     * The MIME type of CONTENT_URI subdirectory of a single saved procedure.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.sana.encounter";

    /**
     * The default sort order.
     */
    public static final String DEFAULT_SORT_ORDER = Contract.MODIFIED +" DESC";
    
    /**
     * Sort by descending created column
     */
    public static final String CREATED_SORT_ORDER = Contract.CREATED +" DESC";
    
    /**
     * Sort by ascending upload_queue column
     */
    public static final String QUEUE_SORT_ORDER = Contract.UPLOAD_QUEUE +" ASC";

	private Encounters() {}
	
	/**
	 * Contract for the Concept table in the database.
	 * 
	 * @author Sana Development
	 *
	 */
    public static interface Contract extends BaseContract<Encounter>{
    	/**
    	 * The guid of this procedure (randomly generated on insert).
    	 */
    	public static final String UUID = "uuid";

    	/**
    	 * A foreign key to the procedure used to create this saved procedure.
    	 */
    	public static final String PROCEDURE = "procedure";

    	/** The JSON data representing the state of this procedure. */
    	@Deprecated
    	public static final String STATE = "_state";


    	/** The subject who data was collected about; i.e. the patient */
    	public static final String SUBJECT = "subject";

    	/**
    	 * The entity which collected the data
    	 */
    	public static final String OBSERVER = "observer";
    	
    	/**
    	 * Status of data inputting. When finished, upload data to MDS.
    	 */
    	public static final String FINISHED = "_finished";

    	// This procedure's text/state has been uploaded to the MDS
    	// successfully. This doesn't mean its binaries have been -- only the
    	// text.
    	/**
    	 * Status of procedure upload to MDS. Does not indicate status of 
    	 * binary upload -- only the text
    	 */
    	public static final String UPLOADED = "_uploaded";

    	// Status of the procedure in the upload queue
    	// For use in SavedProcedureList to show each procedure's status
    	// 0 - Was never put into queue, or "Not Uploaded"
    	// 1 - Still in the queue waiting to be sent
    	// 2 - Upload Successful - has been sent to the MDS, no longer in queue
    	// 3 - Upload in progress
    	// 4 - In the queue but waiting for connectivity to upload
    	// 5 - Upload failed
    	// 6 - Upload stalled - username/password incorrect
    	/**
    	 * Status of the procedure in the upload queue<br>
    	 * <pre><blockquote>
    	 * 0 - Was never put into queue, or "Not Uploaded"
    	 * 1 - Still in the queue waiting to be sent
    	 * 2 - Upload Successful - has been sent to the MDS, no longer in queue
    	 * 3 - Upload in progress
    	 * 4 - In the queue but waiting for connectivity to upload
    	 * 5 - Upload failed
    	 * 6 - Upload stalled - username/password incorrect
    	 * </blockquote></pre>
    	 */
    	public static final String UPLOAD_STATUS = "_queue_status";

    	/**
    	 * Keeps track of the background upload queue.
    	 * <pre><blockquote>
    	 * >=0 - In queue, shows position in line
    	 * -1 - Not in queue (either never added or upload finished)
    	 * </blockquote></pre>
    	 */
    	public static final String UPLOAD_QUEUE = "_upload_queue";
    }
}