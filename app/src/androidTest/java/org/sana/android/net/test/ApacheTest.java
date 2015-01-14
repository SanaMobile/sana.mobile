package org.sana.android.net.test;

import org.apache.http.util.VersionInfo;

import android.test.AndroidTestCase;
import android.util.Log;

public class ApacheTest extends AndroidTestCase {

	
	public void testVersion(){
		VersionInfo vi = VersionInfo.loadVersionInfo("org.apache.http.client",getClass().getClassLoader());  
		String version = vi.getRelease();  
		Log.d("apache http client version", version);
	}
}
