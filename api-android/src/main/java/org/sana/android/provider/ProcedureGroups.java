/**
 * Copyright (c) 2013, Sana
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the Sana nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
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

import android.net.Uri;

import org.sana.core.ProcedureGroup;

/**
 * Metadata and contract for procedure groups in the database.
 *
 * @author Sana Development Team
 */
public class ProcedureGroups {

    /** The authority for procedure groups. */
    public static final String AUTHORITY = "org.sana.provider";

    /** The content:// style URI for this content provider. */
    public static final Uri CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/core/proceduregroup");

    /** The MIME type for a directory of procedure groups.  */
    public static final String CONTENT_TYPE =
            "vnd.android.cursor.dir/org.sana.proceduregroup";

    /** The MIME type of single procedure group. */
    public static final String CONTENT_ITEM_TYPE =
            "vnd.android.cursor.item/org.sana.proceduregroup";

    /** The default sort order. */
    public static final String DEFAULT_SORT_ORDER = "modified DESC";

    /**
     * Contract for the Procedure Groups table in the database.
     *
     * @author Sana Development
     *
     */
    public interface Contract extends BaseContract<ProcedureGroup> {
        /** The title of the procedure group. */
        String TITLE = "title";

        /** The author of the procedure group. */
        String AUTHOR = "author";

        /** The description of the procedure group. */
        String DESCRIPTION = "description";

        /** A list of ids of the procedures. */
        String PROCEDURE_NAMES = "procedure_names";
    }
}
