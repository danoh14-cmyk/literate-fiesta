# FIT TIMER v5.1 Android 소스코드

## 내용
- `FitTimer/`: Android Studio / Gradle 프로젝트 (Java WebView 앱)
- `FitTimer/app/src/main/assets/index.html`: 확정된 FIT TIMER v5.1 웹 미리보기 전체 코드 (CSS + JavaScript 포함)
- `.github/workflows/build-apk.yml`: GitHub Actions APK 빌드 설정

## GitHub에 적용하기
1. ZIP 압축을 풉니다.
2. 기존 GitHub 저장소 루트에 `FitTimer` 폴더와 `.github` 폴더를 **폴더 구조 그대로** 업로드합니다.
3. 변경된 파일을 확인한 뒤 **Commit changes**를 클릭합니다.
4. GitHub의 **Actions → Build APK** 워크플로 실행 후 생성된 `app-debug.apk` 아티팩트를 내려받습니다.

## 직접 빌드
Android Studio에서 `FitTimer` 폴더를 열고 Gradle Sync 후 `assembleDebug`를 실행합니다.

## 중요 사항
- 기존 네이티브 Java UI를 v5.1 웹 UI로 교체한 **WebView 하이브리드 앱 소스**입니다.
- 운동 설정, 루틴, 일정, 기록 등은 WebView의 로컬 저장소에 저장됩니다. 앱 데이터 삭제나 재설치 시 사라질 수 있습니다.
- 사진 가이드 일부는 Wikimedia Commons 온라인 리소스를 사용하므로 인터넷이 필요합니다.
- AI 추천은 서버 AI가 아닌 규칙 기반 시제품입니다.
- 타이머는 화면이 켜진 상태의 JavaScript 타이머입니다. **백그라운드/화면 꺼짐 상태에서 정확한 알림·진행을 보장하지 않습니다.**
- 빌드 환경에서 APK 실제 생성과 단말 실행 검증은 수행하지 않았습니다.
