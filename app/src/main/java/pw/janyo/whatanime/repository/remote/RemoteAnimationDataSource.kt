package pw.janyo.whatanime.repository.remote

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pw.janyo.whatanime.api.SearchApi
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.constant.Constant
import pw.janyo.whatanime.factory.GsonFactory
import pw.janyo.whatanime.factory.RetrofitFactory
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.repository.dataSource.AnimationDateSource
import pw.janyo.whatanime.repository.local.LocalAnimationDataSource
import pw.janyo.whatanime.utils.Base64
import vip.mystery0.rxpackagedata.PackageData
import vip.mystery0.rxpackagedata.rx.RxObserver
import vip.mystery0.tools.utils.FileTools
import java.io.File

object RemoteAnimationDataSource : AnimationDateSource {
	private val searchApi = RetrofitFactory.retrofit.create(SearchApi::class.java)
	private val token = String(Base64.decode(Constant.token))

	override fun queryAnimationByImage(animationLiveData: MutableLiveData<PackageData<Animation>>, file: File, filter: String?) {
		val base64 = FileTools.compressImage(Bitmap.CompressFormat.JPEG, file, 1000, 10)
		searchApi.search(token, base64, filter)
				.subscribeOn(Schedulers.newThread())
				.unsubscribeOn(Schedulers.newThread())
				.map {
					val data = GsonFactory.parse<Animation>(it)
					LocalAnimationDataSource.saveHistory(animationLiveData, file, filter, data)
					data
				}
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(object : RxObserver<Animation>() {
					override fun onFinish(data: Animation?) {
						if (data == null || data.docs.isEmpty()) {
							animationLiveData.value = PackageData.empty()
						} else {
							if (Configure.hideSex)
								data.docs = data.docs.filter { !it.is_adult }
							animationLiveData.value = PackageData.content(data)
						}
					}

					override fun onError(e: Throwable) {
						animationLiveData.value = PackageData.error(e)
					}
				})
	}
}