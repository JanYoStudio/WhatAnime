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
	single(named("must4OkHttpClient")) {
		OkHttpClient.Builder()
				.connectTimeout(4, TimeUnit.SECONDS)
				.build()
	}
	single(named("normalOkHttpClient")) {
		OkHttpClient.Builder()
				.connectTimeout(40, TimeUnit.SECONDS)
				.readTimeout(40, TimeUnit.SECONDS)
				.build()
	}
	single(named("baseRetrofit")) {
		Retrofit.Builder()
				.baseUrl(Constant.baseUrl)
				.client(get(named("normalOkHttpClient")))
				.addConverterFactory(GsonConverterFactory.create())
				.build()
	}
	single(named("must4Retrofit")) {
		Retrofit.Builder()
				.baseUrl(Constant.cloudUrl)
				.client(get(named("must4OkHttpClient")))
				.addConverterFactory(GsonConverterFactory.create())
				.build()
	}
	single(named("cloudRetrofit")) {
		Retrofit.Builder()
				.baseUrl(Constant.cloudUrl)
				.client(get(named("normalOkHttpClient")))
				.addConverterFactory(GsonConverterFactory.create())
				.build()
	}
	single(named("base")) {
		get<Retrofit>(named("baseRetrofit")).create(SearchApi::class.java)
	}
	single(named("cloud")) {
		get<Retrofit>(named("cloudRetrofit")).create(ServerApi::class.java)
	}
	single(named("must4")) {
		get<Retrofit>(named("must4Retrofit")).create(ServerApi::class.java)
	}
}
