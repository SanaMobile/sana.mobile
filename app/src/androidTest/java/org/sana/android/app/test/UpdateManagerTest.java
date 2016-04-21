package org.sana.android.app.test;

import android.content.Context;
import android.net.Uri;
import android.test.AndroidTestCase;

import org.sana.android.app.UpdateManager;

/**
 *
 */
public class UpdateManagerTest extends AndroidTestCase {
    public static final String TAG = UpdateManagerTest.class.getSimpleName();

    Context context;

    public void setUp() throws Exception {
        context = getContext();
        assertNotNull(context);
    }

    public void testGetCheckUri(){
        Uri url = Uri.parse("https://papd-haiti.org:443/mds/clients/");
        Uri uri = UpdateManager.getCheckUri(context);
        assertEquals(uri.getScheme(), url.getScheme());
        assertEquals(uri.getPath(),url.getPath());
        assertEquals(uri.getAuthority(), url.getAuthority());
        assertEquals(uri, url);
    }

}
