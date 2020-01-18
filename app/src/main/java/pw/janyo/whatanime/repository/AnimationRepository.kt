package pw.janyo.whatanime.repository

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import pw.janyo.whatanime.R
import pw.janyo.whatanime.api.SearchApi
import pw.janyo.whatanime.api.ServerApi
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.constant.StringConstant
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.model.request.SignatureRequest
import pw.janyo.whatanime.repository.local.service.HistoryService
import pw.janyo.whatanime.utils.base64CompressImage
import vip.mystery0.tools.ResourceException
import vip.mystery0.tools.factory.fromJson
import vip.mystery0.tools.factory.toJson
import vip.mystery0.tools.utils.isConnectInternet
import java.io.File
import java.util.*

class AnimationRepository(
		private val searchApi: SearchApi,
		private val serverApi: ServerApi,
		private val historyService: HistoryService
) {
	suspend fun queryAnimationByImageOnline(file: File, originPath: String, cachePath: String, filter: String?): Animation = withContext(Dispatchers.IO) {
		val base64 = file.base64CompressImage(Bitmap.CompressFormat.JPEG, 1024 * 1000, 10)
		val history = queryByBase64(base64)
		if (history != null) {
			history
		} else {
			if (!isConnectInternet()) {
				throw ResourceException(R.string.hint_no_network)
			}
			val data = searchApi.search(base64, filter)
			saveHistory(base64, originPath, cachePath, filter, data)
			data
		}
	}

	suspend fun queryAnimationByImageOnlineWithCloud(file: File, originPath: String, cachePath: String, mimeType: String, filter: String?): Animation = withContext(Dispatchers.IO) {
		val signatureRequest = SignatureRequest(file, mimeType)
		val signatureResponse = serverApi.signature(signatureRequest).verify()!!
		val requestFile = file.asRequestBody("multipart/form-data".toMediaType())
		val part = MultipartBody.Part.createFormData("file", file.name, requestFile)
		val keyBody = MultipartBody.Part.createFormData("key", signatureResponse.uploadMeta.key)
		val tokenBody = MultipartBody.Part.createFormData("token", signatureResponse.uploadMeta.signature)
		val uploadResponse = serverApi.uploadFile(signatureResponse.uploadUrl, keyBody, tokenBody, part).verify()!!
		val data = searchApi.searchByUrl(uploadResponse.url)
		saveHistory(uploadResponse.url, originPath, cachePath, null, data)
		data
	}

	suspend fun showQuota(): SearchQuota = withContext(Dispatchers.IO) {
		if (!isConnectInternet()) {
			throw ResourceException(R.string.hint_no_network)
		}
		searchApi.getMe()
	}

	suspend fun queryAnimationByImageLocal(file: File, originPath: String, cachePath: String, mimeType: String, filter: String?): Animation = withContext(Dispatchers.IO) {
		val animationHistory = historyService.queryHistoryByOriginPathAndFilter(originPath, filter)
		val history = animationHistory?.result?.fromJson<Animation>()
		if (history != null) {
			history.quota = -987654
			history.quota_ttl = -987654
			history
		} else {
			if (Configure.enableCloudCompress)
				queryAnimationByImageOnlineWithCloud(file, originPath, cachePath, mimeType, filter)
			else
				queryAnimationByImageOnline(file, originPath, cachePath, filter)
		}
	}

	private suspend fun queryByBase64(base64: String): Animation? = withContext(Dispatchers.IO) {
		val animationHistory = historyService.queryHistoryByBase64(base64)
		val history = animationHistory?.result?.fromJson<Animation>()
		if (history != null) {
			history.quota = -987654
			history.quota_ttl = -987654
			history
		} else {
			null
		}
	}

	suspend fun queryHistoryByOriginPath(originPath: String, filter: String?): AnimationHistory? = withContext(Dispatchers.IO) { historyService.queryHistoryByOriginPathAndFilter(originPath, filter) }

	private suspend fun saveHistory(base64: String, originPath: String, cachePath: String, filter: String?, animation: Animation) = withContext(Dispatchers.IO) {
		val animationHistory = AnimationHistory()
		animationHistory.originPath = originPath
		animationHistory.cachePath = cachePath
		animationHistory.base64 = base64
		animationHistory.result = animation.toJson()
		animationHistory.time = Calendar.getInstance().timeInMillis
		if (animation.docs.isNotEmpty())
			animationHistory.title = animation.docs[0].title_native
		else
			animationHistory.title = StringConstant.hint_no_result
		animationHistory.filter = filter
		historyService.saveHistory(animationHistory)
	}

	suspend fun queryAllHistory(): List<AnimationHistory> = withContext(Dispatchers.IO) {
		historyService.queryAllHistory()
	}

	suspend fun deleteHistory(animationHistory: AnimationHistory, listener: (Boolean) -> Unit) = withContext(Dispatchers.IO) {
		listener(historyService.delete(animationHistory) == 1)
		File(animationHistory.cachePath).delete()
	}
}