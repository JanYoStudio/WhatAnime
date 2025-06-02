package pw.janyo.whatanime.db.service

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pw.janyo.whatanime.db.dao.HistoryDao
import pw.janyo.whatanime.model.AnimationHistory

class HistoryServiceImpl : HistoryService, KoinComponent {
    private val historyDao: HistoryDao by inject()

    override suspend fun saveHistory(animationHistory: AnimationHistory): Long =
        historyDao.saveHistory(animationHistory)

    override suspend fun getById(historyId: Int): AnimationHistory? = historyDao.getById(historyId)

    override suspend fun delete(historyId: Int): Int =
        historyDao.delete(historyId)

    override suspend fun queryAllHistory(): List<AnimationHistory> = historyDao.queryAllHistory()

    override suspend fun update(animationHistory: AnimationHistory): Int =
        historyDao.update(animationHistory)

    override suspend fun queryHistoryByOriginPath(originPath: String): AnimationHistory? =
        historyDao.queryHistoryByOriginPath(originPath)
}