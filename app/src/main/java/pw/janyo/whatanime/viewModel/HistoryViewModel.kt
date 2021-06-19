package pw.janyo.whatanime.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.repository.AnimationRepository
import pw.janyo.whatanime.utils.getCacheFile
import vip.mystery0.rx.*
import java.io.File

class HistoryViewModel : ViewModel(), KoinComponent {
    private val animationRepository: AnimationRepository by inject()

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
            Logger.wtf("deleteHistory: ", throwable)
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