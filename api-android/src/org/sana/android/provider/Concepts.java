package org.sana.android.provider;

import org.sana.core.Concept;

import android.net.Uri;

/**
 * Meta data and contract for the Concept model.
 * 
 * @author Sana Development
 */
public final class Concepts {
	
	public static final String AUTHORITY = "org.sana.provider";

	public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

	/**
     * The MIME type of CONTENT_URI providing a directory of patients.
     */
	public static final String CONTENT_TYPE = 
		"vnd.android.cursor.dir/org.sana.concept";
	
	/** The content type of {@link #CONTENT_URI} for a single instance. */
	public static final String CONTENT_ITEM_TYPE = 
		"vnd.android.cursor.item/org.sana.concept";

	/**
     * The content:// style URI for this content provider.
     */
	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/core/concept");

	private Concepts(){}
	
	/**
	 * Contract for the Concept table in the database.
	 * 
	 * @author Sana Development
	 *
	 */
	public static interface Contract extends BaseContract<Concept>{

        /** A universally unique identifier */
        public static final String UUID = "uuid";

        /** A machine friendly short name or identifier */
        public static final String NAME = "name";

        /** A human readable short name or identifier */
        public static final String DISPLAY_NAME = "displayname";

        /** Longer narrative description. */
        public static final String DESCRIPTION = "description";

        /** Primitive data type. i.e. Date, String */
        public static final String DATA_TYPE = "datatype";

        /** mime type */
        public static final String MEDIA_TYPE = "mediatype";

        /** Some limitation on acceptable values */
    	public static final String CONSTRAINT = "constraints";
    }
}
