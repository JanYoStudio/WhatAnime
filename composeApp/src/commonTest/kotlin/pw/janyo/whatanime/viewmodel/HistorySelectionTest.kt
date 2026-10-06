package pw.janyo.whatanime.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.repository.DeleteHistoryResult
import pw.janyo.whatanime.repository.HistoryGateway
import pw.janyo.whatanime.repository.HistoryLookup
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class HistorySelectionTest {
    private class Gateway : HistoryGateway {
        var rows = (1..3).map { id -> AnimationHistory().apply { this.id = id; title = "$id" } }
        val failed = mutableSetOf<Int>()
        val cleanupFailed = mutableSetOf<Int>()
        val deleted = mutableListOf<Int>()
        var cancelId: Int? = null
        override suspend fun queryAllHistory() = rows
        override suspend fun getHistoryDetails(historyId: Int): HistoryLookup? = null
        override suspend fun deleteHistory(historyId: Int): DeleteHistoryResult {
            if (historyId == cancelId) throw CancellationException()
            if (historyId in failed) error("private-token")
            rows = rows.filterNot { it.id == historyId }
            deleted += historyId
            return if (historyId in cleanupFailed) DeleteHistoryResult.CacheCleanupFailed else DeleteHistoryResult.Deleted
        }
    }

    @Test fun longPressSelectsOnlyItsRowAndClearingDoesNotExitMode() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val vm = HistoryViewModel(Gateway()) { "safe error" }
        try {
            vm.refresh(); runCurrent()
            vm.beginSelection(2)
            assertTrue(vm.historyListState.value.selectionMode)
            assertEquals(setOf(2), vm.historyListState.value.selectedIds)
            vm.toggleSelection(1); vm.toggleSelection(2)
            assertEquals(setOf(1), vm.historyListState.value.selectedIds)
            vm.selectAll()
            assertEquals(setOf(1, 2, 3), vm.historyListState.value.selectedIds)
            vm.clearSelection()
            assertTrue(vm.historyListState.value.selectionMode)
            assertTrue(vm.historyListState.value.selectedIds.isEmpty())
            vm.exitSelection()
            assertFalse(vm.historyListState.value.selectionMode)
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test fun refreshPrunesRemovedSelectionsWithoutSelectingNewRows() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val gateway = Gateway(); val vm = HistoryViewModel(gateway) { "safe error" }
        try {
            vm.refresh(); runCurrent(); vm.beginSelection(1); vm.toggleSelection(2)
            gateway.rows = gateway.rows.filterNot { it.id == 2 }
            vm.refresh(); runCurrent()
            assertEquals(setOf(1), vm.historyListState.value.selectedIds)
            vm.toggleSelection(99)
            assertEquals(setOf(1), vm.historyListState.value.selectedIds)
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test fun partialFailureKeepsOnlyFailedRowsSelectedAndNeverDeletesUnselectedRows() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val gateway = Gateway(); val vm = HistoryViewModel(gateway) { "safe error" }
        try {
            vm.refresh(); runCurrent(); vm.beginSelection(1); vm.toggleSelection(2)
            gateway.failed += 1
            vm.deleteSelected(setOf(1, 2, 3, 99))
            vm.deleteSelected(setOf(1, 2)) // 重复点击不能发起第二批。
            runCurrent()
            assertEquals(listOf(2), gateway.deleted)
            assertEquals(listOf(1, 3), vm.historyListState.value.list.map { it.id })
            assertEquals(setOf(1), vm.historyListState.value.selectedIds)
            assertTrue(vm.historyListState.value.selectionMode)
            assertEquals("safe error", vm.historyListState.value.errorMessage)
            assertEquals(1, vm.historyListState.value.failedDeleteCount)
            assertTrue(vm.historyListState.value.deletingIds.isEmpty())
            gateway.failed.clear()
            vm.deleteSelected(setOf(1)); runCurrent()
            assertEquals(listOf(3), vm.historyListState.value.list.map { it.id })
            assertFalse(vm.historyListState.value.selectionMode)
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test fun cleanupWarningSurvivesLaterSuccessfulDeletesWithoutRestoringDeletedRows() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val gateway = Gateway(); val vm = HistoryViewModel(gateway) { "safe error" }
        try {
            vm.refresh(); runCurrent(); vm.beginSelection(1); vm.selectAll()
            gateway.cleanupFailed += 1
            vm.deleteSelected(setOf(1, 2, 3)); runCurrent()
            assertTrue(vm.historyListState.value.list.isEmpty())
            assertTrue(vm.historyListState.value.cleanupFailed)
            assertFalse(vm.historyListState.value.selectionMode)
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test fun cancellationReleasesBusyStateAndDoesNotDeleteRemainingRows() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val gateway = Gateway(); val vm = HistoryViewModel(gateway) { "safe error" }
        try {
            vm.refresh(); runCurrent(); vm.beginSelection(1); vm.selectAll()
            gateway.cancelId = 2
            vm.deleteSelected(setOf(1, 2, 3)); runCurrent()
            assertEquals(listOf(1), gateway.deleted)
            assertEquals(listOf(2, 3), vm.historyListState.value.list.map { it.id })
            assertEquals(setOf(2, 3), vm.historyListState.value.selectedIds)
            assertTrue(vm.historyListState.value.deletingIds.isEmpty())
            assertEquals("", vm.historyListState.value.errorMessage)
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test fun suspendedFailureMessageCannotRestoreExitedSelection() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val gate = CompletableDeferred<Unit>()
        val gateway = Gateway(); val vm = HistoryViewModel(gateway) { gate.await(); "safe error" }
        try {
            vm.refresh(); runCurrent(); vm.beginSelection(1); gateway.failed += 1
            vm.deleteSelected(setOf(1)); runCurrent()
            vm.exitSelection(); gate.complete(Unit); runCurrent()
            assertFalse(vm.historyListState.value.selectionMode)
            assertTrue(vm.historyListState.value.selectedIds.isEmpty())
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test fun singleDeleteFailureDoesNotReportBatchSelection() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val gateway = Gateway(); val vm = HistoryViewModel(gateway) { "safe error" }
        try {
            vm.refresh(); runCurrent(); gateway.failed += 1
            vm.deleteHistory(1); runCurrent()
            assertEquals(0, vm.historyListState.value.failedDeleteCount)
            assertEquals("safe error", vm.historyListState.value.errorMessage)
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test fun exitingDuringDeleteDoesNotReenterSelectionOnFailure() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val gateway = Gateway(); val vm = HistoryViewModel(gateway) { "safe error" }
        try {
            vm.refresh(); runCurrent(); vm.beginSelection(1); gateway.failed += 1
            vm.deleteSelected(setOf(1)); vm.exitSelection(); runCurrent()
            assertFalse(vm.historyListState.value.selectionMode)
            assertTrue(vm.historyListState.value.selectedIds.isEmpty())
            assertEquals(listOf(1, 2, 3), vm.historyListState.value.list.map { it.id })
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }
}
