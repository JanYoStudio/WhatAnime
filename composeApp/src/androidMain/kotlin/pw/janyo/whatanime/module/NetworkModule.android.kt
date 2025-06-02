package pw.janyo.whatanime.module

import android.webkit.WebSettings
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.engine.okhttp.OkHttpConfig
import pw.janyo.whatanime.context
import java.util.concurrent.TimeUnit

actual fun httpClientEngine(): HttpClientEngineFactory<HttpClientEngineConfig> = OkHttp

actual fun httpClientEngineConfig(config: HttpClientEngineConfig) {
    val okHttpConfig = config as OkHttpConfig
    okHttpConfig.config {
        connectTimeout(40, TimeUnit.SECONDS)
        readTimeout(40, TimeUnit.SECONDS)
    }
}

actual fun userAgent(): String = WebSettings.getDefaultUserAgent(context)