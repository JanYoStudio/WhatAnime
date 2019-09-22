package pw.janyo.whatanime.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.repository.local.LocalAnimationDataSource
import pw.janyo.whatanime.utils.FileUtil
import pw.janyo.whatanime.viewModel.HistoryViewModel
import vip.mystery0.logs.Logs
import vip.mystery0.rx.PackageData
import vip.mystery0.rx.content
import vip.mystery0.rx.dealWith
import vip.mystery0.rx.empty
import vip.mystery0.tools.doByTry
import java.io.File

object HistoryRepository {
	fun loadHistory(historyViewModel: HistoryViewModel) {
		historyViewModel.historyList.value = PackageData.loading()
		doByTry {
			GlobalScope.launch(Dispatchers.Main) {
				val list = LocalAnimationDataSource.queryAllHistory()
				if (list.isNullOrEmpty()) {
					historyViewModel.historyList.empty()
				} else {
					historyViewModel.historyList.content(list)
				}
			}
		}.dealWith(historyViewModel.historyList)
	}

	fun deleteHistory(animationHistory: AnimationHistory, listener: (Boolean) -> Unit) {
		val pair = doByTry {
			GlobalScope.launch(Dispatchers.Main) {
				LocalAnimationDataSource.deleteHistory(animationHistory, listener)
				val savedFile = FileUtil.getCacheFile(File(animationHistory.cachePath))
				if (savedFile != null && savedFile.exists())
					savedFile.delete()
			}
			listener(true)
		}
		if (pair.second != null) {
			Logs.wtf("deleteHistory: ", pair.second)
			listener(false)
		}
	}
}