package pw.janyo.whatanime.viewModel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.ComposeViewModel
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.constant.StringConstant.resString
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.repository.AnimationRepository

class HistoryViewModel : ComposeViewModel() {
    private val animationRepository: AnimationRepository by inject()

    private val _historyListState = MutableStateFlow(HistoryListState())
    val historyListState: StateFlow<HistoryListState> = _historyListState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _historyListState.value = _historyListState.value.copy(
                loading = true,
                errorMessage = "",
            )
            val list = animationRepository.queryAllHistory()
            if (list.isEmpty()) {
                _historyListState.value = _historyListState.value.copy(
                    loading = false,
                    errorMessage = R.string.hint_no_result.resString(),
                )
            } else {
                _historyListState.value = _historyListState.value.copy(
                    loading = false,
                    list = list,
                )
            }
        }
    }

    fun deleteHistory(list: MutableList<Int>) {
        viewModelScope.launch {
            _historyListState.value = _historyListState.value.copy(
                loading = true,
                errorMessage = "",
            )
            list.forEach {
                animationRepository.deleteHistory(it)
            }
            list.clear()
            val historyList = animationRepository.queryAllHistory()
            _historyListState.value = _historyListState.value.copy(
                loading = false,
                list = historyList,
                errorMessage = "",
            )
        }
    }
}

data class HistoryListState(
    val loading: Boolean = false,
    val list: List<AnimationHistory> = emptyList(),
    val errorMessage: String = "",
)