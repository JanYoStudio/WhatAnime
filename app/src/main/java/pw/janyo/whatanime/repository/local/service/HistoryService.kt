package pw.janyo.whatanime.repository.local.service

import pw.janyo.whatanime.model.AnimationHistory

interface HistoryService {
	fun saveHistory(animationHistory: AnimationHistory): Long

	fun delete(animationHistory: AnimationHistory): Int

	fun queryAllHistory(): List<AnimationHistory>

	fun update(animationHistory: AnimationHistory): Int

	fun queryHistoryByOriginPathAndFilter(originPath: String): AnimationHistory?
}