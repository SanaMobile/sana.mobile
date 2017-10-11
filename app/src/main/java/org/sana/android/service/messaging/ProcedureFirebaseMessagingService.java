package org.sana.android.service.messaging;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by gil on 7/19/17.
 */

public class ProcedureFirebaseMessagingService extends FirebaseMessagingService {
    static private String TAG = "GIL";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());




        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
//
//            Toast.makeText(getBaseContext(), remoteMessage.getData().toString(),
//                    Toast.LENGTH_LONG).show();

            scheduleJob();

        }
    }

    private void scheduleJob() {

    }
}
