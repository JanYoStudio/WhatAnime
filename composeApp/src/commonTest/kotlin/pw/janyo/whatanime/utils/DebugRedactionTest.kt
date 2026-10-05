package pw.janyo.whatanime.utils

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

class DebugRedactionTest {
    @Test
    fun credentialsAndVideoTokensNeverReachDisplayOrClipboard() {
        val body = """{"ip":"192.0.2.1","apiKey":"abc+secret","video":"https://example.invalid/v?token=video-secret&size=l","token":"another-secret","title":"test"}"""
        val redacted = redactDebugResponse(body, "abc+secret")
        listOf("192.0.2.1", "abc+secret", "video-secret", "another-secret").forEach {
            assertFalse(redacted.contains(it), "敏感内容未被移除")
        }
        assertTrue(redacted.contains("test"))
        Json.parseToJsonElement(redacted)
    }

    @Test
    fun encodedSecretAndNestedTokensAreRedacted() {
        val body = """{"nested":[{"authorization":"Bearer private"}],"url":"https://example.invalid/?key=private%2Bkey&token=one"}"""
        val redacted = redactDebugResponse(body, "private+key")
        assertFalse(redacted.contains("private"))
        assertFalse(redacted.contains("token=one"))
    }

    @Test
    fun malformedResponseIsOmittedRatherThanLeakingUnknownPayload() {
        val body = "<html>opaque-secret token in a broken response"
        val redacted = redactDebugResponse(body, "")
        assertFalse(redacted.contains("opaque-secret"))
        assertFalse(redacted.contains("<html>"))
        assertTrue(redacted.isNotBlank())
    }
}
