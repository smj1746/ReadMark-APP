package com.readmark.data.model

/**
 * 이어읽기 위치 정보
 */
data class ReadingPosition(
    val title: String,
    val pageInfo: String,
    val anchorSentence: String,
    val lastAccessed: String,
    val link: String? = null
)
