package com.cloudfiveapp.push;

import android.content.Intent;

/**
 * Created by bsamson on 3/24/15.
 */
public interface PushMessageReceiver {
    public void onPushMessageReceived(Intent intent);
}
