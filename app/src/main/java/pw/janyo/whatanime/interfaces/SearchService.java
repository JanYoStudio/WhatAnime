package pw.janyo.whatanime.interfaces;

import pw.janyo.whatanime.classes.Animation;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SearchService {
    @FormUrlEncoded
    @POST("/api/search")
    Call<Animation> search(@Query("token") String token, @Field("image") String image, @Field("filter") String filter);
}
