package com.cloudfiveapp.push;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;


/**
 * @author bsamson
 */

public  class CloudFivePush  {
    public static final String TAG = "CloudFivePush";
    public static final String EXTRA_PUSH_BUNDLE = "pushBundle";

    private static CloudFivePush instance;
    private CloudFivePush() {}
    public static CloudFivePush getInstance() {
        if (instance == null) {
            throw new RuntimeException("Please call CloudFivePush.configure first");
        }
        return instance;
    }

    public static Boolean isConfigured() {
        return instance != null;
    }

    /**
     * Configure Cloud Five Push. This should be called from your Application's or Activity's onCreate method
     * <p>
     *
     * @param context The application context of this application
     * @param gcmSenderId The GCM Sender ID - This is the "Project Number" found on the google api
     *                    console (https://console.developers.google.com)
     * @param pushMessageReceiver A PushMessageReceiver that will handle push notifications.
     *
     */
    public static void configure(Context context, String gcmSenderId, PushMessageReceiver pushMessageReceiver) {
        instance = new CloudFivePush();
        instance.applicationContext = context.getApplicationContext();
        instance.gcmSenderId = gcmSenderId;
        instance.pushMessageReceiver = pushMessageReceiver;
    }

    public static void configure(Context context, String gcmSenderId) {
        configure(context, gcmSenderId, null);
    }

    /**
     * Disable the default notification handling behavior.  This will suppress the Notification, alerts, etc.
     * If you disable the default handlers, be sure to provide a PushMessageReciever to {@code configure}
     * or your app will ignore push notifications.
     *
     */
    public static void disableDefaultNotificationHandler() {
        getInstance().handleNotifications = false;
    }

    /**
     * Enable the default notification handling behavior.  This is enabled by default unless explicitly
     * disabled via {@code disableDefaultNotificationHandler()}
     *
     */
    public static void enableDefaultNotificationHandler() {
        getInstance().handleNotifications = true;
    }

    /**
     *  Register with Google Cloud Messaging in order to receive push notifications
     *  It is not necessary to call this method if you only send broadcast notifications
     *  to all users of this app, only if you wish to target individual users.
     *
     *  @param userIdentifier A unique identifier for the user using the app so you can target
     *                        push notification to this user only.
     */
    public static void register(String userIdentifier) {
        getInstance().userIdentifier = userIdentifier;
        getInstance().registerForRemoteNotifications();
    }

    public static void register() {
        register(null);
    }

    protected void onPushNotificationReceived(Intent intent) {
        if (pushMessageReceiver != null) {
            pushMessageReceiver.onPushMessageReceived(intent);
        }
    }

    protected Boolean handleNotifications() {
        return handleNotifications;
    }

    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "app_version_code";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private Context applicationContext;
    private String userIdentifier;
    private GoogleCloudMessaging gcm;
    private String registrationId;
    private String gcmSenderId;
    private PushMessageReceiver pushMessageReceiver;
    private Boolean handleNotifications = true;

    private void registerForRemoteNotifications() {
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(applicationContext);
            registrationId = getRegistrationId();

            if (registrationId.isEmpty()) {
                registerInBackground();
            } else {
                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] params) {
                        notifyCloudFive();
                        return null;
                    }
                }.execute();
            }
        } else {
            Log.w(TAG, "No valid Google Play Services APK found -- push notifications will not be enabled");
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(applicationContext);
        if (resultCode != ConnectionResult.SUCCESS) {
//            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
//                GooglePlayServicesUtil.getErrorDialog(resultCode, applicationContext, PLAY_SERVICES_RESOLUTION_REQUEST).show();
//            } else {
//                Log.i(TAG, "This device is not supported.");
//            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId() {
        final SharedPreferences prefs = getGCMPreferences();
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(applicationContext);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences() {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the registration ID in your app is up to you.
        return applicationContext.getSharedPreferences(applicationContext.getPackageName() + "-CloudFivePush",
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(applicationContext);
                    }
                    registrationId = gcm.register(getGcmSenderId());
                    msg = "Device registered, registration ID=" + registrationId;
                    notifyCloudFive();

                    // Persist the registration ID - no need to register again.
                    storeRegistrationId();
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }
        }.execute();
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     */
    private void storeRegistrationId() {
        final SharedPreferences prefs = getGCMPreferences();
        int appVersion = getAppVersion(applicationContext);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, registrationId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private void notifyCloudFive() {
        HttpClient httpclient = new DefaultHttpClient();
        Log.i("CloudFivePush", "registering for push notification with registrationId: " + registrationId + " and userIdentifier: " + userIdentifier);
        HttpPost httppost = new HttpPost("https://www.cloudfiveapp.com/push/register");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            //:device_token, :device_model, :device_name, :device_version, :app_version
            nameValuePairs.add(new BasicNameValuePair("device_token", registrationId));
            nameValuePairs.add(new BasicNameValuePair("package_name", applicationContext.getPackageName() ));
            nameValuePairs.add(new BasicNameValuePair("device_model", android.os.Build.MODEL));
            nameValuePairs.add(new BasicNameValuePair("device_name", android.os.Build.DISPLAY));
            nameValuePairs.add(new BasicNameValuePair("device_version", android.os.Build.VERSION.RELEASE));
            nameValuePairs.add(new BasicNameValuePair("device_identifier",
                    Settings.Secure.getString(applicationContext.getContentResolver(), Settings.Secure.ANDROID_ID)));
            nameValuePairs.add(new BasicNameValuePair("device_platform", "android"));
            if (userIdentifier != null) {
                nameValuePairs.add(new BasicNameValuePair("user_identifier", userIdentifier));
            }

            String version;
            try {
                version = applicationContext.getPackageManager().getPackageInfo(applicationContext.getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                version = "unknown";
            }
            nameValuePairs.add(new BasicNameValuePair("app_version", version));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.w(TAG, "Unable to register with cloud five: " + e.getMessage());
            e.printStackTrace();

        }
    }

    private String getGcmSenderId() {
        return gcmSenderId;
    }
}