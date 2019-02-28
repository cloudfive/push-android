package com.cloudfiveapp.push

import android.content.Intent

/**
 * Given to [CloudFivePush.configure] to specify how to handle push notifications.
 */
interface PushMessageReceiver {

    /**
     * Called when a push notification is received.
     *
     * @param intent an [Intent] containing `alert`, `message`, and `data` extras
     */
    fun onPushMessageReceived(intent: Intent)
}
