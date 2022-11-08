package pw.janyo.whatanime.api

import pw.janyo.whatanime.model.AniListChinese
import pw.janyo.whatanime.model.AniListChineseRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AniListChineseApi {
    @POST("/anilist/")
    suspend fun getAniListInfo(
        @Body request: AniListChineseRequest,
    ): AniListChinese
}