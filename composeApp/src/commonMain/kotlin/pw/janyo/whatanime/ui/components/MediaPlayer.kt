package pw.janyo.whatanime.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class PlaybackStateController {
    fun initPlayer(playbackState: (PlaybackState) -> Unit)
    fun setPlayUrl(playUrl: String, playbackState: (PlaybackState) -> Unit)
    fun play(playbackState: (PlaybackState) -> Unit)
    fun release()
    fun isLoading(): Boolean
}

sealed class PlaybackState {
    data object Stop : PlaybackState()
    data object Buffering : PlaybackState()
    data object Playing : PlaybackState()
    data class Error(val errorMessage: String) : PlaybackState()
}

@Composable
expect fun PlatformMediaPlayerView(
    modifier: Modifier = Modifier,
    playbackStateController: PlaybackStateController,
)
