package pw.janyo.whatanime.api

import okhttp3.RequestBody
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.SearchQuota
import retrofit2.http.*

interface SearchApi {
    @POST("/search")
    suspend fun search(
        @Body body: RequestBody,
        @Query("anilistInfo") anilistInfo: String = "",
        @Header("x-trace-key") key: String = Configure.apiKey,
    ): Animation

    @GET("/search")
    suspend fun searchByUrl(
        @Query("url") url: String,
        @Query("anilistInfo") anilistInfo: String = "",
        @Header("x-trace-key") key: String = Configure.apiKey,
    ): Animation

    @GET("/me")
    suspend fun getMe(@Header("x-trace-key") key: String = Configure.apiKey): SearchQuota
}