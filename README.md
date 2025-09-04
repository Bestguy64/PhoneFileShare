# PhoneFileShare - minimal local Android app

Build locally:
1) Install Gradle CLI (see instructions below) and JDK 17+
2) From project root run: gradle wrapper
3) Then run: .\gradlew.bat assembleDebug
4) Install debug APK: adb install -r app\build\outputs\apk\debug\app-debug.apk
