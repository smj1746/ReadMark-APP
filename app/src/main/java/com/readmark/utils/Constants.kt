package com.readmark.utils

object Constants {

    // 앱 정보
    const val APP_NAME = "ReadMark"
    const val APP_VERSION = "1.0.0"

    // LM Studio 기본 설정
    object LMStudio {
        const val DEFAULT_ENDPOINT = "http://10.0.2.2:1234"
        const val DEFAULT_API_KEY = "lm-studio"
        const val DEFAULT_TEMPERATURE = 0.7f
        const val DEFAULT_MAX_TOKENS = 1000
        const val CONNECTION_TIMEOUT_MS = 60_000L  // 60초
        const val READ_TIMEOUT_MS = 300_000L       // 5분
    }

    // 파일 및 저장소 관련
    object Storage {
        const val CONFIG_FILE_NAME = "config.json"
        const val HISTORY_FILE_NAME = "history.json"
        const val CACHE_DIR_NAME = "cache"
        const val OCR_CACHE_DIR_NAME = "cache/ocr"
        const val NOTES_DIR_NAME = "notes"
        const val BACKUP_SUFFIX = ".backup"
        const val MAX_HISTORY_SESSIONS = 100
        const val MAX_CACHE_AGE_DAYS = 30
    }

    // UI 관련
    object UI {
        const val ANIMATION_DURATION_MS = 300L
        const val TOAST_DURATION_MS = 3000L
        const val DEBOUNCE_DELAY_MS = 500L
        const val MAX_INPUT_TEXT_LENGTH = 10_000
        const val MIN_INPUT_TEXT_LENGTH = 10
        const val MAX_NOTE_TITLE_LENGTH = 100
    }

    // 오류 메시지
    object ErrorMessages {
        const val NETWORK_ERROR = "네트워크 연결을 확인해주세요"
        const val SERVER_ERROR = "서버에 문제가 발생했습니다"
        const val INVALID_INPUT = "입력값이 올바르지 않습니다"
        const val MODEL_NOT_LOADED = "AI 모델이 로드되지 않았습니다"
    }
}
