package com.readmark.data.model

import com.google.gson.annotations.SerializedName

/**
 * 히스토리 항목 데이터 모델
 * 사용자가 처리한 텍스트 요약/분석 내역을 저장
 */
data class HistoryItem(
    @SerializedName("id")
    val id: String,

    @SerializedName("timestamp")
    val timestamp: String,

    @SerializedName("inputText")
    val inputText: String,

    @SerializedName("result")
    val result: String,

    @SerializedName("mode")
    val mode: String,

    @SerializedName("tokensUsed")
    val tokensUsed: Int = 0,

    @SerializedName("modelUsed")
    val modelUsed: String = ""
)
