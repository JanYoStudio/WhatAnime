package pw.janyo.whatanime.handler

import com.google.android.exoplayer2.ExoPlayer
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.model.Docs
import pw.janyo.whatanime.viewModel.MainViewModel
import java.net.URLEncoder

class MainItemListener(
		private val player: ExoPlayer,
		private val mainViewModel: MainViewModel
) {
	fun click(docs: Docs) {
		val requestUrl = when (Configure.previewConfig) {
			1 -> "https://media.trace.moe/video/${docs.anilist_id}/${URLEncoder.encode(docs.filename, "UTF-8")}?t=${docs.at}&token=${docs.tokenthumb}"
			2 -> "https://media.trace.moe/video/${docs.anilist_id}/${URLEncoder.encode(docs.filename, "UTF-8")}?t=${docs.at}&token=${docs.tokenthumb}&mute"
			else -> "https://trace.moe/preview.php?anilist_id=${docs.anilist_id}&file=${URLEncoder.encode(docs.filename, "UTF-8")}&t=${docs.at}&token=${docs.tokenthumb}"
		}
		if (mainViewModel.nowPlayUrl.value == requestUrl) {
			if (!player.isPlaying)
				player.seekToDefaultPosition()
			player.playWhenReady = true
		} else {
			mainViewModel.nowPlayUrl.postValue(requestUrl)
		}
	}
}