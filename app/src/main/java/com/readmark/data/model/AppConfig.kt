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

    @SerializedName("lastWorkingModel")
    val lastWorkingModel: String? = null,

    @SerializedName("temperature")
    val temperature: Float = 0.7f,

    @SerializedName("maxTokens")
    val maxTokens: Int = 1000
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

    @SerializedName("app")
    val app: AppInfo = AppInfo()
)
