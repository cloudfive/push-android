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
        Bundle pushBundle = intent.getExtras().getBundle("pushBundle");
        if (pushBundle.containsKey("alert")) {
            showPushAlert(pushBundle);
        } else {
            transitionToLaunchActivity();
        }
    }

    private void transitionToLaunchActivity() {
        PackageManager pm = getPackageManager();
        Intent launchActivityIntent = pm.getLaunchIntentForPackage(this.getPackageName());
        startActivity(launchActivityIntent);
        finish();
    }


    public void showPushAlert(Bundle extras) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String title = extras.getString("alert");
        String message = extras.getString("message");
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
                    transitionToLaunchActivity();
                }
            });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}