package pw.janyo.whatanime.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Configure 负责持久化，这里只广播跨页面共享的检索偏好，不包含凭据。 */
class SearchPreferences(
    cutBorders: Boolean = false,
    hideAdult: Boolean = false,
    preferWebp: Boolean = false,
) {
    private val mutableCutBorders = MutableStateFlow(cutBorders)
    val cutBorders = mutableCutBorders.asStateFlow()
    private val mutableHideAdult = MutableStateFlow(hideAdult)
    val hideAdult = mutableHideAdult.asStateFlow()
    private val mutablePreferWebp = MutableStateFlow(preferWebp)
    val preferWebp = mutablePreferWebp.asStateFlow()

    fun setCutBorders(value: Boolean) { mutableCutBorders.value = value }
    fun setHideAdult(value: Boolean) { mutableHideAdult.value = value }
    fun setPreferWebp(value: Boolean) { mutablePreferWebp.value = value }
}
