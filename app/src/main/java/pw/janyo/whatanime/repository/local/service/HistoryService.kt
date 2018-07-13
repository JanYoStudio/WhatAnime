package pw.janyo.whatanime.repository.local.service

import androidx.lifecycle.LiveData
import pw.janyo.whatanime.model.AnimationHistory

interface HistoryService {
	fun saveHistory(animationHistory: AnimationHistory): LiveData<Long>

	fun delete(animationHistory: AnimationHistory): LiveData<Long>

	fun queryAllHistory(): LiveData<AnimationHistory>

	fun update(animationHistory: AnimationHistory): LiveData<Long>

	fun queryHistoryByOriginPath(originPath: String): LiveData<AnimationHistory>
}