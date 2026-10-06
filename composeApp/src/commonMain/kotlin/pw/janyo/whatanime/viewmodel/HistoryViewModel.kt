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
        if (mutableState.value.loading || mutableState.value.deletingIds.isNotEmpty()) return
        mutableState.value = mutableState.value.copy(loading = true, errorMessage = "")
        viewModelScope.launch {
            operations.withLock {
                try {
                    val rows = gateway.queryAllHistory().map { it.readonly() }
                    mutableState.value = mutableState.value.copy(
                        list = rows, loading = false, loaded = true,
                        selectedIds = mutableState.value.selectedIds.intersect(rows.map { it.id }.toSet()),
                        selectionMode = mutableState.value.selectionMode && rows.isNotEmpty(),
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

    fun beginSelection(historyId: Int) {
        val state = mutableState.value
        if (state.loading || state.deletingIds.isNotEmpty() || state.list.none { it.id == historyId }) return
        mutableState.value = state.copy(selectionMode = true, selectedIds = setOf(historyId))
    }

    fun toggleSelection(historyId: Int) {
        val state = mutableState.value
        if (!state.selectionMode || state.deletingIds.isNotEmpty() || state.list.none { it.id == historyId }) return
        mutableState.value = state.copy(selectedIds = if (historyId in state.selectedIds) {
            state.selectedIds - historyId
        } else state.selectedIds + historyId)
    }

    fun selectAll() {
        val state = mutableState.value
        if (state.selectionMode && state.deletingIds.isEmpty()) {
            mutableState.value = state.copy(selectedIds = state.list.map { it.id }.toSet())
        }
    }

    fun clearSelection() {
        if (mutableState.value.deletingIds.isEmpty()) {
            mutableState.value = mutableState.value.copy(selectedIds = emptySet())
        }
    }

    fun exitSelection() {
        mutableState.value = mutableState.value.copy(selectionMode = false, selectedIds = emptySet())
    }

    fun deleteHistory(historyId: Int) = deleteRows(setOf(historyId), batch = false)

    fun deleteSelected(confirmedIds: Set<Int>) {
        val state = mutableState.value
        if (state.selectionMode) deleteRows(confirmedIds.intersect(state.selectedIds), batch = true)
    }

    private fun deleteRows(requested: Set<Int>, batch: Boolean) {
        val state = mutableState.value
        if (state.loading || state.deletingIds.isNotEmpty()) return
        // 按当前列表顺序处理确认快照；不把刷新后出现的记录纳入这一批。
        val ids = state.list.map { it.id }.filter { it in requested }.toSet()
        if (ids.isEmpty()) return
        mutableState.value = state.copy(
            deletingIds = ids, errorMessage = "", cleanupFailed = false, failedDeleteCount = 0,
        )
        viewModelScope.launch {
            val failures = mutableSetOf<Int>()
            var cleanupFailed = false
            try {
                operations.withLock {
                    for (id in ids) {
                        try {
                            val result = gateway.deleteHistory(id)
                            cleanupFailed = cleanupFailed || result == DeleteHistoryResult.CacheCleanupFailed
                            mutableState.value = mutableState.value.copy(
                                list = mutableState.value.list.filterNot { it.id == id },
                                selectedIds = mutableState.value.selectedIds - id,
                            )
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            failures += id
                        }
                    }
                    val errorMessage = if (failures.isEmpty()) "" else failureMessage()
                    val current = mutableState.value
                    mutableState.value = current.copy(
                        selectionMode = current.selectionMode && (!batch || failures.isNotEmpty()),
                        selectedIds = if (batch) current.selectedIds.intersect(failures) else current.selectedIds,
                        failedDeleteCount = if (batch) failures.size else 0,
                        errorMessage = errorMessage,
                    )
                }
            } finally {
                // 取消也必须释放忙状态；已删除的记录不能因后续失败而恢复。
                mutableState.value = mutableState.value.copy(deletingIds = emptySet(), cleanupFailed = cleanupFailed)
            }
        }
    }

    fun acknowledgeError() {
        mutableState.value = mutableState.value.copy(errorMessage = "", cleanupFailed = false, failedDeleteCount = 0)
    }
}

data class HistoryListState(
    val loading: Boolean = false,
    val loaded: Boolean = false,
    val list: List<ReadOnlyAnimationHistory> = emptyList(),
    val errorMessage: String = "",
    val deletingIds: Set<Int> = emptySet(),
    val cleanupFailed: Boolean = false,
    val selectionMode: Boolean = false,
    val selectedIds: Set<Int> = emptySet(),
    val failedDeleteCount: Int = 0,
)
