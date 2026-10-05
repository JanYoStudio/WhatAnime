package pw.janyo.whatanime.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HistoryPolicyTest {
    @Test fun expiryIsStrictlyAfterTenMinutes() {
        assertFalse(isVideoExpired(1000, 601000))
        assertTrue(isVideoExpired(1000, 601001))
        assertFalse(isVideoExpired(2000, 1000))
    }
    @Test fun cacheDeletionNeverEscapesManagedDirectoryOrDeletesSharedImage() {
        assertTrue(canDeleteCache("/app/cacheImage/a", "/app/cacheImage", emptyList()))
        assertFalse(canDeleteCache("/user/photo.png", "/app/cacheImage", emptyList()))
        assertFalse(canDeleteCache("/app/cacheImage/../a", "/app/cacheImage", emptyList()))
        assertFalse(canDeleteCache("/app/cacheImage/a", "/app/cacheImage", listOf("/app/cacheImage/a")))
    }
    @Test fun videoQueryKeepsExistingTokenAndFragment() {
        assertEquals("https://example.invalid/v?size=l", previewUrl("https://example.invalid/v"))
        assertEquals("https://example.invalid/v?token=abc%2Bdef&size=l#x", previewUrl("https://example.invalid/v?token=abc%2Bdef#x"))
        assertEquals("https://example.invalid/v?token=x&size=l", previewUrl("https://example.invalid/v?token=x&size=s"))
    }
}
