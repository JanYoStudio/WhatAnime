package pw.janyo.whatanime.viewmodel

import io.github.vinceglb.filekit.PlatformFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import pw.janyo.whatanime.model.AniListTitleResult
import pw.janyo.whatanime.model.SearchAniListResult
import pw.janyo.whatanime.model.SearchAnimeResultItem
import pw.janyo.whatanime.model.SearchPhase

class SearchSessionTest {
    private val imageA = PlatformFile("a.png")
    private val imageB = PlatformFile("b.png")

    private fun result(title: String, adult: Boolean = false) = SearchAnimeResultItem(
        aniList = SearchAniListResult(title = AniListTitleResult(title), adult = adult),
        fileName = title,
        video = "https://example.invalid/preview",
        image = "https://example.invalid/image",
    )

    @Test
    fun newImageNeverShowsPreviousCandidates() {
        val session = SearchSession()
        val a = assertNotNull(session.begin(imageA))
        session.complete(a, imageA, listOf(result("A")))
        val b = assertNotNull(session.begin(imageB))
        assertTrue(b > a)
        assertEquals(imageB, session.state.value.image)
        assertEquals(SearchPhase.Loading, session.state.value.phase)
        assertTrue(session.state.value.results.isEmpty())
        assertNull(session.state.value.errorMessage)
    }

    @Test
    fun emptyAndFilteredEmptyAreDifferentFromIdle() {
        val session = SearchSession(hideAdult = true)
        session.complete(assertNotNull(session.begin(imageA)), imageA, emptyList())
        assertEquals(SearchPhase.Empty, session.state.value.phase)
        session.complete(assertNotNull(session.begin(imageB)), imageB, listOf(result("adult", true)))
        assertEquals(SearchPhase.FilteredEmpty, session.state.value.phase)
        assertTrue(session.state.value.results.isEmpty())
    }

    @Test
    fun failedReplacementDoesNotRestorePreviousResults() {
        val session = SearchSession()
        session.complete(assertNotNull(session.begin(imageA)), imageA, listOf(result("A")))
        session.fail(assertNotNull(session.begin(imageB)), "network unavailable")
        assertEquals(SearchPhase.Error, session.state.value.phase)
        assertEquals(imageB, session.state.value.image)
        assertEquals("network unavailable", session.state.value.errorMessage)
        assertTrue(session.state.value.results.isEmpty())
    }

    @Test
    fun duplicateSubmissionIsRejectedWhileRequestIsActive() {
        val session = SearchSession()
        val request = assertNotNull(session.begin(imageA))
        assertNull(session.begin(imageB))
        assertEquals(imageA, session.state.value.image)
        session.complete(request, imageA, listOf(result("A")))
        assertNotNull(session.begin(imageB))
    }

    @Test
    fun lateCompletionAndFailureCannotReplaceCurrentImage() {
        val session = SearchSession()
        val a = assertNotNull(session.begin(imageA))
        session.complete(a, imageA, listOf(result("A")))
        val b = assertNotNull(session.begin(imageB))
        session.complete(a, imageA, listOf(result("late A")))
        session.fail(a, "late failure")
        assertEquals(SearchPhase.Loading, session.state.value.phase)
        session.complete(b, imageB, listOf(result("B")))
        // 同一轮终态不能被重复回调覆盖。
        session.fail(b, "duplicate failure")
        assertEquals("B", session.state.value.results.single().fileName)
        assertEquals(SearchPhase.Success, session.state.value.phase)
    }

    @Test
    fun filteringImmediatelyRecomputesCurrentResultsWithoutSearching() {
        val session = SearchSession()
        session.complete(assertNotNull(session.begin(imageA)), imageA, listOf(result("adult", true)))
        session.setHideAdult(true)
        assertEquals(SearchPhase.FilteredEmpty, session.state.value.phase)
        assertTrue(session.state.value.results.isEmpty())
        session.setHideAdult(false)
        assertEquals(SearchPhase.Success, session.state.value.phase)
        assertEquals("adult", session.state.value.results.single().fileName)
    }

    @Test
    fun filterChangedDuringLoadingIsAppliedToReturnedCandidates() {
        val session = SearchSession()
        val request = assertNotNull(session.begin(imageA))
        session.setHideAdult(true)
        session.complete(request, imageA, listOf(result("safe"), result("adult", true)))
        assertEquals(listOf("safe"), session.state.value.results.map { it.fileName })
        assertFalse(session.state.value.results.any { it.aniList.adult })
    }

    @Test
    fun invalidInputResetsSafelyAndCannotBeResurrectedByLateResult() {
        val session = SearchSession()
        val request = assertNotNull(session.begin(imageA))
        session.reject(request, "too large")
        assertEquals(SearchPhase.Idle, session.state.value.phase)
        assertNull(session.state.value.image)
        assertEquals("too large", session.state.value.errorMessage)
        session.complete(request, imageA, listOf(result("late")))
        assertTrue(session.state.value.results.isEmpty())
    }

    @Test
    fun errorAcknowledgementDoesNotTurnFailedSearchIntoEmptySuccess() {
        val session = SearchSession()
        session.fail(assertNotNull(session.begin(imageA)), "offline")
        val errorId = session.state.value.requestId
        session.acknowledgeError(errorId)
        assertEquals(SearchPhase.Error, session.state.value.phase)
        assertEquals("offline", session.state.value.errorMessage)
        assertTrue(session.state.value.errorAcknowledged)
        session.begin(imageB)
        session.acknowledgeError(errorId)
        assertFalse(session.state.value.errorAcknowledged)
    }
}
