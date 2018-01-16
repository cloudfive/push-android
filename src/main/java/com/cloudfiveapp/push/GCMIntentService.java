package com.cloudfiveapp.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMIntentService extends GcmListenerService {

    public static final int NOTIFICATION_ID = 415;
    private static final String TAG = "GCMIntentService";

    public static void cancelNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(getAppName(context), NOTIFICATION_ID);
    }

    public static String getAppName(Context context) {
        return context.getPackageManager()
                .getApplicationLabel(context.getApplicationInfo())
                .toString();
    }

    @Override
    public void onMessageReceived(String s, Bundle bundle) {
        super.onMessageReceived(s, bundle);

        Intent intent = new Intent("Message");
        intent.putExtras(bundle);

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
    }

    public void createNotification(Bundle extras) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String appName = getAppName(this);

        Intent notificationIntent = new Intent(this, PushHandlerActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra(CloudFivePush.EXTRA_PUSH_BUNDLE, extras);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String message = extras.getString("message");
        String alert = extras.getString("alert");
        if (message == null && alert == null) {
            return;
        }
        if (message == null) {
            message = alert;
            alert = getAppName(this);
        }

        if (alert == null) {
            alert = getAppName(this);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setSmallIcon(this.getApplicationInfo().icon)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(alert)
                        .setTicker(alert)
                        .setContentIntent(contentIntent);

        builder.setContentText(message);

        String msgcnt = extras.getString("msgcnt");
        if (msgcnt != null) {
            builder.setNumber(Integer.parseInt(msgcnt));
        }

        notificationManager.notify(appName, NOTIFICATION_ID, builder.build());
    }
}
