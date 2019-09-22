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
import pw.janyo.whatanime.utils.FileUtil
import vip.mystery0.tools.factory.fromJson
import vip.mystery0.tools.factory.toJson
import vip.mystery0.tools.utils.copyToFile
import java.io.File
import java.util.*

object LocalAnimationDataSource : AnimationDateSource {
	private val historyService: HistoryService = HistoryServiceImpl

	override suspend fun queryAnimationByImage(file: File, filter: String?): Animation {
		return withContext(Dispatchers.IO) {
			val animationHistory = historyService.queryHistoryByOriginPathAndFilter(file.absolutePath, filter)
			animationHistory?.result?.fromJson()
					?: RemoteAnimationDataSource.queryAnimationByImage(file, filter)
		}
	}

	suspend fun saveHistory(file: File, filter: String?, animation: Animation) {
		withContext(Dispatchers.IO) {
			val animationHistory = AnimationHistory()
			animationHistory.originPath = file.absolutePath
			val saveFile = FileUtil.getCacheFile(file) ?: return@withContext
			file.copyToFile(saveFile)
			animationHistory.cachePath = saveFile.absolutePath
			animationHistory.result = animation.toJson()
			animationHistory.time = Calendar.getInstance().timeInMillis
			if (animation.docs.isNotEmpty())
				animationHistory.title = animation.docs[0].title_native
			else
				animationHistory.title = StringConstant.hint_no_result
			animationHistory.filter = filter
			historyService.saveHistory(animationHistory)
		}
	}

	suspend fun queryAllHistory(): List<AnimationHistory> {
		return withContext(Dispatchers.IO) {
			historyService.queryAllHistory()
		}
	}

	suspend fun deleteHistory(animationHistory: AnimationHistory, listener: (Boolean) -> Unit) {
		withContext(Dispatchers.IO) {
			listener(historyService.delete(animationHistory) == 1)
		}
	}
}
