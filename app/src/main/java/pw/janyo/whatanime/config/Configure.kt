package pw.janyo.whatanime.config

import android.content.Context

object Configure {
	private val sharedPreference = APP.context.getSharedPreferences("configure", Context.MODE_PRIVATE)

	var hideSex: Boolean
		set(value) = sharedPreference.edit().putBoolean("config_hide_sex", value).apply()
		get() = sharedPreference.getBoolean("config_hide_sex", true)
}