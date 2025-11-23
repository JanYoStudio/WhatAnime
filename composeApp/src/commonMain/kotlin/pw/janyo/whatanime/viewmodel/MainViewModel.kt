package pw.janyo.whatanime.viewmodel

import androidx.lifecycle.viewModelScope
import chaintech.videoplayer.host.MediaPlayerHost
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.core.component.inject
import pw.janyo.whatanime.Configure
import pw.janyo.whatanime.base.ComposeViewModel
import pw.janyo.whatanime.model.SearchAnimeResultItem
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.repository.AnimationRepository
import pw.janyo.whatanime.ui.components.PlayerState
import pw.janyo.whatanime.utils.getCacheFile
import pw.janyo.whatanime.utils.getMimeType
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.hint_cache_make_dir_error
import whatanime.composeapp.generated.resources.hint_file_too_large
import whatanime.composeapp.generated.resources.hint_no_result
import whatanime.composeapp.generated.resources.hint_select_file_path_null
import whatanime.composeapp.generated.resources.hint_unknown_error

class MainViewModel : ComposeViewModel() {
    private val animationRepository by inject<AnimationRepository>()
    private val mediaPlayerHost by inject<MediaPlayerHost>()
    private val playerState by inject<PlayerState>()

    private val _searchQuota = MutableStateFlow(SearchQuota.EMPTY)
    val searchQuota: StateFlow<SearchQuota> = _searchQuota

    private val _listState = MutableStateFlow(MainListState())
    val listState: StateFlow<MainListState> = _listState

    private val _cutBorders = MutableStateFlow(Configure.cutBorders)
    val cutBorders: StateFlow<Boolean> = _cutBorders

    fun showQuota() {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Logger.w("showQuota: failed", throwable)
            _searchQuota.value = SearchQuota.EMPTY
        }) {
            _searchQuota.value = animationRepository.showQuota()
        }
    }

    fun searchImageFile(imageFile: PlatformFile) {
        viewModelScope.launch(CoroutineExceptionHandler { context, throwable ->
            Logger.w("searchImageFile: failed", throwable)
            _listState.value = _listState.value.copy(
                loading = false,
                errorMessage = throwable.message ?: Res.string.hint_unknown_error.string()
            )
        }) {
            _listState.value = _listState.value.copy(
                loading = true,
                searchImageFile = imageFile
            )
            //开始搜索图片
            if (imageFile.size() > 26214400L) {
                //大于25M，提示文件过大
                _listState.value = _listState.value.copy(
                    loading = false,
                    errorMessage = getString(Res.string.hint_file_too_large)
                )
                return@launch
            }
            //解析缓存路径，将原图片写一份到缓存目录中，避免原图片被删除
            var cachePath =
                animationRepository.queryHistoryByOriginPath(imageFile.absolutePath())?.cachePath
            if (cachePath == null) {
                val cacheFile = getCacheFile(imageFile)
                if (cacheFile == null) {
                    _listState.value = _listState.value.copy(
                        loading = false,
                        errorMessage = getString(Res.string.hint_cache_make_dir_error)
                    )
                    return@launch
                }
                if (cacheFile.exists()) {
                    cacheFile.delete()
                }
                imageFile.copyTo(cacheFile)
                cachePath = cacheFile.absolutePath()
            }
            val mimeType = getMimeType(imageFile.extension)
                ?: throw RuntimeException(getString(Res.string.hint_select_file_path_null))
            val animation = animationRepository.queryAnimationByImageLocal(
                imageFile, imageFile.absolutePath(), cachePath, mimeType,
            )
            val result = if (Configure.hideSex) {
                animation.result.filter { !it.aniList.adult }
            } else {
                animation.result
            }
            if (result.isEmpty()) {
                _listState.value = _listState.value.copy(
                    loading = false,
                    searchImageFile = PlatformFile(cachePath),
                    tokenExpired = false,
                    errorMessage = getString(Res.string.hint_no_result)
                )
                return@launch
            }
            _listState.value = _listState.value.copy(
                loading = false,
                searchImageFile = PlatformFile(cachePath),
                tokenExpired = false,
                list = result,
                errorMessage = "",
            )
        }
    }

    fun playVideo(result: SearchAnimeResultItem) {
        viewModelScope.launch {
            val requestUrl: String = if (result.video.contains("?")) {
                "${result.video}&size=l"
            } else {
                "${result.video}?size=l"
            }
            mediaPlayerHost.loadUrl(requestUrl)
            playerState.loadUrl()
            mediaPlayerHost.play()
        }
    }

    fun changeCutBorders() {
        viewModelScope.launch {
            Configure.cutBorders = !_cutBorders.value
            _cutBorders.value = !_cutBorders.value
        }
    }

    override fun onCleared() {
        viewModelScope.cancel()
        playerState.release()
        super.onCleared()
    }
}

data class MainListState(
    val loading: Boolean = false,
    val searchImageFile: PlatformFile? = null,
    val tokenExpired: Boolean = false,
    val list: List<SearchAnimeResultItem> = emptyList(),
    val errorMessage: String = "",
)