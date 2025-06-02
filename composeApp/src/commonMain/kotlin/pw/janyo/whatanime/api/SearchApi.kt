package pw.janyo.whatanime.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Multipart
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Tag
import io.ktor.client.request.forms.MultiPartFormDataContent
import pw.janyo.whatanime.Configure
import pw.janyo.whatanime.model.SearchAnimeResult
import pw.janyo.whatanime.model.SearchQuota

interface SearchApi {
    companion object {
        const val apiKeyHeader = "x-trace-key"
    }

    @Multipart
    @POST("search?cutBorders&anilistInfo")
    suspend fun search(
        @Body map: MultiPartFormDataContent,
        @Header(apiKeyHeader) key: String = Configure.apiKey,
        @Tag("methodName") methodName: String = "search",
    ): SearchAnimeResult

    @Multipart
    @POST("search?anilistInfo")
    suspend fun searchNoCut(
        @Body map: MultiPartFormDataContent,
        @Header(apiKeyHeader) key: String = Configure.apiKey,
        @Tag("methodName") methodName: String = "searchNoCut",
    ): SearchAnimeResult

    @GET("me")
    suspend fun getMe(
        @Header(apiKeyHeader) key: String = Configure.apiKey,
        @Tag("methodName") methodName: String = "getMe",
    ): SearchQuota
}