package pw.janyo.whatanime.repository.remote

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pw.janyo.whatanime.R
import pw.janyo.whatanime.api.SearchApi
import pw.janyo.whatanime.factory.RetrofitFactory
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.repository.dataSource.AnimationDateSource
import pw.janyo.whatanime.repository.local.LocalAnimationDataSource
import pw.janyo.whatanime.utils.base64CompressImage
import vip.mystery0.tools.ResourceException
import vip.mystery0.tools.utils.isConnectInternet
import java.io.File

object RemoteAnimationDataSource : AnimationDateSource {
	private val searchApi = RetrofitFactory.retrofit.create(SearchApi::class.java)

	override suspend fun queryAnimationByImage(file: File, filter: String?): Animation = withContext(Dispatchers.IO) {
		val base64 = file.base64CompressImage(Bitmap.CompressFormat.JPEG, 1024 * 1000, 10)
		val history = LocalAnimationDataSource.queryByBase64(base64)
		if (history != null) {
			history
		} else {
			if (!isConnectInternet()) {
				throw ResourceException(R.string.hint_no_network)
			}
			val response = searchApi.search(base64, filter).execute()
			if (!response.isSuccessful) {
				throw Exception(response.errorBody()?.string())
			}
			val data = response.body()!!
			LocalAnimationDataSource.saveHistory(base64, file, filter, data)
			data
		}
	}

	suspend fun showQuota(): SearchQuota = withContext(Dispatchers.IO) {
		if (!isConnectInternet()) {
			throw ResourceException(R.string.hint_no_network)
		}
		val response = searchApi.getMe().execute()
		if (response.isSuccessful) {
			response.body()!!
		} else {
			throw Exception(response.errorBody()?.string())
		}
	}
}