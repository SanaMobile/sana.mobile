package org.sana.android.provider;

import android.net.Uri;

/**
 * Contract for Session objects
 */
public class Sessions {
    public static final String TAG = Sessions.class.getSimpleName();

    /** The content:// style URI for this content provider. */
    public static final Uri CONTENT_URI = Uri.parse("content://"
            + Models.AUTHORITY + "/core/session/");
}
