package org.sana.android.service.messaging;

import android.util.JsonWriter;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gil on 7/19/17.
 */

public class ProcedureFirebaseInstanceIdService extends FirebaseInstanceIdService {
    private static HttpPost registerUserRequest = new HttpPost("http://192.168.43.158:8000/api/devices");
    private static String TAG = "GIL";
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

    private void sendRegistrationToServer(String registrationToken) {
        Map<String, String> userMap = new HashMap<>();
        userMap.put("registration_id", registrationToken);

        JSONObject obj=new JSONObject(userMap);

        StringEntity se = null;
        try {
            se = new StringEntity(obj.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        registerUserRequest.setEntity(se);
        registerUserRequest.setHeader("Content-type", "application/json");
        try {
            Log.d(TAG, "response: " + new ExecuteRegisterIdWithServer().execute(registerUserRequest).get());
        } catch (Exception e) {
            Log.d(TAG, "response failed");
        }
    }
}
