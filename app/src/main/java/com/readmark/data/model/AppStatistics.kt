package com.readmark.data.model

import com.google.gson.annotations.SerializedName

/**
 * 앱 통계 정보
 */
data class AppStatistics(
    @SerializedName("totalSessions")
    val totalSessions: Int = 0,

    @SerializedName("pagesProcessed")
    val pagesProcessed: Int = 0,

    @SerializedName("summariesCreated")
    val summariesCreated: Int = 0
)
