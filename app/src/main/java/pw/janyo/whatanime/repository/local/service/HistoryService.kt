package pw.janyo.whatanime.repository.local.service

import pw.janyo.whatanime.model.AnimationHistory

interface HistoryService {
    fun saveHistory(animationHistory: AnimationHistory): Long

    fun getById(historyId: Int): AnimationHistory?

    fun delete(historyId: Int): Int

    fun queryAllHistory(): List<AnimationHistory>

    fun update(animationHistory: AnimationHistory): Int

    fun queryHistoryByOriginPath(originPath: String): AnimationHistory?
}