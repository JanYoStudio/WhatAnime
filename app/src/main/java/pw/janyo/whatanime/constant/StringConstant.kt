package pw.janyo.whatanime.constant

import androidx.annotation.StringRes
import pw.janyo.whatanime.R
import pw.janyo.whatanime.config.APP

object StringConstant {
	private val context = APP.instance

	val hint_cache_make_dir_error = getString(R.string.hint_cache_make_dir_error)
	val hint_no_result = getString(R.string.hint_no_result)
	val hint_response_error = getString(R.string.hint_response_error)
	val hint_origin_file_null = getString(R.string.hint_origin_file_null)
	val hint_file_copy_error = getString(R.string.hint_file_copy_error)
	val hint_history_delete_done = getString(R.string.hint_history_delete_done)
	val hint_history_delete_error = getString(R.string.hint_history_delete_error)

	private fun getString(@StringRes id: Int): String = context.getString(id)
}