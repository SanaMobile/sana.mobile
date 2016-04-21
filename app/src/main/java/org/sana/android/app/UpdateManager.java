package org.sana.android.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.commons.codec.digest.DigestUtils;
import org.sana.R;
import org.sana.android.content.Uris;
import org.sana.android.net.MDSInterface2;
import org.sana.android.provider.Updates;
import org.sana.android.service.impl.UpdateService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for handling application updates.
 *
 */
public class UpdateManager {
    public static final String TAG = UpdateManager.class.getSimpleName();
    public static final String MIMETYPE_PKG = "application/vnd.android.package-archive";
    public static final String ACTION_UPDATE_IN_PROGRESS = "org.sana.android.action.UPDATE_IN_PROGRESS";
    public static final String ACTION_UPDATE_DOWNLOAD_SUCCESS = "org.sana.android.action.UPDATE_DOWNLOAD_SUCCESS";
    public static final String ACTION_UPDATE_DOWNLOAD_FAIL = "org.sana.android.action.UPDATE_DOWNLOAD_FAIL";
    public static final String ACTION_UPDATE_CHECK_COMPLETE = "org.sana.android.action.UPDATE_CHECK_COMPLETE";
    public static final String EXTRA_DOWNLOAD_ID = "extra_download_id";
    public static final String EXTRA_DOWNLOAD_URI = "extra_download_uri";
    public static final String EXTRA_UPDATE_VERSION = "extra_update_version";

    public static String getAuth(Context context){
        return context.getString(R.string.key_update_secret);
    }

    /**
     * Returns the current installed version of the application context.
     *
     * @param context
     * @return
     */
    public static int version(Context context){
        String pkgName = context.getPackageName();
        // Current version
        PackageManager pm = context.getPackageManager();
        PackageInfo pi = null;
        int version = -1;
        try {
            pi = pm.getPackageInfo(pkgName, 0);
            version = pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * Calculates the checksum calculated from an InputStream and compares
     * it against a known value.
     *
     * @param in
     * @param checkSum
     * @return True if the checksums are equal
     */
    public static boolean isValid(InputStream in, String checkSum){
        boolean valid= false;
        try {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            byte[] bytes = new byte[8192];
            int byteCount;
            while ((byteCount = in.read(bytes)) > 0) {
                digester.update(bytes, 0, byteCount);
            }
            byte[] digest = digester.digest();
            String hex = DigestUtils.md5Hex(digest);
            return (hex.compareToIgnoreCase(checkSum) == 0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return valid;
    }

    /**
     * Calculates the checksum calculated from the contents stored at a Uri
     * and compares the result against a known value.
     * @param context
     * @param apkUri
     * @param checksum
     * @return True if the checksums are equal
     * @throws FileNotFoundException
     */
    public static boolean isValid(Context context, Uri apkUri, String checksum) throws FileNotFoundException {
        return isValid(context.getContentResolver().openInputStream(apkUri), checksum);
    }

    /**
     * Calculates the checksum calculated from the contents stored in a file
     * and compares the result against a known value.
     *
     * @param context
     * @param file
     * @param checksum
     * @return True if the checksums are equal
     * @throws FileNotFoundException
     */
    public static boolean isValid(Context context, File file, String checksum) throws FileNotFoundException {
        if(!file.exists()) return false;
        return isValid(context.getContentResolver().openInputStream(Uri.fromFile(file)), checksum);
    }

    public static Uri getCheckUri(Context context){
        String authority = MDSInterface2.getAuthority(context);
        String scheme = MDSInterface2.getScheme(context);
        return Uris.iriToUri(scheme, authority, Updates.CONTENT_URI);
    }

    public static Uri getUpdateUri(Context context){
        Uri uri = getCheckUri(context);
        Uri.Builder builder = uri.buildUpon();
        builder.appendEncodedPath("download/");
        return builder.build();
    }

    public static Uri getLocalUri(Context context, int version){
        String packageName = context.getPackageName();
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File apkFile = new File(dir, String.format("%s-%d.apk", packageName,version));
        return Uri.fromFile(apkFile);
    }

    /**
     * Starts the update action. This checks the current installed version
     * against the latest available from a remote authority.
     *
     * @param context
     * @param authKey A secret key required to authenticate with the authority
     */
    public static void checkUpdate(Context context, String authKey) {
        Uri remoteUri = getCheckUri(context);
        Intent intent = new Intent(context, UpdateService.class);
        intent.setAction(UpdateService.ACTION_UPDATE_CHECK);
        intent.putExtra(UpdateService.EXTRA_KEY_UPDATE_REMOTE, remoteUri);
        intent.putExtra(UpdateService.EXTRA_KEY_UPDATE_AUTH, authKey);
        context.startService(intent);
    }

    /**
     * Starts the update action. This checks the current installed version
     * against the latest available from a remote authority.
     *
     * @param context
     * @param authKey A secret key required to authenticate with the authority
     */
    public static void getUpdate(Context context, String authKey, int versionCode) {
        Uri remoteUri = getUpdateUri(context);
        Uri apkUri = getLocalUri(context, versionCode);
        Intent intent = new Intent(context, UpdateService.class);
        intent.setAction(UpdateService.ACTION_UPDATE_GET);
        intent.putExtra(UpdateService.EXTRA_KEY_UPDATE_REMOTE, remoteUri);
        intent.putExtra(UpdateService.EXTRA_KEY_UPDATE_PACKAGE, apkUri);
        intent.putExtra(UpdateService.EXTRA_KEY_UPDATE_AUTH, authKey);
        context.startService(intent);
    }

    /**
     * Starts an Activity that will install an application using the Uri
     * as the source apk.
     *
     * @param context
     * @param apkUri
     */
    public static void installUpdate(Context context, Uri apkUri){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, UpdateManager.MIMETYPE_PKG);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private static void broadcast(Context context, Intent intent){
        //LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context.getApplicationContext());
        //lbm.sendBroadcast(intent);
        context.sendBroadcast(intent);
    }

    public static void broadcastStatus(Context context, String status, Uri apkUri, Bundle extras){
        Log.i(TAG, "broadcastStatus(Context,String,Uri, Bundle)");
        Intent intent = new Intent(status);
        intent.setDataAndType(apkUri, MIMETYPE_PKG);
        intent.putExtras(extras);
        broadcast(context,intent);
    }
    public static void broadcastStatus(Context context, String status, Bundle extras){
        Log.i(TAG, "broadcastStatus(context,String,Bundle)");
        Intent intent = new Intent(status);
        intent.putExtras(extras);
        broadcast(context,intent);
    }

    public static void broadcastDownloadFail(Context context, long id){
        Bundle extras = new Bundle();
        extras.putLong(UpdateManager.EXTRA_DOWNLOAD_ID, id);
        broadcastStatus(context,UpdateManager.ACTION_UPDATE_DOWNLOAD_FAIL,extras);
    }

    public static void broadcastDownloadSuccess(Context context, long id, Uri apkUri){
        Bundle extras = new Bundle();
        extras.putLong(UpdateManager.EXTRA_DOWNLOAD_ID, id);
        extras.putParcelable(UpdateManager.EXTRA_DOWNLOAD_URI, apkUri);
        broadcastStatus(context,UpdateManager.ACTION_UPDATE_DOWNLOAD_SUCCESS, apkUri, extras);
    }

    public static void broadcastInProgress(Context context, long id){
        Bundle extras = new Bundle();
        extras.putLong(UpdateManager.EXTRA_DOWNLOAD_ID, id);
        broadcastStatus(context,UpdateManager.ACTION_UPDATE_IN_PROGRESS,extras);
    }

    public static void broadcastCheckComplete(Context context, long id, int version){
        Bundle extras = new Bundle();
        extras.putLong(UpdateManager.EXTRA_DOWNLOAD_ID, id);
        extras.putInt(UpdateManager.EXTRA_UPDATE_VERSION, version);
        broadcastStatus(context,UpdateManager.ACTION_UPDATE_CHECK_COMPLETE,extras);
    }
}
