package pw.janyo.whatanime.constant

import androidx.annotation.StringRes
import pw.janyo.whatanime.context

object StringConstant {
    private val stringMap = HashMap<Int, String>()

    fun @receiver:StringRes Int.resString(): String {
        return stringMap[this] ?: context.getString(this).also {
            stringMap[this] = it
        }
    }
}