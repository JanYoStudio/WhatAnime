package pw.janyo.whatanime.viewmodel

import androidx.lifecycle.viewModelScope
import chaintech.videoplayer.host.MediaPlayerHost
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.core.component.inject
import pw.janyo.whatanime.Configure
import pw.janyo.whatanime.base.ComposeViewModel
import pw.janyo.whatanime.model.SearchAnimeResultItem
import pw.janyo.whatanime.repository.AnimationRepository
import pw.janyo.whatanime.ui.components.PlayerState
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.hint_no_result
import kotlin.time.Clock

class DetailViewModel : ComposeViewModel() {
    private val animationRepository by inject<AnimationRepository>()
    private val mediaPlayerHost by inject<MediaPlayerHost>()
    private val playerState by inject<PlayerState>()

    private val _listState = MutableStateFlow(MainListState())
    val listState: StateFlow<MainListState> = _listState

    fun loadHistoryDetail(historyId: Int, cacheFile: PlatformFile) {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(loading = true)
            val pair = animationRepository.getByHistoryId(historyId)
            val result = pair.first
            if (result == null) {
                _listState.value = _listState.value.copy(
                    loading = false,
                    searchImageFile = cacheFile,
                    tokenExpired = false,
                    errorMessage = getString(Res.string.hint_no_result)
                )
                return@launch
            }
            val list = if (Configure.hideSex) {
                result.result.filter { !it.aniList.adult }
            } else {
                result.result
            }
            _listState.value = _listState.value.copy(
                loading = false,
                searchImageFile = cacheFile,
                tokenExpired = pair.second + 1000 * 60 * 10 < Clock.System.now()
                    .toEpochMilliseconds(),
                list = list,
                errorMessage = "",
            )
        }
    }

    fun playVideo(result: SearchAnimeResultItem) {
        viewModelScope.launch {
            val requestUrl = "${result.video}&size=l"
            mediaPlayerHost.loadUrl(requestUrl)
            playerState.loadUrl()
            mediaPlayerHost.play()
        }
    }

    override fun onCleared() {
        viewModelScope.cancel()
        playerState.release()
        super.onCleared()
    }
}