package pw.janyo.whatanime.viewmodel

import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.core.component.inject
import pw.janyo.whatanime.base.ComposeViewModel
import pw.janyo.whatanime.model.DetailPhase
import pw.janyo.whatanime.model.DetailState
import pw.janyo.whatanime.model.SearchAnimeResultItem
import pw.janyo.whatanime.model.SearchPreferences
import pw.janyo.whatanime.repository.HistoryGateway
import pw.janyo.whatanime.repository.HistoryLookup
import pw.janyo.whatanime.utils.isVideoExpired
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.video_play_hint_410
import kotlin.time.Clock

class DetailViewModel(
    private val gateway: HistoryGateway,
    private val preferences: SearchPreferences,
    private val now: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : ComposeViewModel() {
    private val playback by inject<PlaybackCoordinator>()
    private val mutableState = MutableStateFlow(DetailState())
    val detailState = mutableState.asStateFlow()
    private val mutableNotice = MutableStateFlow("")
    val notice = mutableNotice.asStateFlow()
    private var raw: HistoryLookup? = null
    private var generation = 0L

    init {
        viewModelScope.launch { preferences.hideAdult.collect { raw?.let(::publish) } }
    }

    @Suppress("UNUSED_PARAMETER")
    fun loadHistoryDetail(historyId: Int, cacheFile: PlatformFile? = null) {
        if (mutableState.value.historyId == historyId && mutableState.value.phase == DetailPhase.Loading) return
        val request = ++generation
        raw = null
        mutableNotice.value = ""
        mutableState.value = DetailState(historyId)
        viewModelScope.launch {
            try {
                val result = gateway.getHistoryDetails(historyId)
                if (request != generation) return@launch
                raw = result
                if (result == null) mutableState.value = DetailState(historyId, DetailPhase.NotFound)
                else publish(result)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                if (request == generation) mutableState.value = DetailState(historyId, DetailPhase.Error)
            }
        }
    }

    private fun publish(lookup: HistoryLookup) {
        val original = lookup.result.result
        val visible = if (preferences.hideAdult.value) original.filterNot { it.aniList.adult } else original
        mutableState.value = DetailState(
            historyId = lookup.historyId,
            phase = when {
                lookup.legacy -> DetailPhase.Legacy
                original.isEmpty() -> DetailPhase.Empty
                visible.isEmpty() -> DetailPhase.FilteredEmpty
                else -> DetailPhase.Ready
            },
            image = PlatformFile(lookup.cachePath),
            savedAt = lookup.savedAt,
            results = if (lookup.legacy) emptyList() else visible,
        )
    }

    fun playVideo(result: SearchAnimeResultItem) {
        val savedAt = detailState.value.savedAt ?: return
        if (isVideoExpired(savedAt, now())) {
            viewModelScope.launch { mutableNotice.value = getString(Res.string.video_play_hint_410) }
            return
        }
        playback.play(result.video, savedAt)
    }

    fun acknowledgeNotice() { mutableNotice.value = "" }
}
