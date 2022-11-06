package pw.janyo.whatanime.config

import android.content.Context
import com.tencent.mmkv.MMKV
import pw.janyo.whatanime.BuildConfig
import pw.janyo.whatanime.model.entity.NightMode

object Configure {
    private val kv = MMKV.mmkvWithID("configure", Context.MODE_PRIVATE)!!

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

    var language: Int
        set(value) {
            kv.encode("config_language", value)
        }
        get() = kv.decodeInt("config_language", 0)

    var alreadyReadNotice: Boolean
        set(value) {
            kv.encode("config_read_notice", value)
        }
        get() = kv.decodeBool("config_read_notice", false)

    var apiKey: String
        set(value) {
            kv.encode("config_api_key", value)
        }
        get() = kv.decodeString("config_api_key", "")!!
    var allowSendCrashReport: Boolean
        set(value) {
            kv.encode("allowSendCrashReport", value)
        }
        get() = kv.decodeBool("allowSendCrashReport", !BuildConfig.DEBUG)
    var nightMode: NightMode
        set(value) {
            kv.encode("nightMode", value.value)
        }
        get() {
            val value = kv.decodeInt("nightMode", NightMode.AUTO.value)
            return NightMode.values().first { it.value == value }
        }
}