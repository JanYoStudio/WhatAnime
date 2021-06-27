package pw.janyo.whatanime.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import pw.janyo.whatanime.R
import pw.janyo.whatanime.api.SearchApi
import pw.janyo.whatanime.api.ServerApi
import pw.janyo.whatanime.config.trackEvent
import pw.janyo.whatanime.config.useServerCompress
import pw.janyo.whatanime.constant.StringConstant
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.model.request.SignatureRequest
import pw.janyo.whatanime.repository.local.service.HistoryService
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

    companion object {
        private const val maxSize: Int = 1024 * 1024 * 5
    }

    suspend fun queryAnimationByImageOnline(
        file: File,
        originPath: String,
        cachePath: String,
        filter: String?
    ): Animation = withContext(Dispatchers.IO) {
        val history = queryByBase64(originPath)
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
            saveHistory(originPath, cachePath, filter, data)
            data
        }
    }

    suspend fun queryAnimationByImageOnlineWithCloud(
        file: File,
        originPath: String,
        cachePath: String,
        mimeType: String,
        filter: String?
    ): Animation = withContext(Dispatchers.IO) {
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
        trackEvent("search image", mapOf("url" to url))
        saveHistory(originPath, cachePath, null, data)
        data
    }

    suspend fun showQuota(): SearchQuota = withContext(Dispatchers.IO) {
        if (!isConnectInternet()) {
            throw ResourceException(R.string.hint_no_network)
        }
        searchApi.getMe()
    }

    suspend fun queryAnimationByImageLocal(
        file: File,
        originPath: String,
        cachePath: String,
        mimeType: String,
        filter: String?,
        connectServer: Boolean
    ): Animation = withContext(Dispatchers.IO) {
        val animationHistory = historyService.queryHistoryByOriginPathAndFilter(originPath, filter)
        val history = animationHistory?.result?.fromJson<Animation>()
        if (history != null) {
            history.limit = -987654
            history.limit_ttl = -987654
            history
        } else {
            if (useServerCompress && connectServer)
                queryAnimationByImageOnlineWithCloud(file, originPath, cachePath, mimeType, filter)
            else
                queryAnimationByImageOnline(file, originPath, cachePath, filter)
        }
    }

    private suspend fun queryByBase64(originPath: String): Animation? =
        withContext(Dispatchers.IO) {
            val animationHistory =
                historyService.queryHistoryByOriginPathAndFilter(originPath, null)
            val history = animationHistory?.result?.fromJson<Animation>()
            if (history != null) {
                history.limit = -987654
                history.limit_ttl = -987654
                history
            } else {
                null
            }
        }

    suspend fun queryHistoryByOriginPath(originPath: String, filter: String?): AnimationHistory? =
        withContext(Dispatchers.IO) {
            historyService.queryHistoryByOriginPathAndFilter(
                originPath,
                filter
            )
        }

    private suspend fun saveHistory(
        originPath: String,
        cachePath: String,
        filter: String?,
        animation: Animation
    ) = withContext(Dispatchers.IO) {
        val animationHistory = AnimationHistory()
        animationHistory.originPath = originPath
        animationHistory.cachePath = cachePath
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

    suspend fun deleteHistory(animationHistory: AnimationHistory, listener: (Boolean) -> Unit) =
        withContext(Dispatchers.IO) {
            listener(historyService.delete(animationHistory) == 1)
            File(animationHistory.cachePath).delete()
        }
}