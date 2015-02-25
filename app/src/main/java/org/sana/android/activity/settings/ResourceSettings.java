package org.sana.android.activity.settings;

import org.sana.R;
import org.sana.android.Constants;
import org.sana.android.activity.EducationResourceList;
import org.sana.android.media.EducationResource.Audience;
import org.sana.android.util.EnvironmentUtil;
import org.sana.android.util.SanaUtil;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Log;

/**
 * Creates the settings window for configuring and accessing resources 
 * available to the application.
 * 
 * If a user does not specify their own values, default values are used. Most of
 * these are stored in Constants.
 * 
 * String values are stored as preferences and can be retrieved as follows:
 * PreferenceManager.getDefaultSharedPreferences(c).getString("key name")
 * 
 * @author Sana Dev Team
 */
public class ResourceSettings extends PreferenceActivity{
	public static final String TAG = ResourceSettings.class.getSimpleName();
	
	/** {@inheritDoc} */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.resource_settings);
        initPreferences();
	}
	
	/** Sets the default values for the preference screen */
    private void initPreferences() {

		// Binary file location
		EditTextPreference binaryFileLocation = (EditTextPreference) findPreference(Constants.PREFERENCE_STORAGE_DIRECTORY);
		if (TextUtils.isEmpty(binaryFileLocation.getText())) {
		    binaryFileLocation.setText(EnvironmentUtil.getProcedureDirectory());
		}
		
		// Image downscale factor
		EditTextPreference imageDownscale = (EditTextPreference) findPreference(Constants.PREFERENCE_IMAGE_SCALE);
		if (TextUtils.isEmpty(imageDownscale.getText())) {
		    imageDownscale.setText("" + Constants.IMAGE_SCALE_FACTOR);
		}
		imageDownscale.getEditText().setKeyListener(new DigitsKeyListener());
		
		// View all edu resources
        PreferenceScreen resourcePref = (PreferenceScreen) findPreference("s_education_resource");
        Intent intent = EducationResourceList.getIntent(Intent.ACTION_PICK, 
				Audience.ALL);
		intent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_VIEW));
        resourcePref.setIntent(intent);
        
        // SD card loading procedures
        PreferenceScreen intentPref = (PreferenceScreen) findPreference("s_procedures");
        intentPref.setIntent(new Intent("org.sana.android.activity.IMPORT_PROCEDURE"));
        //intentPref.setIntent(new Intent(ResourceSettings.this, 
        //		ProcedureSdImporter.class));
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			final Intent data) 
	{
		Log.d(TAG, "Returned. requestCode: " + requestCode);
		Log.d(TAG, "......... resultCode: " + resultCode);
		if(data == null)
			Log.d(TAG, "data: Returned null data intent");
		
		Log.d(TAG, "......... data: " + data.toUri(Intent.URI_INTENT_SCHEME));
		try{
			switch(resultCode){
			case(RESULT_OK):
				if(data.getAction().equals(Intent.ACTION_VIEW)){
					Log.d(TAG, "EducationResource intent: " + data.getType());
					if(data.getType().contains("text/plain")){
						String text = data.getStringExtra("text");
						String title = data.getStringExtra(Intent.EXTRA_TITLE);
						SanaUtil.createDialog(this, title, text).show();
					} else { 
						Log.d(TAG, "View intent.");
						startActivity(data);
					}
				}
			}
		} catch (Exception e){

		}
	}
}
