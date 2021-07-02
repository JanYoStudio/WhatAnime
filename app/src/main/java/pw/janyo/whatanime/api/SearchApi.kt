package pw.janyo.whatanime.api

import okhttp3.RequestBody
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.SearchQuota
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SearchApi {
    @POST("/search")
    suspend fun search(
        @Body body: RequestBody,
        @Query("anilistInfo") anilistInfo: String = "",
    ): Animation

    @GET("/search")
    suspend fun searchByUrl(
        @Query("url") url: String,
        @Query("anilistInfo") anilistInfo: String = "",
    ): Animation

    @GET("/me")
    suspend fun getMe(): SearchQuota
}