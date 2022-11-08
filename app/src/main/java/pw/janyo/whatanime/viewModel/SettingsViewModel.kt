package pw.janyo.whatanime.viewModel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import pw.janyo.whatanime.base.ComposeViewModel
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.repository.AnimationRepository

class SettingsViewModel : ComposeViewModel() {
    private val animationRepository: AnimationRepository by inject()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _hideSex = MutableStateFlow(Configure.hideSex)
    val hideSex: StateFlow<Boolean> = _hideSex

    private val _searchQuota = MutableStateFlow(SearchQuota.EMPTY)
    val searchQuota: StateFlow<SearchQuota> = _searchQuota

    fun setHideSex(hideSex: Boolean) {
        viewModelScope.launch {
            Configure.hideSex = hideSex
            _hideSex.value = hideSex
        }
    }

    fun showQuota() {
        viewModelScope.launch {
            _searchQuota.value = animationRepository.showQuota()
        }
    }
}