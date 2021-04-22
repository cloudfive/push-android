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

You also need to add a custom GCM permission to your app's AndroidManifest.xml.  Just copy this
directly before the `<application>` tag in your app:

    <permission android:name="${applicationId}.permission.C2D_MESSAGE" android:protectionLevel="signature" />
    <uses-permission android:name="${applicationId}.permission.C2D_MESSAGE" />

## Configuration

In `Application.onCreate`, you need to configure `CloudFivePush`:

```kotlin
override fun onCreate() {
    super.onCreate()
    val pushMessageReceiver: PushMessageReceiver
    // Implement your own instance of PushMessageReceiver
    CloudFivePush.configure(this, pushMessageReceiver)
}
```

Then, register to receive push notifications:

```kotlin
CloudFivePush.register()

// If you wish to send targeted notifications to specific users, simply pass in a
// unique user identifier:
CloudFivePush.register('user@example.com')
```

That's it! Now you can receive pushes from Cloudfive and handle them in your `PushMessageReceiver`.

## `PushMessageReceiver` Configuration

We launch a background service called `FCMIntentService` that handles incoming push notifications.

To handle custom data or behavior, simply implement the interface
`com.cloudfiveapp.push.PushMessageReceiver` which has one method that receives an intent.

```kotlin
override fun onPushMessageReceived(intent: Intent) {
    val alert = intent.extras?.getString("alert")
    val message = intent.extras?.getString("message")
    val dataStr = intent.extras?.getString("data")
    // Create a notification
}
```

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

This will output an `aar` file to `$HOME/.m2/repository/` which can be compiled into an app by
adding the `mavenLocal()` repo:

    allprojects {
        repositories {
            mavenLocal()
        }
    }

Then, append `'@aar'` to the dependency:

    implementation 'com.cloudfiveapp:push-android:1.1.0@aar'
