package pw.janyo.whatanime.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.model.VideoPlayerConfig
import chaintech.videoplayer.ui.video.VideoPlayerComposable
import kotlinx.coroutines.flow.MutableStateFlow

class PlayerState {
    val isPaused = MutableStateFlow(false)
    val isLoadUrl = MutableStateFlow(false)
    val isEnd = MutableStateFlow(false)

    fun loadUrl() {
        isLoadUrl.value = true
        isEnd.value = false
    }

    fun playEnd() {
        isEnd.value = true
    }

    fun release() {
        isPaused.value = false
        isLoadUrl.value = false
        isEnd.value = false
    }
}

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
            isMuteControlEnabled = false,
            isFastForwardBackwardEnabled = false,
        )
    )
}
