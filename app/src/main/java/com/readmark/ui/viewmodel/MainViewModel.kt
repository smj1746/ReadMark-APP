package com.readmark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import com.readmark.data.repository.DataManager
import com.readmark.data.repository.LMStudioRepository
import com.readmark.data.model.AppConfig
import com.readmark.data.model.AppStatistics
import com.readmark.data.model.ConnectionState
import com.readmark.data.model.ErrorType
import com.readmark.data.model.ProcessingMetadata
import com.readmark.data.model.ProcessingMode
import com.readmark.data.model.ProcessingResult
import com.readmark.data.model.SessionRecord
import com.readmark.data.model.WorkMode
import javax.inject.Inject

/**
 * 메인 화면 ViewModel
 * MVVM 패턴에 따라 UI 상태를 관리하고 비즈니스 로직을 처리
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val lmStudioRepository: LMStudioRepository,
    private val dataManager: DataManager
) : ViewModel() {

    // ==================== UI 상태 (StateFlow) ====================

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _processingResult = MutableStateFlow<ProcessingResult>(ProcessingResult.Idle)
    val processingResult: StateFlow<ProcessingResult> = _processingResult.asStateFlow()

    private val _statistics = MutableStateFlow(AppStatistics())
    val statistics: StateFlow<AppStatistics> = _statistics.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _appConfig = MutableStateFlow(AppConfig())
    val appConfig: StateFlow<AppConfig> = _appConfig.asStateFlow()

    // ==================== 초기화 ====================

    init {
        loadConfiguration()
        loadStatistics()
    }

    // ==================== 연결 관련 ====================

    /**
     * LM Studio 연결 테스트
     */
    fun testConnection(endpoint: String? = null, apiKey: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _connectionState.value = ConnectionState.Connecting

            try {
                val testEndpoint = endpoint ?: _appConfig.value.lmStudio.endpoint
                val testApiKey = apiKey ?: _appConfig.value.lmStudio.apiKey

                val result = lmStudioRepository.testConnection(testEndpoint, testApiKey)

                if (result.success) {
                    _connectionState.value = ConnectionState.Connected(
                        models = result.models,
                        message = result.message
                    )
                    // 연결 성공 시 설정 저장
                    updateConfig(mapOf(
                        "endpoint" to testEndpoint,
                        "apiKey" to testApiKey
                    ))
                } else {
                    _connectionState.value = ConnectionState.Error(
                        message = result.message,
                        suggestion = result.suggestion
                    )
                }
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Error(
                    message = "연결 실패: ${e.message}",
                    suggestion = "LM Studio가 실행 중인지 확인하세요"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 연결 해제
     */
    fun disconnect() {
        _connectionState.value = ConnectionState.Disconnected
    }

    // ==================== 텍스트 처리 ====================

    /**
     * 텍스트 처리 (요약 또는 이어읽기)
     */
    fun processText(text: String, mode: WorkMode = WorkMode.AUTO_DETECT) {
        if (text.length < MIN_INPUT_LENGTH) {
            _processingResult.value = ProcessingResult.Error(
                message = "입력된 텍스트가 너무 짧습니다 (최소 ${MIN_INPUT_LENGTH}자)",
                errorType = ErrorType.VALIDATION
            )
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _processingResult.value = ProcessingResult.Loading("텍스트 분석 중...")

            try {
                // 연결 상태 확인
                if (_connectionState.value !is ConnectionState.Connected) {
                    _processingResult.value = ProcessingResult.Error(
                        message = "LM Studio 연결이 필요합니다",
                        suggestion = "먼저 연결 테스트를 수행하세요",
                        errorType = ErrorType.CONNECTION
                    )
                    return@launch
                }

                // 작업 모드 결정
                val processingMode = when (mode) {
                    WorkMode.AUTO_DETECT -> detectMode(text)
                    WorkMode.SUMMARY -> ProcessingMode.SUMMARY
                    WorkMode.CONTINUE_READING -> ProcessingMode.CONTINUE_READING
                }

                // AI 처리 수행
                val aiResult = when (processingMode) {
                    ProcessingMode.SUMMARY -> lmStudioRepository.generateSummary(
                        text = text,
                        temperature = _appConfig.value.lmStudio.temperature,
                        maxTokens = _appConfig.value.lmStudio.maxTokens
                    )
                    ProcessingMode.CONTINUE_READING -> lmStudioRepository.findBookmark(text)
                    ProcessingMode.AUTO_DETECT -> lmStudioRepository.autoProcess(text)
                }

                aiResult.fold(
                    onSuccess = { response ->
                        val content = response.choices.firstOrNull()?.message?.content
                            ?: "처리 결과가 없습니다"
                        val tokensUsed = response.usage?.totalTokens ?: 0

                        _processingResult.value = ProcessingResult.Success(
                            content = content,
                            mode = processingMode,
                            metadata = ProcessingMetadata(
                                tokensUsed = tokensUsed,
                                processingTimeMs = System.currentTimeMillis(),
                                modelUsed = response.model
                            )
                        )

                        // 세션 기록 저장
                        saveSession(text, content, processingMode, tokensUsed)
                    },
                    onFailure = { error ->
                        _processingResult.value = ProcessingResult.Error(
                            message = "처리 실패: ${error.message}",
                            errorType = ErrorType.API
                        )
                    }
                )

            } catch (e: Exception) {
                _processingResult.value = ProcessingResult.Error(
                    message = "예상치 못한 오류: ${e.message}",
                    errorType = ErrorType.UNKNOWN
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 자동으로 작업 모드 감지
     */
    private fun detectMode(text: String): ProcessingMode {
        val bookmarkPatterns = listOf("p.", "페이지", "쪽", "bookmark", "책갈피", "page")
        val hasBookmarkHint = bookmarkPatterns.any { pattern ->
            text.lowercase().contains(pattern)
        }
        return if (hasBookmarkHint) ProcessingMode.CONTINUE_READING else ProcessingMode.SUMMARY
    }

    // ==================== 세션 및 통계 ====================

    /**
     * 세션 기록 저장
     */
    private suspend fun saveSession(
        inputText: String,
        result: String,
        mode: ProcessingMode,
        tokensUsed: Int
    ) {
        try {
            val session = SessionRecord(
                sessionId = "session_${System.currentTimeMillis()}",
                summary = result.take(100) + if (result.length > 100) "..." else "",
                timestamp = java.time.Instant.now().toString(),
                inputLength = inputText.length,
                tokensUsed = tokensUsed,
                mode = mode.name.lowercase()
            )
            dataManager.addSessionRecord(session)
            loadStatistics()
        } catch (e: Exception) {
            // 저장 실패해도 메인 처리에 영향 없음
        }
    }

    /**
     * 통계 로드
     */
    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                val stats = dataManager.getStatistics()
                _statistics.value = stats
            } catch (e: Exception) {
                // 통계 로드 실패 시 기본값 유지
            }
        }
    }

    // ==================== 설정 관리 ====================

    /**
     * 설정 로드
     */
    private fun loadConfiguration() {
        viewModelScope.launch {
            try {
                val config = dataManager.getConfig()
                _appConfig.value = config
            } catch (e: Exception) {
                // 설정 로드 실패 시 기본값 유지
            }
        }
    }

    /**
     * 설정 업데이트
     */
    fun updateConfig(updates: Map<String, Any>) {
        viewModelScope.launch {
            try {
                val success = dataManager.updateConfig(updates)
                if (success) {
                    loadConfiguration()
                }
            } catch (e: Exception) {
                // 설정 업데이트 실패
            }
        }
    }

    // ==================== 유틸리티 ====================

    /**
     * 결과 초기화
     */
    fun clearResults() {
        _processingResult.value = ProcessingResult.Idle
    }

    /**
     * 노트 저장
     */
    fun saveNote(title: String, content: String): Boolean {
        return try {
            dataManager.saveNote(content, title)
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val MIN_INPUT_LENGTH = 10
    }
}
