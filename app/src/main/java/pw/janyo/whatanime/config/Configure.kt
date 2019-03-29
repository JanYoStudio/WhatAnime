package pw.janyo.whatanime.config

import android.content.Context

object Configure {
	private val sharedPreference = APP.context.getSharedPreferences("configure", Context.MODE_PRIVATE)

	var hideSex: Boolean
		set(value) = sharedPreference.edit().putBoolean("config_hide_sex", value).apply()
		get() = sharedPreference.getBoolean("config_hide_sex", true)

	var language: Int
		set(value) = sharedPreference.edit().putInt("config_language", value).apply()
		get() = sharedPreference.getInt("config_language", 0)

	var useInAppImageSelect: Boolean
		set(value) = sharedPreference.edit().putBoolean("config_use_in_app_image_select", value).apply()
		get() = sharedPreference.getBoolean("config_use_in_app_image_select", false)
}