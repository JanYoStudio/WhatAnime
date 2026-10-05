package pw.janyo.whatanime.viewmodel

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pw.janyo.whatanime.model.SearchAnimeResultItem
import pw.janyo.whatanime.model.SearchPhase
import pw.janyo.whatanime.model.SearchState

/** 在所属 ViewModel 的主线程上调用；同步接纳操作先于启动异步 Job。 */
class SearchSession(hideAdult: Boolean = false) {
    private val mutableState = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = mutableState.asStateFlow()
    private var sequence = 0L
    private var hideAdult = hideAdult
    private var rawResults: List<SearchAnimeResultItem> = emptyList()

    fun begin(image: PlatformFile): Long? {
        if (mutableState.value.phase == SearchPhase.Loading) return null
        val requestId = ++sequence
        rawResults = emptyList()
        mutableState.value = SearchState(requestId, SearchPhase.Loading, image)
        return requestId
    }

    fun complete(requestId: Long, image: PlatformFile, results: List<SearchAnimeResultItem>) {
        if (!isActive(requestId)) return
        rawResults = results.toList()
        publishResults(mutableState.value.copy(image = image))
    }

    fun fail(requestId: Long, message: String) {
        if (!isActive(requestId)) return
        rawResults = emptyList()
        mutableState.value = mutableState.value.copy(
            phase = SearchPhase.Error,
            results = emptyList(),
            errorMessage = message,
            errorAcknowledged = false,
        )
    }

    fun reject(requestId: Long, message: String) {
        if (!isActive(requestId)) return
        rawResults = emptyList()
        mutableState.value = SearchState(requestId = requestId, errorMessage = message)
    }

    fun cancel(requestId: Long) {
        if (!isActive(requestId)) return
        rawResults = emptyList()
        mutableState.value = SearchState(requestId = requestId)
    }

    fun setHideAdult(value: Boolean) {
        hideAdult = value
        when (mutableState.value.phase) {
            SearchPhase.Success, SearchPhase.Empty, SearchPhase.FilteredEmpty ->
                publishResults(mutableState.value)
            else -> Unit
        }
    }

    fun acknowledgeError(requestId: Long) {
        val current = mutableState.value
        if (current.requestId == requestId && current.errorMessage != null) {
            mutableState.value = current.copy(errorAcknowledged = true)
        }
    }

    private fun isActive(requestId: Long): Boolean =
        mutableState.value.let { it.requestId == requestId && it.phase == SearchPhase.Loading }

    private fun publishResults(current: SearchState) {
        val visible = if (hideAdult) rawResults.filterNot { it.aniList.adult } else rawResults
        mutableState.value = current.copy(
            phase = when {
                rawResults.isEmpty() -> SearchPhase.Empty
                visible.isEmpty() -> SearchPhase.FilteredEmpty
                else -> SearchPhase.Success
            },
            results = visible,
            errorMessage = null,
            errorAcknowledged = false,
        )
    }
}
