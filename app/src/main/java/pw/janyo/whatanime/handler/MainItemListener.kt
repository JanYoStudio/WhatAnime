package pw.janyo.whatanime.handler

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.databinding.ActivityMainBinding
import pw.janyo.whatanime.model.Docs
import java.net.URLEncoder

class MainItemListener(context: Context,
					   private val player: SimpleExoPlayer) {
	private var nowPlayUrl: String? = null
	private val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, context.packageName))

	fun click(docs: Docs) {
		val requestUrl = when (Configure.previewConfig) {
			1 -> "https://media.trace.moe/video/${docs.anilist_id}/${URLEncoder.encode(docs.filename, "UTF-8")}?t=${docs.at}&token=${docs.tokenthumb}"
			2 -> "https://media.trace.moe/video/${docs.anilist_id}/${URLEncoder.encode(docs.filename, "UTF-8")}?t=${docs.at}&token=${docs.tokenthumb}&mute"
			else -> "https://trace.moe/preview.php?anilist_id=${docs.anilist_id}&file=${URLEncoder.encode(docs.filename, "UTF-8")}&t=${docs.at}&token=${docs.tokenthumb}"
		}
		if (nowPlayUrl != requestUrl) {
			nowPlayUrl = requestUrl
			player.stop(true)
			player.prepare(ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(requestUrl)))
		} else {
			if (!player.isPlaying)
				player.seekToDefaultPosition()
		}
		player.playWhenReady = true
	}
}