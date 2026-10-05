package pw.janyo.whatanime.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.compose.resources.getString
import pw.janyo.whatanime.base.ComposeViewModel
import pw.janyo.whatanime.model.ReadOnlyAnimationHistory
import pw.janyo.whatanime.repository.DeleteHistoryResult
import pw.janyo.whatanime.repository.HistoryGateway
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.hint_unknown_error

class HistoryViewModel(
    private val gateway: HistoryGateway,
    private val failureMessage: suspend () -> String = { getString(Res.string.hint_unknown_error) },
) : ComposeViewModel() {
    private val mutableState = MutableStateFlow(HistoryListState())
    val historyListState = mutableState.asStateFlow()
    private val operations = Mutex()

    fun refresh() {
        if (mutableState.value.loading) return
        mutableState.value = mutableState.value.copy(loading = true, errorMessage = "")
        viewModelScope.launch {
            operations.withLock {
                try {
                    val rows = gateway.queryAllHistory().map { it.readonly() }
                    mutableState.value = mutableState.value.copy(
                        list = rows, loading = false, loaded = true,
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    mutableState.value = mutableState.value.copy(
                        loading = false, errorMessage = failureMessage(),
                    )
                }
            }
        }
    }

    fun deleteHistory(historyId: Int) {
        if (historyId in mutableState.value.deletingIds) return
        mutableState.value = mutableState.value.copy(
            deletingIds = mutableState.value.deletingIds + historyId,
            errorMessage = "", cleanupFailed = false,
        )
        viewModelScope.launch {
            operations.withLock {
                try {
                    val result = gateway.deleteHistory(historyId)
                    mutableState.value = mutableState.value.copy(
                        list = mutableState.value.list.filterNot { it.id == historyId },
                        cleanupFailed = result == DeleteHistoryResult.CacheCleanupFailed,
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    mutableState.value = mutableState.value.copy(errorMessage = failureMessage())
                } finally {
                    mutableState.value = mutableState.value.copy(
                        deletingIds = mutableState.value.deletingIds - historyId,
                    )
                }
            }
        }
    }

    fun acknowledgeError() {
        mutableState.value = mutableState.value.copy(errorMessage = "", cleanupFailed = false)
    }
}

data class HistoryListState(
    val loading: Boolean = false,
    val loaded: Boolean = false,
    val list: List<ReadOnlyAnimationHistory> = emptyList(),
    val errorMessage: String = "",
    val deletingIds: Set<Int> = emptySet(),
    val cleanupFailed: Boolean = false,
)
