package org.sana.android.service;

import android.app.Service;

/**
 * Provides callbacks for connecting and disconnecting to a Service
 *
 * @param <T> a Service subclass to connect to and disconnect from
 * @author Sana Development Team
 */
public interface ServiceListener<T extends Service> {
    void onConnect(T service);

    void onDisconnect(T service);
}
