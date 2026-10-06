package pw.janyo.whatanime.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.ui_loading
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
            // 初始加载和后续缓冲共用播放器的唯一加载槽位，不在弹窗再叠一层。
            loaderView = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Text(stringResource(Res.string.ui_loading), color = Color.White)
                }
            },
        )
    )
}
