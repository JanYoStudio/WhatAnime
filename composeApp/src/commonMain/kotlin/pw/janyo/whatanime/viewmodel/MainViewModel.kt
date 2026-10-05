package pw.janyo.whatanime.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.core.component.inject
import pw.janyo.whatanime.base.ComposeViewModel
import pw.janyo.whatanime.model.SearchAnimeResultItem
import pw.janyo.whatanime.model.SearchPhase
import pw.janyo.whatanime.model.SearchPreferences
import pw.janyo.whatanime.repository.ImageSearchGateway
import pw.janyo.whatanime.repository.SearchFailure
import pw.janyo.whatanime.repository.SearchInputException
import whatanime.composeapp.generated.resources.*

class MainViewModel(
    private val gateway: ImageSearchGateway,
    private val preferences: SearchPreferences,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
    private val failureMessage: suspend (SearchFailure) -> String = { failure ->
        getString(when (failure) {
            SearchFailure.TooLarge -> Res.string.hint_file_too_large
            SearchFailure.FileUnavailable -> Res.string.hint_select_file_not_exist
            SearchFailure.CacheUnavailable -> Res.string.hint_cache_make_dir_error
            SearchFailure.SearchFailed -> Res.string.hint_search_error
        })
    },
) : ComposeViewModel() {
    private val playback by inject<PlaybackCoordinator>()
    private val session = SearchSession(preferences.hideAdult.value)
    val searchState = session.state
    private val mutableRestoration = MutableStateFlow(savedStateHandle.get<Boolean>("has_search") == true)
    val restorationNeedsSelection = mutableRestoration.asStateFlow()

    init {
        viewModelScope.launch { preferences.hideAdult.collect { session.setHideAdult(it) } }
    }

    fun searchImageFile(imageFile: PlatformFile) {
        val requestId = session.begin(imageFile) ?: return
        // 恢复只保留工作区曾存在的标记；不保存凭据、响应、带 Token 的地址或悬空 Loading。
        savedStateHandle["has_search"] = true
        mutableRestoration.value = false
        val cutBorders = preferences.cutBorders.value
        viewModelScope.launch {
            try {
                val response = gateway.search(imageFile, cutBorders)
                session.complete(requestId, response.image, response.results)
            } catch (e: CancellationException) {
                session.cancel(requestId)
                throw e
            } catch (e: SearchInputException) {
                session.reject(requestId, failureMessage(e.failure))
            } catch (_: Exception) {
                session.fail(requestId, failureMessage(SearchFailure.SearchFailed))
            }
        }
    }

    fun retry() {
        if (searchState.value.phase != SearchPhase.Error) return
        searchState.value.image?.let(::searchImageFile)
    }

    fun acknowledgeError(requestId: Long) = session.acknowledgeError(requestId)
    fun playVideo(result: SearchAnimeResultItem) { playback.play(result.video) }
}
