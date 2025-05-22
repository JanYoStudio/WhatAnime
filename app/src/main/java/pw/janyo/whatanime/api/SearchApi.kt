package pw.janyo.whatanime.api

import com.google.common.net.HttpHeaders
import okhttp3.RequestBody
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.model.SearchAnimeResult
import pw.janyo.whatanime.model.SearchQuota
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Tag

interface SearchApi {
    companion object {
        const val REQUEST_TAG_METHOD_NAME = "method_name"
        const val ApiKeyHeader = "x-trace-key"
    }

    @POST("/search")
    suspend fun search(
        @Body body: RequestBody,
        @Header(HttpHeaders.CONTENT_TYPE) contentType: String,
        @Query("cutBorders") cutBorders: String = "",
        @Query("anilistInfo") anilistInfo: String = "",
        @Header(ApiKeyHeader) key: String = Configure.apiKey,
        @Tag method_name: String = "search"
    ): SearchAnimeResult

    @POST("/search")
    suspend fun searchNoCut(
        @Body body: RequestBody,
        @Header(HttpHeaders.CONTENT_TYPE) contentType: String,
        @Query("anilistInfo") anilistInfo: String = "",
        @Header(ApiKeyHeader) key: String = Configure.apiKey,
        @Tag method_name: String = "searchNoCut"
    ): SearchAnimeResult

    @GET("/me")
    suspend fun getMe(
        @Header("x-trace-key") key: String = Configure.apiKey,
        @Tag method_name: String = "getMe"
    ): SearchQuota
}