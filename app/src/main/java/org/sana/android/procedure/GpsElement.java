package org.sana.android.procedure;

import org.sana.R;
import org.w3c.dom.Node;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * GpsElement is a ProcedureElement that is created when a "GPS" element is put 
 * into an XML procedure description. It allows the user to click on a button to
 * grab the current GPS coordinates. 
 * <p/>
 * <ul type="none">
 * <li><b>Clinical Use </b> Defined by subclasses.</li>
 * For field workers, this is often a useful feature to track patients and where
 * they are seen by the health workers.
 * <li><b>Collects </b> GPS coordinates as:<br/>
 * <ul type="none"><li>
 * 	<code>Latitude: <var>val</var> Longitude: <var>val</var><var>val</var> 
 * </li></ul>
 * </li>
 * </ul>
 * 
 * @author Sana Development Team
 */
public class GpsElement extends ProcedureElement implements OnClickListener {
    private Button getLocationButton; 
    private LocationManager locationManager; 
    private LocationListener locationListener;
    private boolean gotCoordinates = false;
    private Handler handler = null;

    /** Disconnects from the location manager. */
    @Override
    protected void finalize() throws Throwable {
    	// We need to make sure that we are not leaving the GPS on.
    	if (locationManager != null && locationListener != null) {
    		locationManager.removeUpdates(locationListener);
    	}
        super.finalize(); //not necessary if extending Object.
    } 
    
    /** {@inheritDoc} */
    @Override
    public ElementType getType() {
        return ElementType.GPS;
    }
    
    /** {@inheritDoc} */
    @Override
    protected View createView(Context c) {
        LinearLayout gpsContainer = new LinearLayout(c);
        gpsContainer.setOrientation(LinearLayout.VERTICAL);
        locationManager = (LocationManager)c.getSystemService(
        		Context.LOCATION_SERVICE);
    	if (locationListener == null) {
    		locationListener = new SanaGPSListener();
    	}
    	
        if(TextUtils.isEmpty(question)) {
            question = c.getString(R.string.question_standard_gps_element);
        }
        getLocationButton = new Button(c);
        getLocationButton.setText("Grab GPS Location");
        getLocationButton.setOnClickListener(this);
        gpsContainer.addView(getLocationButton, 
        		new LinearLayout.LayoutParams(-1,-1,0.1f));
        return encapsulateQuestion(c, gpsContainer);
    }

    /** {@inheritDoc} */
    @Override
    public void onClick(View v) {
        if (v == getLocationButton) {
        	Log.i(TAG, "Requesting GPS updates to get the current location. ");
        	gotCoordinates = false;
        	locationManager.requestLocationUpdates(
        			LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        	getLocationButton.setEnabled(false);
        	getLocationButton.setText(
        			getString(R.string.gps_element_acquire_waiting));
        	Thread gpsWatchdog = new Thread() {
        		public void run() {
        			Log.i(TAG, "GPS watchdog turning off GPS.");
        			locationManager.removeUpdates(locationListener);
        			if (!gotCoordinates) {
        				Log.i(TAG, "GPS coordinates were not acquired.");
        				getLocationButton.setEnabled(true);
        	        	getLocationButton.setText(
        	        		getString(R.string.gps_element_acquire_waiting));
        			}
        		}
        	};
        	if (handler == null)
        		handler = new Handler();
        	handler.postDelayed(gpsWatchdog, 10000);
        	
        }
    }

    /** {@inheritDoc} */
    public void setAnswer(String answer) {
      this.answer = answer;
    }
    
    /** {@inheritDoc} */
    public String getAnswer() {
    	return answer;
    }
    
    /** {@inheritDoc} */
    public void buildXML(StringBuilder sb) {
        sb.append("<Element type=\"" + getType().name() + "\" id=\"" + id);
        sb.append("\" answer=\"" + getAnswer());
        sb.append("\" concept=\"" + getConcept());
        sb.append("\"/>\n");
    }
    
    /** Default constructor. */
    private GpsElement(String id, String question, String answer, 
    		String concept, String figure, String audio) 
    {
        super(id, question, answer, concept, figure, audio);
        setAnswer("Coordinates not acquired.");
    }
    
    /** @see ProcedureElement#fromXML(String, String, String, String, String, String, Node) */
    public static GpsElement fromXML(String id, String question, String answer, 
    		String concept, String figure, String audio, Node node) 
    {
        return new GpsElement(id, question, answer, concept, figure, audio);
    }
    
    /**
     * Listener for changes to GPS service.
     * 
     * @author Sana Development Team
     */
    private class SanaGPSListener implements LocationListener {
    	
    	/** {@inheritDoc} */
    	@Override
    	public void onLocationChanged(Location location) {
            // Called when the location has changed.
			Log.d(TAG, "Got location update :" + location.toString() 
					+ ". Disabling GPS");
			getLocationButton.setEnabled(false);
			getLocationButton.setText("Coordinates acquired.");
			gotCoordinates = true;
			setAnswer("Latitude: " + location.getLatitude() + " Longitude: " 
					+ location.getLongitude());
			locationManager.removeUpdates(locationListener);
        }
    	
    	/** {@inheritDoc} */
    	@Override
        public void onProviderDisabled(String provider) {
            // Called when the provider is disabled by the user.
        	Log.d(TAG, "Provider disabled: " + provider);
        	getLocationButton.setEnabled(true);
			getLocationButton.setText("GPS turned off -- check settings.");
			locationManager.removeUpdates(locationListener);
        }
    	
    	/** {@inheritDoc} */
    	@Override
        public void onProviderEnabled(String provider) {
            // Called when the provider is enabled by the user.
        	Log.d(TAG, "Provider enabled: " + provider);
        	// Do nothing, we should get a location update soon which will 
        	// disable the listener.
        }
    	
    	/** {@inheritDoc} */
    	@Override
        public void onStatusChanged (String provider, int status, Bundle extras) 
        {
            // Called when the provider status changes.
        	Log.d(TAG, "Provider status changed: " + provider + " status: " 
        			+ status);
        	if (status == LocationProvider.AVAILABLE) {
        		// Do nothing, we should get a location update soon which will 
        		// disable the listener.
        	} else if (status == LocationProvider.OUT_OF_SERVICE || 
        			status == LocationProvider.TEMPORARILY_UNAVAILABLE) 
        	{
        		getLocationButton.setEnabled(true);
    			getLocationButton.setText(
    					getString(R.string.gps_element_acquire_unavailable));
            	locationManager.removeUpdates(locationListener);
        	}
        }    	
    }
}
