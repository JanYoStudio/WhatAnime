package pw.janyo.whatanime.constant

import androidx.annotation.StringRes
import pw.janyo.whatanime.R
import pw.janyo.whatanime.config.APP
import vip.mystery0.tools.context

object StringConstant {

	val hint_no_result = getString(R.string.hint_no_result)

	private fun getString(@StringRes id: Int): String = context().getString(id)
}