package pw.janyo.whatanime.repository.remote

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pw.janyo.whatanime.api.SearchApi
import pw.janyo.whatanime.factory.RetrofitFactory
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.repository.dataSource.AnimationDateSource
import pw.janyo.whatanime.repository.local.LocalAnimationDataSource
import vip.mystery0.tools.utils.base64CompressImage
import java.io.File

object RemoteAnimationDataSource : AnimationDateSource {
	private val searchApi = RetrofitFactory.retrofit.create(SearchApi::class.java)

	override suspend fun queryAnimationByImage(file: File, filter: String?): Animation {
		return withContext(Dispatchers.IO) {
			val base64 = file.base64CompressImage(Bitmap.CompressFormat.JPEG, 1000, 10)
			val response = searchApi.search(base64, filter).execute()
			if (!response.isSuccessful) {
				throw Exception(response.errorBody()?.string())
			}
			val data = response.body()!!
			LocalAnimationDataSource.saveHistory(file, filter, data)
			data
		}
	}

	suspend fun showQuota(): SearchQuota {
		return withContext(Dispatchers.IO) {
			val response = searchApi.getMe().execute()
			if (response.isSuccessful) {
				response.body()!!
			} else {
				throw Exception(response.errorBody()?.string())
			}
		}
	}
}