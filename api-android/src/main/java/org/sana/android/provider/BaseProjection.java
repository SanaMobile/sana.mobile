/**
 *
 */
package org.sana.android.provider;

import android.provider.BaseColumns;

/**
 * The base projection which all models that want to provide projections
 * should extend.
 *
 * @param <T> The class represented by this projection.
 * @author Sana Development
 */
public interface BaseProjection<T> {

    /**
     * Projection that retrieves values for the _ID column.
     */
    String[] IDS = new String[]{BaseColumns._ID};

}
