# ReadMark 프로젝트 구조

```
app/
├── src/main/
│   ├── java/com/readmark/
│   │   ├── MainActivity.kt                 # 메인 단일 화면 액티비티
│   │   ├── data/
│   │   │   ├── model/
│   │   │   │   ├── OcrResult.kt           # OCR 결과 데이터 모델
│   │   │   │   ├── BookmarkResult.kt      # 책갈피 감지 결과
│   │   │   │   ├── SessionRecord.kt       # 세션 기록 모델
│   │   │   │   └── AppConfig.kt           # 앱 설정 모델
│   │   │   ├── repository/
│   │   │   │   ├── LMStudioRepository.kt  # LM Studio API 통신
│   │   │   │   ├── DataManager.kt         # JSON 파일 관리
│   │   │   │   └── CacheManager.kt        # 캐시 파일 관리
│   │   │   └── api/
│   │   │       ├── LMStudioService.kt     # Retrofit API 인터페이스
│   │   │       └── ApiResponse.kt         # API 응답 모델
│   │   ├── ui/
│   │   │   ├── compose/
│   │   │   │   ├── MainScreen.kt          # 메인 화면 Compose UI
│   │   │   │   ├── ConnectionTestCard.kt  # 연결 테스트 카드
│   │   │   │   ├── InputSection.kt        # 입력 섹션
│   │   │   │   ├── ResultSection.kt       # 결과 표시 섹션
│   │   │   │   └── SettingsDialog.kt      # 설정 다이얼로그
│   │   │   └── viewmodel/
│   │   │       └── MainViewModel.kt       # 메인 뷰모델
│   │   ├── utils/
│   │   │   ├── OcrProcessor.kt           # OCR 처리 유틸 (2단계)
│   │   │   ├── BookmarkDetector.kt       # 책갈피 감지 (3단계)
│   │   │   ├── ImagePreprocessor.kt      # 이미지 전처리
│   │   │   ├── FileUtils.kt              # 파일 처리 유틸
│   │   │   └── Constants.kt              # 상수 정의
│   │   └── di/
│   │       └── AppModule.kt              # 의존성 주입 (Hilt)
│   ├── res/
│   │   ├── layout/
│   │   │   └── activity_main.xml         # 메인 액티비티 레이아웃
│   │   ├── values/
│   │   │   ├── strings.xml              # 문자열 리소스
│   │   │   ├── colors.xml               # 색상 정의
│   │   │   └── themes.xml               # 테마 정의
│   │   └── xml/
│   │       └── network_security_config.xml  # 네트워크 보안 설정
│   └── AndroidManifest.xml
├── build.gradle.kts
└── proguard-rules.pro
```

## 데이터 저장 구조

### Internal Storage 위치
```
/Android/data/com.readmark/files/
├── config.json                 # 앱 설정
├── history.json                # 사용 기록
├── cache/
│   └── ocr/                    # OCR 캐시
│       ├── 20241112-143022.json
│       └── ...
└── notes/                      # 생성된 노트
    ├── summary_20241112-143022.md
    └── ...
```

### JSON 스키마
```json
// config.json
{
  "lmStudio": {
    "endpoint": "http://192.168.1.100:1234",
    "apiKey": "lm-studio",
    "lastWorkingModel": "model-name",
    "temperature": 0.7,
    "maxTokens": 1000
  },
  "app": {
    "version": "1.0.0",
    "lastUsed": "2024-11-12T14:30:22Z"
  }
}

// history.json
{
  "sessions": [
    {
      "sessionId": "session_1699872622",
      "mode": "summary",
      "timestamp": "2024-11-12T14:30:22Z",
      "inputLength": 500,
      "title": "extracted title",
      "summary": "generated summary",
      "tokensUsed": 150
    }
  ],
  "statistics": {
    "totalSessions": 10,
    "pagesProcessed": 15,
    "summariesCreated": 8
  }
}
```
