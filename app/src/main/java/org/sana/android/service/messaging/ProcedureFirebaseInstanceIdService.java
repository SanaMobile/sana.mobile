package org.sana.android.service.messaging;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class ProcedureFirebaseInstanceIdService extends FirebaseInstanceIdService {
    private static final String TAG = ProcedureFirebaseInstanceIdService.class.getName();

    private static final String SERVER_URL = "http://192.168.43.158:8000/api/devices";
    private static final HttpPost registerUserRequest = new HttpPost(SERVER_URL);

    /**
     * Called when the device specific token changes
     */
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        Log.i(TAG, "I AM HERE IN INFO");
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }

    /**
     * Register updated registration registration token to the SANA server
     */
    private void sendRegistrationToServer(String registrationToken) {
        Map<String, String> userMap = new HashMap<>();
        userMap.put("registration_id", registrationToken);
        JSONObject obj = new JSONObject(userMap);

        StringEntity se = null;
        try {
            se = new StringEntity(obj.toString());
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UnsupportedEncodingException thrown: ", e);
        }
        registerUserRequest.setEntity(se);
        registerUserRequest.setHeader("Content-type", "application/json");

        new ExecuteRegisterIdWithServer().execute(registerUserRequest);
    }

    /**
     * Make HTTP request in background thread
     */
    private static class ExecuteRegisterIdWithServer extends AsyncTask<HttpRequest, Void, Integer> {
        private HttpClient client = new DefaultHttpClient();

        @Override
        protected Integer doInBackground(HttpRequest... params) {
            HttpRequest request = params[0];
            try {
                HttpResponse response = client.execute((HttpUriRequest)request);

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
