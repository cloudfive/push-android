package com.cloudfiveapp.push

import android.content.Intent

interface PushMessageReceiver {
    fun onPushMessageReceived(intent: Intent)
}
