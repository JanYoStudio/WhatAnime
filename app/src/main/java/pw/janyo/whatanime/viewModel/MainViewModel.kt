package pw.janyo.whatanime.viewModel

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import org.koin.core.KoinComponent
import org.koin.core.inject
import pw.janyo.whatanime.R
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.model.Docs
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.model.ShowImage
import pw.janyo.whatanime.repository.AnimationRepository
import pw.janyo.whatanime.utils.cloneUriToFile
import pw.janyo.whatanime.utils.getCacheFile
import vip.mystery0.rx.PackageData
import vip.mystery0.rx.content
import vip.mystery0.rx.launch
import vip.mystery0.rx.loading
import vip.mystery0.tools.ResourceException
import vip.mystery0.tools.utils.copyToFile
import java.io.File
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

class MainViewModel : ViewModel(), KoinComponent {
	private val animationRepository: AnimationRepository by inject()
	private val exoDataSourceFactory: DataSource.Factory by inject()

	private val mediaSourceMap = ConcurrentHashMap<String, MediaSource>()
	val mediaSource = MutableLiveData<PackageData<MediaSource>>()
	private var nowPlayUrl: String = ""
	val imageFile = MutableLiveData<PackageData<ShowImage>>()
	val resultList = MutableLiveData<PackageData<List<Docs>>>()
	val isShowDetail = MutableLiveData<Boolean>()
	val quota = MutableLiveData<PackageData<SearchQuota>>()

	/**
	 * @param file 要显示的文件，也是要搜索的文件
	 * @param filter 过滤条件
	 * @param cacheInPath 缓存路径，如果为null说明没有缓存或者不知道缓存路径
	 * @param originPath 原始路径
	 * @param mimeType 文件类型
	 */
	fun search(file: File, filter: String?, cacheInPath: String?, originPath: String, mimeType: String, connectServer: Boolean) {
		resultList.loading()
		launch(resultList) {
			var cachePath = cacheInPath ?: //没有缓存或者不知道缓存
			animationRepository.queryHistoryByOriginPath(originPath, filter)?.cachePath
			if (cachePath == null) {
				val saveFile = file.getCacheFile()
						?: throw ResourceException(R.string.hint_cache_make_dir_error)
				file.copyToFile(saveFile)
				cachePath = file.absolutePath
			}
			val animation = animationRepository.queryAnimationByImageLocal(file, originPath, cachePath!!, mimeType, filter, connectServer)
			if (animation.quota != -987654 && animation.quota_ttl != -987654) {
				val searchQuota = SearchQuota()
				searchQuota.user_quota = animation.quota
				searchQuota.user_quota_ttl = animation.quota_ttl
				quota.content(searchQuota)
			}
			val result = if (Configure.hideSex) {
				animation.docs.filter { !it.is_adult }
			} else {
				animation.docs
			}
			resultList.content(result)
		}
	}

	fun showQuota() {
		launch(quota) {
			quota.content(animationRepository.showQuota())
		}
	}

	/**
	 * 从Intent中获取Uri，将其clone到临时目录
	 */
	fun parseImageFile(data: Intent, mimeType: String) {
		launch(imageFile) {
			val file = data.data!!.cloneUriToFile()
			if (file != null && file.exists()) {
				val showImage = ShowImage()
				showImage.mimeType = mimeType
				showImage.originPath = file.absolutePath
				showImage.cachePath = null
				imageFile.content(showImage)
			} else {
				throw ResourceException(R.string.hint_select_file_path_null)
			}
		}
	}

	/**
	 * 播放视频
	 */
	fun playVideo(docs: Docs) {
		launch(mediaSource) {
			val requestUrl = when (Configure.previewConfig) {
				1 -> "https://media.trace.moe/video/${docs.anilist_id}/${URLEncoder.encode(docs.filename, "UTF-8")}?t=${docs.at}&token=${docs.tokenthumb}"
				2 -> "https://media.trace.moe/video/${docs.anilist_id}/${URLEncoder.encode(docs.filename, "UTF-8")}?t=${docs.at}&token=${docs.tokenthumb}&mute"
				else -> "https://trace.moe/preview.php?anilist_id=${docs.anilist_id}&file=${URLEncoder.encode(docs.filename, "UTF-8")}&t=${docs.at}&token=${docs.tokenthumb}"
			}
			if (nowPlayUrl == requestUrl) {
				mediaSource.content(null)
			} else {
				nowPlayUrl = requestUrl
				val newMediaSource = mediaSourceMap.getOrPut(nowPlayUrl) {
					ProgressiveMediaSource.Factory(exoDataSourceFactory).createMediaSource(Uri.parse(nowPlayUrl))
				}
				mediaSource.content(newMediaSource)
			}
		}
	}
}