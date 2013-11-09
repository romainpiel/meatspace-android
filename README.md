# Meatspace-android

An android client for the great chatting space [https://chat.meatspac.es](https://chat.meatspac.es).

## Setting up

Check out the sources and submodules:
```
> git submodule update
```

### Run on your machine:

To run the server on your machine, follow the instructions described [there](https://github.com/meatspaces/meatspace-chat).

In the android project, edit the variable DEBUG_BASE_URL Meatspace/gradle.build:

```
def DEBUG_BASE_URL = "http://192.168.1.90:3000" // set your computer IP there
```

To start the android app in debug mode, run that from the android project folder:

```
> ./gradlew installDebug
```

### Using production server:

```
> ./gradlew installRelease
```

## Libraries used

Thanks to the creators/contributors of all of these open-source libraries:
- [android-websockets](https://github.com/koush/android-websockets)
- [butterknife](https://github.com/JakeWharton/butterknife)
- [google-gson](https://code.google.com/p/google-gson)
- [retrofit](https://github.com/square/retrofit)
- [otto](https://github.com/square/otto)