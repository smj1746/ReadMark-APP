package com.readmark.data.model

import com.google.gson.annotations.SerializedName

/**
 * 세션 기록 모델
 */
data class SessionRecord(
    @SerializedName("sessionId")
    val sessionId: String,

    @SerializedName("mode")
    val mode: String, // "summary", "continue_reading", "auto_detect"

    @SerializedName("timestamp")
    val timestamp: String,

    @SerializedName("inputLength")
    val inputLength: Int,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("summary")
    val summary: String? = null,

    @SerializedName("tokensUsed")
    val tokensUsed: Int = 0,

    @SerializedName("hasBookmark")
    val hasBookmark: Boolean? = null,

    @SerializedName("confidence")
    val confidence: Float? = null
)
