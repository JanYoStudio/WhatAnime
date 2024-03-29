package pw.janyo.whatanime.viewModel

import android.content.Intent
import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import kotlinx.coroutines.CoroutineExceptionHandler
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
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.repository.AnimationRepository
import pw.janyo.whatanime.trackEvent
import pw.janyo.whatanime.utils.cloneUriToFile
import pw.janyo.whatanime.utils.firstNotNull
import pw.janyo.whatanime.utils.getCacheFile
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class MainViewModel : ComposeViewModel() {
    companion object {
        private const val TAG = "MainViewModel"
    }

    private val animationRepository: AnimationRepository by inject()
    private val exoDataSourceFactory: DataSource.Factory by inject()

    private val mediaSourceMap = ConcurrentHashMap<String, MediaSource>()

    private val _searchQuota = MutableStateFlow(SearchQuota.EMPTY)
    val searchQuota: StateFlow<SearchQuota> = _searchQuota

    private val _listState = MutableStateFlow(MainListState())
    val listState: StateFlow<MainListState> = _listState

    private val _showChineseTitle = MutableStateFlow(Configure.showChineseTitle)
    val showChineseTitle: StateFlow<Boolean> = _showChineseTitle

    private val _cutBorders = MutableStateFlow(Configure.cutBorders)
    val cutBorders: StateFlow<Boolean> = _cutBorders

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

    fun showQuota() {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Log.w(TAG, "showQuota: failed", throwable)
            _searchQuota.value = SearchQuota.EMPTY
        }) {
            _searchQuota.value = animationRepository.showQuota()
        }
    }

    fun searchImageFile(data: Intent, mimeType: String) {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Log.w(TAG, "searchImageFile: failed", throwable)
            _listState.value = _listState.value.copy(
                loading = false,
                errorMessage = throwable.message ?: R.string.hint_unknown_error.resString()
            )
        }) {
            _listState.value = _listState.value.copy(
                loading = true,
                errorMessage = "",
            )
            //将文件写入缓存目录，用于后续操作
            val file = data.data!!.cloneUriToFile()
            if (file == null || !file.exists()) {
                _listState.value = _listState.value.copy(
                    loading = false,
                    errorMessage = R.string.hint_select_file_path_null.resString()
                )
                return@launch
            }
            //开始搜索图片
            if (file.length() > 26214400L) {
                //大于25M，提示文件过大
                _listState.value = _listState.value.copy(
                    loading = false,
                    errorMessage = R.string.hint_file_too_large.resString()
                )
                return@launch
            }
            //解析缓存路径，将原图片写一份到缓存目录中，避免原图片被删除
            var cachePath =
                animationRepository.queryHistoryByOriginPath(file.absolutePath)?.cachePath
            if (cachePath == null) {
                val cacheFile = file.getCacheFile()
                if (cacheFile == null) {
                    _listState.value = _listState.value.copy(
                        loading = false,
                        errorMessage = R.string.hint_cache_make_dir_error.resString()
                    )
                    return@launch
                }
                withContext(Dispatchers.IO) {
                    if (cacheFile.exists()) {
                        cacheFile.delete()
                    }
                    file.copyTo(cacheFile)
                }
                cachePath = cacheFile.absolutePath
            }
            val animation = animationRepository.queryAnimationByImageLocal(
                file, file.absolutePath, cachePath!!, mimeType,
            )
            val result = if (Configure.hideSex) {
                animation.result.filter { !it.aniList.adult }
            } else {
                animation.result
            }
            val cacheFile = File(cachePath)
            if (result.isEmpty()) {
                _listState.value = _listState.value.copy(
                    loading = false,
                    searchImageFile = cacheFile,
                    tokenExpired = false,
                    errorMessage = R.string.hint_no_result.resString()
                )
                return@launch
            }
            _listState.value = _listState.value.copy(
                loading = false,
                searchImageFile = cacheFile,
                tokenExpired = false,
                list = result,
                errorMessage = "",
            )
        }
    }

    /**
     * 播放视频
     */
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
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

    fun changeCutBorders() {
        viewModelScope.launch {
            Configure.cutBorders = !_cutBorders.value
            _cutBorders.value = !_cutBorders.value
        }
    }
}

data class MainListState(
    val loading: Boolean = false,
    val searchImageFile: File? = null,
    val tokenExpired: Boolean = false,
    val list: List<SearchAnimeResultItem> = emptyList(),
    val errorMessage: String = "",
)