package org.sana.android.app;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 *
 */
public final class Preferences {

    private Preferences(){}

    public static String getString(Context context, String name,
                                             String defValue){
        return PreferenceManager.getDefaultSharedPreferences(context).getString
                (name,
                defValue);
    }

    public static String getString(Context context, String name){
        return getString(context,name,null);
    }
}
