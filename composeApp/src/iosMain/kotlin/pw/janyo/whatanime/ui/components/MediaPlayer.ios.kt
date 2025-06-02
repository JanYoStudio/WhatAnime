package pw.janyo.whatanime.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Suppress(names = ["EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING"])
actual class PlaybackStateController {
    actual fun initPlayer(
        playbackState: (PlaybackState) -> Unit
    ) {
    }

    actual fun setPlayUrl(playUrl: String, playbackState: (PlaybackState) -> Unit) {
    }

    actual fun play(playbackState: (PlaybackState) -> Unit) {
    }

    actual fun release() {
    }

    actual fun isLoading(): Boolean{
    }
}

@Composable
actual fun PlatformMediaPlayerView(
    modifier: Modifier,
    playbackStateController: PlaybackStateController
) {
}