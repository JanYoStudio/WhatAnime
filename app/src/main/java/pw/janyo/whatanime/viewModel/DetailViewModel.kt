package pw.janyo.whatanime.viewModel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.ComposeViewModel
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.constant.StringConstant.resString
import pw.janyo.whatanime.model.SearchAnimeResultItem
import pw.janyo.whatanime.repository.AnimationRepository
import pw.janyo.whatanime.trackEvent
import pw.janyo.whatanime.utils.firstNotNull
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class DetailViewModel : ComposeViewModel() {
    companion object {
        private const val TAG = "DetailViewModel"
    }

    private val animationRepository: AnimationRepository by inject()
    private val exoDataSourceFactory: DataSource.Factory by inject()

    private val mediaSourceMap = ConcurrentHashMap<String, MediaSource>()

    private val _listState = MutableStateFlow(MainListState())
    val listState: StateFlow<MainListState> = _listState

    private val _showChineseTitle = MutableStateFlow(Configure.showChineseTitle)
    val showChineseTitle: StateFlow<Boolean> = _showChineseTitle

    private val _playLoading = MutableStateFlow(false)
    val playLoading: StateFlow<Boolean> = _playLoading

    private val _playMediaSource = MutableStateFlow<MediaSource?>(null)
    val playMediaSource: StateFlow<MediaSource?> = _playMediaSource

    fun loadPlaying(loading: Boolean) {
        _playLoading.value = loading
    }

    fun playDone() {
        _playLoading.value = false
        _playMediaSource.value = null
    }

    fun playError(error: PlaybackException) {
        val errorMessage =
            if (error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS && error.cause is HttpDataSource.InvalidResponseCodeException) {
                when (val code =
                    (error.cause as HttpDataSource.InvalidResponseCodeException).responseCode) {
                    400 -> R.string.video_play_hint_400.resString()
                    403 -> R.string.video_play_hint_403.resString()
                    404 -> R.string.video_play_hint_404.resString()
                    410 -> R.string.video_play_hint_410.resString()
                    else -> {
                        if (code >= 500)
                            R.string.video_play_hint_500.resString()
                        else
                            R.string.video_play_hint_unknown.resString()
                    }
                }
            } else {
                firstNotNull(
                    R.string.video_play_hint_unknown.resString(),
                    error.cause?.message,
                    error.message,
                )
            }
        loadPlaying(false)
        playDone()
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
}