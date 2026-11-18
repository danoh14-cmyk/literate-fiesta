# FIT TIMER (Android Java)

## What's new
- WorkoutService runs as an Android foreground service, with persistent notification and a partial wake lock while exercising.
- Uses `SystemClock.elapsedRealtime()` to calculate intervals while the screen is off.
- Automatically advances workout/rest segments and speaks Korean exercise names / rest transitions with Android TextToSpeech (when a Korean TTS voice is installed).
- Notification pause/resume button and UI state broadcasts.

## Build
Requires Android Studio with Android SDK 35 and Android Gradle Plugin 8.7.3, JDK 17.
Open this folder in Android Studio, sync Gradle, then Build > Build APK(s).
Debug output is normally `app/build/outputs/apk/debug/app-debug.apk`.

## Important limitations
This package contains source code, not a compiled APK. No Android SDK, Gradle executable, emulator, or physical Android device was available in the authoring environment, so APK compilation, installation and device runtime verification have **not** been performed.

The foreground service uses Android `specialUse` for a user-initiated workout timer. Google Play policy review may require further justification or a different approach. Voice playback depends on Android TTS and device sound settings. A user must start the workout while the app is visible. OEM battery management and power-saving behavior should be tested on actual Samsung devices. The service is not configured to restore a workout after a process kill/reboot. The app's activity currently initializes a fresh workout when recreated, so test rotation and activity recreation before release.

## Manual device test checklist
1. Start plank 30s -> rest 15s -> squat 45s and confirm order.
2. Turn off display during plank, wait 40 seconds, wake and check rest phase.
3. Check voice cue at transitions with Korean TTS installed.
4. Use notification pause/resume while the screen is locked.
5. Test incoming calls, power-saving mode, screen rotation, app task swipe, and battery optimization.
6. Verify notification permission on Android 13+.
