package org.sana.android;

import org.sana.R;
import org.sana.R.layout;
import org.sana.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class Sana extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
