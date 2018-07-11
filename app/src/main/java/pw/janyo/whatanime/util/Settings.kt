package pw.janyo.whatanime.util

import android.content.Context
import android.content.SharedPreferences

import pw.janyo.whatanime.APP

object Settings {
	private val sharedPreference = APP.context!!.getSharedPreferences("settings", Context.MODE_PRIVATE)

	var resultNumber: Int
		get() = sharedPreference.getInt("resultNumber", 1)
		set(value) = sharedPreference.edit().putInt("resultNumber", value).apply()

	val isFirst: Boolean
		get() = sharedPreference.getBoolean("isFirst", true)

	var similarity: Float
		get() = sharedPreference.getFloat("similarity", 0f)
		set(value) = sharedPreference.edit().putFloat("similarity", value).apply()

	var filter: String?
		get() = sharedPreference.getString("filter", null)
		set(value) = sharedPreference.edit().putString("filter", value).apply()

	fun setFirst() {
		sharedPreference.edit().putBoolean("isFirst", false).apply()
	}
}
