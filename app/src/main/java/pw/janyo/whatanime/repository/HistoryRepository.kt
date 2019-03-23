package pw.janyo.whatanime.repository

import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.repository.local.LocalAnimationDataSource
import pw.janyo.whatanime.utils.FileUtil
import pw.janyo.whatanime.viewModel.HistoryViewModel
import vip.mystery0.rx.PackageData
import java.io.File

object HistoryRepository {
	fun loadHistory(historyViewModel: HistoryViewModel) {
		historyViewModel.historyList.value= PackageData.loading()
		LocalAnimationDataSource.queryAllHistory(historyViewModel.historyList)
	}

	fun deleteHistory(animationHistory: AnimationHistory, listener: (Boolean) -> Unit) {
		LocalAnimationDataSource.deleteHistory(animationHistory, listener)
		val savedFile = FileUtil.getCacheFile(File(animationHistory.cachePath))
		if (savedFile != null && savedFile.exists())
			savedFile.delete()
	}
}