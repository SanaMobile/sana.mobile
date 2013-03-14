package org.sana.android.provider;

import android.provider.BaseColumns;


/**
 * The base contract which all other ModelContract classes for the Sana database 
 * should extend. The naming convention is to begin columns which do not 
 * represent fields of <code>T</code> to begin with an underscore.
 * 
 * @author Sana Development
 *
 * @param <T> The class represented by this contract.
 */
public interface BaseContract<T> extends BaseColumns {

	/** A universally unique identifier */
	public static final String UUID = "uuid";
	
    /** The date the record was created. */
    public static final String CREATED = "created";

    /** The date the record was last modified. */
    public static final String MODIFIED = "modified";
	
}
