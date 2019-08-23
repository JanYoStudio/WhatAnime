package pw.janyo.whatanime.api

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface SearchApi {
	@FormUrlEncoded
	@POST("/api/search")
	fun search(@Field("image") image: String, @Field("filter") filter: String?): Observable<ResponseBody>

	@GET("/api/me")
	fun getMe(): Observable<ResponseBody>
}