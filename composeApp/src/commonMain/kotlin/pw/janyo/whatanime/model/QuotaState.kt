package pw.janyo.whatanime.model

enum class QuotaPhase { NotLoaded, Loading, Ready, Error }

data class QuotaState(
    val requestId: Long = 0,
    val phase: QuotaPhase = QuotaPhase.NotLoaded,
    val value: SearchQuota? = null,
) {
    val stale: Boolean get() = value != null && phase != QuotaPhase.Ready
    val remaining: Int? get() = value?.takeIf { it.quota >= 0 && it.quotaUsed >= 0 }?.let {
        (it.quota.toLong() - it.quotaUsed).coerceAtLeast(0).toInt()
    }
    val progress: Float? get() = value?.takeIf { it.quota > 0 && it.quotaUsed >= 0 }?.let {
        (it.quotaUsed.toFloat() / it.quota).coerceIn(0f, 1f)
    }
    val exhausted: Boolean get() = value?.let { it.quota > 0 && it.quotaUsed >= it.quota } == true
}
