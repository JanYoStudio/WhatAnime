package pw.janyo.whatanime.config

import android.content.Context
import com.tencent.mmkv.MMKV
import vip.mystery0.tools.utils.use

object Configure {
    private val sharedPreference = MMKV.mmkvWithID("configure", Context.MODE_PRIVATE)!!

    var lastVersion: Int
        set(value) = sharedPreference.use { putInt("config_last_version", value) }
        get() = sharedPreference.getInt("config_last_version", 0)

    var hideSex: Boolean
        set(value) = sharedPreference.use { putBoolean("config_hide_sex", value) }
        get() = sharedPreference.getBoolean("config_hide_sex", true)

    var language: Int
        set(value) = sharedPreference.use { putInt("config_language", value) }
        get() = sharedPreference.getInt("config_language", 0)

    var previewConfig: Int
        set(value) = sharedPreference.use { putInt("config_preview_config", value) }
        get() = sharedPreference.getInt("config_preview_config", 1)

    var requestType: Int
        set(value) = sharedPreference.use { putInt("config_request_type", value) }
        get() = sharedPreference.getInt("config_request_type", 0)

    var alreadyReadNotice: Boolean
        set(value) = sharedPreference.use { putBoolean("config_read_notice", value) }
        get() = sharedPreference.getBoolean("config_read_notice", false)

    var lastAppCenterSecret: String
        set(value) = sharedPreference.use { putString("config_last_app_center_secret", value) }
        get() = sharedPreference.getString("config_last_app_center_secret", "")!!
}