# 📘 READMARK

> **AI 기반 스마트 책갈피 애플리케이션**
> 읽던 위치를 기억하고, 다시 이어주는 온·오프라인 독서 보조 AI

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-purple.svg)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API%2024+-green.svg)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5.0-blue.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

<div align="center">
  <img src="docs/images/app_logo.png" alt="ReadMark Logo" width="200"/>
</div>

---

## 📖 프로젝트 소개

**READMARK**는 독서 중 발생하는 책갈피 관리의 불편함을 해결하는 **AI 기반 스마트 독서 보조 애플리케이션**입니다.

### 🎯 핵심 기능

- 🤖 **AI 텍스트 분석**: 로컬 LLM을 활용한 텍스트 요약 및 분석
- 📚 **히스토리 관리**: 과거 독서 기록 자동 저장 및 조회
- 💾 **노트 저장**: 분석 결과를 마크다운 파일로 저장
- 🔒 **프라이버시**: 로컬 실행으로 개인정보 보호
- 📱 **모바일 최적화**: 언제 어디서나 사용 가능

### ✨ 특징

```
📸 텍스트 입력 → 🤖 AI 분석 → 📝 자동 요약 → 💾 저장
```

- **로컬 LLM 사용**: LM Studio + phi-3-mini-4k-instruct
- **MVVM 아키텍처**: 체계적인 코드 구조
- **Jetpack Compose**: 모던 Android UI
- **완전 무료**: API 비용 없음

---

## 🚀 빠른 시작

### 사전 요구사항

- Android Studio Hedgehog (2023.1.1) 이상
- JDK 17 이상
- LM Studio 설치
- 최소 16GB RAM 권장

### 설치 방법

```bash
# 1. 저장소 클론
git clone https://github.com/smj1746/ReadMark-APP.git
cd ReadMark-APP

# 2. Android Studio로 프로젝트 열기
# File > Open > ReadMark-APP 선택

# 3. LM Studio 설정
# - LM Studio 실행
# - phi-3-mini-4k-instruct 모델 다운로드
# - Start Server 클릭

# 4. 앱 실행
# Shift + F10 (Android Studio)
```

### 초기 설정

1. **LM Studio 연결**
   - 앱 설정(⚙️) 클릭
   - Endpoint: `http://10.0.2.2:1234` (에뮬레이터)
   - 연결 테스트 클릭

2. **모델 선택**
   - 연결 성공 시 드롭다운에서 모델 선택
   - `phi-3-mini-4k-instruct` 권장

3. **사용 시작**
   - 텍스트 입력 → 작업 모드 선택 → 처리 시작

---

## 💻 기술 스택

### Frontend
- **Kotlin** 1.9.0
- **Jetpack Compose** 1.5.0
- **Material Design 3** 1.2.0
- **Hilt** 2.48 (의존성 주입)
- **Coroutines** 1.7.3 (비동기 처리)

### Backend & AI
- **LM Studio** (로컬 LLM 서버)
- **phi-3-mini-4k-instruct** (3.8B 파라미터)
- **OkHttp** 4.12.0 (HTTP 클라이언트)
- **Gson** 2.10.1 (JSON 파싱)

### 아키텍처
- MVVM 패턴
- Clean Architecture
- Repository Pattern
- StateFlow 기반 상태 관리

---

## 📱 주요 화면

<div align="center">

### 1. 메인 화면
<img src="docs/images/화면_1.png" alt="메인 화면" width="250"/>

**통계 대시보드 및 텍스트 입력**
- 📊 세션/페이지/요약 통계
- 🔌 LM Studio 연결 상태
- ✍️ 텍스트 입력 필드
- 🎯 작업 모드 선택 (자동 감지/요약/이어읽기)

---

### 2. 설정 화면
<img src="docs/images/화면2.png" alt="설정 화면" width="250"/>

**LM Studio 및 노트 저장 설정**
- 🔧 Endpoint 및 API Key 설정
- 🌡️ Temperature 조절 슬라이더
- 🎲 Max Tokens 설정
- 📁 외부 저장소 경로 지정

---

### 3. 모델 선택
<img src="docs/images/화면3.png" alt="모델 선택" width="250"/>

**연결 성공 및 모델 리스트**
- ✅ 연결 성공 메시지
- 🤖 사용 가능한 모델 표시
  - phi-3-mini-4k-instruct
  - text-embedding-nomic-embed-text-v1.5

---

### 4. AI 분석 결과
<img src="docs/images/화면4.png" alt="분석 결과" width="250"/>

**텍스트 요약 결과 표시**
- 📝 입력한 텍스트 분석
- ✨ AI가 생성한 요약문
- 💾 "노트로 저장" 버튼

---

### 5. 노트 저장
<img src="docs/images/화면7.png" alt="노트 저장" width="250"/>

**파일 저장 다이얼로그**
- 📄 파일 이름 입력
- 💾 .md 파일로 저장
- ✅ 저장 완료 메시지 (경로 표시)

---

### 6. 히스토리 화면
<img src="docs/images/화면8.png" alt="히스토리" width="250"/>

**과거 분석 내역 조회**
- 📚 총 1개의 기록 표시
- 📖 입력 및 결과 내역
- ⚡ 토큰 사용량: 385
- 🤖 사용 모델: phi-3-mini-4k-instruct
- 🗑️ 개별/전체 삭제 기능

</div>

---

## 🏗 프로젝트 구조

```
ReadMark/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/readmark/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── ReadMarkApplication.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/          # 데이터 모델
│   │   │   │   │   │   ├── AppConfig.kt
│   │   │   │   │   │   ├── HistoryItem.kt
│   │   │   │   │   │   ├── ProcessingModels.kt
│   │   │   │   │   │   └── WorkMode.kt
│   │   │   │   │   └── repository/     # 데이터 소스
│   │   │   │   │       ├── DataManager.kt
│   │   │   │   │       └── LMStudioRepository.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screen/         # 화면 컴포넌트
│   │   │   │   │   │   ├── MainScreen.kt
│   │   │   │   │   │   ├── SettingsScreen.kt
│   │   │   │   │   │   └── HistoryScreen.kt
│   │   │   │   │   ├── viewmodel/      # ViewModel
│   │   │   │   │   │   └── MainViewModel.kt
│   │   │   │   │   ├── theme/          # 테마
│   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   └── Type.kt
│   │   │   │   │   └── components/     # 재사용 컴포넌트
│   │   │   │   └── di/                 # 의존성 주입
│   │   │   │       └── AppModule.kt
│   │   │   │   └── utils/              # 유틸리티
│   │   │   │       └── KeyboardShortcuts.kt
│   │   │   └── res/                    # 리소스
│   │   └── test/                       # 테스트
│   └── build.gradle.kts
├── docs/                               # 문서
│   ├── images/
│   ├── PROJECT_PROPOSAL.md
│   └── API_DOCS.md
├── gradle/
├── .gitignore
├── README.md
└── LICENSE
```

---

## 🔧 개발 가이드

### 브랜치 전략

```
main          - 배포용 안정 버전
├── develop   - 개발 통합 브랜치
    ├── feature/xxx  - 새로운 기능
    ├── bugfix/xxx   - 버그 수정
    └── hotfix/xxx   - 긴급 수정
```

### 커밋 컨벤션

```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 포맷팅, 세미콜론 누락 등
refactor: 코드 리팩토링
test: 테스트 코드
chore: 빌드 업무, 패키지 매니저 설정 등
```

### 코드 스타일

- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) 준수
- ktlint 사용
- 들여쓰기: 4 spaces
- 최대 줄 길이: 120자

---

## 📊 로드맵

### ✅ 완료된 기능 (v0.1.0)

- [x] LM Studio 연동
- [x] AI 텍스트 분석 및 요약
- [x] 모델 선택 기능
- [x] 히스토리 관리
- [x] 노트 저장
- [x] 한글 입력 최적화

### 🚧 개발 중 (v0.2.0)

- [ ] OCR 기능 구현
- [ ] 저자/출판정보 강조
- [ ] 다크모드 완전 지원
- [ ] 성능 최적화

### 📅 계획 중 (v0.3.0+)

- [ ] 책별 관리 시스템
- [ ] 클라우드 동기화
- [ ] 소셜 기능
- [ ] iOS 버전

자세한 로드맵은 [ROADMAP.md](docs/ROADMAP.md) 참조

---

## 🤝 기여하기

프로젝트에 기여하고 싶으신가요? 환영합니다! 🎉

### 기여 방법

1. **Fork** 이 저장소
2. **Feature 브랜치** 생성 (`git checkout -b feature/AmazingFeature`)
3. **변경사항 커밋** (`git commit -m 'feat: Add some AmazingFeature'`)
4. **브랜치에 Push** (`git push origin feature/AmazingFeature`)
5. **Pull Request** 생성

### 기여 가이드라인

- 코드 스타일 준수
- 테스트 코드 작성
- 명확한 커밋 메시지
- 이슈 템플릿 사용

자세한 내용은 [CONTRIBUTING.md](CONTRIBUTING.md) 참조

---

## 🐛 버그 리포트 & 기능 제안

버그를 발견하셨나요? 새로운 기능을 제안하고 싶으신가요?

👉 [Issues](https://github.com/smj1746/ReadMark-APP/issues)에서 등록해주세요!

### 버그 리포트 템플릿

```markdown
**환경**
- OS: [예: Android 14]
- 기기: [예: Pixel 8]
- 앱 버전: [예: 0.1.0]

**문제 설명**
버그에 대한 명확한 설명

**재현 방법**
1. ...
2. ...
3. ...

**예상 동작**
어떻게 동작해야 하는지

**스크린샷**
(선택사항)
```

---

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

```
MIT License

Copyright (c) 2024 민종

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software...
```

---

## 👨‍💻 개발자

**민종**

- GitHub: [@smj1746](https://github.com/smj1746)
- Email: [이메일 주소]
- 소속: [대학교명] [학과] [학년]

---

## 🙏 감사의 말

이 프로젝트는 다음의 오픈소스 프로젝트와 기술을 사용합니다:

- [LM Studio](https://lmstudio.ai/) - 로컬 LLM 실행 환경
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Android UI 프레임워크
- [Hilt](https://dagger.dev/hilt/) - 의존성 주입
- [OkHttp](https://square.github.io/okhttp/) - HTTP 클라이언트
- [Anthropic Claude](https://www.anthropic.com/) - 개발 과정의 AI 파트너

그리고 피드백과 테스트에 참여해주신 모든 분들께 감사드립니다! 🎉

---

## 📚 관련 문서

- [📘 프로젝트 제안서](docs/PROJECT_PROPOSAL.md)
- [🔧 API 문서](docs/API_DOCS.md)
- [🗺 로드맵](docs/ROADMAP.md)
- [💻 개발 가이드](docs/DEVELOPMENT.md)
- [❓ FAQ](docs/FAQ.md)

---

## 📞 연락처

프로젝트에 대한 질문이나 제안이 있으시면 언제든지 연락주세요!

- **Issues**: [GitHub Issues](https://github.com/smj1746/ReadMark-APP/issues)
- **Discussions**: [GitHub Discussions](https://github.com/smj1746/ReadMark-APP/discussions)
- **Email**: [이메일 주소]

---

## 🌟 후원하기

이 프로젝트가 도움이 되셨나요?

- ⭐ **Star** 를 눌러주세요!
- 🔀 **Fork** 하여 개선에 참여해주세요!
- 📢 **공유** 하여 더 많은 사람들에게 알려주세요!

---

<div align="center">

**Made with ❤️ by 민종**

*독서의 즐거움을 더하는 AI 파트너*

[⬆ 맨 위로 돌아가기](#-readmark)

</div>
