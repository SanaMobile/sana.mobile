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

import org.sana.core.Instruction;

import android.net.Uri;

/**
 * Metadata and contract for instructions in the database. Not currently 
 * implemented in the database but included for forward compatibility.
 * 
 * @author Sana Development Team
 */
public class Instructions{

	/** The authority for instructions. */
	public static final String AUTHORITY = "org.sana.provider";

	/** The content:// style URI for this content provider. */
	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/instruction");

	/** The MIME type for a directory of instructions.  */
	public static final String CONTENT_TYPE = 
			"vnd.android.cursor.dir/org.sana.instruction";

	/** The MIME type of a single instruction. */
	public static final String CONTENT_ITEM_TYPE = 
			"vnd.android.cursor.item/org.sana.instruction";

	/** The default sort order. */
	public static final String DEFAULT_SORT_ORDER = "modified DESC";


	/**
	 * Column definitions for the Instruction table.
	 * 
	 * @author Sana Development
	 */
	public static interface Contract extends BaseContract<Instruction>{
		/** */
		public static final String CONCEPT = "concept";

		/** Additional information for the instruction */
		public static final String HELP = "help";
		/** User prompt */
		public static final String HINT = "hint";
		/** Conditional notification message */
		public static final String ALERT = "alert";
		/** Boolean value for whether null values are allowed */
		public static final String REQUIRED = "required";
	}
}
