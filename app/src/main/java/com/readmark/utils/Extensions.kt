package com.readmark.utils

import android.content.Context
import android.widget.Toast
import java.security.MessageDigest
import java.time.Instant

// Context 확장 함수들
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

// String 확장 함수들
fun String.toMD5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(toByteArray())
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}

fun String.cleanText(): String {
    return this
        .replace("\\s+".toRegex(), " ")
        .replace("\\n\\s*\\n".toRegex(), "\n\n")
        .trim()
}

fun String.limitLength(maxLength: Int, ellipsis: Boolean = true): String {
    return if (length <= maxLength) {
        this
    } else {
        take(maxLength) + if (ellipsis) "..." else ""
    }
}

// 현재 시간을 ISO 문자열로
fun getCurrentISOString(): String {
    return Instant.now().toString()
}

// 안전한 정수 파싱
fun String.toIntOrDefault(default: Int = 0): Int {
    return try {
        toInt()
    } catch (e: NumberFormatException) {
        default
    }
}

// 안전한 Float 파싱
fun String.toFloatOrDefault(default: Float = 0f): Float {
    return try {
        toFloat()
    } catch (e: NumberFormatException) {
        default
    }
}
