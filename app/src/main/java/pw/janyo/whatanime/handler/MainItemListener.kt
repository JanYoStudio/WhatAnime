package pw.janyo.whatanime.handler

import android.net.Uri
import android.view.View
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.databinding.ActivityMainBinding
import pw.janyo.whatanime.model.Docs
import vip.mystery0.logs.Logs
import java.net.URLEncoder

class MainItemListener(activityMainBinding: ActivityMainBinding) {
	private val contentMainBinding = activityMainBinding.include
	private var nowPlayUrl: String? = null

	fun click(docs: Docs) {
		try {
			val requestUrl = when (Configure.previewConfig) {
				1 -> "https://media.trace.moe/video/${docs.anilist_id}/${URLEncoder.encode(docs.filename, "UTF-8")}?t=${docs.at}&token=${docs.tokenthumb}"
				2 -> "https://media.trace.moe/video/${docs.anilist_id}/${URLEncoder.encode(docs.filename, "UTF-8")}?t=${docs.at}&token=${docs.tokenthumb}&mute"
				else -> "https://trace.moe/preview.php?anilist_id=${docs.anilist_id}&file=${URLEncoder.encode(docs.filename, "UTF-8")}&t=${docs.at}&token=${docs.tokenthumb}"
			}
			if (nowPlayUrl != requestUrl) {
				nowPlayUrl = requestUrl
				contentMainBinding.videoView.stopPlayback()
				contentMainBinding.videoView.setVideoURI(Uri.parse(requestUrl))
			}
			contentMainBinding.imageView.visibility = View.GONE
			contentMainBinding.videoView.visibility = View.VISIBLE
			contentMainBinding.progressBar.visibility = View.VISIBLE
			contentMainBinding.videoView.setOnPreparedListener { contentMainBinding.progressBar.visibility = View.GONE }
			contentMainBinding.videoView.setOnCompletionListener {
				contentMainBinding.videoView.visibility = View.GONE
				contentMainBinding.imageView.visibility = View.VISIBLE
			}
			contentMainBinding.videoView.start()
		} catch (e: Exception) {
			Logs.wtf("click: ", e)
			contentMainBinding.progressBar.visibility = View.GONE
			contentMainBinding.videoView.visibility = View.GONE
			contentMainBinding.imageView.visibility = View.VISIBLE
		}
	}
}