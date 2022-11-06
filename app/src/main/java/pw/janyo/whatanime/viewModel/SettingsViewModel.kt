package pw.janyo.whatanime.viewModel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import pw.janyo.whatanime.base.ComposeViewModel
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.repository.AnimationRepository

class SettingsViewModel : ComposeViewModel() {
    private val animationRepository: AnimationRepository by inject()

    private val _searchQuota = MutableStateFlow(SearchQuota.EMPTY)
    val searchQuota: StateFlow<SearchQuota> = _searchQuota

    fun showQuota() {
        viewModelScope.launch {
            _searchQuota.value = animationRepository.showQuota()
        }
    }
}