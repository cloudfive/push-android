# Cloud Five Push for Android

## Quick Start

### Using Gradle and Android Studio


It's easy, just add this dependency to your app's build.gradle:

    dependencies {
        compile 'com.cloudfiveapp:push-android:0.9.8'
    }

Cloud Five is hosted on the jcenter repository which is included in new android projects by default. You can verify this by looking at your main build.gradle:

    allprojects {
        repositories {
            jcenter()
        }
    }

You also need to add a custom GCM permission to your app's AndroidManifest.xml.  Just copy this directly before the `<application>` tag in your app:

    <permission android:name="${applicationId}.permission.C2D_MESSAGE" android:protectionLevel="signature" />
    <uses-permission android:name="${applicationId}.permission.C2D_MESSAGE" />

### Maven Local

You can build push-android into a local Maven repository by running

    ./gradlew clean build publishToMavenLocal

This will output an `aar` file to `$HOME/.m2/repository/` which can be compiled into an app by adding the `mavenLocal()` repo:

    allprojects {
        repositories {
            mavenLocal()
        }
    }

Then, append `'@aar'` to the dependency:

    implementation 'com.cloudfiveapp:push-android:0.10.0@aar'

### Ant/Other builds

You can download the AAR file from [the bintray project page](https://bintray.com/cloudfive/maven/push-android/)

Or download the AAR file directly:

[Latest build (0.9.8)](https://bintray.com/artifact/download/cloudfive/maven/com/cloudfiveapp/push-android/0.9.8/push-android-0.9.8.aar)

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

## Default Behavior
The default push handling behavior is quite naive, but often sufficient for barebones functionality.  The behavior depends on the different keys sent from the server.

### alert
If the `alert` key is present, CloudFive will display a notification in the titlebar with the app's default logo, and when the notification is tapped it simply launched the app.

### message
The `message` key is meant for sending longer text. if this key is present, we display a popup alert that shows the full text.

### data
The `data` key is ignored by default and requires advanced implementation (see below)

## Advanced Configuration

We launch a background service called FCMIntentService that handles incoming push notifications.

To handle custom data or behavior, simply implement the interface `com.cloudfiveapp.push.PushMessageReceiver` which has one method that receives an intent.

    public void onPushMessageReceived(Intent intent) {
        Bundle extras = intent.getExtras();
        // { alert: "Alert text", message: "Message body", data: {} }
    }

The parameters passed through the API appear in the `extras` of the `Intent`.

Note that is the `alert` and `message` keys are present, cloud five will still create the notification and popup.  To display this behavior, simply call:

    CloudFivePush.disableDefaultNotificationHandler();

after your initialization code.

## Contributing

We welcome pull requests or issues if you have questions.

### Publishing a new version

Make sure you update the version number in the gradle config, get the correct access keys to bintray and run gradle bintrayUpload
