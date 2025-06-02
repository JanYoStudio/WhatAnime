package pw.janyo.whatanime.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.core.component.inject
import pw.janyo.whatanime.base.ComposeViewModel
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.repository.AnimationRepository
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.hint_no_result

class HistoryViewModel : ComposeViewModel() {
    private val animationRepository: AnimationRepository by inject()

    private val _historyListState = MutableStateFlow(HistoryListState())
    val historyListState: StateFlow<HistoryListState> = _historyListState

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
                    list = emptyList(),
                    errorMessage = getString(Res.string.hint_no_result),
                )
            } else {
                _historyListState.value = _historyListState.value.copy(
                    loading = false,
                    list = list,
                )
            }
        }
    }

    fun deleteHistory(historyId: Int) {
        viewModelScope.launch {
            _historyListState.value = _historyListState.value.copy(
                loading = true,
                errorMessage = "",
            )
            animationRepository.deleteHistory(historyId)
        }.invokeOnCompletion {
            refresh()
        }
    }
}

data class HistoryListState(
    val loading: Boolean = false,
    val list: List<AnimationHistory> = emptyList(),
    val errorMessage: String = "",
)