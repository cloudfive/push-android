# Cloud Five Push for Android

## Quick Start

### Using Gradle and Android Studio

It's easy, just add this dependency to your app's build.gradle:

```groovy
dependencies {
    compile 'com.cloudfiveapp:push-android:1.2.0'
}
```

Cloud Five is hosted on Maven Central:

```groovy
allprojects {
    repositories {
        mavenCentral()
    }
}
```

You also need to add a custom GCM permission to your app's AndroidManifest.xml.  Just copy this directly before the `<application>` tag in your app:

    <permission android:name="${applicationId}.permission.C2D_MESSAGE" android:protectionLevel="signature" />
    <uses-permission android:name="${applicationId}.permission.C2D_MESSAGE" />

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

We launch a background service called `FCMIntentService` that handles incoming push notifications.

To handle custom data or behavior, simply implement the interface `com.cloudfiveapp.push.PushMessageReceiver` which has one method that receives an intent.

    public void onPushMessageReceived(Intent intent) {
        Bundle extras = intent.getExtras();
        // { alert: "Alert text", message: "Message body", data: {} }
    }

The parameters passed through the API appear in the `extras` of the `Intent`.

## Contributing

We welcome pull requests or issues if you have questions.

### Publishing Prerequisites

Get the GPG backup and import it with `gpg --import c5-backup.pgp`.

In `local.properties` in this project, add the following:

```
signing.gnupg.executable=gpg
signing.gnupg.keyName=<last 16 digits of the GPG key ID>
signing.gnupg.passphrase=<get from 1Password>

ossrhUsername=<get from 1Password, "Maven Jira" user>
ossrhPassword=<get from 1Password, "Maven Jira" user>
```

### Publishing a new version

```sh
./gradlew clean build publishReleasePublicationToSonatypeRepository
```

1. Visit [Nexus Repository Manager](https://s01.oss.sonatype.org/) and sign in as the "10fw" user.
2. Visit "Staging Repositories" in the left nav bar and you should see the version you just uploaded via Gradle.
3. Assuming everything worked correctly, "Close" the repository, then "Release" it.

## Development

### Maven Local

*This hasn't been tested recently and may or may not work after the migration to Maven Central.*

You can build push-android into a local Maven repository by running

    ./gradlew clean build publishToMavenLocal

This will output an `aar` file to `$HOME/.m2/repository/` which can be compiled into an app by adding the `mavenLocal()` repo:

    allprojects {
        repositories {
            mavenLocal()
        }
    }

Then, append `'@aar'` to the dependency:

    implementation 'com.cloudfiveapp:push-android:1.1.0@aar'
