package org.sana.android.service;

import android.app.Service;

/**
 * Provides callbacks for connecting and disconnecting to a Service
 * 
 * @author Sana Development Team
 *
 * @param <T> a Service subclass to connect to and disconnect from 
 */
public interface ServiceListener<T extends Service> {
	void onConnect(T service);
	void onDisconnect(T service);
}
