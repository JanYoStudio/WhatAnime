package pw.janyo.whatanime.config

import android.content.Context
import vip.mystery0.tools.context

object Configure {
	private val sharedPreference = context().getSharedPreferences("configure", Context.MODE_PRIVATE)

	var hideSex: Boolean
		set(value) = sharedPreference.edit().putBoolean("config_hide_sex", value).apply()
		get() = sharedPreference.getBoolean("config_hide_sex", true)

	var language: Int
		set(value) = sharedPreference.edit().putInt("config_language", value).apply()
		get() = sharedPreference.getInt("config_language", 0)

	var useInAppImageSelect: Boolean
		set(value) = sharedPreference.edit().putBoolean("config_use_in_app_image_select", value).apply()
		get() = sharedPreference.getBoolean("config_use_in_app_image_select", false)
	var nightMode: Int
		set(value) = sharedPreference.edit()
				.putInt("config_night_mode", value)
				.apply()
		get() = sharedPreference.getInt("config_night_mode", 3)
	var previewConfig: Int
		set(value) = sharedPreference.edit()
				.putInt("config_preview_config", value)
				.apply()
		get() = sharedPreference.getInt("config_preview_config", 0)
	var enableCloudCompress: Boolean
		set(value) = sharedPreference.edit()
				.putBoolean("config_cloud_compress", value)
				.apply()
		get() = sharedPreference.getBoolean("config_cloud_compress", true)
	var deviceID: String
		set(value) = sharedPreference.edit()
				.putString("config_device_id", value)
				.apply()
		get() = sharedPreference.getString("config_device_id", "")!!
}