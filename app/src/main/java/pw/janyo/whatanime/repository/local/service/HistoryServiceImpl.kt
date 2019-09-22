package pw.janyo.whatanime.repository.local.service

import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.repository.local.db.DBHelper

object HistoryServiceImpl : HistoryService {

	private val historyDao = DBHelper.db.getHistoryDao()

	override fun saveHistory(animationHistory: AnimationHistory): Long = historyDao.saveHistory(animationHistory)

	override fun delete(animationHistory: AnimationHistory): Int = historyDao.delete(animationHistory)

	override fun queryAllHistory(): List<AnimationHistory> = historyDao.queryAllHistory()

	override fun update(animationHistory: AnimationHistory): Int = historyDao.update(animationHistory)

	override fun queryHistoryByOriginPathAndFilter(originPath: String, filter: String?): AnimationHistory? {
		return if (filter == null)
			historyDao.queryHistoryByOriginPath(originPath)
		else
			historyDao.queryHistoryByOriginPathAndFilter(originPath, filter)
	}

	override fun queryHistoryByBase64(base64: String): AnimationHistory? = historyDao.queryHistoryByBase64(base64)
}