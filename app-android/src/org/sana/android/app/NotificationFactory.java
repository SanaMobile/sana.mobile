/**
 * Copyright (c) 2013, Sana
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sana nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL Sana BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.sana.android.app;

import org.sana.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

/**
 * Utility class for building and sending notifications to the status bar.
 * 
 * @author Sana Development
 *
 */
public class NotificationFactory {
    private final NotificationCompat.Builder mBuilder;
    private final NotificationManager mNotificationManager;
    private final Context mContext;
    private int notifyID = 1;
    
    private NotificationFactory(Context context) {
        mContext = context;
        mBuilder = new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.ic_notification);
        NotificationCompat.InboxStyle style =
                new NotificationCompat.InboxStyle();
        mBuilder.setStyle(style);
        mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    protected String getText(int resId){
        return mContext.getString(resId);
    }
    
    /**
     * Sets the Activity to launch via a PendingIntent when a user selects the
     * notification from the Notification bar.
     * 
     * @param intent The action to take.
     * @return the instance of the NotificationFactory
     * @see android.support.v4.app.NotificationCompat.Builder#setContentIntent(PendingIntent) setContentIntent(PendingIntent)
     */
    public NotificationFactory setContentIntent(PendingIntent intent){
        mBuilder.setContentIntent(intent);
        return this;
    }
    
    /**
     * Sets the notification content text.
     * 
     * @param resID A string resource to use for the content text
     * @return the instance of the NotificationFactory
     * @see android.support.v4.app.NotificationCompat.Builder#setContentText(CharSequence) setContentText(CharSequence)
     */
    public NotificationFactory setContentText(int resID){
        mBuilder.setContentText(getText(resID));
        return this;
    }
    
    /**
     * Sets the notification content text.
     * 
     * @param resID A string resource to use for the content text
     * @param code A code value to represent something meaningful to the notification
     * @return the instance of the NotificationFactory
     * @see android.support.v4.app.NotificationCompat.Builder#setContentText(CharSequence) setContentText(CharSequence)
     */
    public NotificationFactory setContentText(int resID, int code){
        String message = String.format("(%d)%s", code, getText(resID)); 
        mBuilder.setContentText(message);
        return this;
    }
    
    /**
     * Sets the notification title which will be displayed in the notification
     * list.
     * 
     * @param title The title to display 
     * @return the instance of the NotificationFactory
     * @see android.support.v4.app.NotificationCompat.Builder#setContentTitle(CharSequence) setContentTitle(CharSequence)
     */
    public NotificationFactory setContentTitle(int title){
        mBuilder.setContentTitle(mContext.getText(title));
        return this;
    }
    
    /**
     * Builds and sends a notification to the status bar. This will replace any 
     * notifications previously sent via a call to {@link #doNotify()}. 
     * For more options to preserve a previously sent notification, call 
     * {@link #doNotify(int)} or {@link #doNotify(int, String)}.
     * 
     * @see android.app.NotificationManager#notify(int, android.app.Notification) notify(int, android.app.Notification)
     */
    public void doNotify(){
        mNotificationManager.notify(
                notifyID, mBuilder.build());
    }
    
    /**
     * Builds and sends a notification to the status bar. This will replace any 
     * notifications previously sent via a call to {@link #doNotify(int)} using 
     * the same <code>notifyID</code> value or subsequent calls to
     * {@link #doNotify()}. For more options to preserve a previously sent 
     * notification, call {@link #doNotify(int, String)}.
     * 
     * @param notifyID the id value for the notification
     * @see android.app.NotificationManager#notify(int, android.app.Notification) notify(int, android.app.Notification)
     */
    public void doNotify(int notifyID){
        this.notifyID = notifyID;
        mNotificationManager.notify(
                notifyID, mBuilder.build());
    }

    /**
     * Builds and sends a notification to the status bar. This will replace any 
     * notifications previously sent via a call to {@link #doNotify(int, String)} 
     * using the same <code>notifyID</code> and <code>tag</code> values.
     * 
     * @param notifyID the id value for the notification
     * @param tag A string identifier for this notification. May be null.
     * @see android.app.NotificationManager#notify(String, int, android.app.Notification) notify(String, int, android.app.Notification)
     */
    public void doNotify(int notifyID, String tag){
        this.notifyID = notifyID;
        mNotificationManager.notify(
                tag, notifyID, mBuilder.build());
    }
    
    /**
     * Produces a new NotificationFactory instance with the icon set to use a 
     * drawable named <code>ic_notification</code> and style set to InboxStyle
     * @param context
     * @param title
     * @return
     */
    public static NotificationFactory getInstance(Context context){
        NotificationFactory n = new NotificationFactory(context);
        return n;
    }
    
    public NotificationFactory setNumber(int number){
        mBuilder.setNumber(number);
        return this;
    }
    
    public Notification build(){
        return mBuilder.build();
    }
    
    public void cancel (int id){
        mNotificationManager.cancel(id);
    }
    
    public void cancel (String tag, int id){
        mNotificationManager.cancel(tag, id);
    }

    public void cancelAll (){
        mNotificationManager.cancelAll();
    }
}
