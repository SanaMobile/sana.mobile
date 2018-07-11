package org.sana.android.provider;

import org.sana.core.Observation;

import android.net.Uri;

/**
 * Meta data for Observation records.
 *
 * @author Sana Development Team
 */
public final class Observations {

    public static final String AUTHORITY = "org.sana.provider";

    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    private Observations() {
    }

    /*** The content type for one or more records */
    public static final String CONTENT_TYPE =
            "vnd.android.cursor.dir/org.sana.observation";

    /**
     * The content type for a single record
     */
    public static final String CONTENT_ITEM_TYPE =
            "vnd.android.cursor.item/org.sana.observation";

    /**
     * The content style URI
     */
    public static final Uri CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/core/observation");

    /**
     * Contract for the Observation table in the database.
     *
     * @author Sana Development
     */
    public interface Contract extends BaseContract<Observation> {

        /**
         * Unique id attribute within the encounter
         */
        String UUID = "uuid";

        /**
         * Provides the context for the collected data
         */
        String CONCEPT = "concept";

        /**
         * The encounter where this was collected
         */
        String ENCOUNTER = "encounter";

        /**
         * The subject who data was collected about; i.e. the patient
         */
        String SUBJECT = "subject";

        /**
         * Unique id attribute within the encounter
         */
        String ID = "id";

        /**
         * Mapping to parent node. Defaults to null if at top level of encounter
         */
        String PARENT = "parent";

        /**
         * Text representation of the observation data
         */
        String VALUE_TEXT = "value_text";

        /**
         * File URI for Blob observation data.
         */
        String VALUE_COMPLEX = "value_complex";

        /**
         * The observation data.
         */
        String VALUE = "_data";

        /**
         * The number of packetized bytes successfully uploaded to the MDS.
         */
        String UPLOAD_PROGRESS = "upload_progress";

        /**
         * Indicates whether a Blob is completely uploaded
         */
        String UPLOADED = "uploaded";

    }

}