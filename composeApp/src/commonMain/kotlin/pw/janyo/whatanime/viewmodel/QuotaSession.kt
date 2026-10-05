package pw.janyo.whatanime.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pw.janyo.whatanime.model.QuotaPhase
import pw.janyo.whatanime.model.QuotaState
import pw.janyo.whatanime.model.SearchQuota

/** 由设置 ViewModel 的主线程串行调用，不保存凭据本身。 */
class QuotaSession {
    private val mutableState = MutableStateFlow(QuotaState())
    val state = mutableState.asStateFlow()
    private var sequence = 0L

    fun begin(credentialChanged: Boolean = false): Long? {
        val current = mutableState.value
        if (!credentialChanged && current.phase == QuotaPhase.Loading) return null
        val request = ++sequence
        mutableState.value = QuotaState(
            request, QuotaPhase.Loading, if (credentialChanged) null else current.value,
        )
        return request
    }

    fun complete(requestId: Long, quota: SearchQuota) {
        if (!isActive(requestId)) return
        mutableState.value = QuotaState(requestId, QuotaPhase.Ready, quota)
    }

    fun fail(requestId: Long) {
        if (!isActive(requestId)) return
        mutableState.value = mutableState.value.copy(phase = QuotaPhase.Error)
    }

    private fun isActive(requestId: Long): Boolean = mutableState.value.let {
        it.requestId == requestId && it.phase == QuotaPhase.Loading
    }
}
