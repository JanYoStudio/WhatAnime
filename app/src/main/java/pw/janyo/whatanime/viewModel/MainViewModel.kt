package pw.janyo.whatanime.viewModel

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.zhihu.matisse.Matisse
import pw.janyo.whatanime.R
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.Docs
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.repository.AnimationRepository
import pw.janyo.whatanime.utils.cloneUriToFile
import vip.mystery0.rx.PackageData
import vip.mystery0.rx.content
import vip.mystery0.rx.launch
import vip.mystery0.rx.loading
import vip.mystery0.tools.ResourceException
import java.io.File
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

class MainViewModel(
		private val animationRepository: AnimationRepository,
		private val exoDataSourceFactory: DataSource.Factory
) : ViewModel() {
	private val mediaSourceMap = ConcurrentHashMap<String, MediaSource>()
	val mediaSource = MutableLiveData<PackageData<MediaSource>>()
	private var nowPlayUrl: String = ""
	val imageFile = MutableLiveData<PackageData<File>>()
	val resultList = MutableLiveData<PackageData<Animation>>()
	val isShowDetail = MutableLiveData<Boolean>()
	val quota = MutableLiveData<PackageData<SearchQuota>>()

	fun search(file: File, filter: String?) {
		resultList.loading()
		launch(resultList) {
			val animation = animationRepository.queryAnimationByImageLocal(file, filter)
			if (animation.quota != -987654 && animation.quota_ttl != -987654) {
				val searchQuota = SearchQuota()
				searchQuota.quota = animation.quota
				searchQuota.quota_ttl = animation.quota_ttl
				quota.content(searchQuota)
			}
			resultList.content(animation)
		}
	}

	fun showQuota() {
		launch(quota) {
			quota.content(animationRepository.showQuota())
		}
	}

	fun parseImageFileByMatisse(data: Intent) {
		launch(imageFile) {
			val fileList = Matisse.obtainPathResult(data)
			if (fileList.isNotEmpty()) {
				imageFile.content(File(fileList[0]))
			} else {
				throw ResourceException(R.string.hint_select_file_path_null)
			}
		}
	}

	/**
	 * 从Intent中获取Uri，将其clone到临时目录
	 */
	fun parseImageFile(data: Intent) {
		launch(imageFile) {
			val file = data.data!!.cloneUriToFile()
			if (file != null && file.exists()) {
				imageFile.content(file)
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