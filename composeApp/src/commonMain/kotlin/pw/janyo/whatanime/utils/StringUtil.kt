package pw.janyo.whatanime.utils

import kotlin.math.pow
import kotlin.math.roundToLong

fun formatDecimal(value: Double, digits: Int = 1): String {
    val multiplier = 10.0.pow(digits + 1)
    val rounded = (value * multiplier).roundToLong() / multiplier
    return rounded.toString()
}

fun formatEpisode(episodeElement: kotlinx.serialization.json.JsonElement?): String? {
    return when (episodeElement) {
        null, is kotlinx.serialization.json.JsonNull -> null
        is kotlinx.serialization.json.JsonPrimitive -> {
            if (episodeElement.isString) {
                episodeElement.content
            } else {
                // For numbers or booleans, content gives their string representation
                episodeElement.content
            }
        }
        is kotlinx.serialization.json.JsonArray -> {
            episodeElement.joinToString(", ") {
                if (it is kotlinx.serialization.json.JsonPrimitive) {
                    it.content
                } else {
                    "" // Or some other placeholder for unexpected array elements
                }
            }.ifEmpty { null }
        }
        else -> null // Or a placeholder like "Unknown"
    }
}