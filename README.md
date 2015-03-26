# Cloud Five Push for Android

## Quick Start

### Using Gradle and Android Studio

Add the following repository to your main build.gradle:

    allprojects {
        repositories {
            jcenter()
            maven {
                url  "http://dl.bintray.com/cloudfive/maven"
            }
        }
    }

Add this dependency to your app's build.gradle:

    dependencies {
        compile 'com.cloudfiveapp:push-android:0.9.3'
    }

### Ant/Other builds

You can download the AAR file from [the bintray project page](https://bintray.com/cloudfive/maven/push-android/)

Or download the AAR file directly:

[Latest build (0.9.3)](https://bintray.com/artifact/download/cloudfive/maven/com/cloudfiveapp/push-android/0.9.3/push-android-0.9.3.aar)

## Configuration

In either `Application.onCreate` or your launch `Activity.onCreate`, you need to configure Push with your GCM Sender ID (This is the project number found on the [Google API Console](https://console.developers.google.com)

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


That's it!  Now you can send basic push notifications which will create a notification icon in the title bar and launch your app and optionally show an alert dialog when tapped.

## Advanced Configuration

* coming soon
