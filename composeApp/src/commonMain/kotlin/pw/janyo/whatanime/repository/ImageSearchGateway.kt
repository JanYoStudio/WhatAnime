package pw.janyo.whatanime.repository

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pw.janyo.whatanime.model.SearchAnimeResultItem
import pw.janyo.whatanime.utils.getCacheFile
import pw.janyo.whatanime.utils.getCacheFilePathBySavedCacheFilePath

fun interface ImageSearchGateway {
    suspend fun search(image: PlatformFile, cutBorders: Boolean): ImageSearchOutput
}

data class ImageSearchOutput(val image: PlatformFile, val results: List<SearchAnimeResultItem>)

enum class SearchFailure { TooLarge, FileUnavailable, CacheUnavailable, SearchFailed }
class SearchInputException(val failure: SearchFailure) : Exception(failure.name)

internal fun imageSizeFailure(size: Long): SearchFailure? = when {
    size < 0 -> SearchFailure.FileUnavailable
    size > 25L * 1024 * 1024 -> SearchFailure.TooLarge
    else -> null
}

class RepositoryImageSearchGateway(private val repository: AnimationRepository) : ImageSearchGateway {
    override suspend fun search(image: PlatformFile, cutBorders: Boolean): ImageSearchOutput = withContext(Dispatchers.IO) {
        repository.withCacheOperation { searchLocked(image, cutBorders) }
    }

    private suspend fun searchLocked(image: PlatformFile, cutBorders: Boolean): ImageSearchOutput {
        try {
            if (!image.exists()) throw SearchInputException(SearchFailure.FileUnavailable)
            imageSizeFailure(image.size())?.let { throw SearchInputException(it) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: SearchInputException) {
            throw e
        } catch (_: Exception) {
            throw SearchInputException(SearchFailure.FileUnavailable)
        }
        val existing = repository.queryHistoryByOriginPath(image.absolutePath())
        val cachedImage = try {
            val target = existing?.cachePath?.let { PlatformFile(getCacheFilePathBySavedCacheFilePath(it)) }
                ?: getCacheFile(image) ?: throw SearchInputException(SearchFailure.CacheUnavailable)
            // 不删除同路径源文件或已被历史引用的缓存；搜索与删除共享 Repository 的事务锁。
            if (!target.exists() && target.absolutePath() != image.absolutePath()) image.copyTo(target)
            target
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            throw SearchInputException(SearchFailure.CacheUnavailable)
        }
        val result = repository.queryAnimationByImageLocal(image, image.absolutePath(), cachedImage.absolutePath(), cutBorders)
        return ImageSearchOutput(cachedImage, result.result)
    }
}
