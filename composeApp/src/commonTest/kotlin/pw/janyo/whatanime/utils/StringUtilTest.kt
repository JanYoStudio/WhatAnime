package pw.janyo.whatanime.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StringUtilTest {
    @Test
    fun missingEpisodeDoesNotProduceLiteralNull() {
        assertNull(formatEpisode(null))
        assertNull(formatEpisode(JsonNull))
    }

    @Test
    fun numericAndSpecialEpisodesKeepTheirDisplayValue() {
        assertEquals("12", formatEpisode(JsonPrimitive(12)))
        assertEquals("OVA", formatEpisode(JsonPrimitive("OVA")))
    }

    @Test
    fun episodeArrayPreservesOrderAndUsesReadableSeparators() {
        assertEquals("1, 2, SP", formatEpisode(Json.parseToJsonElement("""[1,2,"SP"]""")))
    }

    @Test
    fun emptyArrayAndUnsupportedObjectHaveNoEpisode() {
        assertNull(formatEpisode(Json.parseToJsonElement("[]")))
        assertNull(formatEpisode(Json.parseToJsonElement("""{"episode":1}""")))
    }
}
