package com.readmark.data.model

import com.google.gson.annotations.SerializedName

/**
 * LM Studio 설정 정보
 */
data class LMStudioConfig(
    @SerializedName("endpoint")
    val endpoint: String = "http://10.0.2.2:1234",

    @SerializedName("apiKey")
    val apiKey: String = "lm-studio",

    @SerializedName("selectedModel")
    val selectedModel: String = "meta-llama-3-8b-instruct",  // 텍스트 요약/분석에 최적화된 모델

    @SerializedName("lastWorkingModel")
    val lastWorkingModel: String? = null,

    @SerializedName("temperature")
    val temperature: Float = 0.7f,

    @SerializedName("maxTokens")
    val maxTokens: Int = 1500  // 요약 작업을 위해 1500으로 증가
)

/**
 * 노트 저장 설정
 */
data class NoteSaveConfig(
    @SerializedName("saveToExternal")
    val saveToExternal: Boolean = false,

    @SerializedName("externalPath")
    val externalPath: String = "",

    @SerializedName("defaultFileName")
    val defaultFileName: String = "ReadMark_Note"
)

/**
 * 앱 정보
 */
data class AppInfo(
    @SerializedName("version")
    val version: String = "1.0.0",

    @SerializedName("lastUsed")
    val lastUsed: String? = null
)

/**
 * 앱 전체 설정
 */
data class AppConfig(
    @SerializedName("lmStudio")
    val lmStudio: LMStudioConfig = LMStudioConfig(),

    @SerializedName("noteSave")
    val noteSave: NoteSaveConfig = NoteSaveConfig(),

    @SerializedName("app")
    val app: AppInfo = AppInfo()
)
