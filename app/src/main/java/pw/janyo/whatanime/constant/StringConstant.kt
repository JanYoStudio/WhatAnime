package pw.janyo.whatanime.constant

import androidx.annotation.StringRes
import pw.janyo.whatanime.R
import vip.mystery0.tools.context

object StringConstant {

    val hint_no_result = getString(R.string.hint_no_result)
    val hint_no_network = getString(R.string.hint_no_network)
    val hint_file_too_large = getString(R.string.hint_file_too_large)
    val hint_cache_make_dir_error = getString(R.string.hint_cache_make_dir_error)
    val hint_select_file_path_null = getString(R.string.hint_select_file_path_null)
    val hint_history_delete_done = getString(R.string.hint_history_delete_done)

    private fun getString(@StringRes id: Int): String = context().getString(id)
}