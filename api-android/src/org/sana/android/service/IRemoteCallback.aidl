package org.sana.android.service;

import android.os.Bundle;

oneway interface IRemoteCallback{
 
 	void onKeySet(String key);
 
    /**
     * Handles the result data Bundle and provided status code.
     *
     * @param code
     * @param data
     */
	void onResult(int code, in Bundle data);

}