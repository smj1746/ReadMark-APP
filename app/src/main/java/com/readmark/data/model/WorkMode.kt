package com.readmark.data.model

/**
 * 작업 모드 (사용자 선택)
 */
enum class WorkMode(val displayName: String, val description: String) {
    AUTO_DETECT(
        displayName = "자동 감지",
        description = "책갈피 유무를 자동으로 판단하여 처리합니다"
    ),
    SUMMARY(
        displayName = "요약/노트 생성",
        description = "페이지 내용을 요약하고 핵심 키워드를 추출합니다"
    ),
    CONTINUE_READING(
        displayName = "이어읽기 (책갈피)",
        description = "이전 독서 위치를 찾아 연결합니다"
    );

    companion object {
        fun fromString(mode: String): WorkMode {
            return when (mode.lowercase()) {
                "summary" -> SUMMARY
                "continue_reading", "continue" -> CONTINUE_READING
                "auto_detect", "auto" -> AUTO_DETECT
                else -> AUTO_DETECT
            }
        }
    }
}
