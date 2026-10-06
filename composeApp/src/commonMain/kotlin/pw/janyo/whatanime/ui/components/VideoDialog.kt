package pw.janyo.whatanime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import multiplatform.network.cmptoast.showToast
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import pw.janyo.whatanime.model.PlaybackPhase
import pw.janyo.whatanime.viewmodel.PlaybackCoordinator
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.ui_close
import whatanime.composeapp.generated.resources.ui_retry
import whatanime.composeapp.generated.resources.video_play_hint_410
import whatanime.composeapp.generated.resources.video_play_hint_unknown

@Composable
fun BuildVideoDialog() {
    val coordinator = koinInject<PlaybackCoordinator>()
    val state by coordinator.state.collectAsState()
    val expired by coordinator.expiredNotice.collectAsState()
    LaunchedEffect(expired) {
        if (expired) {
            coordinator.acknowledgeExpired()
            showToast(getString(Res.string.video_play_hint_410))
        }
    }
    if (state.phase == PlaybackPhase.Closed) return
    Dialog(
        onDismissRequest = coordinator::close,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            val availableHeight = (maxHeight - 96.dp).coerceAtLeast(0.dp)
            val width = minOf(maxWidth * 0.9f, 480.dp, availableHeight * (16f / 9f))
            Column(
                Modifier.width(width).clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                if (state.phase == PlaybackPhase.Error) {
                    Text(
                        stringResource(Res.string.video_play_hint_unknown),
                        modifier = Modifier.padding(16.dp),
                    )
                } else {
                    Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f), contentAlignment = Alignment.Center) {
                        key(state.sessionId) {
                            coordinator.host?.let { host ->
                                PlatformMediaPlayerView(Modifier.fillMaxWidth().aspectRatio(16f / 9f), host)
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (state.phase == PlaybackPhase.Error) {
                        TextButton(onClick = { coordinator.retry() }) { Text(stringResource(Res.string.ui_retry)) }
                    }
                    TextButton(onClick = coordinator::close) { Text(stringResource(Res.string.ui_close)) }
                }
            }
        }
    }
}
