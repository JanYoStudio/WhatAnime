package pw.janyo.whatanime.ui.components

import android.app.Application
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession

class Media3PlayerComponent(
    private val context: Application,
    private val cachedPlaybackDataSourceFactory: Media3CachedPlaybackDataSourceFactory
) {
    private lateinit var player: ExoPlayer
    private var mediaController: MediaController? = null
    private var mediaSession: MediaSession? = null

    fun initPlayer() {
        player = ExoPlayer.Builder(context)
            .setMediaSourceFactory(cachedPlaybackDataSourceFactory.buildCacheDataSourceFactory())
            .build()
        mediaSession =
            MediaSession.Builder(context, player).setCallback(MediaSessionCallback()).build()
        mediaController = mediaSession?.token?.let {
            MediaController.Builder(
                context,
                it
            ).buildAsync().get()
        }
    }

    fun setPlayUrl(playUrl: String) {
        val mediaItemWithMetadata = MediaItem.Builder()
            .setUri(playUrl)
            .setMediaId(playUrl)
            .build()
        player.setMediaItem(mediaItemWithMetadata)
    }

    fun releasePlayer() {
        player.release()
        mediaSession?.release()
        mediaSession = null
        cachedPlaybackDataSourceFactory.clearCache()
    }

    fun getMediaController(): MediaController? {
        return mediaController
    }

    fun setControllerListener(playbackControllerListener: PlaybackStateController.PlaybackControllerListener) {
        mediaController?.addListener(playbackControllerListener)
    }

    fun play() {
        mediaController?.play()
    }
}