package pw.janyo.whatanime.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import chaintech.videoplayer.host.MediaPlayerHost
import org.koin.compose.koinInject

@Composable
fun BuildVideoDialog() {
    val playerState = koinInject<PlayerState>()
    val mediaPlayerHost = koinInject<MediaPlayerHost>()
    val isLoadUrl by playerState.isLoadUrl.collectAsState()
    if (!isLoadUrl) {
        return
    }
    Dialog(onDismissRequest = {
        mediaPlayerHost.pause()
        playerState.release()
    }) {
        Box(modifier = Modifier.padding(8.dp)) {
            PlatformMediaPlayerView(
                modifier = Modifier
                    .width(480.dp)
                    .height(270.dp),
                mediaPlayerHost,
            )
        }
    }
}