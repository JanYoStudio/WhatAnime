package pw.janyo.whatanime.viewmodel

import chaintech.videoplayer.host.MediaPlayerEvent
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.model.ScreenResize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pw.janyo.whatanime.model.PlaybackPhase
import pw.janyo.whatanime.model.PlaybackState
import pw.janyo.whatanime.utils.isVideoExpired
import pw.janyo.whatanime.utils.previewUrl
import kotlin.time.Clock

enum class PlaybackEvent { Buffering, Ready, Paused, Resumed, Ended, Failed }

interface PlaybackDriver {
    fun load(url: String)
    fun play()
    fun close()
}

/** App 根部为唯一生命周期所有者。每次播放有独立回调身份，旧 host 无权修改新会话。 */
class PlaybackCoordinator(
    private val factory: ((PlaybackEvent) -> Unit) -> PlaybackDriver = { HostPlaybackDriver(it) },
    private val now: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) {
    private val mutableState = MutableStateFlow(PlaybackState())
    val state = mutableState.asStateFlow()
    private val mutableExpiredNotice = MutableStateFlow(false)
    val expiredNotice = mutableExpiredNotice.asStateFlow()
    private var driver: PlaybackDriver? = null
    private var sequence = 0L
    private var lastRequest: Pair<String, Long?>? = null
    val host: MediaPlayerHost? get() = (driver as? HostPlaybackDriver)?.host

    fun play(url: String, savedAt: Long? = null): Boolean {
        close()
        mutableExpiredNotice.value = false
        if (savedAt != null && isVideoExpired(savedAt, now())) {
            mutableExpiredNotice.value = true
            return false
        }
        lastRequest = url to savedAt
        val request = ++sequence
        mutableState.value = PlaybackState(request, PlaybackPhase.Loading)
        try {
            driver = factory { event -> accept(request, event) }
            driver?.load(previewUrl(url))
            driver?.play()
        } catch (_: Exception) {
            accept(request, PlaybackEvent.Failed)
        }
        return true
    }

    fun retry(): Boolean {
        val request = lastRequest ?: return false
        return play(request.first, request.second)
    }

    fun close() {
        ++sequence
        val previous = driver
        driver = null
        lastRequest = null
        mutableState.value = PlaybackState(sequence, PlaybackPhase.Closed)
        try { previous?.close() } catch (_: Exception) { /* 原生视图仍通过移出组合释放。 */ }
    }

    fun onBackground() = close()
    fun dispose() = close()
    fun acknowledgeExpired() { mutableExpiredNotice.value = false }

    private fun accept(request: Long, event: PlaybackEvent) {
        val current = mutableState.value
        if (request != current.sessionId || current.phase in setOf(PlaybackPhase.Closed, PlaybackPhase.Error)) return
        val phase = when (event) {
            PlaybackEvent.Buffering -> PlaybackPhase.Loading
            PlaybackEvent.Ready -> PlaybackPhase.Playing
            PlaybackEvent.Paused -> PlaybackPhase.Paused
            PlaybackEvent.Resumed -> if (current.phase == PlaybackPhase.Loading) PlaybackPhase.Loading else PlaybackPhase.Playing
            PlaybackEvent.Ended -> PlaybackPhase.Ended
            PlaybackEvent.Failed -> PlaybackPhase.Error
        }
        mutableState.value = current.copy(phase = phase)
    }
}

private class HostPlaybackDriver(callback: (PlaybackEvent) -> Unit) : PlaybackDriver {
    val host = MediaPlayerHost(isLooping = false, isFullScreen = false, initialVideoFitMode = ScreenResize.FIT)

    init {
        host.onEvent = { event ->
            when (event) {
                is MediaPlayerEvent.BufferChange -> callback(if (event.isBuffering) PlaybackEvent.Buffering else PlaybackEvent.Ready)
                is MediaPlayerEvent.PauseChange -> callback(if (event.isPaused) PlaybackEvent.Paused else PlaybackEvent.Resumed)
                MediaPlayerEvent.MediaEnd -> callback(PlaybackEvent.Ended)
                else -> Unit
            }
        }
        // 当前库只暴露字符串错误，不解析可能携带 Token 的 details 猜测 HTTP 码。
        host.onError = { callback(PlaybackEvent.Failed) }
    }

    override fun load(url: String) = host.loadUrl(url)
    override fun play() = host.play()
    override fun close() {
        host.onEvent = null
        host.onError = null
        host.pause()
        host.resetMetadata()
        // CMPPlayer 的原生资源由移出组合后的 onDispose 释放，不存在 host.release() API。
    }
}
