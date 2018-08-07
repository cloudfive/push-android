package com.cloudfiveapp.push

import android.content.Intent

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMIntentService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val intent = Intent("Message")

        remoteMessage.data.entries.forEach { (k, v) ->
            intent.putExtra(k, v)
        }

        CloudFivePush.onPushNotificationReceived(intent)
    }
}
