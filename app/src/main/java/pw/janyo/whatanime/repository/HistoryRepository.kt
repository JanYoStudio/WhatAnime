package pw.janyo.whatanime.repository

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.repository.local.LocalAnimationDataSource
import pw.janyo.whatanime.utils.FileUtil
import pw.janyo.whatanime.viewModel.HistoryViewModel
import vip.mystery0.logs.Logs
import vip.mystery0.rx.PackageData
import vip.mystery0.rx.content
import vip.mystery0.rx.empty
import vip.mystery0.rx.error
import java.io.File

object HistoryRepository {
	fun loadHistory(historyViewModel: HistoryViewModel) {
		historyViewModel.historyList.value = PackageData.loading()
		GlobalScope.launch(CoroutineExceptionHandler { _, throwable -> historyViewModel.historyList.error(throwable) }) {
			val list = LocalAnimationDataSource.queryAllHistory()
			if (list.isNullOrEmpty()) {
				historyViewModel.historyList.empty()
			} else {
				historyViewModel.historyList.content(list)
			}
		}
	}

	fun deleteHistory(animationHistory: AnimationHistory, listener: (Boolean) -> Unit) {
		GlobalScope.launch(CoroutineExceptionHandler { _, throwable ->
			Logs.wtf("deleteHistory: ", throwable)
			listener(false)
		}) {
			LocalAnimationDataSource.deleteHistory(animationHistory, listener)
			val savedFile = FileUtil.getCacheFile(File(animationHistory.cachePath))
			if (savedFile != null && savedFile.exists())
				savedFile.delete()
		}
		listener(true)
	}
}