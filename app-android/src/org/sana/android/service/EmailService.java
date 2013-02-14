package org.sana.android.service;


import android.app.IntentService;
import android.content.Intent;

/**
 * Provides email sending services. Emails may contain text and zero or more 
 * attachments. 
 *
 * @author Sana Development Team
 */
public class EmailService extends IntentService{
	public static final String SENT = "_sent";
	public static final String FAILED = "_fail";

	public EmailService(String name) {
		super("EmailService");
	}

	/**
	 *  Classes wishing to access the email sending functionality should pass an 
	 *  intent constructed with the following action and extras.
	 *  
	 *  recipient addresses:
	 *  	Intent.EXTRA_EMAIL, String[]
	 *  subject:
	 *  	Intent.EXTRA_SUBJECT, String
	 *  text:
	 *  	Intent.EXTRA_TEXT, CharSequence
	 *  attachments:
	 *  	Intent.EXTRA_STREAM, List&lt;Uri&gt; or Uri
	 *  
	 */
	protected void onHandleIntent(Intent intent) {
		try{
			// TODO Auto-generated method stub
			final Intent mailer = new Intent(Intent.ACTION_SEND);
			mailer.setType("message/rfc822");
			mailer.putExtras(intent);
			mailer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//TODO Success status?
			startActivity(mailer);
			sendBroadcast(new Intent(SENT));
		} catch (Exception e){
			sendBroadcast(new Intent(FAILED));
		}
	}

}
