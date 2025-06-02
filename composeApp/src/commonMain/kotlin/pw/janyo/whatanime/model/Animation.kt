package pw.janyo.whatanime.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchAnimeResult(
    val error: String = "",
    val frameCount: Long = 0,
    val result: List<SearchAnimeResultItem>,
)

@Serializable
data class SearchAnimeResultItem(
    @SerialName("anilist")
    val aniList: SearchAniListResult,
    @SerialName("filename")
    val fileName: String,
    val episode: String?,
    val from: Double = 0.0,
    val to: Double = 0.0,
    val similarity: Double = 0.0,
    val video: String,
    val image: String,
)

@Serializable
data class SearchAniListResult(
    val id: Long? = 0,
    val idMal: Long? = 0,
    val title: AniListTitleResult,
    @SerialName("isAdult")
    val adult: Boolean = false,
)

@Serializable
data class AniListTitleResult(
    val native: String? = "",
)