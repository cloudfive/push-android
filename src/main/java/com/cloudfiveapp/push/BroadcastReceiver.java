package com.cloudfiveapp.push;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class BroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // This is the Intent to deliver to our service.
        ComponentName comp = new ComponentName(context.getPackageName(),  GCMIntentService.class.getName());

        // Start the service, keeping the device awake while it is launching.
        Log.i("BroadcastReceiver", "Starting service @ " + SystemClock.elapsedRealtime());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}