package pw.janyo.whatanime.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pw.janyo.whatanime.base.ComposeViewModel
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.repository.AnimationRepository
import pw.janyo.whatanime.utils.getCacheFile
import java.io.File

class HistoryViewModel : ComposeViewModel(), KoinComponent {
    private val animationRepository: AnimationRepository by inject()

    val historyList = MutableLiveData<List<AnimationHistory>?>(null)

    fun refresh() {
        launchLoadData {
            historyList.postValue(animationRepository.queryAllHistory())
        }
    }

    fun deleteHistory(animationHistory: AnimationHistory, listener: (Boolean) -> Unit) {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Logger.e(throwable, "deleteHistory failed")
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