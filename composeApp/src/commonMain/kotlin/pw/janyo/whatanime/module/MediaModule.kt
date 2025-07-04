package pw.janyo.whatanime.module

import chaintech.videoplayer.host.MediaPlayerEvent
import chaintech.videoplayer.host.MediaPlayerHost
import co.touchlab.kermit.Logger
import org.koin.dsl.module
import pw.janyo.whatanime.ui.components.PlayerState

val mediaModule = module {
    single {
        MediaPlayerHost(
            isPaused = true,
            isLooping = false,
            isFullScreen = false,
        )
    }
    single {
        val mediaPlayerHost = get<MediaPlayerHost>()
        val playerState = PlayerState()
        mediaPlayerHost.onEvent = { event ->
            Logger.i("player event: $event")
            when (event) {
                is MediaPlayerEvent.PauseChange -> {
                    playerState.isPaused.value = event.isPaused
                }

                MediaPlayerEvent.MediaEnd -> {
                    playerState.playEnd()
                }

                else -> {}
            }
        }
        playerState
    }
}