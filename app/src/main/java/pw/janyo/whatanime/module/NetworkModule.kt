package pw.janyo.whatanime.module

import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pw.janyo.whatanime.api.SearchApi
import pw.janyo.whatanime.api.ServerApi
import pw.janyo.whatanime.constant.Constant
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
	single {
		OkHttpClient.Builder()
				.connectTimeout(40, TimeUnit.SECONDS)
				.readTimeout(40, TimeUnit.SECONDS)
				.build()
	}
	single(named("base")) {
		Retrofit.Builder()
				.baseUrl(Constant.baseUrl)
				.client(get())
				.addConverterFactory(GsonConverterFactory.create())
				.build()
	}
	single(named("cloud")) {
		Retrofit.Builder()
				.baseUrl(Constant.cloudUrl)
				.client(get())
				.addConverterFactory(GsonConverterFactory.create())
				.build()
	}
	single {
		get<Retrofit>(named("base")).create(SearchApi::class.java)
	}
	single {
		get<Retrofit>(named("cloud")).create(ServerApi::class.java)
	}
}
