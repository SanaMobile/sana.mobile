package org.sana.android.service;

import org.sana.android.service.ISessionCallback;

/**
 * Restful interface for the remote session service.
 */
interface ISessionService{

	// Opens a session and returns the session key
	/**
	 * Attempts to open a session. This method will return a temporary
	 * key immediately. To get a valid session key, it an ISessionCallback
	 * be implemented to catch any asynchronous responses.
	 * @param username
	 * @param password
	 * @returns a temporary session key
	 */ 
	String create(in String tempKey, in String username, in String password);
	
	// Check the status
	/**
	 * Checks whether the supplied sessionKey matches an open, validated
	 * session
	 * @param sessionKey
	 * @returns true if the session is open and authenticated.
	 */
	boolean read(in String sessionKey);
	
	
	/**
	 * Closes any sessions open based on the supplied key
	 * @param sessionKey the key for the sessions to close
	 * @returns true if a session was closed.
	 */
	boolean delete(in String sessionKey);
	
	void registerCallback(ISessionCallback cb, String instanceKey);
	
	void unregisterCallback(ISessionCallback cb);

}