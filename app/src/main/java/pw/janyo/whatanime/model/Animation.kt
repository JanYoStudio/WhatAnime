package pw.janyo.whatanime.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

val searchAnimeResultAdapter: JsonAdapter<SearchAnimeResult> =
    moshi.adapter(SearchAnimeResult::class.java)

data class SearchAnimeResult(
    val error: String = "",
    val frameCount: Long = 0,
    val result: List<SearchAnimeResultItem>,
)

data class SearchAnimeResultItem(
    @Json(name = "anilist")
    val aniList: SearchAniListResult,
    @Json(name = "filename")
    val fileName: String,
    val from: Double = 0.0,
    val to: Double = 0.0,
    val similarity: Double = 0.0,
    val video: String,
    val image: String,
)

data class SearchAniListResult(
    val id: Long = 0,
    val idMal: Long = 0,
    val title: AniListTitleResult,
    val synonyms: List<String> = emptyList(),
    @Json(name = "isAdult")
    val adult: Boolean = false,
)

data class AniListTitleResult(
    val native: String? = "",
    val romaji: String? = "",
    val english: String? = "",
    var chinese: String? = "",
)