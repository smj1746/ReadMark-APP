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
        private const val CONNECT_TIMEOUT = 30L  // 30초
        private const val READ_TIMEOUT = 60L     // 60초 (1분)
        private const val WRITE_TIMEOUT = 30L    // 30초
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .build()

    private var currentEndpoint: String = "http://10.0.2.2:1234"
    private var currentApiKey: String = "lm-studio"
    private var currentModel: String = "meta-llama-3-8b-instruct"  // 텍스트 요약/분석 최적 모델

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

                // 모델이 없으면 에러 반환 (사용자가 LM Studio에서 모델을 로드해야 함)
                if (models.isEmpty()) {
                    Log.w(TAG, "No models loaded in LM Studio")
                    return@withContext ConnectionResult(
                        success = false,
                        message = "LM Studio에 로드된 모델이 없습니다",
                        suggestion = "LM Studio를 열고 모델을 로드한 후 다시 시도하세요.\n\n" +
                                "방법:\n" +
                                "1. LM Studio 실행\n" +
                                "2. 왼쪽 메뉴에서 모델 선택\n" +
                                "3. 'Load Model' 버튼 클릭\n" +
                                "4. 모델 로드 완료 후 이 앱에서 '연결 테스트' 다시 클릭",
                        models = emptyList()
                    )
                }

                // 첫 번째 모델을 기본 모델로 설정
                currentModel = models.first()
                Log.d(TAG, "Using model: $currentModel")

                ConnectionResult(
                    success = true,
                    message = "연결 성공! ${models.size}개의 모델을 사용할 수 있습니다.",
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
        temperature: Float = 0.5f,
        maxTokens: Int = 600,
        model: String? = null
    ): Result<LMStudioResponse> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildSummaryPrompt(text)
            // 빈 문자열도 null로 처리하고 currentModel 사용
            val modelToUse = if (model.isNullOrBlank()) currentModel else model

            val response = callChatCompletion(prompt, temperature, maxTokens, modelToUse)
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Summary generation failed", e)
            Result.failure(e)
        }
    }

    /**
     * 책갈피/이어읽기 위치 찾기
     */
    suspend fun findBookmark(
        text: String,
        model: String? = null
    ): Result<LMStudioResponse> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildBookmarkPrompt(text)
            // 빈 문자열도 null로 처리하고 currentModel 사용
            val modelToUse = if (model.isNullOrBlank()) currentModel else model

            val response = callChatCompletion(prompt, 0.3f, 400, modelToUse)
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Bookmark finding failed", e)
            Result.failure(e)
        }
    }

    /**
     * 자동 처리 (모드 자동 감지)
     */
    suspend fun autoProcess(
        text: String,
        model: String? = null
    ): Result<LMStudioResponse> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildAutoProcessPrompt(text)
            // 빈 문자열도 null로 처리하고 currentModel 사용
            val modelToUse = if (model.isNullOrBlank()) currentModel else model

            val response = callChatCompletion(prompt, 0.5f, 600, modelToUse)
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
        maxTokens: Int,
        model: String
    ): LMStudioResponse {
        val url = "${currentEndpoint.trimEnd('/')}/v1/chat/completions"

        val requestBody = JSONObject().apply {
            put("model", model)  // 선택된 모델 사용
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

            // 400 에러 시 더 자세한 안내
            val errorMessage = when (response.code) {
                400 -> {
                    if (errorBody?.contains("Failed to load model") == true) {
                        "모델 로드 실패: '$model' 모델이 LM Studio에서 로드되지 않았습니다.\n\n" +
                        "해결 방법:\n" +
                        "1. LM Studio를 열고 모델이 로드되어 있는지 확인\n" +
                        "2. 로드되지 않았다면 왼쪽 메뉴에서 모델 선택 후 'Load Model' 클릭\n" +
                        "3. 모델 로드 완료 후 이 앱에서 '연결 테스트'를 다시 실행\n" +
                        "4. 사용 가능한 모델 목록에서 선택하여 사용"
                    } else {
                        "잘못된 요청 (400): $errorBody"
                    }
                }
                401 -> "인증 실패: API 키를 확인하세요"
                404 -> "엔드포인트를 찾을 수 없습니다: $url"
                500 -> "LM Studio 서버 오류: 서버를 재시작해보세요"
                else -> "API 요청 실패 (${response.code}): $errorBody"
            }

            throw Exception(errorMessage)
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
            다음 텍스트의 핵심 내용을 2-3문장으로 간단히 요약하고, 주요 키워드 3개를 제시하세요.

            텍스트:
            $text
        """.trimIndent()
    }

    /**
     * 책갈피 찾기 프롬프트 생성
     */
    private fun buildBookmarkPrompt(text: String): String {
        return """
            이 텍스트에서 책갈피 위치를 찾아 간단히 알려주세요. (페이지/챕터 번호와 앵커 문장)

            텍스트:
            $text
        """.trimIndent()
    }

    /**
     * 자동 처리 프롬프트 생성
     */
    private fun buildAutoProcessPrompt(text: String): String {
        return """
            이 텍스트를 분석해주세요. 책갈피 정보가 있으면 위치를, 없으면 간단한 요약을 제공하세요.

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
