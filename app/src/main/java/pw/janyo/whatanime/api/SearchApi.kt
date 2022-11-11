package pw.janyo.whatanime.api

import com.google.common.net.HttpHeaders
import okhttp3.RequestBody
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.model.SearchAnimeResult
import pw.janyo.whatanime.model.SearchQuota
import retrofit2.http.*

interface SearchApi {
    companion object {
        const val ApiKeyHeader = "x-trace-key"
    }

    @POST("/search")
    suspend fun search(
        @Body body: RequestBody,
        @Header(HttpHeaders.CONTENT_TYPE) contentType: String,
        @Query("cutBorders") cutBorders: String = "",
        @Query("anilistInfo") anilistInfo: String = "",
        @Header(ApiKeyHeader) key: String = Configure.apiKey,
    ): SearchAnimeResult

    @POST("/search")
    suspend fun searchNoCut(
        @Body body: RequestBody,
        @Header(HttpHeaders.CONTENT_TYPE) contentType: String,
        @Query("anilistInfo") anilistInfo: String = "",
        @Header(ApiKeyHeader) key: String = Configure.apiKey,
    ): SearchAnimeResult

    @GET("/me")
    suspend fun getMe(@Header("x-trace-key") key: String = Configure.apiKey): SearchQuota
}