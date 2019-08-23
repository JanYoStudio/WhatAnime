package pw.janyo.whatanime.repository.remote

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pw.janyo.whatanime.api.SearchApi
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.factory.RetrofitFactory
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.repository.dataSource.AnimationDateSource
import pw.janyo.whatanime.repository.local.LocalAnimationDataSource
import vip.mystery0.logs.Logs
import vip.mystery0.rx.OnlyCompleteObserver
import vip.mystery0.rx.PackageData
import vip.mystery0.tools.factory.fromJson
import vip.mystery0.tools.utils.FileTools
import java.io.File

object RemoteAnimationDataSource : AnimationDateSource {
	private val searchApi = RetrofitFactory.retrofit.create(SearchApi::class.java)

	fun showQuota(quotaLiveData: MutableLiveData<PackageData<SearchQuota>>) {
		searchApi.getMe()
				.subscribeOn(Schedulers.io())
				.map { it.string().fromJson<SearchQuota>() }
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(object : OnlyCompleteObserver<SearchQuota>() {
					override fun onFinish(data: SearchQuota?) {
						if (data == null) {
							quotaLiveData.value = PackageData.empty()
						} else {
							quotaLiveData.value = PackageData.content(data)
						}
					}

					override fun onError(e: Throwable) {
						quotaLiveData.value = PackageData.error(e)
					}
				})
	}

	override fun queryAnimationByImage(animationLiveData: MutableLiveData<PackageData<Animation>>, file: File, filter: String?) {
		val base64 = FileTools.instance.compressImage(Bitmap.CompressFormat.JPEG, file, 1000, 10)
		searchApi.search(base64, filter)
				.subscribeOn(Schedulers.io())
				.map {
					val data = it.string().fromJson<Animation>()
					LocalAnimationDataSource.saveHistory(animationLiveData, file, filter, data)
					data
				}
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(object : OnlyCompleteObserver<Animation>() {
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