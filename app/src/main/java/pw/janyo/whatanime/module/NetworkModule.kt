package pw.janyo.whatanime.module

import android.webkit.WebSettings
import com.google.common.net.HttpHeaders
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pw.janyo.whatanime.api.SearchApi
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.constant.Constant
import pw.janyo.whatanime.httpResponses
import pw.janyo.whatanime.model.moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

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
            .addInterceptor { chain ->
                val request = chain.request()
                val response = chain.proceed(request)
                if (Configure.debugMode) {
                    val responseBody = response.peekBody(Long.MAX_VALUE).string()
                    // 获取当前时间字符串，以年-月-日 时:分:秒 的格式
                    val currentDateTime = Calendar.getInstance().time
                    httpResponses.add(formatter.format(currentDateTime) to responseBody)
                    if (httpResponses.size > 3) {
                        httpResponses.removeAt(0)
                    }
                }
                response
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
}
