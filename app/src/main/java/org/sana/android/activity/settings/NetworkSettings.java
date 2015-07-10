
package org.sana.android.activity.settings;

import org.sana.R;
import org.sana.android.Constants;

import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Log;

/**
 * Creates the settings window for communicating with the Sana network layer If
 * a user does not specify their own values, default values are used. Most of
 * these are stored in Constants. The default phone name is the phone's number.
 * String values are stored as preferences and can be retrieved as follows:
 * PreferenceManager.getDefaultSharedPreferences(c).getString("key name")
 * 
 * @author Sana Dev Team
 */
public class NetworkSettings extends PreferenceActivity {
    public static final String TAG = NetworkSettings.class.getSimpleName();

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.network_settings);
        initPreferences();
    }

    /** Sets the default values for the preference screen */
    private void initPreferences() {
        // Phone name
        String phoneNum = ((TelephonyManager) getSystemService(
                Context.TELEPHONY_SERVICE))
                .getLine1Number();
        Log.d(TAG, "Phone number of this phone: " + phoneNum);
        if (TextUtils.isEmpty(phoneNum))
            phoneNum = Constants.DEFAULT_PHONE_NUMBER;

        EditTextPreference prefPhoneName = (EditTextPreference) findPreference(Constants.PREFERENCE_PHONE_NAME);
        if (TextUtils.isEmpty(prefPhoneName.getText())) {
            prefPhoneName.setText(phoneNum);
        }
        // Sana Dispatch Server URL
        EditTextPreference prefMdsUrl = (EditTextPreference) findPreference(Constants.PREFERENCE_MDS_URL);
        if (TextUtils.isEmpty(prefMdsUrl.getText())) {
            prefMdsUrl.setText(Constants.DEFAULT_DISPATCH_SERVER);
        }

        // Initial packet size
        EditTextPreference prefInitPacketSize = (EditTextPreference) findPreference(Constants.PREFERENCE_PACKET_SIZE);
        if (TextUtils.isEmpty(prefMdsUrl.getText())) {
            prefInitPacketSize.setText("" + Constants.DEFAULT_INIT_PACKET_SIZE);
        }
        prefInitPacketSize.getEditText().setKeyListener(new DigitsKeyListener());

        // How often the database gets refreshed
        EditTextPreference prefDatabaseRefresh = (EditTextPreference) findPreference(Constants.PREFERENCE_DATABASE_UPLOAD);
        if (TextUtils.isEmpty(prefDatabaseRefresh.getText())) {
            prefDatabaseRefresh.setText("" + Constants.DEFAULT_DATABASE_UPLOAD);
        }
        prefDatabaseRefresh.getEditText().setKeyListener(new DigitsKeyListener());

        // Estimated network bandwidth
        EditTextPreference prefEstimatedNetworkBandwidth = (EditTextPreference) findPreference(Constants.PREFERENCE_NETWORK_BANDWIDTH);
        if (TextUtils.isEmpty(prefEstimatedNetworkBandwidth.getText())) {
            prefEstimatedNetworkBandwidth.setText("" + Constants.ESTIMATED_NETWORK_BANDWIDTH);
        }
        prefEstimatedNetworkBandwidth.getEditText().setKeyListener(
                new DigitsKeyListener());
    }
}
