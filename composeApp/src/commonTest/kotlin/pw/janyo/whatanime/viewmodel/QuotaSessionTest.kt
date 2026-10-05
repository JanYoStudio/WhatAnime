package pw.janyo.whatanime.viewmodel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import pw.janyo.whatanime.model.QuotaPhase
import pw.janyo.whatanime.model.SearchQuota

class QuotaSessionTest {
    @Test
    fun zeroResponseIsReadyButHasNoComputableProgress() {
        val session = QuotaSession()
        assertEquals(QuotaPhase.NotLoaded, session.state.value.phase)
        session.complete(assertNotNull(session.begin()), SearchQuota())
        assertEquals(QuotaPhase.Ready, session.state.value.phase)
        assertNull(session.state.value.progress)
        assertFalse(session.state.value.exhausted)
    }

    @Test
    fun usedAboveTotalIsClampedWithoutNegativeRemaining() {
        val session = QuotaSession()
        session.complete(assertNotNull(session.begin()), SearchQuota(quota = 100, quotaUsed = 120))
        assertEquals(0, session.state.value.remaining)
        assertEquals(1f, session.state.value.progress)
        assertTrue(session.state.value.exhausted)
    }

    @Test
    fun negativeResponseDoesNotProduceIllegalProgress() {
        val session = QuotaSession()
        session.complete(assertNotNull(session.begin()), SearchQuota(quota = -1, quotaUsed = -2))
        assertNull(session.state.value.progress)
        assertNull(session.state.value.remaining)
        assertFalse(session.state.value.exhausted)
    }

    @Test
    fun previousCredentialsCannotOverwriteNewCredentialResponse() {
        val session = QuotaSession()
        val old = assertNotNull(session.begin())
        val current = assertNotNull(session.begin(credentialChanged = true))
        session.complete(old, SearchQuota(quota = 1000))
        assertEquals(QuotaPhase.Loading, session.state.value.phase)
        assertNull(session.state.value.value)
        session.complete(current, SearchQuota(quota = 20, quotaUsed = 3))
        session.fail(old)
        assertEquals(17, session.state.value.remaining)
        assertEquals(QuotaPhase.Ready, session.state.value.phase)
    }

    @Test
    fun failedRefreshRetainsExplicitlyStaleValueOnlyForSameCredentials() {
        val session = QuotaSession()
        session.complete(assertNotNull(session.begin()), SearchQuota(quota = 50))
        val refresh = assertNotNull(session.begin())
        assertNull(session.begin())
        session.fail(refresh)
        assertEquals(QuotaPhase.Error, session.state.value.phase)
        assertTrue(session.state.value.stale)
        assertEquals(50, session.state.value.value?.quota)
        session.begin(credentialChanged = true)
        assertNull(session.state.value.value)
        assertFalse(session.state.value.stale)
    }
}
