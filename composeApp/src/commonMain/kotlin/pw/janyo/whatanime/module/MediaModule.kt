package pw.janyo.whatanime.module

import chaintech.videoplayer.host.MediaPlayerEvent
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.model.ScreenResize
import org.koin.dsl.module
import pw.janyo.whatanime.ui.components.PlayerState

val mediaModule = module {
    single {
        MediaPlayerHost(
            isLooping = false,
            isFullScreen = false,
            initialVideoFitMode = ScreenResize.FIT,
        )
    }
    single {
        val mediaPlayerHost = get<MediaPlayerHost>()
        val playerState = PlayerState()
        mediaPlayerHost.onEvent = { event ->
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