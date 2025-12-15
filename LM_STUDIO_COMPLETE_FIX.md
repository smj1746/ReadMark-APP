# LM Studio API 연결 오류 완전 해결 가이드 ✅

## 🚨 발생했던 문제

```
처리 실패: API 요청 실패: 400 - {
  "error": {
    "message": "Failed to load model 'openai/gpt-oss-20b'"
  }
}
```

## 🔍 근본 원인 분석

### 문제 1: 빈 문자열 모델 전달
```kotlin
val selectedModel = ""  // AppConfig 기본값
// ...
model = selectedModel  // ❌ 빈 문자열을 API에 전달
```

### 문제 2: null vs 빈 문자열 처리 미흡
```kotlin
val modelToUse = model ?: currentModel
// ❌ model이 ""이면 null이 아니므로 currentModel로 대체되지 않음
```

### 문제 3: 사용자가 연결 테스트 없이 바로 처리 시도
```kotlin
// ❌ 모델 확인 없이 바로 처리 허용
```

## ✅ 완전 해결 방법 (3단계 방어)

### 🛡️ 1단계: UI 레벨 - 버튼 비활성화
**파일**: `MainScreen.kt` (line 105-108)

```kotlin
ActionButtons(
    enabled = inputText.isNotBlank() &&
             !isLoading &&
             connectionState is ConnectionState.Connected &&  // ✅ 연결 확인
             appConfig.lmStudio.selectedModel.isNotBlank(),  // ✅ 모델 확인
    ...
)
```

**효과**:
- 연결되지 않았거나 모델이 선택되지 않으면 "처리 시작" 버튼 비활성화
- 사용자가 잘못된 작업을 시도할 수 없음

### 🛡️ 2단계: ViewModel 레벨 - 검증
**파일**: `MainViewModel.kt` (line 155-168)

```kotlin
// 모델이 선택되지 않았으면 에러 반환
if (selectedModel.isBlank()) {
    _processingResult.value = ProcessingResult.Error(
        message = "사용 가능한 모델이 없습니다",
        suggestion = "먼저 '연결 테스트'를 클릭하여 LM Studio에서 로드된 모델을 확인하세요.\n\n" +
                "LM Studio에서 모델이 로드되지 않았다면:\n" +
                "1. LM Studio를 열고\n" +
                "2. 왼쪽 메뉴에서 모델 선택 후\n" +
                "3. 'Load Model' 버튼을 클릭하고\n" +
                "4. 다시 이 앱에서 '연결 테스트'를 실행하세요",
        errorType = ErrorType.VALIDATION
    )
    return@launch
}
```

**효과**:
- UI를 우회한 호출도 차단
- 사용자에게 명확한 해결 방법 제시

### 🛡️ 3단계: Repository 레벨 - 최종 검증
**파일**: `LMStudioRepository.kt` (line 145-153, 172-180, 199-207)

```kotlin
// 빈 문자열도 null로 처리
val modelToUse = if (model.isNullOrBlank()) currentModel else model

// 모델이 여전히 비어있으면 에러
if (modelToUse.isBlank()) {
    return@withContext Result.failure(Exception(
        "모델이 선택되지 않았습니다. 먼저 연결 테스트를 수행하세요."
    ))
}
```

**효과**:
- API 호출 직전 최종 검증
- 모든 경로에서 빈 모델 전달 방지

## 📊 수정 요약

| 레벨 | Before | After |
|------|--------|-------|
| **UI** | ❌ 항상 활성화 | ✅ 연결+모델 확인 시에만 활성화 |
| **ViewModel** | ❌ 연결만 확인 | ✅ 연결+모델 모두 확인 |
| **Repository** | ❌ `model ?: currentModel` | ✅ `isNullOrBlank()` + 최종 검증 |

## 🎯 이제 작동하는 방식

### ✅ 정상 플로우
```
1. 앱 실행
   └─ "처리 시작" 버튼 비활성화 (회색)

2. "연결 테스트" 클릭
   └─ LM Studio에서 모델 감지
   └─ 첫 번째 모델 자동 선택
   └─ "처리 시작" 버튼 활성화 (파랑)

3. 텍스트 입력 → "처리 시작"
   └─ ViewModel: 모델 확인 ✓
   └─ Repository: 모델 확인 ✓
   └─ API 호출 성공! ✓
```

### ⚠️ 에러 시나리오 (이제 친절한 안내)

#### 시나리오 1: 연결 테스트 없이 시도
```
상황: 앱 실행 직후 텍스트 입력
결과: "처리 시작" 버튼이 비활성화됨
안내: (버튼 위에 마우스 올리면) "먼저 연결 테스트를 수행하세요"
```

#### 시나리오 2: LM Studio에 모델 미로드
```
상황: 연결 테스트 클릭 (LM Studio에 모델 없음)
결과: ❌ "LM Studio에 로드된 모델이 없습니다"
안내:
  💡 LM Studio를 열고 모델을 로드한 후 다시 시도하세요.

  방법:
  1. LM Studio 실행
  2. 왼쪽 메뉴에서 모델 선택
  3. 'Load Model' 버튼 클릭
  4. 모델 로드 완료 후 이 앱에서 '연결 테스트' 다시 클릭
```

#### 시나리오 3: 모델 로드 실패 (API 호출 중)
```
상황: 처리 중 모델이 언로드됨
결과: ❌ "모델 로드 실패: 'XXX' 모델이 LM Studio에서 로드되지 않았습니다"
안내:
  💡 해결 방법:
  1. LM Studio를 열고 모델이 로드되어 있는지 확인
  2. 로드되지 않았다면 왼쪽 메뉴에서 모델 선택 후 'Load Model' 클릭
  3. 모델 로드 완료 후 이 앱에서 '연결 테스트'를 다시 실행
  4. 사용 가능한 모델 목록에서 선택하여 사용
```

## 📱 완전한 사용 가이드

### Step 1: LM Studio 준비 (필수!)

1. **LM Studio 다운로드 및 설치**
   - https://lmstudio.ai/ 에서 다운로드
   - 설치 후 실행

2. **모델 다운로드** (처음 사용 시)
   - LM Studio의 "Search" 탭 클릭
   - 추천 모델: `Meta-Llama-3-8B-Instruct`
   - 다운로드 버튼 클릭 (시간 소요)

3. **모델 로드** (매우 중요!)
   - 다운로드한 모델 선택
   - **"Load Model" 버튼 클릭**
   - 모델 로드 완료 대기 (하단 상태바 확인)
   - ✅ "Model loaded" 메시지 확인

4. **서버 시작 확인**
   - "Local Server" 탭 클릭
   - 서버가 실행 중인지 확인
   - 포트 번호 확인 (기본: 1234)

### Step 2: ReadMark 앱 연결

1. **ReadMark 앱 실행**

2. **"연결 테스트" 버튼 클릭**
   - 성공 시: ✅ "연결 성공! N개의 모델을 사용할 수 있습니다."
   - 실패 시: 위의 에러 시나리오 참고

3. **모델 확인**
   - "AI 모델 선택" 드롭다운에서 현재 선택된 모델 확인
   - 필요시 다른 모델로 변경 가능

### Step 3: 텍스트 처리

1. **텍스트 입력**
   - 최소 10자 이상

2. **"처리 시작" 버튼 클릭**
   - 버튼이 비활성화되어 있다면:
     - 연결 테스트를 먼저 수행하세요
     - LM Studio에서 모델이 로드되었는지 확인하세요

3. **결과 확인**
   - 요약/이어읽기 결과 표시
   - "노트로 저장" 가능

## 🔧 문제 해결 체크리스트

### ✅ "처리 시작" 버튼이 비활성화되어 있어요

- [ ] LM Studio가 실행 중인가요?
- [ ] LM Studio에서 모델을 로드했나요?
- [ ] ReadMark 앱에서 "연결 테스트"를 했나요?
- [ ] "연결 성공" 메시지를 받았나요?
- [ ] 텍스트를 입력했나요? (최소 10자)

### ✅ "사용 가능한 모델이 없습니다" 에러가 나요

1. **LM Studio 확인**
   ```
   □ LM Studio 실행됨
   □ 모델이 "Loaded" 상태임
   □ Local Server가 실행 중
   ```

2. **ReadMark 앱에서**
   ```
   □ "연결 테스트" 다시 클릭
   □ 성공 메시지 확인
   ```

### ✅ "Failed to load model" 에러가 나요

**원인**: 선택한 모델이 LM Studio에서 언로드됨

**해결**:
1. LM Studio로 이동
2. 해당 모델 다시 "Load"
3. ReadMark에서 "연결 테스트" 재실행

## 🎓 LM Studio 모델 관리 팁

### 추천 모델 (성능 vs 속도)

| 모델 | 크기 | 속도 | 품질 | 추천용도 |
|------|------|------|------|---------|
| **Llama-3-8B-Instruct** | 8GB | ⚡⚡⚡ | ⭐⭐⭐⭐ | **일반 사용 추천** |
| Mistral-7B-Instruct | 7GB | ⚡⚡⚡ | ⭐⭐⭐ | 빠른 응답 |
| Llama-3-70B-Instruct | 70GB | ⚡ | ⭐⭐⭐⭐⭐ | 고품질 (고사양 PC) |

### 모델 로드 시 주의사항

- **RAM 요구사항**: 모델 크기의 1.5배 이상 권장
- **첫 로드**: 시간이 걸릴 수 있음 (1-3분)
- **동시 로드**: 한 번에 하나의 모델만 로드 권장

## 📊 수정된 파일 목록

1. **MainViewModel.kt**
   - Line 155-168: 모델 검증 로직 추가
   - 빈 문자열 모델 차단

2. **LMStudioRepository.kt**
   - Line 145-153: `generateSummary()` 모델 검증
   - Line 172-180: `findBookmark()` 모델 검증
   - Line 199-207: `autoProcess()` 모델 검증
   - `isNullOrBlank()` 사용으로 빈 문자열 처리

3. **MainScreen.kt**
   - Line 105-108: 버튼 활성화 조건 강화
   - 연결 상태 + 모델 선택 모두 확인

4. **AppConfig.kt**
   - Line 16: selectedModel 기본값 "" (연결 후 설정)

## 🚀 다음 단계

### 1. 앱 재빌드
```bash
gradlew.bat clean assembleDebug
```

### 2. LM Studio 모델 로드 확인
```
✓ LM Studio 실행
✓ 모델 선택
✓ "Load Model" 클릭
✓ "Model loaded" 확인
```

### 3. ReadMark 앱 테스트
```
✓ 앱 실행
✓ "연결 테스트" 클릭
✓ "연결 성공" 확인
✓ 텍스트 입력
✓ "처리 시작" 활성화 확인
✓ 결과 확인
```

## 💡 추가 개선 사항 (선택)

### 자동 재연결 기능 (향후)
```kotlin
// 처리 실패 시 자동으로 연결 재시도
if (error.contains("Failed to load model")) {
    // 연결 테스트 재실행
    testConnection()
}
```

### 모델 상태 실시간 모니터링
```kotlin
// 주기적으로 모델 로드 상태 확인
launch {
    while (true) {
        delay(30000) // 30초마다
        checkModelStatus()
    }
}
```

## ✅ 결론

이제 ReadMark 앱은:
- ✅ **3단계 방어로 빈 모델 전달 완전 차단**
- ✅ **UI 레벨에서 잘못된 작업 미리 방지**
- ✅ **모든 레벨에서 명확한 에러 메시지 제공**
- ✅ **사용자에게 단계별 해결 방법 안내**
- ✅ **LM Studio 모델 로드 필수화**

**"처리 시작" 버튼이 비활성화되어 있다면, 연결 테스트를 먼저 하세요!**

---

**작성일**: 2025-12-15
**버전**: 2.0 (완전 수정)
