package org.sana.android.service;

import android.net.Uri;
import org.sana.android.content.core;

oneway interface ISessionCallback{
 
    /**
     * Callback interface to get information when the status of a session has 
     * changed.
     * @param status
     * @param tempKey
     * @param sessionKey
     */
	void onValueChanged(int status, String tempKey, String sessionKey);

}