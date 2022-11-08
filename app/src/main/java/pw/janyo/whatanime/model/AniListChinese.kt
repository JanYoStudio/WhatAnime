package pw.janyo.whatanime.model

import com.squareup.moshi.Json

data class AniListChineseRequest(
    val variables: AniListChineseRequestVar,
    val query: String = "query (\$id: Int) {Media (id: \$id, type: ANIME) {id title{romaji english native}}}"
)

data class AniListChineseRequestVar(
    val id: Long,
)

data class AniListChinese(
    val data: AniListChineseData,
)

data class AniListChineseData(
    @Json(name = "Media")
    val media: AniListChineseMedia,
)

data class AniListChineseMedia(
    val id: Long,
    val title: AniListChineseTitle,
)

data class AniListChineseTitle(
    val romaji: String,
    val english: String,
    val native: String,
    val chinese: String,
)