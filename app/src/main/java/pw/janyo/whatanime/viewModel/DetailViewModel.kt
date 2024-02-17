package pw.janyo.whatanime.viewModel

import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
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
            val pair = withContext(Dispatchers.IO) {
                animationRepository.getByHistoryId(historyId)
            }
            val result = pair.first
            if (result == null) {
                _listState.value = _listState.value.copy(
                    loading = false,
                    searchImageFile = cacheFile,
                    tokenExpired = false,
                    errorMessage = R.string.hint_no_result.resString()
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
                tokenExpired = pair.second + 1000 * 60 * 10 < System.currentTimeMillis(),
                list = list,
                errorMessage = "",
            )
        }
    }

    /**
     * 播放视频
     */
    @androidx.annotation.OptIn(UnstableApi::class)
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