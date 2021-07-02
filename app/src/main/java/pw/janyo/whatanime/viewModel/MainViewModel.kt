package pw.janyo.whatanime.viewModel

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pw.janyo.whatanime.base.ComposeViewModel
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.config.connectServer
import pw.janyo.whatanime.config.trackEvent
import pw.janyo.whatanime.constant.StringConstant
import pw.janyo.whatanime.model.Result
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.model.ShowImage
import pw.janyo.whatanime.repository.AnimationRepository
import pw.janyo.whatanime.utils.cloneUriToFile
import pw.janyo.whatanime.utils.getCacheFile
import vip.mystery0.tools.utils.copyToFile
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class MainViewModel : ComposeViewModel(), KoinComponent {
    private val animationRepository: AnimationRepository by inject()
    private val exoDataSourceFactory: DataSource.Factory by inject()

    private val mediaSourceMap = ConcurrentHashMap<String, MediaSource>()
    val quota = MutableLiveData<SearchQuota>()
    val imageFile = MutableLiveData<ShowImage>()
    val resultList = MutableLiveData<List<Result>>()
    val clickDocs = MutableLiveData<Result>()
    val mediaSource = MutableLiveData<MediaSource>()
    val loadingVideo = MutableLiveData(false)

    /**
     * @param file 要显示的文件，也是要搜索的文件
     * @param filter 过滤条件
     * @param cacheInPath 缓存路径，如果为null说明没有缓存或者不知道缓存路径
     * @param originPath 原始路径
     * @param mimeType 文件类型
     */
    fun search(
        file: File,
        cacheInPath: String?,
        originPath: String,
        mimeType: String,
        connectServer: Boolean
    ) {
        launchLoadData {
            if (file.length() > 10485760L) {
                //大于10M，提示文件过大
                throw Exception(StringConstant.hint_file_too_large)
            }
            var cachePath = cacheInPath ?: //没有缓存或者不知道缓存
            animationRepository.queryHistoryByOriginPath(originPath)?.cachePath
            if (cachePath == null) {
                val saveFile = file.getCacheFile()
                    ?: throw Exception(StringConstant.hint_cache_make_dir_error)
                file.copyToFile(saveFile)
                cachePath = saveFile.absolutePath
            }
            val animation = animationRepository.queryAnimationByImageLocal(
                file,
                originPath,
                cachePath!!,
                mimeType,
                connectServer
            )
            val result = if (Configure.hideSex) {
                animation.result.filter { !it.anilist.isAdult }
            } else {
                animation.result
            }
            resultList.postValue(result)
        }
    }

    fun showQuota() {
        launch {
            quota.postValue(animationRepository.showQuota())
        }
    }

    /**
     * 从Intent中获取Uri，将其clone到临时目录
     */
    fun parseImageFile(data: Intent, mimeType: String) {
        launch {
            refreshState(true)
            val file = data.data!!.cloneUriToFile()
            if (file != null && file.exists()) {
                val showImage = ShowImage()
                showImage.mimeType = mimeType
                showImage.originPath = file.absolutePath
                showImage.cachePath = null
                imageFile.postValue(showImage)
            } else {
                throw Exception(StringConstant.hint_select_file_path_null)
            }
            //搜索图片
            search(
                file,
                null,
                file.absolutePath,
                mimeType,
                connectServer
            )
        }
    }

    /**
     * 播放视频
     */
    fun playVideo(result: Result) {
        launch {
            val requestUrl = "${result.video}&size=l"
            trackEvent("play video", mapOf("url" to requestUrl))
            val newMediaSource = mediaSourceMap.getOrPut(requestUrl) {
                val source = MediaItem.Builder()
                    .setUri(requestUrl)
                    .build()
                ProgressiveMediaSource.Factory(exoDataSourceFactory)
                    .createMediaSource(source)
            }
            mediaSource.postValue(newMediaSource)
            loadingVideo.postValue(true)
        }
    }
}