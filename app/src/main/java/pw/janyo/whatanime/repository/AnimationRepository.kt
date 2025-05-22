package pw.janyo.whatanime.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody.Companion.asRequestBody
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pw.janyo.whatanime.R
import pw.janyo.whatanime.api.SearchApi
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.config.DispatcherConfig
import pw.janyo.whatanime.constant.StringConstant.resString
import pw.janyo.whatanime.isOnline
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.model.ResourceException
import pw.janyo.whatanime.model.SearchAnimeResult
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.model.searchAnimeResultAdapter
import pw.janyo.whatanime.repository.local.service.HistoryService
import pw.janyo.whatanime.utils.md5
import java.io.File
import java.util.Calendar

class AnimationRepository : KoinComponent {
    companion object {
        private const val TAG = "AnimationRepository"
    }

    private val searchApi: SearchApi by inject()
    private val historyService: HistoryService by inject()

    private suspend fun checkNetwork() {
        withContext(DispatcherConfig.CHECK_NETWORK) {
            if (!isOnline()) {
                throw ResourceException(R.string.hint_no_network)
            }
        }
    }

    private suspend fun queryAnimationByImageOnline(
        file: File,
        originPath: String,
        cachePath: String,
        mimeType: String,
    ): SearchAnimeResult {
        val history = queryByFileMd5(file)
        if (history != null) {
            return history
        }
        checkNetwork()
        val data = withContext(DispatcherConfig.NETWORK) {
            val data = if (Configure.cutBorders) {
                searchApi.search(file.asRequestBody(), mimeType)
            } else {
                searchApi.searchNoCut(file.asRequestBody(), mimeType)
            }
            if (data.error.isNotBlank()) {
                Log.e(TAG, "http request failed, ${data.error}")
                throw ResourceException(R.string.hint_search_error)
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
        mimeType: String,
    ): SearchAnimeResult {
        val animationHistory = withContext(Dispatchers.IO) {
            historyService.queryHistoryByOriginPath(originPath)
        } ?: return queryAnimationByImageOnline(file, originPath, cachePath, mimeType)
        return searchAnimeResultAdapter.fromJson(animationHistory.result)!!
    }

    private suspend fun queryByFileMd5(file: File): SearchAnimeResult? {
        val md5 = file.md5()
        val animationHistory = withContext(Dispatchers.IO) {
            //用现有的originPath字段来存储md5
            historyService.queryHistoryByOriginPath(md5)
        } ?: return null
        return searchAnimeResultAdapter.fromJson(animationHistory.result)
    }

    suspend fun queryHistoryByOriginPath(originPath: String): AnimationHistory? =
        withContext(Dispatchers.IO) {
            historyService.queryHistoryByOriginPath(originPath)
        }

    suspend fun getByHistoryId(historyId: Int): Pair<SearchAnimeResult?, Long> {
        val history = withContext(Dispatchers.IO) {
            historyService.getById(historyId)
        } ?: return null to 0
        val result: SearchAnimeResult? = searchAnimeResultAdapter.fromJson(history.result)
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
                this.result = searchAnimeResultAdapter.toJson(searchAnimeResult)
                this.time = Calendar.getInstance().timeInMillis
                if (searchAnimeResult.result.isNotEmpty()) {
                    val result = searchAnimeResult.result[0]
                    this.title = result.aniList.title.native ?: ""
                    this.anilistId = result.aniList.id ?: 0
                    this.episode = ""
                    this.similarity = result.similarity
                } else {
                    this.title = R.string.hint_no_result.resString()
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