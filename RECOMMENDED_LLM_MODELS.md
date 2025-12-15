# ReadMark용 추천 LLM 모델 가이드 🤖

## 🎯 텍스트 요약/분석에 최적화된 모델

ReadMark 앱은 **텍스트 요약, 책갈피 찾기, 이어읽기 분석**을 위해 설계되었습니다.
다음 모델들이 이러한 작업에 최적화되어 있습니다.

---

## ⭐ 1순위 추천: Meta Llama 3.1 시리즈

### 🥇 **Meta-Llama-3.1-8B-Instruct** (강력 추천!)
```
모델명: meta-llama-3.1-8b-instruct
크기: ~8GB
속도: ⚡⚡⚡⚡ (매우 빠름)
품질: ⭐⭐⭐⭐⭐ (탁월)
RAM 요구: 12GB 이상
```

**장점:**
- ✅ 한국어 요약 품질이 매우 우수
- ✅ 긴 텍스트 처리 능력 탁월 (최대 128K 토큰)
- ✅ 빠른 응답 속도
- ✅ 책갈피/이어읽기 위치 파악 정확도 높음
- ✅ 일반 PC에서 원활히 작동

**ReadMark 설정:**
```kotlin
selectedModel = "meta-llama-3.1-8b-instruct"
temperature = 0.7
maxTokens = 1500
```

**LM Studio에서 다운로드:**
1. Search 탭에서 "llama-3.1-8b" 검색
2. "meta-llama/Meta-Llama-3.1-8B-Instruct" 선택
3. GGUF 파일 다운로드 (Q4_K_M 권장)

---

### 🥈 **Meta-Llama-3-8B-Instruct** (차선책)
```
모델명: meta-llama-3-8b-instruct
크기: ~8GB
속도: ⚡⚡⚡⚡
품질: ⭐⭐⭐⭐
RAM 요구: 12GB 이상
```

**특징:**
- ✅ Llama 3.1보다 약간 낮은 성능이지만 여전히 우수
- ✅ 안정성이 검증됨
- ✅ 한국어 지원 양호

**사용 시나리오:**
- Llama 3.1을 찾을 수 없을 때
- 더 안정적인 버전을 원할 때

---

## ⭐ 2순위 추천: Mistral 시리즈

### 🥉 **Mistral-7B-Instruct-v0.3**
```
모델명: mistral-7b-instruct-v0.3
크기: ~7GB
속도: ⚡⚡⚡⚡⚡ (초고속)
품질: ⭐⭐⭐⭐
RAM 요구: 10GB 이상
```

**장점:**
- ✅ 매우 빠른 응답 속도
- ✅ 간결한 요약 생성에 탁월
- ✅ 낮은 리소스 요구사항

**단점:**
- ⚠️ Llama 3.1보다 긴 텍스트 처리가 약함
- ⚠️ 한국어 성능이 Llama보다 낮음

**추천 상황:**
- 빠른 응답이 필요할 때
- PC 사양이 낮을 때
- 짧은 텍스트 요약 작업

---

## ⭐ 3순위 추천: Phi-3 시리즈

### **Phi-3-Medium-4K-Instruct**
```
모델명: phi-3-medium-4k-instruct
크기: ~4GB
속도: ⚡⚡⚡⚡⚡
품질: ⭐⭐⭐
RAM 요구: 8GB 이상
```

**장점:**
- ✅ 매우 작은 크기
- ✅ 저사양 PC에서도 작동
- ✅ 기본적인 요약 작업 가능

**단점:**
- ⚠️ 한국어 성능 제한적
- ⚠️ 복잡한 분석 작업에 부족
- ⚠️ 최대 4K 토큰으로 긴 텍스트 처리 불가

**추천 상황:**
- RAM이 8GB 이하인 PC
- 간단한 요약만 필요할 때

---

## 🚫 피해야 할 모델

### ❌ GPT 계열 (OpenAI 모델)
```
모델명: gpt-3.5-turbo, gpt-4 등
```
**이유:**
- ❌ LM Studio에서 직접 실행 불가
- ❌ OpenAI API 키와 유료 결제 필요
- ❌ ReadMark는 로컬 모델 전용

### ❌ 70B 이상의 대형 모델
```
예: Llama-3.1-70B, Mixtral-8x7B 등
```
**이유:**
- ❌ 70GB 이상의 RAM 필요
- ❌ 고사양 워크스테이션 필수
- ❌ 일반 PC에서 실행 불가능

### ❌ 채팅 전용 모델
```
예: ChatGLM, Baichuan 등
```
**이유:**
- ⚠️ 요약 작업에 최적화되지 않음
- ⚠️ 대화형 응답에만 특화

---

## 📊 모델 비교표

| 모델 | 크기 | 속도 | 요약 품질 | 한국어 | RAM | 추천도 |
|------|------|------|-----------|--------|-----|--------|
| **Llama-3.1-8B** | 8GB | ⚡⚡⚡⚡ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 12GB | 🥇 |
| **Llama-3-8B** | 8GB | ⚡⚡⚡⚡ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | 12GB | 🥈 |
| Mistral-7B-v0.3 | 7GB | ⚡⚡⚡⚡⚡ | ⭐⭐⭐⭐ | ⭐⭐⭐ | 10GB | 🥉 |
| Phi-3-Medium | 4GB | ⚡⚡⚡⚡⚡ | ⭐⭐⭐ | ⭐⭐ | 8GB | ⭐ |

---

## 🔧 ReadMark에서 모델 설정하기

### 1. LM Studio에서 모델 다운로드

**Llama 3.1 8B 예시:**
```
1. LM Studio 실행
2. "Search" 탭 클릭
3. 검색창에 "llama-3.1-8b-instruct" 입력
4. "meta-llama/Meta-Llama-3.1-8B-Instruct-GGUF" 선택
5. 파일 선택: "Meta-Llama-3.1-8B-Instruct-Q4_K_M.gguf"
6. Download 클릭
7. 완료 대기 (5-10분)
```

### 2. 모델 로드

```
1. LM Studio 왼쪽 메뉴에서 다운로드한 모델 선택
2. "Load Model" 버튼 클릭
3. 모델 로드 완료 대기 (1-3분)
4. 하단 상태바에서 "Model loaded" 확인
```

### 3. ReadMark 앱에서 연결

```
1. ReadMark 앱 실행
2. "연결 테스트" 버튼 클릭
3. 성공 메시지 확인
4. "AI 모델 선택" 드롭다운에서 로드된 모델 확인
```

---

## 💡 모델 선택 가이드

### PC 사양별 추천

#### 🖥️ 고사양 PC (RAM 16GB 이상, GPU 있음)
```
추천: Meta-Llama-3.1-8B-Instruct
대안: Meta-Llama-3-8B-Instruct
```

#### 💻 중사양 PC (RAM 12-16GB)
```
추천: Meta-Llama-3-8B-Instruct
대안: Mistral-7B-Instruct-v0.3
```

#### 📱 저사양 PC (RAM 8-12GB)
```
추천: Mistral-7B-Instruct-v0.3
대안: Phi-3-Medium-4K-Instruct
```

### 작업별 추천

#### 📄 긴 텍스트 요약 (1000자 이상)
```
1순위: Meta-Llama-3.1-8B (128K 컨텍스트)
2순위: Meta-Llama-3-8B
```

#### ⚡ 빠른 응답 필요
```
1순위: Mistral-7B-Instruct-v0.3
2순위: Phi-3-Medium
```

#### 📚 책갈피/이어읽기 분석
```
1순위: Meta-Llama-3.1-8B (문맥 이해 탁월)
2순위: Meta-Llama-3-8B
```

#### 🇰🇷 한국어 텍스트
```
1순위: Meta-Llama-3.1-8B (한국어 최고)
2순위: Meta-Llama-3-8B
```

---

## 🎓 고급 설정

### Temperature 설정

```kotlin
// 요약 작업
temperature = 0.7  // 균형잡힌 창의성

// 책갈피 찾기
temperature = 0.3  // 정확성 우선

// 이어읽기 분석
temperature = 0.5  // 중간 수준
```

### Max Tokens 설정

```kotlin
// 짧은 요약 (100-200자)
maxTokens = 500

// 중간 요약 (300-500자)
maxTokens = 1000

// 긴 요약 (500-1000자)
maxTokens = 1500  // ReadMark 기본값

// 매우 상세한 요약
maxTokens = 2000
```

---

## 📥 LM Studio 다운로드 링크

**공식 사이트:** https://lmstudio.ai/

**시스템 요구사항:**
- Windows 10/11, macOS, Linux
- 최소 8GB RAM (16GB 권장)
- 10GB 이상 저장 공간

---

## 🔄 기존 모델에서 업그레이드

### 이전에 다른 모델을 사용했다면:

1. **LM Studio에서 기존 모델 언로드**
   ```
   - 왼쪽 메뉴에서 현재 로드된 모델 우클릭
   - "Unload Model" 선택
   ```

2. **새 모델 로드**
   ```
   - Meta-Llama-3.1-8B-Instruct 선택
   - "Load Model" 클릭
   ```

3. **ReadMark에서 연결 테스트**
   ```
   - "연결 테스트" 다시 실행
   - 새 모델이 목록에 표시되는지 확인
   ```

---

## ✅ 현재 ReadMark 기본 설정

```kotlin
// AppConfig.kt에 설정됨
selectedModel = "meta-llama-3.1-8b-instruct"
temperature = 0.7
maxTokens = 1500
```

이 설정은 **텍스트 요약과 분석에 최적화**되어 있습니다!

---

## 🆘 문제 해결

### "모델을 찾을 수 없습니다" 오류
```
해결:
1. LM Studio에서 정확한 모델 이름 확인
2. ReadMark "연결 테스트"로 실제 모델 이름 확인
3. 설정에서 올바른 모델 이름 입력
```

### "메모리 부족" 오류
```
해결:
1. 더 작은 모델 사용 (Phi-3-Medium)
2. 다른 프로그램 종료
3. PC 재시작 후 재시도
```

### "응답이 너무 느림"
```
해결:
1. Mistral-7B로 변경 (더 빠름)
2. maxTokens 줄이기 (1500 → 1000)
3. GPU 가속 활성화 (LM Studio 설정)
```

---

**작성일:** 2025-12-15
**최종 추천:** Meta-Llama-3.1-8B-Instruct 🥇
