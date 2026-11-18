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

## v1.1 변경 사항
- 화면 회전 시 Activity를 재구성하더라도 실행 중인 WorkoutService의 타이머를 초기화하지 않습니다.
- 이름을 입력해 여러 운동 루틴을 저장하고, 목록에서 불러오거나 삭제할 수 있습니다.
- 완료된 운동 세션을 날짜별로 집계하는 운동 리포트를 제공합니다. 운동 시간은 운동/휴식 구간을 모두 포함한 타이머 실행 시간(일시정지 제외)입니다.
- 기록은 앱의 로컬 SharedPreferences에 저장됩니다. 앱 데이터 삭제 시 기록도 삭제됩니다.

## GitHub 업로드 주의
기존 저장소에서 `FitTimer/` 폴더 내부의 변경 파일을 덮어쓰세요. `.github/workflows/build-apk.yml`은 저장소 루트에 위치해야 합니다.

## 검증 범위
코드 수정 및 압축 파일 생성만 수행했습니다. Android SDK가 없어 실제 APK 빌드와 실기기 실행 검증은 진행하지 못했습니다.
