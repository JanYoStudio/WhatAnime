package pw.janyo.whatanime.viewModel

import androidx.lifecycle.MutableLiveData
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pw.janyo.whatanime.base.ComposeViewModel
import pw.janyo.whatanime.constant.StringConstant
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.repository.AnimationRepository

class HistoryViewModel : ComposeViewModel(), KoinComponent {
    private val animationRepository: AnimationRepository by inject()

    val historyList = MutableLiveData<List<AnimationHistory>?>(null)

    fun refresh() {
        launchLoadData {
            historyList.postValue(animationRepository.queryAllHistory())
        }
    }

    fun deleteHistory(list: MutableList<Int>) {
        launch {
//            list.forEach {
//                animationRepository.deleteHistory(it)
//            }
            list.clear()
            errorMessageState(StringConstant.hint_history_delete_done)
            refreshState(true)
            historyList.postValue(animationRepository.queryAllHistory())
            refreshState(false)
        }
    }
}