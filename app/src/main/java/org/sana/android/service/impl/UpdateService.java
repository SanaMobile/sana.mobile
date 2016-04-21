package org.sana.android.service.impl;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.sana.android.app.UpdateManager;
import org.sana.android.content.Uris;
import org.sana.android.net.MDSInterface2;
import org.sana.net.Response;
import org.sana.net.http.HttpTaskFactory;
import org.sana.net.http.handler.FileHandler;
import org.sana.net.http.handler.StringHandler;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Date;

/**
 * An {@link IntentService} subclass for handling asynchronous update tasks in
 * a service on a separate handler thread.
 */
public class UpdateService extends IntentService {

    public static final String TAG = UpdateService.class.getSimpleName();
    public static final String ACTION_UPDATE_CHECK = "org.sana.android.action.ACTION_UPDATE_CHECK";
    public static final String ACTION_UPDATE_GET = "org.sana.android.action.ACTION_UPDATE_GET";
    public static final String ACTION_UPDATE_INSTALL = "org.sana.android.action.ACTION_UPDATE_INSTALL";
    public static final String EXTRA_KEY_UPDATE_AUTH = "org.sana.android.intent.EXTRA_KEY_UPDATE_AUTH";
    public static final String EXTRA_KEY_UPDATE_VERSION = "org.sana.android.intent.EXTRA_KEY_UPDATE_VERSION";
    public static final String EXTRA_KEY_UPDATE_PACKAGE = "org.sana.android.intent.EXTRA_KEY_UPDATE_PACKAGE";
    public static final String EXTRA_KEY_UPDATE_REMOTE = "org.sana.android.intent.EXTRA_KEY_UPDATE_REMOTE";

    public UpdateService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent(Intent)");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_CHECK.equals(action)) {
                final Uri source = intent.getParcelableExtra(EXTRA_KEY_UPDATE_REMOTE);
                final String authKey = intent.getStringExtra(EXTRA_KEY_UPDATE_AUTH);
                handleActionUpdateCheck(source, authKey);
            } else if(ACTION_UPDATE_GET.equals(action)) {
                final String authKey = intent.getStringExtra(EXTRA_KEY_UPDATE_AUTH);
                final Uri source = intent.getParcelableExtra(EXTRA_KEY_UPDATE_REMOTE);
                final Uri target = intent.getParcelableExtra(EXTRA_KEY_UPDATE_PACKAGE);
                final int version = intent.getIntExtra(EXTRA_KEY_UPDATE_VERSION,-1);
                handleActionUpdateGet(source, target, authKey);
            }
        }
    }

    /**
     * Initiates the application update check
     */
    private void handleActionUpdateCheck(Uri remoteUri, String authKey) {
        Log.i(TAG, "handleActionUpdateCheck(String)");
        long id = getPackageName().hashCode();
        UpdateManager.broadcastInProgress(getApplicationContext(), id);
        int availableVersion = -1;
        Response<String> response = null;
        try {
            StringHandler handler = new StringHandler();
            response = MDSInterface2.apiGet(URI.create(remoteUri.toString()), handler);
            availableVersion = Integer.parseInt(response.getMessage());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        UpdateManager.broadcastCheckComplete(getApplicationContext(), id, availableVersion);
    }

    private Uri getUpdate(Uri remoteUri, Uri apkUri, String authKey){
        File out = null;
        FileHandler handler = new FileHandler(apkUri.getPath());
        URI uri = URI.create(remoteUri.toString());
        HttpGet get = new HttpGet(uri);
        //get.addHeader("Authorization", authKey);
        HttpClient client = HttpTaskFactory.CLIENT_FACTORY.produce();
        HttpResponse response = null;
        try {
            response = client.execute(get);
            out = handler.handleResponse(response);
            Log.e(TAG, "Size: " + out.length());
            return Uri.fromFile(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.EMPTY;
    }

    private void handleActionUpdateGet(Uri remoteUri, Uri apkUri, String authKey) {
        Log.i(TAG, "handleActionUpdateGet(String)");
        Uri uri = getUpdate(remoteUri, apkUri, authKey);
        long id = getPackageName().hashCode();
        if(Uris.isEmpty(uri)){
            UpdateManager.broadcastDownloadFail(getApplicationContext(),id);
        } else {
            UpdateManager.broadcastDownloadSuccess(getApplicationContext(),id,uri);
        }
    }

    private static Uri getUpdateDM(Context context, Uri requestUri, Uri destinationUri, String authToken){
        Log.i(TAG, "updateDM(Context, Uri)");
        try {
            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(requestUri);
            // Add the auth headers and DM configs
            request.addRequestHeader("Authorization", authToken)
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
                    .setDestinationUri(destinationUri)
                    .setMimeType(UpdateManager.MIMETYPE_PKG)
                    .setNotificationVisibility(
                            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            // CONSIDER
            request.setTitle(destinationUri.getLastPathSegment())
                    .setDescription("Updating..." + destinationUri.getLastPathSegment())
                    .setVisibleInDownloadsUi(true);
            long id = dm.enqueue(request);
            Intent intent = new Intent(UpdateManager.ACTION_UPDATE_IN_PROGRESS);
            intent.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, id);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "Exception: "+e);
            e.printStackTrace();
        }
        return destinationUri;
    }
}
