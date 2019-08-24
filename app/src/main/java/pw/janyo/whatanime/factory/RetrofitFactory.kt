package pw.janyo.whatanime.factory

import okhttp3.OkHttpClient
import pw.janyo.whatanime.constant.Constant
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitFactory {
	private val client = OkHttpClient.Builder()
			.connectTimeout(20, TimeUnit.SECONDS)
			.readTimeout(20, TimeUnit.SECONDS)
//			.addInterceptor(HttpLoggingInterceptor()
//					.setLevel(HttpLoggingInterceptor.Level.BODY))
			.build()

	val retrofit: Retrofit = Retrofit.Builder()
			.baseUrl(Constant.baseUrl)
			.client(client)
			.addConverterFactory(GsonConverterFactory.create())
			.build()
}