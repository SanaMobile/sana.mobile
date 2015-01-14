package org.sana.android.content;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class DispatchResponseReceiver extends BroadcastReceiver {

	private static final String TAG = DispatchResponseReceiver.class.getSimpleName();
	public static final String KEY_RESPONSE_MESSAGE = "response_message";
	public static final String KEY_RESPONSE_CODE = "response_code";
	public static final String KEY_RESPONSE_ID = "response_id";
	public static final String BROADCAST_RESPONSE = "org.sana.android.DISPATCH_RESPONSE";
	
	
	private final String defaultMessage;
	
	public DispatchResponseReceiver() {
		this(DispatchResponseReceiver.class.getSimpleName());
	}

	public DispatchResponseReceiver(String defaultMessage) {
		super();
		this.defaultMessage = new String(defaultMessage);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "context: " + context.getClass().getSimpleName() + ", intent: " + intent.toUri(Intent.URI_INTENT_SCHEME));
		if(context instanceof Activity){
			String text = intent.hasExtra(KEY_RESPONSE_MESSAGE)? intent.getStringExtra(KEY_RESPONSE_MESSAGE): getDefaultMessage() + intent.getDataString();
			Toast.makeText(((Activity) context).getBaseContext(), text, Toast.LENGTH_LONG).show();
		} else {
			
		}
	}

	public String getDefaultMessage(){
		return defaultMessage;
	}
	
}
