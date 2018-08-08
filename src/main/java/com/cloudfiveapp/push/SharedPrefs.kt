package com.cloudfiveapp.push

import android.content.Context
import android.content.SharedPreferences
import java.util.*

internal class SharedPrefs(context: Context) {

    companion object {
        private const val SHARED_PREFS_NAME_SUFFIX = "-CloudFivePush"
        private const val KEY_DEVICE_IDENTIFIER = "device_id"
    }

    private val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(
                    context.packageName + SHARED_PREFS_NAME_SUFFIX,
                    Context.MODE_PRIVATE)

    val deviceIdentifier: String
        get() = sharedPreferences.getString(KEY_DEVICE_IDENTIFIER, null)
                ?: UUID.randomUUID().toString().also { deviceIdentifier ->
                    val editor = sharedPreferences.edit()
                    editor.putString(KEY_DEVICE_IDENTIFIER, deviceIdentifier)
                    editor.apply()
                }
}
