package com.readmark.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.readmark.data.model.*
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * JSON 파일 기반 데이터 관리자
 * - config.json: 앱 설정
 * - history.json: 세션 기록 및 통계
 * - notes/: 생성된 노트 파일들
 */
class DataManager(private val context: Context) {

    companion object {
        private const val TAG = "DataManager"
        private const val CONFIG_FILE = "config.json"
        private const val HISTORY_FILE = "history.json"
        private const val NOTES_DIR = "notes"
        private const val CACHE_DIR = "cache"

        private val gson: Gson = GsonBuilder()
            .setPrettyPrinting()
            .create()
    }

    private val filesDir: File = context.filesDir
    private val configFile: File = File(filesDir, CONFIG_FILE)
    private val historyFile: File = File(filesDir, HISTORY_FILE)
    private val notesDir: File = File(filesDir, NOTES_DIR)
    private val cacheDir: File = File(filesDir, CACHE_DIR)

    init {
        // 디렉토리 생성
        notesDir.mkdirs()
        cacheDir.mkdirs()
    }

    /**
     * 설정 파일 읽기 (없으면 기본값 반환)
     */
    fun getConfig(): AppConfig {
        return try {
            if (configFile.exists()) {
                val json = configFile.readText()
                gson.fromJson(json, AppConfig::class.java) ?: AppConfig()
            } else {
                // 기본 설정 저장
                val defaultConfig = AppConfig()
                saveConfig(defaultConfig)
                defaultConfig
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading config", e)
            AppConfig()
        }
    }

    /**
     * 설정 업데이트
     */
    fun updateConfig(updates: Map<String, Any>): Boolean {
        return try {
            val currentConfig = getConfig()
            val updatedConfig = applyConfigUpdates(currentConfig, updates)
            saveConfig(updatedConfig)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating config", e)
            false
        }
    }

    private fun applyConfigUpdates(config: AppConfig, updates: Map<String, Any>): AppConfig {
        var lmStudio = config.lmStudio
        var app = config.app
        var noteSave = config.noteSave

        updates.forEach { (key, value) ->
            when (key) {
                "endpoint" -> lmStudio = lmStudio.copy(endpoint = value as String)
                "apiKey" -> lmStudio = lmStudio.copy(apiKey = value as String)
                "lastWorkingModel" -> lmStudio = lmStudio.copy(lastWorkingModel = value as? String)
                "temperature" -> lmStudio = lmStudio.copy(temperature = (value as Number).toFloat())
                "maxTokens" -> lmStudio = lmStudio.copy(maxTokens = (value as Number).toInt())
                "selectedModel" -> lmStudio = lmStudio.copy(selectedModel = value as String)
                "version" -> app = app.copy(version = value as String)
                "lastUsed" -> app = app.copy(lastUsed = value as? String)
                "noteSave.saveToExternal" -> noteSave = noteSave.copy(saveToExternal = value as Boolean)
                "noteSave.externalPath" -> noteSave = noteSave.copy(externalPath = value as String)
                "noteSave.defaultFileName" -> noteSave = noteSave.copy(defaultFileName = value as String)
            }
        }

        // lastUsed 자동 업데이트
        app = app.copy(lastUsed = Instant.now().toString())

        return config.copy(lmStudio = lmStudio, app = app, noteSave = noteSave)
    }

    private fun saveConfig(config: AppConfig) {
        try {
            val json = gson.toJson(config)
            configFile.writeText(json)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving config", e)
        }
    }

    /**
     * 세션 기록 추가
     */
    fun addSessionRecord(record: SessionRecord): Boolean {
        return try {
            val history = getHistory()
            val updatedSessions = history.sessions + record
            val updatedStatistics = calculateStatistics(updatedSessions)

            val updatedHistory = history.copy(
                sessions = updatedSessions,
                statistics = updatedStatistics
            )

            saveHistory(updatedHistory)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding session record", e)
            false
        }
    }

    /**
     * 통계 조회
     */
    fun getStatistics(): AppStatistics {
        return try {
            val history = getHistory()
            history.statistics
        } catch (e: Exception) {
            Log.e(TAG, "Error getting statistics", e)
            AppStatistics()
        }
    }

    /**
     * 최근 세션 조회
     */
    fun getRecentSessions(limit: Int = 10): List<SessionRecord> {
        return try {
            val history = getHistory()
            history.sessions
                .sortedByDescending { it.timestamp }
                .take(limit)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recent sessions", e)
            emptyList()
        }
    }

    /**
     * 히스토리 파일 읽기
     */
    private fun getHistory(): HistoryData {
        return try {
            if (historyFile.exists()) {
                val json = historyFile.readText()
                gson.fromJson(json, HistoryData::class.java) ?: HistoryData()
            } else {
                HistoryData()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading history", e)
            HistoryData()
        }
    }

    private fun saveHistory(history: HistoryData) {
        try {
            val json = gson.toJson(history)
            historyFile.writeText(json)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving history", e)
        }
    }

    private fun calculateStatistics(sessions: List<SessionRecord>): AppStatistics {
        val totalSessions = sessions.size
        val pagesProcessed = sessions.size // 각 세션이 한 페이지를 처리
        val summariesCreated = sessions.count { it.mode == "summary" }

        return AppStatistics(
            totalSessions = totalSessions,
            pagesProcessed = pagesProcessed,
            summariesCreated = summariesCreated
        )
    }

    /**
     * 노트 저장 (외부 저장소 지원)
     * @param content 저장할 내용
     * @param title 파일 이름 (확장자 제외)
     * @param saveToExternal 외부 저장소에 저장 여부
     * @param externalPath 외부 저장소 경로 (비어있으면 Documents/ReadMark)
     * @return 저장된 파일의 절대 경로
     */
    fun saveNote(
        content: String,
        title: String = "",
        saveToExternal: Boolean = false,
        externalPath: String = ""
    ): String {
        return try {
            val timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                .replace(":", "-")
                .replace(".", "-")

            val noteTitle = if (title.isNotBlank()) {
                title.replace(Regex("[^a-zA-Z0-9가-힣\\s]"), "_")
            } else {
                "note"
            }

            val filename = "${noteTitle}_$timestamp.md"

            val noteFile = if (saveToExternal) {
                // 외부 저장소에 저장
                val externalDir = if (externalPath.isNotBlank()) {
                    File(externalPath)
                } else {
                    // 기본 경로: Documents/ReadMark
                    val documentsDir = android.os.Environment.getExternalStoragePublicDirectory(
                        android.os.Environment.DIRECTORY_DOCUMENTS
                    )
                    File(documentsDir, "ReadMark")
                }

                // 디렉토리가 없으면 생성
                if (!externalDir.exists()) {
                    externalDir.mkdirs()
                }

                File(externalDir, filename)
            } else {
                // 내부 저장소에 저장
                File(notesDir, filename)
            }

            noteFile.writeText(content, Charsets.UTF_8)

            Log.i(TAG, "Note saved: ${noteFile.absolutePath}")
            noteFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error saving note", e)
            throw e
        }
    }

    /**
     * 이어읽기 위치 찾기
     */
    fun findReadingPosition(
        title: String,
        contentSnippet: String
    ): ReadingPosition? {
        return try {
            val history = getHistory()
            // 간단한 구현: 제목과 내용 스니펫으로 매칭
            history.sessions
                .filter { it.mode == "continue_reading" }
                .firstOrNull {
                    it.title?.contains(title, ignoreCase = true) == true ||
                    contentSnippet.contains(it.title ?: "", ignoreCase = true)
                }
                ?.let { session ->
                    ReadingPosition(
                        title = session.title ?: title,
                        pageInfo = "저장된 위치",
                        anchorSentence = contentSnippet.take(200),
                        lastAccessed = session.timestamp,
                        link = null
                    )
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding reading position", e)
            null
        }
    }

    /**
     * 이어읽기 위치 저장
     */
    fun saveReadingPosition(
        title: String,
        pageInfo: String,
        anchorSentence: String,
        link: String?
    ): Boolean {
        return try {
            val record = SessionRecord(
                sessionId = "reading_${System.currentTimeMillis()}",
                mode = "continue_reading",
                timestamp = Instant.now().toString(),
                inputLength = anchorSentence.length,
                title = title
            )
            addSessionRecord(record)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving reading position", e)
            false
        }
    }

    // ==================== 히스토리 기능 ====================

    /**
     * 히스토리 저장
     */
    fun saveHistoryItem(item: HistoryItem): Boolean {
        return try {
            val historyListFile = File(filesDir, "history_list.json")
            val historyList = getHistoryList().toMutableList()
            historyList.add(0, item) // 최신 항목을 맨 앞에

            // 최대 100개까지만 저장
            if (historyList.size > 100) {
                historyList.removeAt(historyList.size - 1)
            }

            historyListFile.writeText(gson.toJson(historyList))
            Log.d(TAG, "History item saved: ${item.id}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving history item", e)
            false
        }
    }

    /**
     * 히스토리 목록 가져오기
     */
    fun getHistoryList(): List<HistoryItem> {
        return try {
            val historyListFile = File(filesDir, "history_list.json")
            if (!historyListFile.exists()) return emptyList()

            val json = historyListFile.readText()
            gson.fromJson(json, Array<HistoryItem>::class.java).toList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting history list", e)
            emptyList()
        }
    }

    /**
     * 히스토리 삭제
     */
    fun deleteHistoryItem(id: String): Boolean {
        return try {
            val historyListFile = File(filesDir, "history_list.json")
            val historyList = getHistoryList().toMutableList()
            historyList.removeIf { it.id == id }

            historyListFile.writeText(gson.toJson(historyList))
            Log.d(TAG, "History item deleted: $id")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting history item", e)
            false
        }
    }

    /**
     * 전체 히스토리 삭제
     */
    fun clearAllHistory(): Boolean {
        return try {
            val historyListFile = File(filesDir, "history_list.json")
            historyListFile.delete()
            Log.d(TAG, "All history cleared")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing all history", e)
            false
        }
    }
}

/**
 * 히스토리 데이터 구조
 */
private data class HistoryData(
    val sessions: List<SessionRecord> = emptyList(),
    val statistics: AppStatistics = AppStatistics()
)
