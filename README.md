# Cloud Five Push for Android

## Quick Start

### Using Gradle and Android Studio

It's easy, just add this dependency to your app's build.gradle:

```groovy
dependencies {
    compile 'com.cloudfiveapp:push-android:x.y.z'
}
```

The current version can be found
[here](https://bintray.com/cloudfive/maven/push-android).

Cloud Five is hosted on the jcenter repository which is included in new
android projects by default. You can verify this by looking at your main
build.gradle:

```groovy
allprojects {
    repositories {
        jcenter()
    }
}
```

## Configuration

In either `Application.onCreate` you need to configure `push-android`
with an instance of a `PushMessageReceiver`.  The class implementing
this interface will be called whenever `push-android` receives a message.

```java
@Override
public void onCreate() {
    super.onCreate();
    CloudFivePush.configure(this, pushMessageReceiver);
}
```

Then, register to receive push notifications:

```java
CloudFivePush.register();

// If you wish to send targeted notifications to specific users, simply pass in a
// unique user identifier:

CloudFivePush.register('user@example.com');
```


That's it!  Now you can send basic push notifications which will call
your implementation of `PushMessageReceiver` detailed below.

### `PushMessageReceiver` Configuration

As part of configuration, you provide an implementation of
`PushMessageReceiver` that CloudFivePush will call to handle received
push notifications. CloudFivePush passes an `Intent` to your
implementation. The extras in that intent contain the information
CloudFivePush received.

```java
public class MyPushReceiver implements PushMessageReceiver
    public void onPushMessageReceived(Intent intent) {
        Bundle extras = intent.getExtras();
        String alert = extras.getString("alert")
        String message = extras.getString("message")
        String data = extras.getString("data")
    }
}
```

Note that `data` is a `String`, but you may pass JSON in there. You can
then parse that with the built in `org.json.JSONObject` class or other
JSON parsing libraries.

### Advanced Firebase Configuration

Sometimes it may be useful to have multiple Firebase projects associated
with your app. If the Firebase project that you want to use for
Messaging is not your default `FirebaseApp` instance, you can pass the
instance name into `CloudFivePush.configure` and CloudFivePush will use
the correct instance when hooking into Firebase Messaging. See this
[Firebase documentation](https://firebase.google.com/docs/projects/multiprojects)
for more information about configuring multiple Firebase projects.

```java
FirebaseApp.initializeApp(this, firebaseOptions, "messaging-instance-name");

CloudFivePush.configure(context, pushMessageReceiver, "messaging-instance-name");
```

## Contributing

We welcome pull requests or issues if you have questions.

### Publishing a new version

Make sure you update the version number in the gradle config, get the
correct access keys to bintray and run:

```sh
./gradlew bintrayUpload
```

## Development

### Maven Local

You can build and publish `push-android` to a local Maven repository by
running:

```sh
./gradlew clean build publishToMavenLocal
```

This will output an `aar` file to `$HOME/.m2/repository/` which can be
compiled into an app by adding the `mavenLocal()` repository as the
first repository:

```groovy
allprojects {
    repositories {
        mavenLocal()
        // All other repositories ...
    }
}
```

Then, append `'@aar'` to the dependency:

```groovy
implementation 'com.cloudfiveapp:push-android:x.y.z@aar'
```

### Ant/Other builds

You can download the AAR file from 
[the bintray project page](https://bintray.com/cloudfive/maven/push-android/)
