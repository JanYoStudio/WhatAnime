package pw.janyo.whatanime.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.session.MediaController
import androidx.media3.ui.PlayerView


@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class PlaybackStateController(private val media3PlayerComponent: Media3PlayerComponent) {
    actual fun initPlayer(playbackState: (PlaybackState) -> Unit) {
        media3PlayerComponent.initPlayer()
        media3PlayerComponent.setControllerListener(PlaybackControllerListener(playbackState))
    }

    actual fun setPlayUrl(playUrl: String,playbackState: (PlaybackState) -> Unit) {
        media3PlayerComponent.setPlayUrl(playUrl)
        playbackState(PlaybackState.Buffering)
    }

    actual fun play(playbackState: (PlaybackState) -> Unit) {
        media3PlayerComponent.play()
        playbackState(PlaybackState.Playing)
    }

    actual fun release() {
        media3PlayerComponent.releasePlayer()
    }

    actual fun isLoading(): Boolean {
        val controller = media3PlayerComponent.getMediaController() ?: return false
        return controller.isLoading
    }

    internal fun getController(): MediaController? {
        return media3PlayerComponent.getMediaController()
    }

    class PlaybackControllerListener(val playbackState: (PlaybackState) -> Unit) : Listener {
        override fun onPlaybackStateChanged(state: Int) {
            super.onPlaybackStateChanged(state)
            when (state) {
                Player.STATE_BUFFERING,
                Player.STATE_READY -> {
                    playbackState(PlaybackState.Buffering)
                }

                Player.STATE_ENDED -> {
                    playbackState(PlaybackState.Stop)
                }

                Player.STATE_IDLE -> {
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            error.message?.let { PlaybackState.Error(errorMessage = it) }
                ?.let { playbackState(it) }
        }
    }
}

@Composable
actual fun PlatformMediaPlayerView(
    modifier: Modifier,
    playbackStateController: PlaybackStateController
) {
    val controller = remember { mutableStateOf(playbackStateController.getController()) }
    DisposableEffect(Unit) {
        onDispose {
            playbackStateController.release()
        }
    }
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                player = controller.value
                useController = false
            }
        },
        modifier = modifier,
    )
}