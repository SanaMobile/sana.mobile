package org.sana.android.activity.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import org.sana.R;
import org.sana.android.Constants;

/**
 * Simplified version of the network settings. Provides minimal set of
 * preferences needed to connect to server.
 *
 * @author Sana Development
 */
public class BasicSettings extends PreferenceActivity {
    public static final String TAG = BasicSettings.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.basic_network_settings);
        initPreferences();
    }

    private void initPreferences() {
        Log.i(TAG, "initPreferences()");
        // Phone name
        // This doesn't always work
        String line1Number = ((TelephonyManager) getSystemService(
                Context.TELEPHONY_SERVICE))
                .getLine1Number();
        String deviceId = ((TelephonyManager) getSystemService(
                Context.TELEPHONY_SERVICE)).getDeviceId();
        Log.d(TAG, "...line1 number: " + line1Number);
        Log.d(TAG, "...device id: " + deviceId);
        String phoneNum = (!TextUtils.isEmpty(line1Number))? line1Number:
                (!TextUtils.isEmpty(deviceId))? deviceId: Constants
                        .DEFAULT_PHONE_NUMBER;
        EditTextPreference prefPhoneName = (EditTextPreference) findPreference(Constants.PREFERENCE_PHONE_NAME);
        if (TextUtils.isEmpty(prefPhoneName.getText())) {
            prefPhoneName.setText(phoneNum);
        }
        // Sana Dispatch Server URL
        EditTextPreference prefMdsUrl = (EditTextPreference) findPreference(Constants.PREFERENCE_MDS_URL);
        if (TextUtils.isEmpty(prefMdsUrl.getText())) {
            prefMdsUrl.setText(Constants.DEFAULT_DISPATCH_SERVER);
        }
    }
}
