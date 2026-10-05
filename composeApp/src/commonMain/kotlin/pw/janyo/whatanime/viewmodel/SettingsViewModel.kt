package pw.janyo.whatanime.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import org.jetbrains.compose.resources.getString
import pw.janyo.whatanime.Configure
import pw.janyo.whatanime.base.ComposeViewModel
import pw.janyo.whatanime.httpResponses
import pw.janyo.whatanime.model.DebugHttpInfo
import pw.janyo.whatanime.model.SearchPreferences
import pw.janyo.whatanime.repository.AnimationRepository
import pw.janyo.whatanime.ui.theme.NightMode
import pw.janyo.whatanime.ui.theme.Theme
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.hint_unknown_error

class SettingsViewModel : ComposeViewModel() {
    private val animationRepository: AnimationRepository by inject()
    private val searchPreferences: SearchPreferences by inject()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _cutBorders = MutableStateFlow(Configure.cutBorders)
    val cutBorders: StateFlow<Boolean> = _cutBorders

    private val _hideSex = MutableStateFlow(Configure.hideSex)
    val hideSex: StateFlow<Boolean> = _hideSex

    private val _preferWebp = MutableStateFlow(Configure.preferWebp)
    val preferWebp: StateFlow<Boolean> = _preferWebp

    private val _debugMode = MutableStateFlow(Configure.debugMode)
    val debugMode: StateFlow<Boolean> = _debugMode

    private val _nightMode = MutableStateFlow(Configure.nightMode)
    val nightMode: StateFlow<NightMode> = _nightMode

    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey

    private val quotaSession = QuotaSession()
    val quotaState = quotaSession.state
    private val _hasApiKey = MutableStateFlow(Configure.apiKey.isNotBlank())
    val hasApiKey: StateFlow<Boolean> = _hasApiKey
    private var initialized = false

    val httpResponsesFlow: StateFlow<List<DebugHttpInfo>> = combine(httpResponses.entries, debugMode) { entries, enabled ->
        if (enabled) entries else emptyList()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun acknowledgeError() { _errorMessage.value = "" }

    fun init() {
        if (initialized) return
        initialized = true
        viewModelScope.launch {
            Theme.nightMode.value = Configure.nightMode
            _customApiKey.value = Configure.apiKey
        }
        refreshQuota()
    }

    fun setCutBorders(value: Boolean) {
        viewModelScope.launch {
            Configure.cutBorders = value
            _cutBorders.value = value
            searchPreferences.setCutBorders(value)
        }
    }

    fun setHideSex(hideSex: Boolean) {
        viewModelScope.launch {
            Configure.hideSex = hideSex
            _hideSex.value = hideSex
            searchPreferences.setHideAdult(hideSex)
        }
    }

    fun setDebugMode(debugMode: Boolean) {
        viewModelScope.launch {
            Configure.debugMode = debugMode
            _debugMode.value = debugMode
        }
    }

    fun setPreferWebp(preferWebp: Boolean) {
        viewModelScope.launch {
            Configure.preferWebp = preferWebp
            _preferWebp.value = preferWebp
            searchPreferences.setPreferWebp(preferWebp)
        }
    }

    fun setNightMode(nightMode: NightMode) {
        viewModelScope.launch {
            Configure.nightMode = nightMode
            _nightMode.value = nightMode
            Theme.nightMode.value = nightMode
        }
    }

    suspend fun saveApiKey(customApiKey: String): Boolean {
        return try {
            Configure.apiKey = customApiKey
            _customApiKey.value = customApiKey
            _hasApiKey.value = customApiKey.isNotBlank()
            _errorMessage.value = ""
            refreshQuota(credentialChanged = true)
            true
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            _errorMessage.value = getString(Res.string.hint_unknown_error)
            false
        }
    }

    fun setCustomApiKey(customApiKey: String) {
        viewModelScope.launch { saveApiKey(customApiKey) }
    }

    fun refreshQuota(credentialChanged: Boolean = false) {
        val requestId = quotaSession.begin(credentialChanged) ?: return
        val apiKey = Configure.apiKey
        viewModelScope.launch {
            try {
                quotaSession.complete(requestId, animationRepository.showQuota(apiKey))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                quotaSession.fail(requestId)
            }
        }
    }
}