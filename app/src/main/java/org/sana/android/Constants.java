package org.sana.android;

/**
 * Defaults and constants including keys for looking up user preference values.
 *
 * @author Sana Dev Team
 */
public class Constants {
    /* *************************************************************************
     * Transport layer constants
     **************************************************************************/
    /** A phone identifier */
    public static final String PHONE_ID = "";

    /** Url for validating authorization credentials */
    public static final String VALIDATE_CREDENTIALS_PATTERN =
        "/json/validate/credentials/";

    /** Url for uploading encounters */
    public static final String PROCEDURE_SUBMIT_PATTERN =
        "/json/procedure/submit/";

    /** Url for uploading whole binaries. <b>Deprecated</b> */
    public static final String BINARY_SUBMIT_PATTERN = "/json/binary/submit/";

    /** Url for uploading packetized chunks of binary data */
    public static final String BINARYCHUNK_SUBMIT_PATTERN =
        "/json/binarychunk/submit/";

    /** Url for uploading base64 encoded text chunks of binary data */
    public static final String BINARYCHUNK_HACK_SUBMIT_PATTERN =
        "/json/textchunk/submit/";

    /** Url for requesting lists of patients */
    public static final String DATABASE_DOWNLOAD_PATTERN =
        "/json/patient/list/";

    /** Url for requesting single patients */
    public static final String USERINFO_DOWNLOAD_PATTERN = "/json/patient/";

    /** Url for sending event submissions */
    public static final String EVENTLOG_SUBMIT_PATTERN =
        "/json/eventlog/submit/";

    // PACKETIZATION FIELDS
    /** The default binary packet size in KB */
    public static final int DEFAULT_INIT_PACKET_SIZE = 20;

    /** The smallest allowable packet size. If the algorithm in MDSInterface
     *  results in the packet size updating to a smaller value, the upload will
     *  abort.
     */
    public static final int MIN_PACKET_SIZE = 1;

    /** */
    public static final int USER_INFO_TIMEOUT_PERIOD = 2; //in seconds

    // chunksize / estimated bytes per second = average time to upload
    // Timeout = 4 * average time to upload = 4 * chunksize / estimate bps
    // Some data rates:
    // GPRS: 56-114 kbit/s   7-14.25 kb/sec
    // EDGE: max 236.8 kbkit/s

    /** Calculated value for mean GPRS speed. */
    public static final float ESTIMATED_NETWORK_BANDWIDTH = 10625;

    /** Period, in seconds to poll for ready upload requests */
    public static final int DEFAULT_POLL_PERIOD = 60; // in seconds

    /** Used to calculate time outs for packetization algorithm */
    public static final int ESTIMATED_TO_MIN_BANDWIDTH_FACTOR = 1000;

    /* *************************************************************************
     * Publicly accessible file and directory constants. Should be appended to
     * the external storage directory at run time.
     **************************************************************************/
    // use "/sdcard/dcim/Camera/" for easy testing w/ Android camera app
    /** Default location to look for binary files */
    public static final String DEFAULT_BINARY_FILE_FOLDER = "/sdcard/";


    // TODO add trailing slash for conformity
    /** Dispatch server path. */
    public static final String PATH_MDS = "mds";

    /** The application media path */
    public static final String PATH_ROOT = "/media/sana/";

    /** Top level of the resource directories, procedures and education */
    public static final String PATH_RESOURCE = PATH_ROOT;

    /** Where procedure updates get stored. */
    public static final String PATH_PROCEDURE = "/Downloads/";

    /** Default location to look for xml files containing Procedures at run
     *  time. */
    public static final String DEFAULT_XML_PROCEDURE_PATH =
        "/sdcard/sana/procedures/";

    /** Where education resources get stored */
    public static final String PATH_EDUCATION = PATH_RESOURCE + "education/";

    /** Where education resources get stored */
    public static final String PATH_OBSERVATION = PATH_RESOURCE + "obs/";

    /** Default name for all manifest xml files. */
    public static final String MANIFEST = "manifest.xml";

    /** Default name for metadata xml files. */
    public static final String METADATA = "metadata.xml";

    /* *************************************************************************
     * Constants related to preferences
     **************************************************************************/
    //DEFAULT PREFERENCE VALUES
    /** Default location of the Sana mobile Dispatch Server */
    public static final String DEFAULT_DISPATCH_SERVER ="demo.sana.csail.mit" +
            ".edu";

    /** A default phone number */
    public static final String DEFAULT_PHONE_NUMBER = "5555555555";

    /** A default username for authorization with the Sana demo server. */
    public static final String DEFAULT_USERNAME = "guest";

    /** A default password for authorization with the Sana demo server. */
    public static final String DEFAULT_PASSWORD = "Sanamobile1";

    /** Default image scale factor */
    public static final int IMAGE_SCALE_FACTOR = 1; // in KB

    // PREFERENCES
    /** Key for looking up phone name preference. */
    public static final String PREFERENCE_PHONE_NAME = "s_phone_name";

    /** Key for looking up mds server host. */
    public static final String PREFERENCE_MDS_URL = "s_mds_url";

    /** Key for looking up mds server host. */
    public static final String PREFERENCE_MDS_PORT = "s_mds_port";

    /** Key for looking up mds server host. */
    public static final String PREFERENCE_MDS_ROOT = "s_mds_root";

    /** Key for looking up permanent data store username. */
    public static final String PREFERENCE_EMR_USERNAME = "s_username";

    /** Key for looking up data store password. */
    public static final String PREFERENCE_EMR_PASSWORD = "s_password";

    /** Key for looking up image scaling factor */
    public static final String PREFERENCE_IMAGE_SCALE = "s_pic_scale";

    /** Key for looking up whether base64 encoded binary packet uploads are
     *  enabled. */
    public static final String PREFERENCE_UPLOAD_HACK = "s_upload_hack";

    /** Key for looking up whether barcode reading is enabled */
    public static final String PREFERENCE_BARCODE_ENABLED = "s_barcode_enabled";

    /** Key for looking up whether a proxy host is set */
    public static final String PREFERENCE_PACKET_SIZE = "s_packet_init_size";

    /** Key for looking up whether a proxy host is set */
    public static final String PREFERENCE_PROXY_HOST = "s_proxy_host";

    /** Key for looking up whether a proxy port is set */
    public static final String PREFERENCE_PROXY_PORT = "s_proxy_port";

    /** Key for the estimated network bandwidth */
    public static final String PREFERENCE_NETWORK_BANDWIDTH = "s_network_bandwidth";

    /** Key for looking up whether transport encryption is enabled */
    public static final String PREFERENCE_SECURE_TRANSMISSION = "s_secure";

    /** Key for looking up whether transport encryption is enabled */
    public static final String PREFERENCE_STORAGE_DIRECTORY = "s_binary_file_path";

    /** Key for looking up whether education resources are visible */
    public static final String PREFERENCE_EDUCATION_RESOURCE = "s_edu_rsrc";

    /** Key for in-app locale setting */
    public static final String PREFERENCE_LOCALE = "s_locale";

    /** The root authority */
    public static final String AUTHORITY = "org.sana.provider";

    /** */
    public static final int DEFAULT_DATABASE_UPLOAD = 1;

    /** */
    public static final String PREFERENCE_DATABASE_UPLOAD =
        "s_database_refresh_period";

    public static final String DB_INIT = "_db_init";

    public static final String PREFERENCE_NOTIFY_TASK_STATUS = "s_notify_task_status";

    /**
     * Calculates the timeout for a given bandwidth. The formula used for
     * calculating the value is given by:
     * <br/>
     * <code>
     * timeout = (1000 * <b>ESTIMATED_TO_MIN_BANDWITH_FACTOR</b>/bandwith)*bytes
     * </code>
     *
     * @param bandwidth The estimated bandwidth
     * @param bytes Number of bytes that will be sent
     * @return A timeout value for network requests.
     */
    public static int getTimeoutForBandwidth(float bandwidth, int bytes) {
        return (int)(((1.0 * 1000 * ESTIMATED_TO_MIN_BANDWIDTH_FACTOR) /
                bandwidth) * bytes);
    }
}
