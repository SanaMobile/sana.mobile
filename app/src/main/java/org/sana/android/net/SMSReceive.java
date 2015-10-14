/* 
 * Copyright (c) 2010 - 2011, Sana
 * All rights reserved.
 * License: BSD New
 */
package org.sana.android.net;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sana.R;
import org.sana.android.db.NotificationMessage;
import org.sana.android.provider.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * SMSReceive handles the doctor->healthworker diagnosis exchange.
 * 
 * The diagnosis from the physician comes to the phone via SMS. The body of the
 * SMS has a Sana header that tells Sana, which listens for SMS messages, that
 * the SMS is destined for it. Sana grabs the SMS and creates a custom Android
 * notification from it. In addition, the diagnosis is filed in the phone's
 * database of received notifications.
 * 
 * @author Sana Development Team
 */
public class SMSReceive extends BroadcastReceiver {
	private static final String TAG = SMSReceive.class.toString();
	private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

	/**
	 * Automatically called when an SMS message is received. This method does
	 * four things: 1) It reads the incoming SMS to make sure it is well formed
	 * (it came from the dispatch server) 2) It stores the diagnosis
	 * notification in the notification database 3) It pops up a temporary
	 * message saying a diagnosis has been received 4) It inserts an alert into
	 * the status bar, clearing all previous alerts in the status bar
	 * 
	 * A well-formed SMS message looks like this: <patient=422>Prescribe him
	 * antibiotics.
	 */
	public void onReceive(Context context, Intent intent) {
		if (!intent.getAction().equals(ACTION))
			return;
		
		Bundle bundle = intent.getExtras();
		if (bundle == null)
			return;
		
		Object[] pdus = (Object[]) bundle.get("pdus");
		if (pdus == null)
			return;
		
		for (int i = 0; i < pdus.length; ++i) {
			SmsMessage m = SmsMessage.createFromPdu((byte[]) pdus[i]); 
			Log.i(TAG, "Got message from" + m.getOriginatingAddress());
			Log.i(TAG, m.toString());
			processMessage(context, m);
		}
	}
	
	private void processNotificationMessage(Context context, 
			MDSNotification notificationHeader, String message) 
	{
		Gson g = new Gson();
		
		if (notificationHeader.n == null) {
			Log.e(TAG, "Received mal-formed notification UUID -- none provided.");
		}
		
		Cursor c = context.getContentResolver().query(Notifications.CONTENT_URI, 
				new String[] { Notifications.Contract._ID, 
							   Notifications.Contract.PATIENT_ID, 
							   Notifications.Contract.PROCEDURE_ID, 
							   Notifications.Contract.MESSAGE }, 
							   Notifications.Contract.UUID+"=?",
				new String[] { notificationHeader.n }, null);
		
		ContentValues cv = new ContentValues();
		String patientId = null;
		if (notificationHeader.p != null) {
			patientId = notificationHeader.p;
			cv.put(Notifications.Contract.PATIENT_ID, notificationHeader.p);
		}
		if (notificationHeader.c != null) {
			cv.put(Notifications.Contract.PROCEDURE_ID, notificationHeader.c);
		}
		Uri notificationUri;
		
		boolean complete = false;
		String fullMessage = "";
		Pattern pattern = Pattern.compile("^(\\d+)/(\\d+)$");
		if (c.moveToFirst()) {
			// Notification already exists
			int notificationId = c.getInt(c.getColumnIndex(
					Notifications.Contract._ID));
			String storedMessage = c.getString(c.getColumnIndexOrThrow(
					Notifications.Contract.MESSAGE));
			
			NotificationMessage m = g.fromJson(storedMessage, 
					NotificationMessage.class);
			
			if (patientId == null) {
				patientId = c.getString(c.getColumnIndex(
						Notifications.Contract.PATIENT_ID));
			}
			
			if (notificationHeader.d != null) {
				Matcher matcher = pattern.matcher(notificationHeader.d);
				if (matcher.matches()) {
					Integer current = Integer.parseInt(matcher.group(1));
					Integer total = Integer.parseInt(matcher.group(2));
					m.receivedMessages++;
					assert(m.totalMessages == total);
					m.messages.put(current, message);
					
					if (m.totalMessages == m.receivedMessages) { 
						complete = true;
						StringBuilder sbFullMessage = new StringBuilder();
						for (int i = 1; i <= m.totalMessages; i++) {
							sbFullMessage.append(m.messages.get(i));
						}
						fullMessage = sbFullMessage.toString();
						cv.put(Notifications.Contract.FULL_MESSAGE, fullMessage);
						cv.put(Notifications.Contract.DOWNLOADED, 1);
					}
				}
			} else {
				Log.e(TAG, "Received mal-formed Notification Message length: " 
						+ notificationHeader.d);
			}
			
			c.close();
			
			storedMessage = g.toJson(m);
			cv.put(Notifications.Contract.MESSAGE, storedMessage);
			
			notificationUri = ContentUris.withAppendedId(
					Notifications.CONTENT_URI, notificationId);
			int rowsUpdated = context.getContentResolver().update(
					notificationUri, cv, null, null);
			if (rowsUpdated != 1) {
				Log.e(TAG, "Failed updating notification URI: " 
						+ notificationUri);
			}
		} else {
			// Notification is new, create one.
			NotificationMessage m = new NotificationMessage();
			if (notificationHeader.d != null) {
				// This is a multipart message
				Log.i(TAG, "Received multi-part SMS");
				String parts = notificationHeader.d;
				
				Matcher matcher = pattern.matcher(notificationHeader.d);
				if (matcher.matches()) {
					Integer current = Integer.parseInt(matcher.group(1));
					Integer total = Integer.parseInt(matcher.group(2));
					m.totalMessages = total;
					m.receivedMessages = 1;
					m.messages.put(current, message);
					
					if (m.totalMessages == m.receivedMessages) {
						complete = true;
						StringBuilder sbFullMessage = new StringBuilder();
						for (int i = 1; i <= m.totalMessages; i++) {
							sbFullMessage.append(m.messages.get(i));
						}
						fullMessage = sbFullMessage.toString();
						cv.put(Notifications.Contract.FULL_MESSAGE, fullMessage);
						cv.put(Notifications.Contract.DOWNLOADED, 1);
					}
					
				} else {
					Log.e(TAG, "Received malformed Notification Message length:" 
							+ " " + notificationHeader.d);
				}
			} else {
				// This is a single message. 
				Log.i(TAG, "Received single-part SMS");
				m.totalMessages = 1;
				m.receivedMessages = 1;
				m.messages.put(1, message);
				cv.put(Notifications.Contract.FULL_MESSAGE, message);
				cv.put(Notifications.Contract.DOWNLOADED, 1);
				complete = true;
			}
			
			String storedMessage = g.toJson(m);
			cv.put(Notifications.Contract.MESSAGE, storedMessage);
			cv.put(Notifications.Contract.UUID, 
					notificationHeader.n);
			notificationUri = context.getContentResolver().insert(
					Notifications.CONTENT_URI, cv);
		}
		
		
		if (complete) {
			// Show Toast that a notification was received
			String notifHdr = "DIAGNOSIS RECEIVED\nPatient ID# " 
				+ patientId + "\n";
			Toast.makeText(context, notifHdr, Toast.LENGTH_LONG).show();
			
			Intent viewIntent = new Intent(Intent.ACTION_VIEW, notificationUri);
			showNotification(context, "Patient ID# " + patientId, fullMessage, 
					viewIntent);			
		}
	}
	
	private void processMessage(Context context, SmsMessage m) {
		String msg = m.getDisplayMessageBody();
		// check if this is a patient diagnosis SMS
		Log.i(TAG, "Got SMS message " + msg);
		
		String leftCurly = new String(new byte[] { 0x1B, 0x28 });
		String rightCurly = new String(new byte[] { 0x1b, 0x29 });
		
		// Decode escapes
		
		msg = msg.replace(leftCurly, "{").replace(rightCurly, "}");
		Log.i(TAG, "Decode1: " + msg);
		msg = msg.replace("?(", "{").replace("?)", "}");
		Log.i(TAG, "Decode2: " + msg);

		int lastRightBrace = msg.lastIndexOf('}');
		
		if (lastRightBrace == -1) {
			Log.i(TAG, "SMS not destined for Sana. Wrong header.");
			return;
		}
		
		Gson gson = new Gson();
		
		try {
			String header = msg.substring(0, lastRightBrace+1);
			String message = msg.substring(lastRightBrace+1);
			MDSNotification notificationHeader = gson.fromJson(header, 
					MDSNotification.class);
			processNotificationMessage(context, notificationHeader, message);
		} catch (JsonParseException e) {
			Log.i(TAG, "Could not parse Sana header in SMS: " + e.toString());
			return;
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			return;
		}
	}

	/**
	 * Creates a notification used when a patient diagnosis is received.
	 * 
	 * @param c 	current context
	 * @param title the patient's ID number should be the title
	 * @param textMessage the message text sent from the doctor
	 * @param viewIntent
	 */
	private void showNotification(Context c, String title, String textMessage, 
			Intent viewIntent) {
		// Look up the notification manager service
		NotificationManager nm = (NotificationManager) c
				.getSystemService(Context.NOTIFICATION_SERVICE);

		// The PendingIntent launches the Notification Viewer for the particular
		// alert
		PendingIntent contentIntent = PendingIntent.getActivity(c, 0, 
				viewIntent, 0);

		// The ticker text
		String tickerText = "PATIENT DIAGNOSIS RECEIVED";

		// Construct the Notification object.
		Notification notif = new Notification(R.drawable.ic_notification, tickerText,
				System.currentTimeMillis());

		// Set the info for the views that show in the notification panel
		//notif.setLatestEventInfo(c, title, textMessage, contentIntent);

		// After a 100ms delay, vibrate for 200ms, pause for 100 ms and
		// then vibrate for 300ms.
		notif.vibrate = new long[] { 100, 200, 100, 300 };

		// Use this line if you want a new persistent notification each time:
		// nm.notify((int)Math.round((Math.random() * 32000)), notif);

		// Or use this to overwrite the last notification each time:
		nm.cancelAll();
		nm.notify(1, notif);
	}
}
