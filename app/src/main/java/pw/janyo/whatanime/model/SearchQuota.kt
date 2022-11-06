package pw.janyo.whatanime.model

data class SearchQuota(
    val priority: Int = 0,
    val concurrency: Int = 0,
    val quota: Int = 0,
    val quotaUsed: Int = 0,
) {
    companion object {
        val EMPTY = SearchQuota()
    }
}
