package pw.janyo.whatanime.model

import androidx.annotation.StringRes
import pw.janyo.whatanime.constant.StringConstant.resString

class ResourceException(@StringRes val resId: Int) : RuntimeException() {
    override val message: String
        get() = resId.resString()
}