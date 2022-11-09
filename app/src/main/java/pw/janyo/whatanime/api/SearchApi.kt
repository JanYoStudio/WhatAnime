package pw.janyo.whatanime.api

import okhttp3.RequestBody
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.model.SearchAnimeResult
import pw.janyo.whatanime.model.SearchQuota
import retrofit2.http.*

interface SearchApi {
    @POST("/search")
    suspend fun search(
        @Body body: RequestBody,
        @Query("cutBorders") cutBorders: String = "",
        @Query("anilistInfo") anilistInfo: String = "",
        @Header("x-trace-key") key: String = Configure.apiKey,
    ): SearchAnimeResult

    @POST("/search")
    suspend fun searchNoCut(
        @Body body: RequestBody,
        @Query("anilistInfo") anilistInfo: String = "",
        @Header("x-trace-key") key: String = Configure.apiKey,
    ): SearchAnimeResult

    @GET("/me")
    suspend fun getMe(@Header("x-trace-key") key: String = Configure.apiKey): SearchQuota
}