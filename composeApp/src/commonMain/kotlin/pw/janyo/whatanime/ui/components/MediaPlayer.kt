package pw.janyo.whatanime.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.model.VideoPlayerConfig
import chaintech.videoplayer.ui.video.VideoPlayerComposable

@Composable
fun PlatformMediaPlayerView(modifier: Modifier = Modifier, mediaPlayerHost: MediaPlayerHost) {
    VideoPlayerComposable(
        modifier = modifier,
        playerHost = mediaPlayerHost,
        playerConfig = VideoPlayerConfig(
            isFullScreenEnabled = false,
            isScreenLockEnabled = false,
            isSpeedControlEnabled = false,
            isZoomEnabled = false,
            isScreenResizeEnabled = false,
            isGestureVolumeControlEnabled = false,
            isFastForwardBackwardEnabled = false,
        )
    )
}
