package com.readmark.data.repository

import android.util.Log
import com.readmark.data.model.ConnectionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LM Studio API 통신을 담당하는 Repository
 */
@Singleton
class LMStudioRepository @Inject constructor() {

    companion object {
        private const val TAG = "LMStudioRepository"
        private const val CONNECT_TIMEOUT = 60L
        private const val READ_TIMEOUT = 300L  // 5분
        private const val WRITE_TIMEOUT = 60L
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .build()

    private var currentEndpoint: String = "http://192.168.1.100:1234"
    private var currentApiKey: String = "lm-studio"
    private var currentModel: String = ""  // testConnection에서 자동으로 설정됨

    /**
     * LM Studio 연결 테스트
     */
    suspend fun testConnection(
        endpoint: String? = null,
        apiKey: String? = null
    ): ConnectionResult = withContext(Dispatchers.IO) {
        val testEndpoint = endpoint ?: currentEndpoint
        val testApiKey = apiKey ?: currentApiKey

        try {
            val modelsUrl = "${testEndpoint.trimEnd('/')}/v1/models"

            val request = Request.Builder()
                .url(modelsUrl)
                .addHeader("Authorization", "Bearer $testApiKey")
                .get()
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val models = parseModels(body)

                // 연결 성공 시 현재 설정 업데이트
                currentEndpoint = testEndpoint
                currentApiKey = testApiKey

                // 첫 번째 모델을 기본 모델로 설정
                if (models.isNotEmpty()) {
                    currentModel = models.first()
                    Log.d(TAG, "Using model: $currentModel")
                }

                ConnectionResult(
                    success = true,
                    message = "연결 성공! ${models.size}개의 모델을 찾았습니다.",
                    models = models
                )
            } else {
                ConnectionResult(
                    success = false,
                    message = "서버 응답 오류: ${response.code}",
                    suggestion = "LM Studio가 실행 중인지 확인하세요"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Connection test failed", e)
            ConnectionResult(
                success = false,
                message = "연결 실패: ${e.message}",
                suggestion = when {
                    e.message?.contains("timeout", ignoreCase = true) == true ->
                        "서버 응답 시간이 초과되었습니다. 네트워크를 확인하세요."
                    e.message?.contains("refused", ignoreCase = true) == true ->
                        "연결이 거부되었습니다. LM Studio가 실행 중인지 확인하세요."
                    e.message?.contains("host", ignoreCase = true) == true ->
                        "호스트를 찾을 수 없습니다. IP 주소를 확인하세요."
                    else -> "네트워크 연결 상태를 확인하세요."
                }
            )
        }
    }

    /**
     * 모델 설정
     * @param modelName 사용할 모델 이름
     */
    fun setModel(modelName: String) {
        currentModel = modelName
        Log.d(TAG, "Model set to: $modelName")
    }

    /**
     * 현재 설정된 모델 이름 가져오기
     */
    fun getCurrentModel(): String = currentModel

    /**
     * 텍스트 요약 생성
     */
    suspend fun generateSummary(
        text: String,
        temperature: Float = 0.7f,
        maxTokens: Int = 1000
    ): Result<LMStudioResponse> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildSummaryPrompt(text)
            val response = callChatCompletion(prompt, temperature, maxTokens)
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Summary generation failed", e)
            Result.failure(e)
        }
    }

    /**
     * 책갈피/이어읽기 위치 찾기
     */
    suspend fun findBookmark(text: String): Result<LMStudioResponse> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildBookmarkPrompt(text)
            val response = callChatCompletion(prompt, 0.3f, 500)
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Bookmark finding failed", e)
            Result.failure(e)
        }
    }

    /**
     * 자동 처리 (모드 자동 감지)
     */
    suspend fun autoProcess(text: String): Result<LMStudioResponse> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildAutoProcessPrompt(text)
            val response = callChatCompletion(prompt, 0.5f, 1000)
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Auto processing failed", e)
            Result.failure(e)
        }
    }

    /**
     * Chat Completion API 호출
     */
    private fun callChatCompletion(
        prompt: String,
        temperature: Float,
        maxTokens: Int
    ): LMStudioResponse {
        val url = "${currentEndpoint.trimEnd('/')}/v1/chat/completions"

        // 모델이 설정되지 않은 경우 예외 발생
        if (currentModel.isEmpty()) {
            throw Exception("모델이 설정되지 않았습니다. 먼저 연결 테스트를 수행하세요.")
        }

        val requestBody = JSONObject().apply {
            put("model", currentModel)  // 모델 필드 추가
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are a helpful reading assistant that summarizes text and helps users track their reading progress.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
            put("temperature", temperature.toDouble())
            put("max_tokens", maxTokens)
            put("stream", false)
        }

        // 요청 정보 로깅
        Log.d("LMStudio", "=== API 요청 ===")
        Log.d("LMStudio", "URL: $url")
        Log.d("LMStudio", "Model: $currentModel")
        Log.d("LMStudio", "Request Body: ${requestBody.toString()}")

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $currentApiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = client.newCall(request).execute()
        Log.d("LMStudio", "Response Code: ${response.code}")

        if (!response.isSuccessful) {
            val errorBody = response.body?.string()
            Log.e("LMStudio", "Error Response: $errorBody")
            throw Exception("API 요청 실패: ${response.code} - $errorBody")
        }

        val body = response.body?.string() ?: throw Exception("빈 응답")
        Log.d("LMStudio", "Response Body: ${body.take(200)}...")  // 처음 200자만 로깅
        return parseResponse(body)
    }

    /**
     * 요약 프롬프트 생성
     */
    private fun buildSummaryPrompt(text: String): String {
        return """
            다음 텍스트를 읽고 핵심 내용을 요약해주세요.

            요약 형식:
            1. 핵심 요약 (2-3문장)
            2. 주요 키워드 (3-5개)
            3. 중요 포인트 (bullet points)

            텍스트:
            $text
        """.trimIndent()
    }

    /**
     * 책갈피 찾기 프롬프트 생성
     */
    private fun buildBookmarkPrompt(text: String): String {
        return """
            다음 텍스트에서 책갈피나 이어읽기 위치를 찾아주세요.
            페이지 번호, 챕터 정보, 또는 특정 문장을 기준으로 위치를 파악해주세요.

            텍스트:
            $text

            응답 형식:
            - 위치 정보: (페이지/챕터/문단)
            - 앵커 문장: (해당 위치의 시작 문장)
            - 추천: (이어읽기 방법 제안)
        """.trimIndent()
    }

    /**
     * 자동 처리 프롬프트 생성
     */
    private fun buildAutoProcessPrompt(text: String): String {
        return """
            다음 텍스트를 분석하고 적절한 처리를 수행해주세요.

            1. 책갈피/페이지 정보가 있으면: 이어읽기 위치 정보 제공
            2. 일반 텍스트면: 요약 및 핵심 키워드 추출

            텍스트:
            $text
        """.trimIndent()
    }

    /**
     * 모델 목록 파싱
     */
    private fun parseModels(body: String): List<String> {
        return try {
            val json = JSONObject(body)
            val data = json.optJSONArray("data") ?: return emptyList()

            (0 until data.length()).mapNotNull { i ->
                data.optJSONObject(i)?.optString("id")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse models", e)
            emptyList()
        }
    }

    /**
     * API 응답 파싱
     */
    private fun parseResponse(body: String): LMStudioResponse {
        val json = JSONObject(body)

        val choices = json.optJSONArray("choices")?.let { choicesArray ->
            (0 until choicesArray.length()).map { i ->
                val choice = choicesArray.getJSONObject(i)
                val message = choice.getJSONObject("message")
                Choice(
                    index = choice.optInt("index", 0),
                    message = Message(
                        role = message.optString("role", "assistant"),
                        content = message.optString("content", "")
                    ),
                    finishReason = choice.optString("finish_reason")
                )
            }
        } ?: emptyList()

        val usage = json.optJSONObject("usage")?.let {
            Usage(
                promptTokens = it.optInt("prompt_tokens", 0),
                completionTokens = it.optInt("completion_tokens", 0),
                totalTokens = it.optInt("total_tokens", 0)
            )
        }

        return LMStudioResponse(
            id = json.optString("id", ""),
            model = json.optString("model", ""),
            choices = choices,
            usage = usage
        )
    }
}

/**
 * LM Studio API 응답 모델
 */
data class LMStudioResponse(
    val id: String,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage?
)

data class Choice(
    val index: Int,
    val message: Message,
    val finishReason: String?
)

data class Message(
    val role: String,
    val content: String
)

data class Usage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)
