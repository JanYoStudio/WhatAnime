package pw.janyo.whatanime.viewmodel

import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CompletableDeferred
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
import kotlin.test.assertTrue
import pw.janyo.whatanime.model.AniListTitleResult
import pw.janyo.whatanime.model.SearchAniListResult
import pw.janyo.whatanime.model.SearchAnimeResultItem
import pw.janyo.whatanime.model.SearchPhase
import pw.janyo.whatanime.model.SearchPreferences
import pw.janyo.whatanime.repository.ImageSearchGateway
import pw.janyo.whatanime.repository.ImageSearchOutput
import pw.janyo.whatanime.repository.SearchInputException
import pw.janyo.whatanime.repository.SearchFailure

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private val image = PlatformFile("input.png")
    private fun candidate(adult: Boolean = false) = SearchAnimeResultItem(
        SearchAniListResult(title = AniListTitleResult("test"), adult = adult),
        "test", video = "https://example.invalid/video", image = "https://example.invalid/image",
    )

    @Test
    fun repeatedInputBeforeCoroutineRunsStartsOnlyOneSearch() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val reply = CompletableDeferred<ImageSearchOutput>()
        var calls = 0
        val vm = MainViewModel(ImageSearchGateway { _, _ -> calls++; reply.await() }, SearchPreferences()) { it.name }
        try {
            vm.searchImageFile(image)
            vm.searchImageFile(PlatformFile("other.png"))
            assertEquals(SearchPhase.Loading, vm.searchState.value.phase)
            runCurrent()
            assertEquals(1, calls)
            reply.complete(ImageSearchOutput(image, listOf(candidate())))
            runCurrent()
            assertEquals(SearchPhase.Success, vm.searchState.value.phase)
        } finally {
            vm.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun fileFailureReturnsToIdleWithoutLeakingExceptionText() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val vm = MainViewModel(
            ImageSearchGateway { _, _ -> throw SearchInputException(SearchFailure.TooLarge) },
            SearchPreferences(),
        ) { "safe:${it.name}" }
        try {
            vm.searchImageFile(image)
            runCurrent()
            assertEquals(SearchPhase.Idle, vm.searchState.value.phase)
            assertEquals("safe:TooLarge", vm.searchState.value.errorMessage)
        } finally {
            vm.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun networkFailureClearsCandidatesAndUsesSafeMessage() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var calls = 0
        val vm = MainViewModel(ImageSearchGateway { file, _ ->
            if (++calls == 1) ImageSearchOutput(file, listOf(candidate()))
            else throw IllegalStateException("secret-key-in-url")
        }, SearchPreferences()) { "safe error" }
        try {
            vm.searchImageFile(image)
            runCurrent()
            vm.searchImageFile(PlatformFile("replacement.png"))
            runCurrent()
            assertEquals(SearchPhase.Error, vm.searchState.value.phase)
            assertTrue(vm.searchState.value.results.isEmpty())
            assertEquals("safe error", vm.searchState.value.errorMessage)
        } finally {
            vm.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun cancellationLeavesNoPermanentLoadingOrError() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val vm = MainViewModel(
            ImageSearchGateway { _, _ -> throw kotlinx.coroutines.CancellationException() }, SearchPreferences(),
        ) { "must not surface cancellation" }
        try {
            vm.searchImageFile(image)
            runCurrent()
            assertEquals(SearchPhase.Idle, vm.searchState.value.phase)
            assertEquals(null, vm.searchState.value.errorMessage)
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test
    fun cacheCopyFailureReturnsToSelectionAndRetryUsesSameInput() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var calls = 0
        val vm = MainViewModel(ImageSearchGateway { file, _ ->
            when (++calls) {
                1 -> throw SearchInputException(SearchFailure.CacheUnavailable)
                2 -> throw IllegalStateException("network")
                else -> ImageSearchOutput(file, listOf(candidate()))
            }
        }, SearchPreferences()) { it.name }
        try {
            vm.searchImageFile(image); runCurrent()
            assertEquals(SearchPhase.Idle, vm.searchState.value.phase)
            vm.searchImageFile(image); runCurrent()
            assertEquals(SearchPhase.Error, vm.searchState.value.phase)
            vm.retry(); runCurrent()
            assertEquals(SearchPhase.Success, vm.searchState.value.phase)
            assertEquals(image, vm.searchState.value.image)
            assertEquals(3, calls)
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test
    fun restoredWorkspaceNeverAutomaticallyResubmits() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var calls = 0
        val vm = MainViewModel(
            ImageSearchGateway { file, _ -> calls++; ImageSearchOutput(file, emptyList()) },
            SearchPreferences(),
            savedStateHandle = androidx.lifecycle.SavedStateHandle(mapOf("has_search" to true)),
        ) { it.name }
        try {
            runCurrent()
            assertEquals(0, calls)
            assertEquals(SearchPhase.Idle, vm.searchState.value.phase)
            assertTrue(vm.restorationNeedsSelection.value)
        } finally {
            vm.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun sharedFilterUpdatesAlreadyDisplayedResultsWithoutNetwork() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val preferences = SearchPreferences()
        var calls = 0
        val vm = MainViewModel(ImageSearchGateway { file, _ ->
            calls++
            ImageSearchOutput(file, listOf(candidate(true)))
        }, preferences) { it.name }
        try {
            vm.searchImageFile(image)
            runCurrent()
            assertEquals(SearchPhase.Success, vm.searchState.value.phase)
            preferences.setHideAdult(true)
            runCurrent()
            assertEquals(SearchPhase.FilteredEmpty, vm.searchState.value.phase)
            assertEquals(1, calls)
        } finally {
            vm.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }
}
