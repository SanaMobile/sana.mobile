package org.sana.android.util.test;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.os.EnvironmentCompat;
import android.test.AndroidTestCase;

import org.sana.android.provider.Procedures;
import org.sana.android.util.SanaUtil;

import java.io.File;
import java.net.URLConnection;

/**
 * Created by winkler.em@gmail.com, on 04/15/2016.
 */
public class SanaUtilTest extends AndroidTestCase {
    public static final String TAG = SanaUtilTest.class.getSimpleName();

    private Context mContext = null;
    private static final int DEFAULT_COUNT = 1;

    @Override
    protected void setUp() throws Exception {
        mContext = getContext();
    }

    public void testAudioCapture(){
        File sounds = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        File target = new File(sounds, "sound.m4a");
        Uri uri = Uri.fromFile(target);
        String mime = URLConnection.guessContentTypeFromName(uri.toString());
        Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION,uri);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.setDataAndType(uri,mime );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    public void testLoadDefaultDatabase(){
        // Clear procedures first
        mContext.getContentResolver().delete(Procedures.CONTENT_URI,null,null);
        // Load defaults and check
        assertEquals(DEFAULT_COUNT, SanaUtil.loadDefaultDatabase(mContext));
    }


    public void testCheckConnection(){
        //TODO

    }
}
