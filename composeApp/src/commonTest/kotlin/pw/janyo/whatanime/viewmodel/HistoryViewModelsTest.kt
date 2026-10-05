package pw.janyo.whatanime.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.model.DetailPhase
import pw.janyo.whatanime.model.SearchPreferences
import pw.janyo.whatanime.repository.DeleteHistoryResult
import pw.janyo.whatanime.repository.HistoryGateway
import pw.janyo.whatanime.repository.HistoryLookup

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelsTest {
    private class Gateway : HistoryGateway {
        var rows = listOf(AnimationHistory().apply { id = 1; title = "A" })
        var failRead = false
        var failDelete = false
        var cleanupFailure = false
        override suspend fun queryAllHistory(): List<AnimationHistory> {
            if (failRead) error("private-token")
            return rows
        }
        override suspend fun getHistoryDetails(historyId: Int): HistoryLookup? {
            if (failRead) error("broken JSON including private-token")
            return null
        }
        override suspend fun deleteHistory(historyId: Int): DeleteHistoryResult {
            if (failDelete) error("private-token")
            rows = rows.filterNot { it.id == historyId }
            return if (cleanupFailure) DeleteHistoryResult.CacheCleanupFailed else DeleteHistoryResult.Deleted
        }
    }

    @Test fun refreshFailureKeepsPreviouslyLoadedRows() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val gateway = Gateway()
        val vm = HistoryViewModel(gateway) { "safe error" }
        try {
            vm.refresh(); runCurrent()
            gateway.failRead = true
            vm.refresh(); runCurrent()
            assertEquals(listOf(1), vm.historyListState.value.list.map { it.id })
            assertFalse(vm.historyListState.value.loading)
            assertEquals("safe error", vm.historyListState.value.errorMessage)
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test fun databaseFailureKeepsRowButCleanupFailureDoesNotRestoreDeletedRow() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val gateway = Gateway()
        val vm = HistoryViewModel(gateway) { "safe error" }
        try {
            vm.refresh(); runCurrent()
            gateway.failDelete = true
            vm.deleteHistory(1); runCurrent()
            assertEquals(1, vm.historyListState.value.list.size)
            gateway.failDelete = false
            gateway.cleanupFailure = true
            vm.deleteHistory(1); runCurrent()
            assertTrue(vm.historyListState.value.list.isEmpty())
            assertTrue(vm.historyListState.value.cleanupFailed)
            assertTrue(vm.historyListState.value.deletingIds.isEmpty())
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test fun missingDetailAndBrokenJsonAreDistinctStates() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val gateway = Gateway()
        val vm = DetailViewModel(gateway, SearchPreferences())
        try {
            vm.loadHistoryDetail(1); runCurrent()
            assertEquals(DetailPhase.NotFound, vm.detailState.value.phase)
            gateway.failRead = true
            vm.loadHistoryDetail(1); runCurrent()
            assertEquals(DetailPhase.Error, vm.detailState.value.phase)
            assertTrue(vm.detailState.value.results.isEmpty())
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }
}
