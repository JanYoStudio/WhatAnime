package pw.janyo.whatanime.api

import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.SearchQuota
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface SearchApi {
	@FormUrlEncoded
	@POST("/api/search")
	fun search(@Field("image") image: String, @Field("filter") filter: String?): Call<Animation>

	@GET("/api/me")
	fun getMe(): Call<SearchQuota>
}