package pw.janyo.whatanime.util

import android.content.Context
import pw.janyo.whatanime.APP

object Settings {
    private val sharedPreference = APP.getContext().getSharedPreferences("settings", Context.MODE_PRIVATE)

    var resultNumber: Int
        set(value) = sharedPreference.edit().putInt("resultNumber", value).apply()
        get() = sharedPreference.getInt("resultNumber", 1)

    var isFirst: Boolean
        set(value) = sharedPreference.edit().putBoolean("isFirst", value).apply()
        get() = sharedPreference.getBoolean("isFirst", true)

    var similarity: Float
        set(value) = sharedPreference.edit().putFloat("similarity", value).apply()
        get() = sharedPreference.getFloat("similarity", 0F)

    var filter: String
        set(value) = sharedPreference.edit().putString("filter", value).apply()
        get() = sharedPreference.getString("filter", null)
}