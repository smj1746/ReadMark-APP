# 한글 입력 문제 - 환경설정 해결 가이드

코드 수정 없이 환경설정만으로 한글 입력 문제를 해결하는 방법들입니다.

## 1️⃣ AndroidManifest.xml 설정 (현재 적용됨 ✅)

### 현재 상태
```xml
<activity
    android:name=".MainActivity"
    android:windowSoftInputMode="adjustResize">
```

### 다른 옵션들 시도해보기

#### Option 1: adjustPan
```xml
<activity
    android:name=".MainActivity"
    android:windowSoftInputMode="adjustPan">
```
- 키보드가 나타날 때 화면을 이동시켜 입력 필드를 보이게 함
- 일부 IME에서 한글 입력이 더 잘 동작할 수 있음

#### Option 2: stateVisible
```xml
<activity
    android:name=".MainActivity"
    android:windowSoftInputMode="adjustResize|stateVisible">
```
- 키보드를 자동으로 표시
- IME 상태 전환이 더 원활할 수 있음

#### Option 3: adjustNothing (Android 11+)
```xml
<activity
    android:name=".MainActivity"
    android:windowSoftInputMode="adjustNothing">
```
- 앱이 직접 레이아웃 조정
- IME와의 충돌 최소화

## 2️⃣ 기기/에뮬레이터 설정

### A. 키보드 설정 변경

#### 에뮬레이터에서:
1. **Settings** (설정) 열기
2. **System** → **Languages & input** → **Virtual keyboard**
3. **Gboard** 선택
4. **Text correction** (텍스트 수정)에서:
   - Auto-correction: OFF
   - Show suggestion strip: OFF
   - Next-word suggestions: OFF

#### 실제 기기에서:
1. **설정** → **일반 관리** → **언어 및 입력**
2. **화상 키보드** → 사용 중인 키보드 선택
3. **자동 완성 기능 끄기**:
   - 자동 대문자
   - 자동 띄어쓰기
   - 자동 완성

### B. 다른 키보드 앱 사용

더 나은 호환성을 위해 다른 키보드를 시도해보세요:

1. **Google 한글 키보드**
   - Play Store에서 "Google Korean Keyboard" 검색
   - 설치 후 기본 키보드로 설정

2. **삼성 키보드**
   - 삼성 기기에서는 기본 탑재
   - 한글 입력 최적화가 잘 되어 있음

3. **Swiftkey**
   - Microsoft에서 제공
   - 다국어 입력 지원이 우수

### C. 개발자 옵션 설정

1. **개발자 옵션 활성화**:
   - 설정 → 휴대전화 정보 → 빌드 번호 7번 탭

2. **개발자 옵션** → **입력** 섹션에서:
   - "Show taps" (터치 표시): OFF
   - "Pointer location" (포인터 위치): OFF
   - "Show surface updates" (화면 업데이트 표시): OFF

## 3️⃣ Compose 런타임 설정

### build.gradle (app 수준) 추가

```gradle
android {
    buildFeatures {
        compose true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    // 한글 입력 최적화를 위한 추가 설정
    packagingOptions {
        resources {
            excludes += '/META-INF/{AL2.0,LGPL2.1}'
        }
    }
}

dependencies {
    // Compose Foundation 최신 버전 사용
    implementation "androidx.compose.foundation:foundation:1.5.4"
    implementation "androidx.compose.ui:ui-text:1.5.4"
}
```

## 4️⃣ 에뮬레이터 설정 (AVD Manager)

### 물리적 키보드 비활성화

1. **AVD Manager** 열기
2. 에뮬레이터 **Edit** (편집)
3. **Show Advanced Settings** 클릭
4. **Keyboard** 섹션:
   - ✅ **Enable keyboard input** 체크 해제

이렇게 하면 화면 키보드(소프트 키보드)만 사용하게 되어 한글 입력이 더 안정적일 수 있습니다.

### 에뮬레이터 성능 설정

1. **AVD Manager** → 에뮬레이터 편집
2. **Emulated Performance**:
   - Graphics: **Hardware - GLES 2.0** 또는 **Automatic**
   - Boot option: **Cold boot**

## 5️⃣ Android Studio 설정

### A. 에디터 설정
1. **File** → **Settings** (Ctrl+Alt+S)
2. **Editor** → **General**
3. **On Save** 섹션:
   - "Remove trailing spaces on:" → **Modified Lines**

### B. Compose Preview 설정
1. **File** → **Settings**
2. **Experimental** → **Compose**
3. ✅ **Enable Live Edit of literals** 활성화

## 6️⃣ gradle.properties 설정

프로젝트의 `gradle.properties` 파일에 추가:

```properties
# Compose 최적화
android.enableJetifier=true
android.useAndroidX=true

# 빌드 최적화
org.gradle.jvmargs=-Xmx2048m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true

# Kotlin 컴파일 최적화
kotlin.incremental=true
kotlin.code.style=official
```

## 7️⃣ 테스트 방법

각 설정을 변경한 후 다음을 테스트해보세요:

### 기본 테스트
1. 앱 재실행
2. 텍스트 필드 클릭
3. "안녕하세요" 입력
4. 문제가 계속되면 다음 설정으로 이동

### 고급 테스트
```kotlin
// 개발 중 로그 확인
Log.d("IME", "Composition: ${textFieldValue.composition}")
Log.d("IME", "Selection: ${textFieldValue.selection}")
```

## 8️⃣ 권장 순서

문제 해결을 위해 다음 순서로 시도하세요:

1. ✅ **키보드 자동 완성 끄기** (가장 쉬움)
2. ✅ **에뮬레이터 물리적 키보드 비활성화**
3. ⚙️ **AndroidManifest의 windowSoftInputMode 변경**
4. 📱 **다른 키보드 앱 사용**
5. 🔧 **Compose 라이브러리 버전 업데이트**

## 9️⃣ 현재 AndroidManifest.xml 상태

```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:label="@string/app_name"
    android:theme="@style/Theme.ReadMark"
    android:windowSoftInputMode="adjustResize">  <!-- 이 부분 수정 가능 -->
```

### 변경 방법
1. `app/src/main/AndroidManifest.xml` 파일 열기
2. line 26의 `android:windowSoftInputMode` 값을 변경
3. 앱 재빌드 및 실행

## 🔟 추가 팁

### IME 디버깅
```bash
# 현재 IME 확인
adb shell ime list -s

# IME 설정 보기
adb shell settings get secure default_input_method

# Gboard 강제 설정
adb shell settings put secure default_input_method com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME
```

### 로그 확인
```bash
# IME 관련 로그 필터링
adb logcat | grep -i "IME\|InputMethod\|TextField"
```

---

**참고**: 대부분의 한글 입력 문제는 **키보드 자동 완성 기능**과 **에뮬레이터 물리적 키보드** 설정으로 해결됩니다.

**작성일**: 2025-12-14
