package pw.janyo.whatanime

import com.ctrip.flight.mmkv.MMKVMode
import com.ctrip.flight.mmkv.mmkvWithID
import pw.janyo.whatanime.model.DebugHttpInfo
import pw.janyo.whatanime.ui.theme.NightMode

object Configure {
    private val kv = mmkvWithID("configure", MMKVMode.SINGLE_PROCESS)

    var lastVersion: Int
        set(value) {
            kv.set("config_last_version", value)
        }
        get() = kv.takeInt("config_last_version", 0)
    var hideSex: Boolean
        set(value) {
            kv.set("config_hide_sex", value)
        }
        get() = kv.takeBoolean("config_hide_sex", true)
    var apiKey: String
        set(value) {
            kv.set("config_api_key", value)
        }
        get() = kv.takeString("config_api_key", "")
    var nightMode: NightMode
        set(value) {
            kv.set("nightMode", value.value)
        }
        get() {
            val value = kv.takeInt("nightMode", NightMode.AUTO.value)
            return NightMode.entries.first { it.value == value }
        }
    var preferWebp: Boolean
        set(value) {
            kv.set("preferWebp", value)
        }
        get() = kv.takeBoolean("preferWebp", false)
    var cutBorders: Boolean
        set(value) {
            kv.set("cutBorders", value)
        }
        get() = kv.takeBoolean("cutBorders", false)
    var debugMode: Boolean
        set(value) {
            kv.set("debugMode", value)
        }
        get() = kv.takeBoolean("debugMode", false)
}

object Constant {
    const val baseUrl = "https://api.trace.moe/"

    const val whatAnimeUrl="https://trace.moe/about"
    const val janYoStudioUrl="https://studio.janyos.top"

    const val donateUrl = "https://github.com/sponsors/soruly"
}

// Global list to store HTTP responses
val httpResponses = mutableListOf<DebugHttpInfo>()
