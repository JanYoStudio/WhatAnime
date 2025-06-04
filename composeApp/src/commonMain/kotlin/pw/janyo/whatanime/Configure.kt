package pw.janyo.whatanime

import pw.janyo.whatanime.model.DebugHttpInfo
import pw.janyo.whatanime.ui.theme.NightMode

expect inline fun <reified T> getConfiguration(key: String, defaultValue: T): T
expect inline fun <reified T> setConfiguration(key: String, value: T)

object Configure {
    var lastVersion: Int
        set(value) = setConfiguration("config_last_version", value)
        get() = getConfiguration("config_last_version", 0)
    var hideSex: Boolean
        set(value) = setConfiguration("config_hide_sex", value)
        get() = getConfiguration("config_hide_sex", true)
    var apiKey: String
        set(value) = setConfiguration("config_api_key", value)
        get() = getConfiguration("config_api_key", "")
    var nightMode: NightMode
        set(value) = setConfiguration("nightMode", value.value)
        get() {
            val value = getConfiguration("nightMode", NightMode.AUTO.value)
            return NightMode.entries.first { it.value == value }
        }
    var preferWebp: Boolean
        set(value) = setConfiguration("preferWebp", value)
        get() = getConfiguration("preferWebp", false)
    var cutBorders: Boolean
        set(value) = setConfiguration("cutBorders", value)
        get() = getConfiguration("cutBorders", false)
    var debugMode: Boolean
        set(value) = setConfiguration("debugMode", value)
        get() = getConfiguration("debugMode", false)
}

object Constant {
    const val baseUrl = "https://api.trace.moe/"

    const val whatAnimeUrl = "https://trace.moe/about"
    const val janYoStudioUrl = "https://studio.janyos.top"

    const val donateUrl = "https://github.com/sponsors/soruly"
}

// Global list to store HTTP responses
val httpResponses = mutableListOf<DebugHttpInfo>()