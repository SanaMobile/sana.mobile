package org.sana.android.service.messaging;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.sana.android.procedure.Procedure;
import org.sana.android.procedure.ProcedureParseException;
import org.sana.android.util.SanaUtil;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import static org.sana.android.service.messaging.ProcedureFirebaseInstanceIdService.SERVER_URL;

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
            final String dataString = mapToString(data);
            Log.d(TAG, "Message data payload: " + dataString);

            processMessage(data);
        }
    }

    private static String mapToString(Map<String, String> map) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.append("\t");
            builder.append(entry.getKey());
            builder.append(": ");
            builder.append(entry.getValue());
            builder.append(",\n");
        }
        builder.append("}\n");
        return builder.toString();
    }

    /**
     * Message received contains information on how to ping the SANA API to
     * fetch the updated procedure (i.e. URL, procedure ID, etc).
     *
     * This method calls the SANA API to fetch the updated procedure(s).
     */
    private void processMessage(Map<String, String> data) {
        String type = data.get("type");

        if ("newProcedure".equals(type)) {
            String fetchUrl = SERVER_URL + data.get("fetchUrl");
            String idStr = data.get("procedureId");
            String outputDir = getFilesDir().getPath();
            String outputFilePath = String.format(
                    "%s/procedure/%s.xml",
                    outputDir,
                    idStr
            );

            new FetchProcedure(fetchUrl, outputFilePath, getApplicationContext()).execute(this);
        }
    }

    /**
     * Downloads a procedure XML from the SANA Protocol Builder and stores it in Android internal
     * storage.
     */
    private static class FetchProcedure extends AsyncTask<Context, Void, Procedure> {

        private String urlStr;
        private String outputFilePath;
        private WeakReference<Context> applicationContext;

        public FetchProcedure(String fetchUrl, String outputFilePath, Context applicationContext) {
            this.urlStr = fetchUrl;
            this.outputFilePath = outputFilePath;
            this.applicationContext = new WeakReference<>(applicationContext);
        }

        @Override
        protected Procedure doInBackground(Context... ctx) {
            try {
                URL url = new URL(urlStr);
                URLConnection connection = url.openConnection();
                connection.connect();

                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                File outputDir = new File(outputFilePath).getParentFile();
                if (!outputDir.exists()) {
                    outputDir.mkdirs();
                }

                OutputStream output = new FileOutputStream(outputFilePath);

                byte[] buffer = new byte[1024];
                int bufferLength;

                Log.i(TAG, "Downloading file");
                while ((bufferLength = input.read(buffer)) > 0) {
                    output.write(buffer, 0, bufferLength);
                    Log.i(TAG, new String(buffer, 0, bufferLength));
                }
                output.flush();
                output.close();
                input.close();

                Log.i(TAG, "File successfully downloaded!");

                return SanaUtil.insertProcedureFromSd(ctx[0], outputFilePath);
            } catch (IOException e) {
                Log.e(TAG, "Fetch procedure file failed: ", e);
                return null;
            } catch (ParserConfigurationException | SAXException | ProcedureParseException e) {
                Log.e(TAG, "Parsing procedure failed: ", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(final Procedure procedure) {
            new Handler(Looper.getMainLooper())
                .post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(
                            applicationContext.get(),
                            String.format("Procedure updated: %s", procedure.getTitle()),
                            Toast.LENGTH_LONG
                        ).show();
                    }
                });
        }
    }
}
