package pw.janyo.whatanime.repository.remote

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import pw.janyo.whatanime.api.SearchApi
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.factory.RetrofitFactory
import pw.janyo.whatanime.manager.content
import pw.janyo.whatanime.manager.empty
import pw.janyo.whatanime.manager.error
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.repository.dataSource.AnimationDateSource
import pw.janyo.whatanime.repository.local.LocalAnimationDataSource
import vip.mystery0.rx.PackageData
import vip.mystery0.tools.utils.FileTools
import java.io.File

object RemoteAnimationDataSource : AnimationDateSource {
	private val searchApi = RetrofitFactory.retrofit.create(SearchApi::class.java)

	fun showQuota(quotaLiveData: MutableLiveData<PackageData<SearchQuota>>) {
		val response = searchApi.getMe().execute()
		if (response.isSuccessful) {
			quotaLiveData.content(response.body())
		} else {
			quotaLiveData.error(Exception(response.errorBody()?.string()))
		}
	}

	override fun queryAnimationByImage(animationLiveData: MutableLiveData<PackageData<Animation>>, quotaLiveData: MutableLiveData<PackageData<SearchQuota>>, file: File, filter: String?) {
		val base64 = FileTools.instance.compressImage(Bitmap.CompressFormat.JPEG, file, 1000, 10)
		val response = searchApi.search(base64, filter).execute()
		if (response.isSuccessful) {
			val headers = response.headers()
			val quota = SearchQuota()
			quota.quota = headers["x-whatanime-quota"]!!.toInt()
			quota.quota_ttl = headers["x-whatanime-quota-ttl"]!!.toInt()
			quotaLiveData.content(quota)
			val data = response.body()!!
			LocalAnimationDataSource.saveHistory(animationLiveData, file, filter, data)
			if (data.docs.isEmpty()) {
				animationLiveData.empty()
			} else {
				if (Configure.hideSex)
					data.docs = data.docs.filter { !it.is_adult }
				animationLiveData.content(data)
			}
		} else {
			animationLiveData.error(Exception(response.errorBody()?.string()))
		}
	}
}