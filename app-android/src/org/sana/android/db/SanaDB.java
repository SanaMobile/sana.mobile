package org.sana.android.db;


import android.net.Uri;
import android.provider.BaseColumns;

/**
 * This class defines the URI and data fields of the content providers for each 
 * of the content types uploaded to the MDS.
 * 
 * @author Sana Development Team
 */
@Deprecated
public final class SanaDB {
    
    /**
     * The authority for the sound provider.
     */
    public static final String BINARY_AUTHORITY = "org.sana.provider.Binary";
    
    /**
     * The authority for the image provider.
     */
    public static final String IMAGE_AUTHORITY = "org.sana.provider.Image";
    
    /**
     * The authority for the sound provider.
     */
    public static final String SOUND_AUTHORITY = "org.sana.provider.Sound";
    
    /**
     * The authority for the helpinfo provider.
     */
    public static final String EDUCATIONRESOURCE_AUTHORITY = 
    	"org.sana.provider.EducationResource";
    
    /**
     * Name of SQLite database the content is stored in.
     */
    public static final String DATABASE_NAME = "sana.db";
    
    /**
     * SQLite database version.
     * 
     * Versions:
     * 1 - 1.0 release
     * 2 - Development versions between 1.0 and 1.1
     * 3 - 1.1 Release
     * 4 - Development versions between 1.1and 1.2
     * ...
     */
    public static final int DATABASE_VERSION = 4; 
    
    /**
     * This class defines the URI and data fields for the content provider 
     * storing the metadata for a binary file collected during a procedure.
     * 
     * @author Sana Development Team
     */
    public static final class BinarySQLFormat implements BaseColumns {
        private BinarySQLFormat() {
        }

        /**
         * The content:// style URI for this content provider.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + BINARY_AUTHORITY + "/binaries" );

        /** The MIME type of CONTENT_URI providing a directory of binaries.*/
        public static final String CONTENT_TYPE = 
        	"vnd.android.cursor.dir/org.sana.binary";

        /** The MIME type of CONTENT_URI subdirectory for a single binary.*/
        public static final String CONTENT_ITEM_TYPE = 
        	"vnd.android.cursor.item/org.sana.binary";

        /** Default sort order.*/
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        
        // COLUMNS
        /**
         * The id uniquely identifying this element within the procedure it is
         * associated with. The id is unique in the context of a procedure xml.
         */
        public static final String ELEMENT_ID = "element_id";

        /**
         * The id of the saved procedure associated with the  file.
         */
        public static final String ENCOUNTER_ID = "procedure";
        
        /**
         * The number of bytes successfully uploaded to the MDS.
         */
        public static final String UPLOAD_PROGRESS = "upload_progress";
        
        /** Indicates whether the content file was completely uploaded. */
        public static final String UPLOADED = "uploaded";
        
        /** Date the file was created. */
        public static final String CREATED_DATE = "created";

        /** Date the file was last modified. */
        public static final String MODIFIED_DATE = "modified";

        /** 
         * Holds the content style uri for the item. What gets used to open a 
         * file stream.  
         */
        public static final String CONTENT = "_content";
        
        /** Mime type of the item. */
        public static final String MIME = "_mime";
        
        /** The absolute path on the file system */
        public static final String DATA = "_data";
        
        /** Text data associated with this observation  */
        public static final String TEXT = "_text";
        
        /** Flag whether this is a file or string data  */
        public static final String COMPLEX = "_complex";
        
    }
    
    /**
     * This class defines the URI and data fields for the content provider 
     * storing the metadata for an image collected during a procedure.
     * @author Sana Development Team
     */
    public static final class ImageSQLFormat implements BaseColumns {
        private ImageSQLFormat() {
        }

        /**
         * The content:// style URI for this content provider.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + IMAGE_AUTHORITY + "/images");

        /**
         * The MIME type of CONTENT_URI providing a directory of images.
         */
        public static final String CONTENT_TYPE = 
        	"vnd.android.cursor.dir/org.sana.savedProcedure";
        
        /**
         * The MIME type of CONTENT_URI subdirectory of a single image.
         */
        public static final String CONTENT_ITEM_TYPE = 
        	"vnd.android.cursor.item/org.sana.image";

        /**
         * Default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        // COLUMNS
        
        /**
         * The id of the saved procedure the image is associated with.
         */
        public static final String ENCOUNTER_ID = "procedure";
        
        /**
         * The id uniquely identifying this element in the procedure it is 
         * associated with. The id is unique in the context of a procedure xml.
         */
        public static final String ELEMENT_ID = "element_id";
        
        /**
         * The URI of the image file.
         */
        public static final String FILE_URI = "file_uri";
        
        // Is the file written completely to storage yet?
        /**
         * Indicates if the file is completely written to storage.
         */
        public static final String FILE_VALID = "file_valid";

        /**
         * The file size in bytes.
         */
        public static final String FILE_SIZE = "file_size";
        
        /**
         * The number of bytes successfully uploaded to the MDS.
         */
        public static final String UPLOAD_PROGRESS = "upload_progress";
        
        /**
         * Indicates whether or not the image is completely uploaded.
         */
        public static final String UPLOADED = "uploaded";

        /**
         * The date the image was created.
         */
        public static final String CREATED_DATE = "created";

        /**
         * The date the image was last modified.
         */
        public static final String MODIFIED_DATE = "modified";
    }

    /**
     * This class defines the URI and data fields for the content provider 
     * storing the metadata for a sound recording collected during a procedure.
     * @author Sana Development Team
     *
     */
    public static final class SoundSQLFormat implements BaseColumns {
        private SoundSQLFormat() {
        }

        /**
         * The content:// style URI for this content provider.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + SOUND_AUTHORITY + "/sounds");

        /**
         * The MIME type of CONTENT_URI providing a directory of sound files.
         */
        public static final String CONTENT_TYPE = 
        	"vnd.android.cursor.dir/org.sana.sound";

        /**
         * The MIME type of CONTENT_URI subdirectory for a single sound file.
         */
        public static final String CONTENT_ITEM_TYPE = 
        	"vnd.android.cursor.item/org.sana.sound";

        /**
         * Default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        
        // COLUMNS

        /**
         * The id uniquely identifying this sound element in the procedure it is
         * associated with. The id is unique in the context of a procedure xml.
         */
        public static final String ELEMENT_ID = "element_id";

        /**
         * The id of the saved procedure associated with the sound file.
         */
        public static final String ENCOUNTER_ID = "procedure";
        
        /**
         * The URI of the sound file.
         */
        public static final String FILE_URI = "file_uri";
        
        // Is the file written completely to storage yet?
//        public static final String FILE_VALID = "file_valid";
//
//        public static final String FILE_SIZE = "file_size";
//        
        /**
         * The number of bytes successfully uploaded to the MDS.
         */
        public static final String UPLOAD_PROGRESS = "upload_progress";
        
        /**
         * Indicates whether the sound file is completely uploaded.
         */
        public static final String UPLOADED = "uploaded";
        
        /**
         * Date the sound file was created.
         */
        public static final String CREATED_DATE = "created";

        /**
         * Date the sound file was last modified.
         */
        public static final String MODIFIED_DATE = "modified";
    }
    
    /**
     * This class defines the URI and data fields for the content provider 
     * storing doctor information.
     * 
     * @author Sana Development Team
     *
     */
    public static final class DoctorGroupSQLFormat implements BaseColumns {
    	private DoctorGroupSQLFormat() {	
    	}
    	
    	/**
         * The content:// style URI for this content provider.
         */
    	//public static final Uri CONTENT_URI = Uri.parse("content://"
    		//	+ DOCTOR_GROUP_AUTHORITY + "/doctorGroups");
    	
    	/**
         * The MIME type of CONTENT_URI providing a directory of doctors.
         */
    	public static final String CONTENT_TYPE = 
    		"vnd.android.cursor.dir/org.sana.doctorGroup";
    	
    	/**
    	 * The MIME type of a CONTENT_URI subdirectory of a single doctor.
    	 */
    	public static final String CONTENT_ITEM_TYPE = 
    		"vnd.android.cursor.item/org.sana.doctorGroup";
    	
    	/**
    	 * Default sort order.
    	 */
    	public static final String DEFAULT_SORT_ORDER = "modified DESC";
    	
    	//COLUMNS
    	
    	/**
    	 * The doctor group id.
    	 */
    	public static final String DOCTOR_GROUP_ID = "doctor_group_id";
    	    	
    	/**
    	 * The doctor group name.
    	 */
    	public static final String DOCTOR_GROUP_NAME = "doctor_group_name";
    }
    
    /**
     * This class defines the URI and data fields for the content provider 
     * storing education resources.
     * @author Sana Development Team
     *
     */
    public static final class EducationResourceSQLFormat implements BaseColumns 
    {
        private EducationResourceSQLFormat() {
        }
        
        /**
         * The MIME type of CONTENT_URI providing a directory of education 
         * reources.
         */
    	public static final String CONTENT_TYPE = 
    								"vnd.android.cursor.dir/org.sana.info";
    	
    	
    	/**
    	 * The MIME type of a CONTENT_URI subdirectory of a single resource.
    	 */
    	public static final String CONTENT_ITEM_TYPE = 
    								"vnd.android.cursor.item/org.sana.info";

    	/**
         * The content:// style URI for this table
         */
    	public static final Uri CONTENT_URI = Uri.parse(
    					"content://" +EDUCATIONRESOURCE_AUTHORITY + "/info");
        
    }
    
}
