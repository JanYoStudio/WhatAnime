package pw.janyo.whatanime.config

import android.content.Context
import com.tencent.mmkv.MMKV
import pw.janyo.whatanime.model.entity.NightMode

object Configure {
    private val kv = MMKV.mmkvWithID("configure", Context.MODE_PRIVATE)

    var lastVersion: Int
        set(value) {
            kv.encode("config_last_version", value)
        }
        get() = kv.decodeInt("config_last_version", 0)
    var hideSex: Boolean
        set(value) {
            kv.encode("config_hide_sex", value)
        }
        get() = kv.decodeBool("config_hide_sex", true)
    var apiKey: String
        set(value) {
            kv.encode("config_api_key", value)
        }
        get() = kv.decodeString("config_api_key", "")!!
    var nightMode: NightMode
        set(value) {
            kv.encode("nightMode", value.value)
        }
        get() {
            val value = kv.decodeInt("nightMode", NightMode.AUTO.value)
            return NightMode.entries.first { it.value == value }
        }
    var preferWebp: Boolean
        set(value) {
            kv.encode("preferWebp", value)
        }
        get() = kv.decodeBool("preferWebp", false)
    var cutBorders: Boolean
        set(value) {
            kv.encode("cutBorders", value)
        }
        get() = kv.decodeBool("cutBorders", false)
    var debugMode: Boolean
        set(value) {
            kv.encode("debugMode", value)
        }
        get() = kv.decodeBool("debugMode", false)
}