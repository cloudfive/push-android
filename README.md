# Cloud Five Push for Android

## Quick Start

*These instructions will change rapidly as we move toward hosting on Maven.

Inside your main project directory, simply clone the repo:

    git clone https://github.com/cloudfive/push-android.git cloudfivepush

Then add the library to your `build.gradle` file:

    dependencies {
        compile project(":cloudfivepush")
    }


and add the project to your `settings.gradle` file as well:

    include ':app', ':cloudfivepush'


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
