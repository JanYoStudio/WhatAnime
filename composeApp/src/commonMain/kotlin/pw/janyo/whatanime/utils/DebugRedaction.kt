package pw.janyo.whatanime.utils

import io.ktor.http.encodeURLParameter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private val sensitiveFields = setOf(
    "apikey", "xtracekey", "token", "accesstoken", "refreshtoken", "authorization", "password", "secret",
)
private val sensitiveQuery = Regex(
    "(?i)([?&](?:token|key|api[_-]?key|access[_-]?token|auth|signature)=)[^&#\\s]*",
)
private val ipAddress = Regex("\\b(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\\b")

/** 在进入调试缓存之前处理；无法安全解析的正文不允许作为显示或复制兜底。 */
fun redactDebugResponse(body: String, apiKey: String): String {
    fun safeText(value: String): String {
        var safe = sensitiveQuery.replace(value) { "${it.groupValues[1]}[redacted]" }
        if (apiKey.isNotEmpty()) {
            safe = safe.replace(apiKey, "[redacted]")
                .replace(apiKey.encodeURLParameter(), "[redacted]", ignoreCase = true)
        }
        return ipAddress.replace(safe, "0.0.0.0")
    }

    fun sanitize(element: JsonElement): JsonElement = when (element) {
        is JsonObject -> JsonObject(element.mapValues { (key, value) ->
            val normalized = key.lowercase().filter { it.isLetterOrDigit() }
            if (normalized in sensitiveFields) JsonPrimitive("[redacted]") else sanitize(value)
        })
        is JsonArray -> JsonArray(element.map(::sanitize))
        is JsonPrimitive -> if (element.isString) JsonPrimitive(safeText(element.content)) else element
    }

    return try {
        sanitize(Json.parseToJsonElement(body)).toString()
    } catch (_: Exception) {
        "[response omitted]"
    }
}
