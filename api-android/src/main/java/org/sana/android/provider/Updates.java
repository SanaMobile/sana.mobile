package org.sana.android.provider;

import android.net.Uri;

/**
 * Contract for application packages
 */
public final class Updates {
    public static final String TAG = Updates.class.getSimpleName();

    /** The content:// style URI for this content provider. */
    public static final Uri CONTENT_URI = Uri.parse("content://"
            + Models.AUTHORITY + "/mds/core/session/");
}
