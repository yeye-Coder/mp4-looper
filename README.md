# MP4 Looper

Loop a sentence (an A–B section) of a video or song over and over – for language practice and more.
Available as a web page, an Android app and a Windows app.

## Features

- Set A and B, choose the gap between repeats, the number of repeats and the speed.
- Repeats start instantly, also on phones.
- Save the A–B section as a new file: MP4 / MOV / M4A and MP3 are cut directly from the original.
  The saved clip starts exactly at A.
- Videos with few key frames (common for downloaded TikTok videos) jump slowly on phones.
  The app offers to convert such a video once into a copy that jumps instantly (“… (loop).mp4”).
- Saved sentences: keep a list of A–B sections per file.

## Download

Get the latest version from the [Releases page](https://github.com/yeye-Coder/mp4-looper/releases):

- **Android:** download `MP4Looper.apk`, open it on your phone and allow "install unknown apps" when asked. Requires Android 10 or later.
- **Windows:** download `MP4Looper.exe` and run it. Windows may show a SmartScreen warning because the app isn't code-signed; click **More info → Run anyway**.
- **Browser:** download `mp4-looper.html` and open it in Chrome or Edge.

## Building from source

- **Android:** in `android/`, run `gradlew assembleRelease` (APK) or `gradlew bundleRelease` (AAB). Needs the Android SDK with platform 37 (Android 17). Release signing needs your own `release.jks` and `keystore.properties`, which aren't included in this repository.
- **Windows:** run `windows\build.bat`. It uses the .NET Framework C# compiler that comes with Windows and creates `MP4Looper.exe` in the project folder.
