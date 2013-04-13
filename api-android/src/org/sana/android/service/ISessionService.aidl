package org.sana.android.service;

import org.sana.android.content.core;

interface ISessionService{

	// Opens a session and returns the session key 
	String open(in core.ObserverParcel t);
	
	// Check the status
	boolean read(in ParcelUuid uuid);
	
	boolean close(in ParcelUuid uuid);

}