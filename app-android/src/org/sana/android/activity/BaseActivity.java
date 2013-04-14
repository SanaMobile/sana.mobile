package org.sana.android.activity;

import android.content.Intent;

import com.actionbarsherlock.app.SherlockActivity;

/**
 * Base class that contains basic functionalities and behaviors that all
 * activities should do. 
 * @author Sana Dev Team
 */
public abstract class BaseActivity extends SherlockActivity {
    
    /**
     * Finishes the calling activity and launches the activity contained in
     * <code>intent</code>
     * @param intent
     */
    void switchActivity(Intent intent) {
        startActivity(intent);
        finish();
    }
    
}
