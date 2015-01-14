/**
 * 
 */
package org.sana.android.provider;

import android.provider.BaseColumns;

/**
 * The base projection which all models that want to provide projections
 * should extend.
 * 
 * @author Sana Development
 * 
 * @param <T> The class represented by this projection.
 */
public interface BaseProjection<T> {
	
	/** Projection that retrieves values for the _ID column. */
	public static final String[] IDS = new String[] { BaseColumns._ID };
	
}
