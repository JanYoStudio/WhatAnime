package pw.janyo.whatanime.module

import android.webkit.WebSettings
import com.google.common.net.HttpHeaders
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pw.janyo.whatanime.api.AniListChineseApi
import pw.janyo.whatanime.api.SearchApi
import pw.janyo.whatanime.constant.Constant
import pw.janyo.whatanime.model.moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single {
        OkHttpClient.Builder()
            .connectTimeout(40, TimeUnit.SECONDS)
            .readTimeout(40, TimeUnit.SECONDS)
            .addInterceptor {
                val request = it.request()
                val builder = request.newBuilder()
                if (request.header(SearchApi.ApiKeyHeader).isNullOrBlank()) {
                    builder.removeHeader(SearchApi.ApiKeyHeader)
                }
                val newRequest = builder
                    .removeHeader(HttpHeaders.USER_AGENT)
                    .addHeader(
                        HttpHeaders.USER_AGENT,
                        WebSettings.getDefaultUserAgent(androidContext())
                    )
                    .build()
                it.proceed(newRequest)
            }
            .build()
    }
    single(named("baseUrl")) {
        Retrofit.Builder()
            .baseUrl(Constant.baseUrl)
            .client(get())
            .addConverterFactory(
                MoshiConverterFactory.create(moshi)
            )
            .build()
    }
    single(named("aniListChineseUrl")) {
        Retrofit.Builder()
            .baseUrl(Constant.aniListChineseUrl)
            .client(get())
            .addConverterFactory(
                MoshiConverterFactory.create(moshi)
            )
            .build()
    }
    single {
        get<Retrofit>(named("baseUrl")).create(SearchApi::class.java)
    }
    single {
        get<Retrofit>(named("aniListChineseUrl")).create(AniListChineseApi::class.java)
    }
}
