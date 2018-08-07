package com.cloudfiveapp.push

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.support.annotation.WorkerThread
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import java.io.IOException

class CloudFivePush
private constructor(private val applicationContext: Context,
                    private val gcmSenderId: String,
                    private val pushMessageReceiver: PushMessageReceiver) {

    companion object {

        private const val TAG = "CloudFivePush"

        private const val REGISTER_ENDPOINT = "${BuildConfig.BASE_ENDPOINT}/push/register"
        private const val UNREGISTER_ENDPOINT = "${BuildConfig.BASE_ENDPOINT}/push/unregister"

        // We only keep an applicationContext, so this is fine
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: CloudFivePush? = null

        @Suppress("unused") // Api
        @JvmStatic
        fun isConfigured(): Boolean {
            return instance != null
        }

        @Suppress("unused") // Api
        @JvmStatic
        fun configure(context: Context, gcmSenderId: String, pushMessageReceiver: PushMessageReceiver) {
            synchronized(this) {
                instance = CloudFivePush(context.applicationContext, gcmSenderId, pushMessageReceiver)
            }
        }

        @Suppress("unused") // Api
        @JvmStatic
        fun register(userIdentifier: String) {
            instance?.registerInBackground(userIdentifier)
                    ?: throw IllegalStateException("CloudFivePush has not been configured yet.")
        }

        @Suppress("unused") // Api
        @JvmStatic
        fun unregister(userIdentifier: String?) {
            instance?.unregisterInBackground(userIdentifier)
                    ?: throw IllegalStateException("CloudFivePush has not been configured yet.")
        }

        internal fun onPushNotificationReceived(intent: Intent) {
            instance?.pushMessageReceiver?.onPushMessageReceived(intent)
        }
    }

    private val sharedPrefs: SharedPrefs by lazy {
        SharedPrefs(applicationContext)
    }

    private val deviceIdentifier: String
        get() = sharedPrefs.deviceIdentifier

    private val packageName = applicationContext.packageName

    private val appVersion = try {
        applicationContext.packageManager.getPackageInfo(packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        "unknown"
    }

    private fun registerInBackground(userIdentifier: String?) {
        RegistrationAsyncTask(this, userIdentifier).execute()
    }

    private fun unregisterInBackground(userIdentifier: String?) {
        UnregistrationAsyncTask(this, userIdentifier).execute()
    }

    private abstract class BaseAsyncTask(cloudFivePush: CloudFivePush,
                                         val userIdentifier: String?)
        : AsyncTask<Unit, Unit, Unit>() {

        private val gcmSenderId = cloudFivePush.gcmSenderId
        private val deviceIdentifier = cloudFivePush.deviceIdentifier
        private val packageName = cloudFivePush.packageName
        private val appVersion = cloudFivePush.appVersion

        protected fun getParams(): UrlEncodedFormEntity {
            val nameValuePairs = mutableListOf<NameValuePair>().apply {
                add(BasicNameValuePair("device_token", getRegistrationId()))
                add(BasicNameValuePair("package_name", packageName))
                add(BasicNameValuePair("device_model", android.os.Build.MODEL))
                add(BasicNameValuePair("device_name", android.os.Build.DISPLAY))
                add(BasicNameValuePair("device_identifier", deviceIdentifier))
                add(BasicNameValuePair("device_platform", "android"))
                add(BasicNameValuePair("app_version", appVersion))
                if (userIdentifier != null) {
                    add(BasicNameValuePair("user_identifier", userIdentifier))
                }
            }
            return UrlEncodedFormEntity(nameValuePairs)
        }

        private var _registrationId: String? = null

        @WorkerThread
        protected fun getRegistrationId(): String {
            // Must not be run on main thread
            return _registrationId
                    ?: FirebaseInstanceId.getInstance().getToken(gcmSenderId, "FCM").also {
                        _registrationId = it
                    }
        }
    }

    private class RegistrationAsyncTask(cloudFivePush: CloudFivePush,
                                        userIdentifier: String?)
        : BaseAsyncTask(cloudFivePush, userIdentifier) {

        override fun doInBackground(vararg params: Unit) {
            val httpClient = DefaultHttpClient()
            Log.i(TAG, "Registering for push notification with registrationId: ${getRegistrationId()} and userIdentifier: $userIdentifier")
            val httpPost = HttpPost(REGISTER_ENDPOINT)
            httpPost.entity = getParams()

            try {
                httpClient.execute(httpPost)
            } catch (e: IOException) {
                Log.w(TAG, "Unable to register with cloud five: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private class UnregistrationAsyncTask(cloudFivePush: CloudFivePush,
                                          userIdentifier: String?)
        : BaseAsyncTask(cloudFivePush, userIdentifier) {

        override fun doInBackground(vararg params: Unit) {
            val httpClient = DefaultHttpClient()
            Log.i(TAG, "Unregistering for push notification with registrationId: ${getRegistrationId()} and userIdentifier: $userIdentifier")
            val httpPost = HttpPost(UNREGISTER_ENDPOINT)
            httpPost.entity = getParams()

            try {
                httpClient.execute(httpPost)
            } catch (e: IOException) {
                Log.w(TAG, "Unable to unregister from cloud five: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
