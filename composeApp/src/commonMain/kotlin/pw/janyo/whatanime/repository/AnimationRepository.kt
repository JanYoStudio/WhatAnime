package pw.janyo.whatanime.repository

import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.size
import io.github.vinceglb.filekit.source
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.CancellationException
import pw.janyo.whatanime.utils.canDeleteCache
import pw.janyo.whatanime.utils.managedCacheDirectory
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.buffered
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pw.janyo.whatanime.Configure
import pw.janyo.whatanime.api.SearchApi
import pw.janyo.whatanime.db.service.HistoryService
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.model.SearchAnimeResult
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.utils.formatEpisode
import pw.janyo.whatanime.utils.getCacheFilePathBySavedCacheFilePath
import pw.janyo.whatanime.utils.isOnline
import pw.janyo.whatanime.utils.md5
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.hint_no_network
import whatanime.composeapp.generated.resources.hint_no_result
import whatanime.composeapp.generated.resources.hint_search_error
import kotlin.time.Clock

class AnimationRepository : KoinComponent, HistoryGateway {
    private val searchApi by inject<SearchApi>()
    private val historyService by inject<HistoryService>()
    private val cacheOperations = Mutex()

    // 覆盖选图缓存到历史落库的完整区间，避免切到历史删除时清走正在使用的同一缓存。
    internal suspend fun <T> withCacheOperation(block: suspend () -> T): T = cacheOperations.withLock { block() }

    private suspend fun checkNetwork() {
        if (!isOnline()) {
            throw RuntimeException(getString(Res.string.hint_no_network))
        }
    }

    @OptIn(InternalAPI::class)
    private suspend fun queryAnimationByImageOnline(
        file: PlatformFile,
        originPath: String,
        cachePath: String,
        cutBorders: Boolean,
    ): SearchAnimeResult {
        val history = queryByFileMd5(file)
        if (history != null) {
            return history
        }
        checkNetwork()
        val multipart = MultiPartFormDataContent(formData {
            append("image", InputProvider(file.size()) {
                file.source().buffered()
            }, Headers.build {
                append(HttpHeaders.ContentType, file.mimeType()!!.toString())
                append(HttpHeaders.ContentDisposition, "filename=${file.name}")
            })
        })
        val data = if (cutBorders) {
            searchApi.search(multipart)
        } else {
            searchApi.searchNoCut(multipart)
        }
        if (data.error.isNotBlank()) {
            Logger.e("搜索接口返回错误")
            throw RuntimeException(getString(Res.string.hint_search_error))
        }
        saveHistory(originPath, cachePath, data)
        return data
    }

    suspend fun showQuota(apiKey: String = Configure.apiKey): SearchQuota {
        checkNetwork()
        return searchApi.getMe(key = apiKey)
    }

    suspend fun queryAnimationByImageLocal(
        file: PlatformFile,
        originPath: String,
        cachePath: String,
        cutBorders: Boolean = Configure.cutBorders,
    ): SearchAnimeResult {
        val animationHistory = historyService.queryHistoryByOriginPath(originPath)
            ?: return queryAnimationByImageOnline(file, originPath, cachePath, cutBorders)
        return Json.decodeFromString(animationHistory.result)
    }

    private suspend fun queryByFileMd5(file: PlatformFile): SearchAnimeResult? {
        val md5 = file.md5()
        //用现有的originPath字段来存储md5
        val animationHistory = historyService.queryHistoryByOriginPath(md5) ?: return null
        return Json.decodeFromString(animationHistory.result)
    }

    suspend fun queryHistoryByOriginPath(originPath: String): AnimationHistory? =
        historyService.queryHistoryByOriginPath(originPath)

    suspend fun getByHistoryId(historyId: Int): Pair<SearchAnimeResult?, Long> {
        val history = historyService.getById(historyId) ?: return null to 0
        val result: SearchAnimeResult? = Json.decodeFromString(history.result)
        return result to history.time
    }

    private suspend fun saveHistory(
        originPath: String,
        cachePath: String,
        searchAnimeResult: SearchAnimeResult
    ) {
        val animationHistory = withContext(Dispatchers.Default) {
            AnimationHistory().apply {
                this.originPath = originPath
                this.cachePath = cachePath
                this.result = Json.encodeToString(searchAnimeResult)
                this.time = Clock.System.now().toEpochMilliseconds()
                if (searchAnimeResult.result.isNotEmpty()) {
                    val result = searchAnimeResult.result[0]
                    this.title = result.aniList.title.native ?: ""
                    this.anilistId = result.aniList.id ?: 0
                    this.episode = formatEpisode(result.episode) ?: ""
                    this.similarity = result.similarity
                } else {
                    this.title = getString(Res.string.hint_no_result)
                }
            }
        }
        historyService.saveHistory(animationHistory)
    }

    override suspend fun getHistoryDetails(historyId: Int): HistoryLookup? {
        val history = historyService.getById(historyId) ?: return null
        return HistoryLookup(
            history.id,
            getCacheFilePathBySavedCacheFilePath(history.cachePath),
            history.time,
            Json.decodeFromString(history.result),
            history.readonly().isOldData,
        )
    }

    override suspend fun queryAllHistory(): List<AnimationHistory> {
        val histories = historyService.queryAllHistory()
        //重新组装缓存图片路径，因为iOS沙盒id会变
        histories.forEach { history ->
            history.cachePath = getCacheFilePathBySavedCacheFilePath(history.cachePath)
        }
        return histories
    }

    override suspend fun deleteHistory(historyId: Int): DeleteHistoryResult = withCacheOperation {
        val history = historyService.getById(historyId) ?: return@withCacheOperation DeleteHistoryResult.Deleted
        historyService.delete(historyId)
        try {
            val path = getCacheFilePathBySavedCacheFilePath(history.cachePath)
            val directory = managedCacheDirectory() ?: return@withCacheOperation DeleteHistoryResult.CacheCleanupFailed
            val references = historyService.queryAllHistory().map { getCacheFilePathBySavedCacheFilePath(it.cachePath) }
            if (path in references) return@withCacheOperation DeleteHistoryResult.Deleted
            if (!canDeleteCache(path, directory, references)) return@withCacheOperation DeleteHistoryResult.CacheCleanupFailed
            val removed = withContext(Dispatchers.IO) {
                val file = PlatformFile(path)
                if (file.exists()) file.delete()
                !file.exists()
            }
            if (removed) DeleteHistoryResult.Deleted else DeleteHistoryResult.CacheCleanupFailed
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            DeleteHistoryResult.CacheCleanupFailed
        }
    }
}