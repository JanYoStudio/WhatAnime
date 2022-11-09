package pw.janyo.whatanime.viewModel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.ComposeViewModel
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.constant.StringConstant.resString
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.repository.AnimationRepository

class SettingsViewModel : ComposeViewModel() {
    private val animationRepository: AnimationRepository by inject()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _hideSex = MutableStateFlow(Configure.hideSex)
    val hideSex: StateFlow<Boolean> = _hideSex

    private val _showChineseTitle = MutableStateFlow(Configure.showChineseTitle)
    val showChineseTitle: StateFlow<Boolean> = _showChineseTitle

    private val _allowSendCrashReport = MutableStateFlow(Configure.allowSendCrashReport)
    val allowSendCrashReport: StateFlow<Boolean> = _allowSendCrashReport

    private val _searchQuota = MutableStateFlow(SearchQuota.EMPTY)
    val searchQuota: StateFlow<SearchQuota> = _searchQuota

    init {
        showQuota()
    }

    fun setHideSex(hideSex: Boolean) {
        viewModelScope.launch {
            Configure.hideSex = hideSex
            _hideSex.value = hideSex
        }
    }

    fun setShowChineseTitle(showChineseTitle: Boolean) {
        viewModelScope.launch {
            Configure.showChineseTitle = showChineseTitle
            _showChineseTitle.value = showChineseTitle
        }
    }

    fun setAllowSendCrashReport(allowSendCrashReport: Boolean) {
        viewModelScope.launch {
            Configure.allowSendCrashReport = allowSendCrashReport
            _allowSendCrashReport.value = allowSendCrashReport
        }
    }

    fun showQuota() {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            _errorMessage.value = throwable.message ?: R.string.hint_unknown_error.resString()
        }) {
            _searchQuota.value = animationRepository.showQuota()
        }
    }
}