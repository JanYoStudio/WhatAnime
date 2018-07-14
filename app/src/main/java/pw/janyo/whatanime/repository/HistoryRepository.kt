package pw.janyo.whatanime.repository

import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.repository.local.LocalAnimationDataSource
import pw.janyo.whatanime.utils.FileUtil
import pw.janyo.whatanime.viewModel.HistoryViewModel
import java.io.File

object HistoryRepository {
	fun loadHistory(historyViewModel: HistoryViewModel) {
		LocalAnimationDataSource.queryAllHistory(historyViewModel.historyList, historyViewModel.message)
	}

	fun deleteHistory(animationHistory: AnimationHistory, historyViewModel: HistoryViewModel) {
		LocalAnimationDataSource.deleteHistory(animationHistory, historyViewModel.historyList, historyViewModel.message)
		val savedFile = FileUtil.getCacheFile(File(animationHistory.cachePath))
		if (savedFile != null && savedFile.exists())
			savedFile.delete()
	}
}