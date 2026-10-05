package pw.janyo.whatanime.repository

import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.model.SearchAnimeResult

data class HistoryLookup(
    val historyId: Int,
    val cachePath: String,
    val savedAt: Long,
    val result: SearchAnimeResult,
    val legacy: Boolean = false,
)

enum class DeleteHistoryResult { Deleted, CacheCleanupFailed }

interface HistoryGateway {
    suspend fun queryAllHistory(): List<AnimationHistory>
    suspend fun getHistoryDetails(historyId: Int): HistoryLookup?
    suspend fun deleteHistory(historyId: Int): DeleteHistoryResult
}
