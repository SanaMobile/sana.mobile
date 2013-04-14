package org.sana.android.activity;

import android.content.Intent;
import android.os.Bundle;

/**
 * Main Activity which handles user authentication and initializes services that
 * Sana uses.
 * @author Sana Dev Team
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (isSessionExists()) {
            showHomeActivity();
        } else {
            showAuthenticationActivity();
        }
    }
    
    /**
     * Launches the home activity
     */
    void showHomeActivity() {
        Intent intent = new Intent();
        intent.setClass(this, Sana.class);
        switchActivity(intent);
    }
    
    /**
     * Launches the authentication activity
     */
    void showAuthenticationActivity() {
        Intent intent = new Intent();
        intent.setClass(this, AuthenticationActivity.class);
        switchActivity(intent);
    }
    
    // Checks if an existing session exists
    private boolean isSessionExists() {
        // TODO
        return false;
    }

}
