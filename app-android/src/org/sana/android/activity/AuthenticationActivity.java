
package org.sana.android.activity;

import org.sana.R;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * Activity that handles user authentication
 * 
 * @author Sana Dev Team
 */
public class AuthenticationActivity extends BaseActivity {

    // Views
    EditText mInputUsername;

    EditText mInputPassword;

    Button mBtnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        
        mInputUsername = (EditText) findViewById(R.id.input_username);
        mInputPassword = (EditText) findViewById(R.id.input_password);
        mBtnLogin = (Button) findViewById(R.id.btn_login);
        mBtnLogin.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                logIn();
            }
        });
    }
    
    // Attempts a log-in
    private void logIn() {
        // TODO
    }
}
