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

/**
 * Receive push notifications from Cloud Five.
 */
class CloudFivePush
private constructor(private val applicationContext: Context,
                    private val senderId: String,
                    private val pushMessageReceiver: PushMessageReceiver) {

    companion object {

        private const val TAG = "CloudFivePush"

        private const val REGISTER_ENDPOINT = "${BuildConfig.BASE_ENDPOINT}/push/register"
        private const val UNREGISTER_ENDPOINT = "${BuildConfig.BASE_ENDPOINT}/push/unregister"

        // We only keep an applicationContext, so this is fine
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: CloudFivePush? = null

        /**
         * Configure CloudFivePush. This should be called from your Application's onCreate method.
         *
         * @param context your application's context
         * @param senderId the Sender ID found in the Firebase console
         * @param pushMessageReceiver a [PushMessageReceiver] that will handle push notifications
         */
        @Suppress("unused", "MemberVisibilityCanBePrivate") // Api
        @JvmStatic
        fun configure(context: Context, senderId: String, pushMessageReceiver: PushMessageReceiver) {
            synchronized(this) {
                instance = CloudFivePush(context.applicationContext, senderId, pushMessageReceiver)
            }
        }

        /**
         * Register this device with Cloud Five to receive push notifications.
         *
         * @param userIdentifier a unique identifier for the current user. this is not necessary
         * if you only send broadcast notifications
         */
        @Suppress("unused", "MemberVisibilityCanBePrivate") // Api
        @JvmStatic
        @JvmOverloads
        fun register(userIdentifier: String? = null) {
            instance?.registerInBackground(userIdentifier)
                    ?: throw IllegalStateException("CloudFivePush has not been configured yet.")
        }

        /**
         * Unregister this device from Cloud Five push notifications.
         *
         * @param userIdentifier the unique identifier used to [register] the current user
         */
        @Suppress("unused", "MemberVisibilityCanBePrivate") // Api
        @JvmStatic
        @JvmOverloads
        fun unregister(userIdentifier: String? = null) {
            instance?.unregisterInBackground(userIdentifier)
                    ?: throw IllegalStateException("CloudFivePush has not been configured yet.")
        }

        internal fun onNewToken() {
            instance?.registerInBackground()
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

    private var userIdentifier: String? = null

    private fun registerInBackground(userIdentifier: String?) {
        this.userIdentifier = userIdentifier
        registerInBackground()
    }

    private fun registerInBackground() {
        RegistrationAsyncTask(this, userIdentifier).execute()
    }

    private fun unregisterInBackground(userIdentifier: String?) {
        this.userIdentifier = null
        UnregistrationAsyncTask(this, userIdentifier).execute()
    }

    private abstract class BaseAsyncTask(cloudFivePush: CloudFivePush,
                                         val userIdentifier: String?)
        : AsyncTask<Unit, Unit, Unit>() {

        private val senderId = cloudFivePush.senderId
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
                    ?: FirebaseInstanceId.getInstance().getToken(senderId, "FCM").also {
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
