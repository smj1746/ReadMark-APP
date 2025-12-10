# 껐다 재시작

모든 에뮬레이터 종료
adb kill-server

%LOCALAPPDATA%\Android\Sdk\emulator\emulator.exe -avd Pixel_8

에뮬레이터 확인: adb devices - Pixel 8 에뮬레이터 준비됨

앱 설치: adb install -r app-deb

앱 실행: adb shell am start -n com.example.myapplication/.MainActivity