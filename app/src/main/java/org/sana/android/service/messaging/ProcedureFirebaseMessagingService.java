package org.sana.android.service.messaging;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Arrays;
import java.util.Map;

public class ProcedureFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = ProcedureFirebaseMessagingService.class.getName();

    /**
     * Called when Firebase sends a message to the Android device
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        Map<String, String> data = remoteMessage.getData();

        // Check if message contains a data payload.
        if (data.size() > 0) {
            String dataString = Arrays.toString(data.entrySet().toArray());
            Log.d(TAG, "Message data payload: " + dataString);

            Toast.makeText(this, dataString, Toast.LENGTH_LONG).show();

            scheduleJob();
        }
    }

    /**
     * Message received contains information on how to ping the SANA API to
     * fetch the updated procedure (i.e. URL, procedure ID, etc).
     *
     * This method calls the SANA API to fetch the updated procedure(s).
     */
    private void scheduleJob() {

    }
}
