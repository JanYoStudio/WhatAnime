package pw.janyo.whatanime.viewModel

import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.ComposeViewModel
import pw.janyo.whatanime.constant.StringConstant.resString
import pw.janyo.whatanime.model.SearchAnimeResultItem
import pw.janyo.whatanime.repository.AnimationRepository
import pw.janyo.whatanime.trackEvent
import pw.janyo.whatanime.utils.firstNotNull
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class DetailViewModel : ComposeViewModel() {
    private val animationRepository: AnimationRepository by inject()
    private val exoDataSourceFactory: DataSource.Factory by inject()

    private val mediaSourceMap = ConcurrentHashMap<String, MediaSource>()

    private val _listState = MutableStateFlow(MainListState())
    val listState: StateFlow<MainListState> = _listState

    private val _playLoading = MutableStateFlow(false)
    val playLoading: StateFlow<Boolean> = _playLoading

    private val _playMediaSource = MutableStateFlow<MediaSource?>(null)
    val playMediaSource: StateFlow<MediaSource?> = _playMediaSource

    private val _showFloatDialog = MutableStateFlow(false)
    val showFloatDialog: StateFlow<Boolean> = _showFloatDialog

    fun loadPlaying(loading: Boolean) {
        _playLoading.value = loading
    }

    fun playDone() {
        _playLoading.value = false
        _playMediaSource.value = null
    }

    fun playError(error: PlaybackException) {
        val errorMessage = firstNotNull(
            R.string.hint_unknow_error.resString(),
            error.cause?.message,
            error.message,
        )
        _playLoading.value = false
        _listState.value = _listState.value.copy(
            loading = false,
            errorMessage = errorMessage
        )
    }

    fun loadHistoryDetail(historyId: Int, cacheFile: File) {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(loading = true)
            val result = withContext(Dispatchers.IO) {
                animationRepository.getByHistoryId(historyId)
            }
            if (result == null) {
                _listState.value = _listState.value.copy(
                    loading = false,
                    searchImageFile = cacheFile,
                    errorMessage = R.string.hint_no_result.resString()
                )
                return@launch
            }
            _listState.value = _listState.value.copy(
                loading = false,
                searchImageFile = cacheFile,
                list = result.result,
                errorMessage = "",
            )
        }
    }

    /**
     * 播放视频
     */
    fun playVideo(result: SearchAnimeResultItem) {
        viewModelScope.launch {
            val requestUrl = "${result.video}&size=l"
            trackEvent("play video", mapOf("url" to requestUrl))
            _playMediaSource.value = mediaSourceMap.getOrPut(requestUrl) {
                ProgressiveMediaSource.Factory(exoDataSourceFactory)
                    .createMediaSource(
                        MediaItem.Builder()
                            .setUri(requestUrl)
                            .build()
                    )
            }
            _playLoading.value = true
        }
    }

    fun changeFloatDialogVisibility() {
        _showFloatDialog.value = !_showFloatDialog.value
    }
}