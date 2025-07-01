package pw.janyo.whatanime.utils

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlin.math.pow
import kotlin.math.roundToLong

fun formatDecimal(value: Double, digits: Int = 1): String {
    val multiplier = 10.0.pow(digits + 1)
    val rounded = (value * multiplier).roundToLong() / multiplier
    return rounded.toString()
}

fun formatEpisode(episodeElement: JsonElement?): String? {
    return when (episodeElement) {
        null, is JsonNull -> null
        is JsonPrimitive -> {
            if (episodeElement.isString) {
                episodeElement.content
            } else {
                episodeElement.content
            }
        }
        is JsonArray -> {
            episodeElement.joinToString(", ") {
                if (it is JsonPrimitive) {
                    it.content
                } else {
                    ""
                }
            }.ifEmpty { null }
        }
        else -> null
    }
}