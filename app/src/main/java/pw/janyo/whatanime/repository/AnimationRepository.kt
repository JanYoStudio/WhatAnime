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
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.config.DispatcherConfig
import pw.janyo.whatanime.config.trackEvent
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
    private val serverAppApi: ServerApi by inject(named("cloudAppApi"))
    private val historyService: HistoryService by inject()

    companion object {
        private const val maxSize: Int = 1024 * 1024 * 5
    }

    private suspend fun checkNetwork() {
        withContext(DispatcherConfig.CHECK_NETWORK) {
            if (!isConnectInternet()) {
                throw ResourceException(R.string.hint_no_network)
            }
        }
    }

    private suspend fun queryAnimationByImageOnline(
        file: File,
        originPath: String,
        cachePath: String,
    ): Animation {
        val history = queryByBase64(originPath)
        if (history != null) {
            return history
        }
        checkNetwork()
        trackEvent("search image")
        val data = withContext(DispatcherConfig.NETWORK) {
            val requestBody: RequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", file.name, file.asRequestBody())
                .build()
            val data = searchApi.search(requestBody)
            if (data.error.isNotBlank()) {
                throw Exception("rest request failed, ${data.error}")
            }
            data
        }
        saveHistory(originPath, cachePath, data)
        return data
    }

    private suspend fun queryAnimationByImageOnlineWithCloud(
        file: File,
        originPath: String,
        cachePath: String,
        mimeType: String,
    ): Animation {
        val data = withContext(DispatcherConfig.NETWORK) {
            val signatureRequest = SignatureRequest(file, mimeType)
            val signatureResponse = serverAppApi.signature(signatureRequest)
            val requestBody: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("key", signatureResponse.uploadMeta.key)
                .addFormDataPart("token", signatureResponse.uploadMeta.signature)
                .addFormDataPart("file", file.name, file.asRequestBody())
                .build()
            val uploadResponse = serverAppApi.uploadFile(signatureResponse.uploadUrl, requestBody)
            val url = if (file.length() > maxSize) uploadResponse.url else uploadResponse.url
            trackEvent("search image", mapOf("url" to url))
            val data = searchApi.searchByUrl(url)
            if (data.error.isNotBlank()) {
                throw Exception("rest request failed, ${data.error}")
            }
            data
        }
        saveHistory(originPath, cachePath, data)
        return data
    }

    suspend fun showQuota(): SearchQuota {
        checkNetwork()
        return withContext(DispatcherConfig.NETWORK) {
            searchApi.getMe()
        }
    }

    suspend fun queryAnimationByImageLocal(
        file: File,
        originPath: String,
        cachePath: String,
        mimeType: String
    ): Animation {
        val animationHistory = withContext(Dispatchers.IO) {
            historyService.queryHistoryByOriginPathAndFilter(originPath)
        }
        return animationHistory?.result?.fromJson<Animation>()
            ?: if (Configure.useServerCompress)
                queryAnimationByImageOnlineWithCloud(file, originPath, cachePath, mimeType)
            else
                queryAnimationByImageOnline(file, originPath, cachePath)
    }

    private suspend fun queryByBase64(originPath: String): Animation? {
        val animationHistory = withContext(Dispatchers.IO) {
            historyService.queryHistoryByOriginPathAndFilter(originPath)
        }
        return animationHistory?.result?.fromJson<Animation>()
    }

    suspend fun queryHistoryByOriginPath(originPath: String): AnimationHistory? =
        withContext(Dispatchers.IO) {
            historyService.queryHistoryByOriginPathAndFilter(originPath)
        }

    private suspend fun saveHistory(
        originPath: String,
        cachePath: String,
        animation: Animation
    ) {
        val animationHistory = withContext(Dispatchers.Default) {
            AnimationHistory().apply {
                this.originPath = originPath
                this.cachePath = cachePath
                this.result = animation.toJson()
                this.time = Calendar.getInstance().timeInMillis
                if (animation.result.isNotEmpty()) {
                    val result = animation.result[0]
                    this.title = result.anilist.title?.native ?: ""
                    this.anilistId = result.anilist.id
                    this.episode = result.episode ?: ""
                    this.similarity = result.similarity
                } else {
                    this.title = StringConstant.hint_no_result
                }
            }
        }
        withContext(Dispatchers.IO) {
            historyService.saveHistory(animationHistory)
        }
    }

    suspend fun queryAllHistory(): List<AnimationHistory> = withContext(Dispatchers.IO) {
        historyService.queryAllHistory()
    }

    suspend fun deleteHistory(historyId: Int) =
        withContext(Dispatchers.IO) {
            val animationHistory = historyService.getById(historyId)
            historyService.delete(historyId)
            animationHistory?.let {
                File(it.cachePath).delete()
            }
        }
}