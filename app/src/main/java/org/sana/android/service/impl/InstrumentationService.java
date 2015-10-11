/**
 * Copyright (c) 2013, Sana
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sana nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL Sana BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.sana.android.service.impl;

import java.util.List;
import java.util.Locale;

import org.sana.BuildConfig;
import org.sana.android.activity.ProcedureRunner;
import org.sana.android.content.Uris;
import org.sana.android.content.core.ObservationWrapper;
import org.sana.android.provider.Observations;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.support.v4.util.SparseArrayCompat;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author Sana Development
 *
 */
public class InstrumentationService extends Service {
	
	public static final String TAG = InstrumentationService.class.getSimpleName();
	
	public static final String ACTION_RECORD_GPS = "org.sana.android.intent.ACTION_RECORD_GPS";
	public static final int MSG_GET_LOCATION = 0;
	
	public static final int MSG_STATUS_SEND = -1;
	public static final int MSG_STATUS_REPLY = 0;
	public static final int MSG_STATUS_UNAVAILABLE = 1;
	
    
	/* Handler for the incoming messages */
	private final class IncomingHandler extends Handler{
		
		public IncomingHandler(Looper looper){
			super(looper);
		}
		
		@SuppressLint("NewApi")
		public void handleMessage(Message msg) {
			Log.i(TAG, "msg obj = " + String.valueOf(msg.obj));
			switch(msg.what){
			case MSG_GET_LOCATION:
				Uri uri = Uri.parse(msg.obj.toString());
				if(msg.getData() != null){
					//Intent reply = msg.getData().getParcelable(Intent.EXTRA_INTENT);
					PendingIntent replyTo = msg.getData().getParcelable(Intent.EXTRA_INTENT);
					//Log.d(TAG, "replyTo: " + String.valueOf(replyTo));
					LocationListener listener = getListener(null,uri,msg.arg1, msg.getData());
					try{
						Criteria criteria = new Criteria();
						if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO){
							criteria.setPowerRequirement(Criteria.POWER_HIGH);
							criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
						} else {
							criteria.setAccuracy(Criteria.ACCURACY_FINE);
						}
						List<String> providers = locationManager.getProviders(criteria, true);
						for(String provider: providers){
							Log.d(TAG, "Using location provider: " + provider);
							locationManager.requestLocationUpdates(provider, 0, 0, listener);
						}
						//if(TextUtils.isEmpty(provider))
						//	throw new IllegalArgumentException("No location providers available");
						// add to our listeners so that we can clean up later if necessary
						//replies.put(msg.arg1, replyTo);
						if(providers.size() == 0){
							Location nullLocation = new Location(LocationManager.GPS_PROVIDER);
							nullLocation.setAccuracy(0);
							nullLocation.setLatitude(0);
							nullLocation.setLongitude(0);
							listener.onLocationChanged(nullLocation);
						} else {
							listeners.put(msg.arg1, listener);
						}
					} catch (Exception e){
						Log.e(TAG, "Error getting location updates: " + e.getMessage());
						e.printStackTrace();
						removeListener(msg.arg1);
					}
				} else {
					Log.w(TAG, "no replyTo in original intent sent to InstrumentationService");
					removeListener(msg.arg1);
				}
				break;
			default:
				Log.w(TAG, "Unknown message! Message = " + msg.what);
				removeListener(msg.arg1);
			}
		}
	}

    private LocationManager locationManager = null;
    private Looper mLooper;
	Messenger mMessenger;
	IncomingHandler mHandler;
	private SparseArrayCompat<LocationListener> listeners = new SparseArrayCompat<LocationListener>();
	private SparseArrayCompat<PendingIntent> replies = new SparseArrayCompat<PendingIntent>();
	
	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}
    
	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate(){
		super.onCreate();
		HandlerThread thread = new HandlerThread("InstrumentationService",
	            Process.THREAD_PRIORITY_BACKGROUND);
	    thread.start();
	    
	    // Get the HandlerThread's Looper and use it for our Handler
	    mLooper = thread.getLooper();
	    mHandler = new IncomingHandler(mLooper);
	    mMessenger = new Messenger(mHandler);
		locationManager = (LocationManager) getBaseContext().getSystemService(
        		Context.LOCATION_SERVICE);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy(){
		int key = 0;
		for(int index =0; index < listeners.size(); index++){
			try{
				locationManager.removeUpdates(listeners.get(key));
			} catch(Exception e){}
		}
		listeners.clear();
		for(int index =0; index < replies.size(); index++){
			try{
				locationManager.removeUpdates(replies.get(index));
			} catch(Exception e){}
		}
		replies.clear();
		super.onDestroy();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent != null){
			String action = intent.getAction();
			Uri uri = intent.getData();
			Message msg = mHandler.obtainMessage();
			msg.obj = uri.toString();
			msg.arg1 = startId;
			//Bundle data = new Bundle();
			//data.putAll(intent.getExtras());
			msg.setData(intent.getExtras());
			if(action.equals(ACTION_RECORD_GPS)){
				msg.what = MSG_GET_LOCATION;
			}
			mHandler.sendMessage(msg);
		}
		return START_STICKY;
	}
	
	/* returns a Location Listener which will write the data if successful */
	private final LocationListener getListener(final PendingIntent replyTo, int id){
		LocationListener listener = new LocationInstrumentationListener(replyTo,id);
		return listener;
	}
	
	private final LocationListener getListener(final PendingIntent replyTo, Uri uri,int id, Bundle data){
		LocationListener listener = new LocationInstrumentationListener(replyTo,uri,id, data);
		return listener;
	}
	
	private final void addListener(int key, LocationListener listener){
		listeners.append(key, listener);
	}
	
	private final void addListener(int key, PendingIntent replyTo, String provider){
		LocationListener listener = new LocationInstrumentationListener(replyTo, key);
		listeners.put(key, listener);
		locationManager.requestLocationUpdates(provider, 0,0, listener);
	}
	
	private final void removeListener(int key){
		try{
			LocationListener listener = listeners.get(key);
			if(listener != null){
				try{
					locationManager.removeUpdates(listener);
					//mHandler.removeMessages(key, null);
				} catch (Exception e){
					Log.e(TAG, "removeListener(): " + e);
				}
			}
			listeners.remove(key);
			PendingIntent replyTo = replies.get(key);
			replies.delete(key);
			if(replyTo != null){
				try{
					locationManager.removeUpdates(replyTo);
				} catch (Exception e){
					Log.e(TAG, "removeListener(): " + e);
				}
			}
		} catch(Exception e) {
			
		}
		if(listeners.size() == 0){
			stopSelf();
		}
	}
	
	final void updateObservation(Uri uri, Object value){
		Log.i(TAG, "Updating: " + uri + ", value: " + value);
		ContentValues vals = new ContentValues();
		vals.put(Observations.Contract.VALUE, String.valueOf(value));
		getContentResolver().update(uri, vals, null, null);
	}
	
	class LocationInstrumentationListener implements LocationListener{

		final int id;
		final Uri uri;
		final PendingIntent replyTo;
		final Bundle bundle = new Bundle();
		Location currentLocation = null;
		final int minAccuracy;
		final static int DEFAULT_MIN_ACCURACY = 16;
		//final Context mContext;
		
		public LocationInstrumentationListener(PendingIntent replyTo, int id){
			this(replyTo,Uri.EMPTY, id,null, DEFAULT_MIN_ACCURACY);
		}
		
		public LocationInstrumentationListener(PendingIntent replyTo, Uri uri, int id, Bundle data){
			this(replyTo, uri,id,data,DEFAULT_MIN_ACCURACY);
		}
		
		public LocationInstrumentationListener(PendingIntent replyTo, Uri uri, int id, Bundle data, int minAccuracy){
			this.id = id;
			this.replyTo = replyTo;
			this.uri = uri;
			bundle.putAll(data.getBundle("extra_data"));
			this.minAccuracy = minAccuracy;
            currentLocation = null;
		}
		
		@Override
		public void onLocationChanged(Location location) {
            Log.i(TAG, "onLocationChanged()");
            if(currentLocation == null){
                currentLocation = location;
            }
            if (currentLocation.getAccuracy() > location.getAccuracy()){
                Log.d(TAG, "...Location update more accurate. Swapping");
                currentLocation = location;
            } else {
                Log.d(TAG, "...Location update less accurate. Not swapping");
                location = currentLocation;
            }
            Log.i(TAG, "Location update." + location);
			float accuracy = location.getAccuracy();
			Log.d(TAG, "Accuracy: " + accuracy);
			String locStr = "( "+location.getLatitude() +", "+ location.getLongitude()+", " + accuracy+" )";

			if(!Uris.isEmpty(uri))
				updateObservation(uri,locStr);
			if(replyTo != null){
				try {
					Intent reply = new Intent();
					reply.putExtra(Observations.Contract.VALUE,locStr);
					reply.putExtras(bundle);
					reply.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
					replyTo.send(InstrumentationService.this, Activity.RESULT_OK, reply);
					//PendingIntent.getActivity(getBaseContext(), Activity.RESULT_OK, reply, PendingIntent.FLAG_UPDATE_CURRENT).send();
					
					//replyTo.send();
				} catch (Exception e) {
					Log.e(TAG, "Error sending replyTo: " + e.getMessage());
					e.printStackTrace();
				}
			} else {
				Log.w(TAG, "No Pending Intent replyTo. Did you provide extra" 
						+ "Intent.EXTRA_INTENT in Intent sent to service?");
			}
            // Keep listening if accuracy is > minimum acceptable
			if(accuracy > minAccuracy)
				return;
			removeListener(id);
			stopSelf(id);
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.d(TAG, "LocationListener.onProviderDisabled(String)");
			removeListener(id);
			stopSelf(id);
		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.d(TAG, "LocationListener.onProviderEnabled(String)");
			removeListener(id);
		}

		@Override
		public void onStatusChanged(String provider, int status,
				Bundle extras) {
			Log.d(TAG, "LocationListener.onStatusChanged(...)");
			if (status == LocationProvider.AVAILABLE) {
        		// Do nothing, we should get a location update soon which will 
        		// disable the listener.
        	} else if (status == LocationProvider.OUT_OF_SERVICE || 
        			status == LocationProvider.TEMPORARILY_UNAVAILABLE) 
        	{
    			removeListener(id);
        		stopSelf(id);
        	}
		}
	}
}
