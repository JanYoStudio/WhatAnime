package pw.janyo.whatanime.repository.remote

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pw.janyo.whatanime.api.SearchApi
import pw.janyo.whatanime.constant.Constant
import pw.janyo.whatanime.constant.StringConstant
import pw.janyo.whatanime.factory.GsonFactory
import pw.janyo.whatanime.factory.RetrofitFactory
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.repository.dataSource.AnimationDateSource
import pw.janyo.whatanime.repository.local.LocalAnimationDataSource
import pw.janyo.whatanime.utils.Base64
import pw.janyo.whatanime.utils.RxObserver
import vip.mystery0.tools.utils.FileTools
import java.io.File
import java.io.InputStreamReader

object RemoteAnimationDataSource : AnimationDateSource {

	private val searchApi = RetrofitFactory.retrofit.create(SearchApi::class.java)
	private val token = String(Base64.decode(Constant.token))

	override fun queryAnimationByImage(animationLiveData: MutableLiveData<Animation>, messageLiveData: MutableLiveData<String>, file: File, filter: String?) {
		val base64 = FileTools.compressImage(Bitmap.CompressFormat.JPEG, file, 1000, 10)
		searchApi.search(token, base64, filter)
				.subscribeOn(Schedulers.newThread())
				.unsubscribeOn(Schedulers.newThread())
				.map {
					val data = GsonFactory.gson.fromJson(InputStreamReader(it.byteStream()), Animation::class.java)
					if (data != null)
						LocalAnimationDataSource.saveHistory(messageLiveData, file, filter, data)
					data
				}
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(object : RxObserver<Animation>() {
					override fun onFinish(data: Animation?) {
						if (data == null) {
							messageLiveData.value = StringConstant.hint_response_error
						} else {
							animationLiveData.value = data
						}
					}

					override fun onError(e: Throwable) {
						messageLiveData.value = e.message
					}
				})
	}
}