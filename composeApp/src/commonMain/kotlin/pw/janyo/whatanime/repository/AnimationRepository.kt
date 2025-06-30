package pw.janyo.whatanime.repository

import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
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
import pw.janyo.whatanime.utils.isOnline
import pw.janyo.whatanime.utils.md5
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.hint_no_network
import whatanime.composeapp.generated.resources.hint_no_result
import whatanime.composeapp.generated.resources.hint_search_error

class AnimationRepository : KoinComponent {
    private val searchApi by inject<SearchApi>()
    private val historyService by inject<HistoryService>()

    private suspend fun checkNetwork() {
        if (!isOnline()) {
            throw RuntimeException(getString(Res.string.hint_no_network))
        }
    }

    private suspend fun queryAnimationByImageOnline(
        file: PlatformFile,
        originPath: String,
        cachePath: String,
        mimeType: String,
    ): SearchAnimeResult {
        val history = queryByFileMd5(file)
        if (history != null) {
            return history
        }
        checkNetwork()
        val byteArray = file.readBytes()
        val multipart = MultiPartFormDataContent(formData {
            append("image", byteArray, Headers.build {
                append(HttpHeaders.ContentType, mimeType)
                append(HttpHeaders.ContentDisposition, "filename=${file.name}")
            })
        })
        val data = if (Configure.cutBorders) {
            searchApi.search(multipart)
        } else {
            searchApi.searchNoCut(multipart)
        }
        if (data.error.isNotBlank()) {
            Logger.e("http request failed, ${data.error}")
            throw RuntimeException(getString(Res.string.hint_search_error))
        }
        saveHistory(originPath, cachePath, data)
        return data
    }

    suspend fun showQuota(): SearchQuota {
        checkNetwork()
        return searchApi.getMe()
    }

    suspend fun queryAnimationByImageLocal(
        file: PlatformFile,
        originPath: String,
        cachePath: String,
        mimeType: String,
    ): SearchAnimeResult {
        val animationHistory = historyService.queryHistoryByOriginPath(originPath)
            ?: return queryAnimationByImageOnline(file, originPath, cachePath, mimeType)
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

    suspend fun queryAllHistory(): List<AnimationHistory> = historyService.queryAllHistory()

    suspend fun deleteHistory(historyId: Int) {
        val animationHistory = historyService.getById(historyId)
        historyService.delete(historyId)
        animationHistory?.let {
            PlatformFile(it.cachePath).delete()
        }
    }
}