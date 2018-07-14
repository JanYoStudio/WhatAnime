package pw.janyo.whatanime.constant

import androidx.annotation.StringRes
import pw.janyo.whatanime.R
import pw.janyo.whatanime.config.APP

object StringConstant {
	private val context = APP.instance

	val hint_cache_make_dir_error = getString(R.string.hint_cache_make_dir_error)
	val hint_no_result = getString(R.string.hint_no_result)

	private fun getString(@StringRes id: Int): String = context.getString(id)
}