package pw.janyo.whatanime.repository.remote

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pw.janyo.whatanime.api.SearchApi
import pw.janyo.whatanime.constant.Constant
import pw.janyo.whatanime.factory.RetrofitFactory
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.repository.dataSource.AnimationDateSource
import pw.janyo.whatanime.repository.local.LocalAnimationDataSource
import pw.janyo.whatanime.utils.Base64
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import vip.mystery0.tools.utils.FileTools
import java.io.File

object RemoteAnimationDataSource : AnimationDateSource {

	private val searchApi = RetrofitFactory.retrofit.create(SearchApi::class.java)
	private val token = String(Base64.decode(Constant.token))

	override fun queryAnimationByImage(animationLiveData: MutableLiveData<Animation>, messageLiveData: MutableLiveData<String>, file: File, filter: String?) {
		val base64 = FileTools.compressImage(Bitmap.CompressFormat.JPEG, file, 1000, 10)
		searchApi.search(token, base64, filter)
				.enqueue(object : Callback<Animation> {
					override fun onFailure(call: Call<Animation>?, t: Throwable?) {
						messageLiveData.value = t?.message
					}

					override fun onResponse(call: Call<Animation>?, response: Response<Animation>) {
						val animation = response.body()!!
						LocalAnimationDataSource.saveHistory(messageLiveData, file, filter, animation)
						animationLiveData.value = animation
					}
				})
	}
}