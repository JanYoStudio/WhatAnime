package pw.janyo.whatanime.model

import kotlin.test.Test
import kotlin.test.assertEquals

class DebugResponseStoreTest {
    @Test fun storeKeepsOnlyLastFiveSafeEntries() {
        val store = DebugResponseStore()
        repeat(9) { store.append(DebugHttpInfo(it.toString(), "time", "[REDACTED]")) }
        assertEquals(listOf("4", "5", "6", "7", "8"), store.entries.value.map { it.title })
    }
}
