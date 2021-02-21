package pw.janyo.whatanime.repository

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
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
import top.zibin.luban.Luban
import vip.mystery0.tools.ResourceException
import vip.mystery0.tools.factory.fromJson
import vip.mystery0.tools.factory.toJson
import vip.mystery0.tools.utils.isConnectInternet
import java.io.File
import java.util.*

class AnimationRepository : KoinComponent {
    private val searchApi: SearchApi by inject()
    private val serverVipApi: ServerApi by inject(named("cloudVipApi"))
    private val historyService: HistoryService by inject()
    private val luban: Luban.Builder by inject()

    companion object {
        private const val maxSize: Int = 1024 * 1024 * 5
    }

    suspend fun queryAnimationByImageOnline(file: File, originPath: String, cachePath: String, filter: String?): Animation = withContext(Dispatchers.IO) {
        fun File.compress(): String {
            val base64String = Base64.encodeToString(readBytes(), Base64.DEFAULT)
            return if (base64String.length <= maxSize)
                base64String
            else {
                val f = luban.load(this).get()[0]
                Base64.encodeToString(f.readBytes(), Base64.DEFAULT)
            }
        }

        val base64 = file.compress()
        val history = queryByBase64(base64)
        if (history != null) {
            history
        } else {
            if (!isConnectInternet()) {
                throw ResourceException(R.string.hint_no_network)
            }
            val requestBody: RequestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("filter", filter ?: "")
                    .addFormDataPart("image", file.name, file.asRequestBody())
                    .build()
            val data = searchApi.search(requestBody)
            saveHistory(base64, originPath, cachePath, filter, data)
            data
        }
    }

    suspend fun queryAnimationByImageOnlineWithCloud(file: File, originPath: String, cachePath: String, mimeType: String, filter: String?): Animation = withContext(Dispatchers.IO) {
        val signatureRequest = SignatureRequest(file, mimeType)
        val signatureResponse = serverVipApi.signature(signatureRequest)
        val requestBody: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("key", signatureResponse.uploadMeta.key)
                .addFormDataPart("token", signatureResponse.uploadMeta.signature)
                .addFormDataPart("file", file.name, file.asRequestBody())
                .build()
        val uploadResponse = serverVipApi.uploadFile(signatureResponse.uploadUrl, requestBody)
        val url = if (file.length() > maxSize) uploadResponse.url else uploadResponse.url
        val data = searchApi.searchByUrl(url)
        saveHistory(uploadResponse.url, originPath, cachePath, null, data)
        data
    }

    suspend fun showQuota(): SearchQuota = withContext(Dispatchers.IO) {
        if (!isConnectInternet()) {
            throw ResourceException(R.string.hint_no_network)
        }
        searchApi.getMe()
    }

    suspend fun queryAnimationByImageLocal(file: File, originPath: String, cachePath: String, mimeType: String, filter: String?, connectServer: Boolean): Animation = withContext(Dispatchers.IO) {
        val animationHistory = historyService.queryHistoryByOriginPathAndFilter(originPath, filter)
        val history = animationHistory?.result?.fromJson<Animation>()
        if (history != null) {
            history.quota = -987654
            history.quota_ttl = -987654
            history
        } else {
            if (Configure.enableCloudCompress && connectServer)
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
            animationHistory.title = animation.docs[0].title_native ?: ""
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