package pw.janyo.whatanime.db.service

import pw.janyo.whatanime.model.AnimationHistory

interface HistoryService {
    suspend fun saveHistory(animationHistory: AnimationHistory): Long

    suspend fun getById(historyId: Int): AnimationHistory?

    suspend fun delete(historyId: Int): Int

    suspend fun queryAllHistory(): List<AnimationHistory>

    suspend fun update(animationHistory: AnimationHistory): Int

    suspend fun queryHistoryByOriginPath(originPath: String): AnimationHistory?
}