package pw.janyo.whatanime.api

import okhttp3.RequestBody
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.SearchQuota
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SearchApi {
    @POST("/api/search")
    suspend fun search(@Body body: RequestBody): Animation

    @GET("/api/search")
    suspend fun searchByUrl(@Query("url") url: String): Animation

    @GET("/api/me")
    suspend fun getMe(): SearchQuota
}