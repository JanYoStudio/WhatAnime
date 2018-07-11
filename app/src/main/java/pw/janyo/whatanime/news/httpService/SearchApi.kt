package pw.janyo.whatanime.news.httpService

import pw.janyo.whatanime.news.model.Animation
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

interface SearchApi {
	@FormUrlEncoded
	@POST("/api/search")
	fun search(@Query("token") token: String, @Field("image") image: String, @Field("filter") filter: String?): Call<Animation>
}