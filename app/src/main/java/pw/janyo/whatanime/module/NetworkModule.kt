package pw.janyo.whatanime.module

import okhttp3.OkHttpClient
import org.koin.dsl.module
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
            .build()
    }
    single {
        Retrofit.Builder()
            .baseUrl(Constant.baseUrl)
            .client(get())
            .addConverterFactory(
                MoshiConverterFactory.create(moshi)
            )
            .build()
    }
    single {
        get<Retrofit>().create(SearchApi::class.java)
    }
}
