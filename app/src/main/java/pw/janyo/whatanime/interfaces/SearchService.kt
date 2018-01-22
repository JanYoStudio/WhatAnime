package pw.janyo.whatanime.interfaces

import pw.janyo.whatanime.classes.Animation
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface SearchService {
    @FormUrlEncoded
    @POST("/")
    fun search(@Field("image") image: String, @Field("filter") filter: String?): Call<Animation>
}