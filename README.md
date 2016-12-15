# Cloud Five Push for Android

## Quick Start

### Using Gradle and Android Studio


It's easy, just add this dependency to your app's build.gradle:

    dependencies {
        compile 'com.cloudfiveapp:push-android:0.9.5'
    }

Cloud Five is hosted on the jcenter repository which is included in new android projects by default.
You can verify this by looking at your main build.gradle:

    allprojects {
        repositories {
            jcenter()
        }
    }

You also need to add a custom GCM permission to your app's AndroidManifest.xml.  Just copy this
directly before the `<application>` tag in your app:

    <permission android:name="${applicationId}.permission.C2D_MESSAGE" android:protectionLevel="signature" />
    <uses-permission android:name="${applicationId}.permission.C2D_MESSAGE" />

### Ant/Other builds

You can download the AAR file from [the bintray project page](https://bintray.com/cloudfive/maven/push-android/)

Or download the AAR file directly:

[Latest build (0.9.5)](https://bintray.com/artifact/download/cloudfive/maven/com/cloudfiveapp/push-android/0.9.5/push-android-0.9.5.aar)

## Configuration

In either `Application.onCreate` or your launch `Activity.onCreate`, you need to configure Push with
your GCM Sender ID (This is the project number found on the [Google API Console](https://console.developers.google.com)

    @Override
    public void onCreate() {
        super.onCreate();
        CloudFivePush.configure(this, <your GCM Sender ID>);
    }

Then, register to receive push notifications:

    CloudFivePush.register();

    // If you wish to send targeted notifications to specific users, simply pass in a
    // unique user identifier:

    CloudFivePush.register('user@example.com');


That's it!  Now you can send basic push notifications which will create a notification icon in the
title bar and launch your app and optionally show an alert dialog when tapped.

## Advanced Configuration

After someone interacts with the notification, your apps main launch activity will be called with
an intent with the full notification payload included.  You can use this to create custom behavior.
