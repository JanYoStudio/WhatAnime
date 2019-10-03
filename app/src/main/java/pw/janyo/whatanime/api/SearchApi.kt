package pw.janyo.whatanime.api

import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.SearchQuota
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface SearchApi {
	@FormUrlEncoded
	@POST("/api/search")
	suspend fun search(@Field("image") image: String, @Field("filter") filter: String?): Animation

	@GET("/api/me")
	suspend fun getMe(): SearchQuota
}