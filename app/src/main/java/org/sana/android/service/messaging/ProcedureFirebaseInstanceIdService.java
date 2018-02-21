package org.sana.android.service.messaging;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class ProcedureFirebaseInstanceIdService extends FirebaseInstanceIdService {
    // TODO hardcoded server URL
    public static final String SERVER_URL = "http://192.168.2.59:8000";

    private static final String TAG = ProcedureFirebaseInstanceIdService.class.getName();

    private static final String REGISTER_DEVICE_ENDPOINT = SERVER_URL + "/api/devices";

    /**
     * Called when the device specific token changes
     */
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        Log.i(TAG, "I AM HERE IN INFO");
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.i(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        new RegisterIdWithServer().execute(refreshedToken);
    }

    /**
     * Register this device on SANA Protocol Builder in background thread
     */
    private static class RegisterIdWithServer extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            String registrationToken = params[0];

            Map<String, String> userMap = new HashMap<>();
            userMap.put("registration_id", registrationToken);
            JSONObject obj = new JSONObject(userMap);

            StringEntity se = null;
            try {
                se = new StringEntity(obj.toString());
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "UnsupportedEncodingException thrown: ", e);
            }

            HttpPost request = new HttpPost(REGISTER_DEVICE_ENDPOINT);
            request.setEntity(se);
            request.setHeader("Content-type", "application/json");

            try {
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(request);

                Log.d(TAG, response.toString());
                int responseCode = response.getStatusLine().getStatusCode();
                Log.d(TAG, "response: " + responseCode);

                return responseCode;
            } catch (ClientProtocolException e) {
                Log.e(TAG, "ClientProtocolException thrown: ", e);
            } catch (IOException e) {
                Log.e(TAG, "IOException thrown: ", e);
            }
            return -1;
        }
    }
}
