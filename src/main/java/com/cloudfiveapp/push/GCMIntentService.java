package com.cloudfiveapp.push;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class GCMIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 415;
    private static final String TAG = "GCMIntentService";

    public GCMIntentService() {
        super("GCMIntentService");
    }

    protected void onHandleIntent(Intent intent) {
        // Pass the intent to any custom handlers registered
        CloudFivePush.getInstance().onPushNotificationReceived(intent);

        if (CloudFivePush.getInstance().handleNotifications()) {
            Bundle extras = intent.getExtras();
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            String messageType = gcm.getMessageType(intent);

            if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
                if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                    createNotification(extras);
                    Log.i(TAG, "Received Push: " + extras.toString());
                }
            }
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        BroadcastReceiver.completeWakefulIntent(intent);
    }

    public void createNotification(Bundle extras)
    {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String appName = getAppName(this);

        Intent notificationIntent = new Intent(this, PushHandlerActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra("pushBundle", extras);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        String message = extras.getString("message");
        String alert = extras.getString("alert");
        if (message == null) {
            message = alert;
            alert = GCMIntentService.getAppName(this);
        }

        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setSmallIcon(this.getApplicationInfo().icon)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(alert)
                        .setTicker(alert)
                        .setContentIntent(contentIntent);

        mBuilder.setContentText(message);

        String msgcnt = extras.getString("msgcnt");
        if (msgcnt != null) {
            mBuilder.setNumber(Integer.parseInt(msgcnt));
        }

        mNotificationManager.notify((String) appName, NOTIFICATION_ID, mBuilder.build());
    }

    public static void cancelNotification(Context context)
    {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel((String)getAppName(context), NOTIFICATION_ID);
    }

    private static String getAppName(Context context)
    {
        CharSequence appName =
                context
                        .getPackageManager()
                        .getApplicationLabel(context.getApplicationInfo());

        return (String)appName;
    }


}