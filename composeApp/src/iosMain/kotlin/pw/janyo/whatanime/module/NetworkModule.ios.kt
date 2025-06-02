package pw.janyo.whatanime.module

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual fun httpClientEngine(): HttpClientEngineFactory<HttpClientEngineConfig> = Darwin
actual fun httpClientEngineConfig(config: HttpClientEngineConfig) {
}

actual fun userAgent(): String = "TODO"