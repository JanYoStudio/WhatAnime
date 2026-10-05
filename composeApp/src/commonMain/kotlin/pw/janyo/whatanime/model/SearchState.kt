package pw.janyo.whatanime.model

import io.github.vinceglb.filekit.PlatformFile

enum class SearchPhase {
    Idle, Loading, Success, Empty, FilteredEmpty, Error,
}

/** 图片、候选和错误只能来自同一 requestId；原始未过滤结果不暴露给界面。 */
data class SearchState(
    val requestId: Long = 0,
    val phase: SearchPhase = SearchPhase.Idle,
    val image: PlatformFile? = null,
    val results: List<SearchAnimeResultItem> = emptyList(),
    val errorMessage: String? = null,
    val errorAcknowledged: Boolean = false,
)
