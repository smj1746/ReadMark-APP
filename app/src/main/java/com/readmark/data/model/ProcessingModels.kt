package com.readmark.data.model

/**
 * 연결 상태
 */
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    data class Connected(
        val models: List<String> = emptyList(),
        val message: String = ""
    ) : ConnectionState()
    data class Error(
        val message: String,
        val suggestion: String? = null
    ) : ConnectionState()
}

/**
 * 처리 모드 (내부)
 */
enum class ProcessingMode {
    AUTO_DETECT,
    SUMMARY,
    CONTINUE_READING
}

/**
 * 에러 타입
 */
enum class ErrorType {
    NETWORK,
    API,
    VALIDATION,
    CONNECTION,
    UNKNOWN
}

/**
 * 처리 메타데이터
 */
data class ProcessingMetadata(
    val tokensUsed: Int = 0,
    val processingTimeMs: Long = 0,
    val modelUsed: String? = null
)

/**
 * 처리 결과
 */
sealed class ProcessingResult {
    object Idle : ProcessingResult()
    data class Loading(val message: String) : ProcessingResult()
    data class Success(
        val content: String,
        val mode: ProcessingMode,
        val metadata: ProcessingMetadata = ProcessingMetadata()
    ) : ProcessingResult()
    data class Error(
        val message: String,
        val errorType: ErrorType = ErrorType.UNKNOWN,
        val suggestion: String? = null
    ) : ProcessingResult()
}

/**
 * 연결 테스트 결과 (Repository에서 사용)
 */
data class ConnectionResult(
    val success: Boolean,
    val message: String,
    val models: List<String> = emptyList(),
    val suggestion: String? = null
)
