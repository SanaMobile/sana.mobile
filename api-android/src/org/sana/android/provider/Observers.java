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

import org.sana.core.Observer;

import android.net.Uri;

/**
 * Metadata and contract for procedures in the database.
 * 
 * @author Sana Development Team
 */
public class Observers{

	private Observers(){}
	/** The authority for observers. */
	public static final String AUTHORITY = "org.sana.provider";

	/** The content:// style URI for this content provider. */
	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/core/observer");

	/** The MIME type for a directory of procedures.  */
	public static final String CONTENT_TYPE = 
			"vnd.android.cursor.dir/org.sana.observer";

	/** The MIME type of single procedure. */
	public static final String CONTENT_ITEM_TYPE = 
			"vnd.android.cursor.item/org.sana.observer";

	/** The default sort order. */
	public static final String DEFAULT_SORT_ORDER = "modified DESC";

	/**
	 * Column definitions for the Observer table.
	 * 
	 * @author Sana Development
	 */
	public static interface Contract extends BaseContract<Observer>{
		/** The author of the procedure. */
		public static final String USERNAME = "username";

		/** The unique ID of the procedure. */
		public static final String PASSWORD = "password";

		/** The raw procedure text. */
		public static final String ROLE = "role";
		
		/** Primary contact information for this person */
		public static final String CONTACT1 = "contact1";

		/** Secondary contact information for this person */
		public static final String CONTACT2 = "contact2";
	}
}
