package pw.janyo.whatanime.model

import io.github.vinceglb.filekit.PlatformFile

enum class DetailPhase { Loading, Ready, Empty, FilteredEmpty, NotFound, Legacy, Error }

data class DetailState(
    val historyId: Int? = null,
    val phase: DetailPhase = DetailPhase.Loading,
    val image: PlatformFile? = null,
    val savedAt: Long? = null,
    val results: List<SearchAnimeResultItem> = emptyList(),
)
