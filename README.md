# MP4 Looper

A simple MP4 video looper, available as a web page, an Android app and a Windows app.

## Download

Get the latest version from the [Releases page](https://github.com/yeye-Coder/mp4-looper/releases):

- **Android:** download `MP4Looper.apk`, open it on your phone and allow "install unknown apps" when asked. Requires Android 10 or later.
- **Windows:** download `MP4Looper.exe` and run it. Windows may show a SmartScreen warning because the app isn't code-signed; click **More info → Run anyway**.
- **Browser:** download `mp4-looper.html` and open it in any modern browser.

## Building from source

- **Android:** in `android/`, run `gradlew assembleRelease` (APK) or `gradlew bundleRelease` (AAB). Release signing needs your own `release.jks` and `keystore.properties`, which aren't included in this repository.
- **Windows:** run `windows\build.bat`. It uses the .NET Framework C# compiler that comes with Windows and creates `MP4Looper.exe` in the project folder.
