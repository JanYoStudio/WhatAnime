package pw.janyo.whatanime.interfaces;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SearchService {
    @FormUrlEncoded
    @POST("/api/search")
	Observable<ResponseBody> search(@Query("token") String token, @Field("image") String image, @Field("filter") String filter);
}
