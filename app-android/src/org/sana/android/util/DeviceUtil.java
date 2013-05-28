package org.sana.android.util;

import org.sana.android.activity.ProcedureRunner;
import org.sana.android.activity.tablet.ProcedureTabletRunner;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;

/**
 * Utility class for doing device specific operations such as checking the
 * device version, screen size, etc.
 * 
 * @author Sana Dev Team
 */
public class DeviceUtil {
    
    // Array of phone and tablet specific activities
    private static final Class<?>[] sPhoneActivities = new Class[] {
        ProcedureRunner.class
    };

    private static final Class<?>[] sTabletActivities = new Class[] {
        ProcedureTabletRunner.class
    };
    
    /**
     * Enables/disables an Activity on the package manager level depending
     * on the device specs - version, screen size, etc.
     *  
     * @param context
     */
    public static void enableOnlyDeviceSpecificActivities(Context context) {
        boolean isHoneycombTablet = hasHoneycomb() && isTablet(context);
        PackageManager pm = context.getPackageManager();

        // Enable/disable phone activities
        for (Class<?> a : sPhoneActivities) {
            pm.setComponentEnabledSetting(new ComponentName(context, a),
                    isHoneycombTablet
                            ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                            : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }

        // Enable/disable tablet activities
        for (Class<?> a : sTabletActivities) {
            pm.setComponentEnabledSetting(new ComponentName(context, a),
                    isHoneycombTablet
                            ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                            : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        } 
    }
    
    /**
     * @return True if the current device is honeycomb or newer (API >= 12)
     */
    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }
    
    /**
     * @param context
     * @return True if the device is a tablet, otherwise, false.
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
