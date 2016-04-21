package org.sana.android.app;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.sana.android.service.impl.UpdateService;

import java.util.concurrent.atomic.AtomicLong;

public class UpdateReceiver extends BroadcastReceiver {
    private static final String TAG = UpdateReceiver.class.getSimpleName();
    static AtomicLong updateId = new AtomicLong(0l);
    public UpdateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive(context,intent)");
        long id = 0L;
        String action = intent.getAction();
        Log.d(TAG, "action="+action);
        id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
        Log.d(TAG, "id match: " + (updateId.longValue() == id));
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            Log.d(TAG, "Update download complete(DM)");
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(id);
            Cursor cursor = downloadManager.query(query);
            if (!cursor.moveToFirst()) {
                Log.e(TAG, "Empty row");
                return;
            }
            int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex)) {
                Log.w(TAG, "Download Failed");
                return;
            }
            int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
            Uri uri = Uri.parse(cursor.getString(uriIndex));
            UpdateManager.installUpdate(context, uri);
        } else if (UpdateManager.ACTION_UPDATE_IN_PROGRESS.equals(action)){
            Log.d(TAG, "Update in progress.");
            updateId.set(id);
        } else if (UpdateManager.ACTION_UPDATE_CHECK_COMPLETE.equals(action)){
            Log.d(TAG, "Update check complete");
            int availableVersion = intent.getIntExtra(
                    UpdateManager.EXTRA_UPDATE_VERSION, -1);
            if(availableVersion > UpdateManager.version(context)){
                String authKey = intent.getStringExtra(UpdateService.EXTRA_KEY_UPDATE_AUTH);
                UpdateManager.getUpdate(context, authKey, availableVersion);
            }
        } else if (UpdateManager.ACTION_UPDATE_DOWNLOAD_FAIL.equals(action)){
            Log.w(TAG, "Update download failed!");
            updateId.set(0L);
        } else if (UpdateManager.ACTION_UPDATE_DOWNLOAD_SUCCESS.equals(action)){
            Log.i(TAG, "Update download success");
            Uri uri = intent.getParcelableExtra(UpdateManager.EXTRA_DOWNLOAD_URI);
            try {
                UpdateManager.installUpdate(context, uri);
            } catch (Exception e){
                e.printStackTrace();
            }
            updateId.set(0L);
        }
    }
}
