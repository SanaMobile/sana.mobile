package org.sana.android.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

/**
 * Provides a connection to the BackgroundUploader
 * 
 * @author Sana Development Team
 *
 */
public class ServiceConnector {
	private static final String TAG = ServiceConnector.class.getSimpleName();
	
	ServiceListener<BackgroundUploader> mListener = null;
	private BackgroundUploader mUploadService = null;
    
    private ServiceConnection serviceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName name,
				IBinder service) {
			Log.i(TAG, "onServiceConnected");
			mUploadService = ((BackgroundUploader.LocalBinder)service).getService();
			if (mListener != null)
				mListener.onConnect(mUploadService);
		}

		public void onServiceDisconnected(ComponentName name) {
			Log.i(TAG, "onServiceDisconnected");
			if (mListener != null)
				mListener.onDisconnect(mUploadService);
			mUploadService = null;
		}
		
	};

	/**
	 * Binds a Context to the BackgroundUploader
	 * @param c the current Context
	 */
	public void connect(Context c) {
		if (mUploadService == null) {
			Intent serviceIntent = new Intent(c, BackgroundUploader.class);
			c.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
		}
	}
	

	/**
	 * Unbinds a Context to the BackgroundUploader
	 * @param c the current Context
	 */
	public void disconnect(Context c) {
		if (mUploadService != null) {
			c.unbindService(serviceConnection);
		}
	}
	
	/**
	 * Sets the current listener to the BackgroundUploader
	 * 
	 * @param listener the new ServiceListener
	 */
	public void setServiceListener(ServiceListener<BackgroundUploader> listener) 
	{
		this.mListener = listener;
		
		if (listener instanceof Context) {
			Log.w(TAG, "Provided ServiceListener is a Context. You may be "+
					"leaking a Context.");
		}
	}
}
