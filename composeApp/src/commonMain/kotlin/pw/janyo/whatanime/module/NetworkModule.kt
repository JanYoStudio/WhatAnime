package pw.janyo.whatanime.module

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import pw.janyo.whatanime.Configure
import pw.janyo.whatanime.Constant
import pw.janyo.whatanime.api.SearchApi
import pw.janyo.whatanime.api.createSearchApi
import pw.janyo.whatanime.httpResponses
import pw.janyo.whatanime.model.DebugHttpInfo
import pw.janyo.whatanime.utils.formatDateTime
import pw.janyo.whatanime.utils.redactDebugResponse
import kotlin.time.Clock

val networkModule = module {
    single {
        HttpClient(httpClientEngine()) {
            engine { httpClientEngineConfig(this) }
            install(ContentNegotiation) {
                json(Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(ApiKeyHeaderPlugin)
            install(DebugResponsePlugin)
            install(UserAgent) {
                agent = userAgent()
            }
        }
    }
    single {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>())
            .baseUrl(Constant.BASE_URL)
            .build()
    }
    single {
        get<Ktorfit>().createSearchApi()
    }
}

private val ApiKeyHeaderPlugin = createClientPlugin("ApiKeyHeaderPlugin") {
    onRequest { request, _ ->
        if (request.headers[SearchApi.apiKeyHeader].isNullOrBlank()) {
            request.headers.remove(SearchApi.apiKeyHeader)
        }
    }
}

private val DebugResponsePlugin = createClientPlugin("DebugResponsePlugin") {
    onResponse { resp ->
        if (!Configure.debugMode) return@onResponse
        val methodName =
            resp.call.attributes.allKeys.firstOrNull { it -> it.name == "methodName" }?.let {
                resp.call.attributes[it].toString()
            } ?: "tag"
        val responseBody = redactDebugResponse(
            resp.bodyAsText(), resp.call.request.headers[SearchApi.apiKeyHeader].orEmpty(),
        )
        val currentDateTime =
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).formatDateTime()
        val responseCode = resp.status.value
        httpResponses.append(
            DebugHttpInfo(
                title = "$methodName --> $responseCode",
                datetime = currentDateTime,
                response = responseBody
            )
        )
    }
}

expect fun httpClientEngine(): HttpClientEngineFactory<HttpClientEngineConfig>

expect fun httpClientEngineConfig(config: HttpClientEngineConfig)

expect fun userAgent(): String