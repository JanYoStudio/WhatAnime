package pw.janyo.whatanime.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.repository.AnimationRepository
import pw.janyo.whatanime.utils.getCacheFile
import vip.mystery0.logs.Logs
import vip.mystery0.rx.*
import java.io.File

class HistoryViewModel(
		private val animationRepository: AnimationRepository
) : ViewModel() {
	var historyList = MutableLiveData<PackageData<List<AnimationHistory>>>()

	fun loadHistory() {
		historyList.loading()
		launch(historyList) {
			val list = animationRepository.queryAllHistory()
			if (list.isNullOrEmpty()) {
				historyList.empty()
			} else {
				historyList.content(list)
			}
		}
	}

	fun deleteHistory(animationHistory: AnimationHistory, listener: (Boolean) -> Unit) {
		viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
			Logs.wtf("deleteHistory: ", throwable)
			listener(false)
		}) {
			animationRepository.deleteHistory(animationHistory, listener)
			val savedFile = File(animationHistory.cachePath).getCacheFile()
			if (savedFile != null && savedFile.exists())
				savedFile.delete()
		}
		listener(true)
	}
}