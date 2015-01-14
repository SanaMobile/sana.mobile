package org.sana.android.service;

import org.sana.android.content.core;

import org.sana.android.service.IRemoteCallback;

interface IRemoteService{

    /**
     * Registers the callback using a key provided by the service.
     * @param cb the callback to register
     * @returns the String cookie used as a key for registering.
     */
	String registerCallback(IRemoteCallback cb);
	
	/**
	 *
	 */
	void unregisterCallback(IRemoteCallback cb);

}