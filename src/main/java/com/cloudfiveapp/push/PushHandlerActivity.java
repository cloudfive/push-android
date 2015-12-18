package com.cloudfiveapp.push;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;

public class PushHandlerActivity extends Activity
{
    private static String TAG = "PushHandlerActivity";

    /*
     * this activity will be started if the user touches a notification that we own.
     * We send it's data off to the push plugin for processing.
     * If needed, we boot up the main activity to kickstart the application.
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        GCMIntentService.cancelNotification(this);
        Intent intent = getIntent();
        Bundle pushBundle = intent.getExtras().getBundle(CloudFivePush.EXTRA_PUSH_BUNDLE);
        if (pushBundle.containsKey("message")) {
            showPushAlert(pushBundle);
        } else {
            transitionToLaunchActivity(pushBundle);
        }
    }

    private void transitionToLaunchActivity(Bundle pushBundle) {
        PackageManager pm = getPackageManager();
        Intent launchActivityIntent = pm.getLaunchIntentForPackage(this.getPackageName());
        launchActivityIntent.putExtra(CloudFivePush.EXTRA_PUSH_BUNDLE, pushBundle);
        startActivity(launchActivityIntent);
        finish();
    }


    public void showPushAlert(final Bundle pushBundle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String title = pushBundle.getString("alert");
        String message = pushBundle.getString("message");
        if (message == null) {
            message = title;
            PackageManager packageManager = this.getPackageManager();
            try {
                title = packageManager.getApplicationLabel(packageManager.getApplicationInfo(this.getPackageName(), 0)).toString();
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        builder.setMessage(message)
            .setTitle(title)
            .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    transitionToLaunchActivity(pushBundle);
                }
            });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}