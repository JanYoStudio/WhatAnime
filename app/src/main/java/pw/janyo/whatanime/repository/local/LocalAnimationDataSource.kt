package pw.janyo.whatanime.repository.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pw.janyo.whatanime.constant.StringConstant
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.repository.dataSource.AnimationDateSource
import pw.janyo.whatanime.repository.local.service.HistoryService
import pw.janyo.whatanime.repository.local.service.HistoryServiceImpl
import pw.janyo.whatanime.repository.remote.RemoteAnimationDataSource
import pw.janyo.whatanime.utils.getCacheFile
import vip.mystery0.tools.factory.fromJson
import vip.mystery0.tools.factory.toJson
import vip.mystery0.tools.utils.copyToFile
import java.io.File
import java.util.*

object LocalAnimationDataSource : AnimationDateSource {
	private val historyService: HistoryService = HistoryServiceImpl

	override suspend fun queryAnimationByImage(file: File, filter: String?): Animation = withContext(Dispatchers.IO) {
		val animationHistory = historyService.queryHistoryByOriginPathAndFilter(file.absolutePath, filter)
		val history = animationHistory?.result?.fromJson<Animation>()
		if (history != null) {
			history.quota = -987654
			history.quota_ttl = -987654
			history
		} else {
			RemoteAnimationDataSource.queryAnimationByImage(file, filter)
		}
	}

	suspend fun queryByBase64(base64: String): Animation? = withContext(Dispatchers.IO) {
		val animationHistory = historyService.queryHistoryByBase64(base64)
		val history = animationHistory?.result?.fromJson<Animation>()
		if (history != null) {
			history.quota = -987654
			history.quota_ttl = -987654
			history
		} else {
			null
		}
	}

	suspend fun saveHistory(base64: String, file: File, filter: String?, animation: Animation) = withContext(Dispatchers.IO) {
		val animationHistory = AnimationHistory()
		animationHistory.originPath = file.absolutePath
		val saveFile = file.getCacheFile() ?: return@withContext
		file.copyToFile(saveFile)
		animationHistory.cachePath = saveFile.absolutePath
		animationHistory.base64 = base64
		animationHistory.result = animation.toJson()
		animationHistory.time = Calendar.getInstance().timeInMillis
		if (animation.docs.isNotEmpty())
			animationHistory.title = animation.docs[0].title_native
		else
			animationHistory.title = StringConstant.hint_no_result
		animationHistory.filter = filter
		historyService.saveHistory(animationHistory)
	}

	suspend fun queryAllHistory(): List<AnimationHistory> = withContext(Dispatchers.IO) {
		historyService.queryAllHistory()
	}

	suspend fun deleteHistory(animationHistory: AnimationHistory, listener: (Boolean) -> Unit) = withContext(Dispatchers.IO) {
		listener(historyService.delete(animationHistory) == 1)
	}
}
