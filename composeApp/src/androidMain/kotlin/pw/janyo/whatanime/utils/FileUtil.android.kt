package pw.janyo.whatanime.utils

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import pw.janyo.whatanime.context
import java.io.File

private const val CACHE_IMAGE_FILE_NAME = "cacheImage"

actual suspend fun getCacheFile(file: PlatformFile): PlatformFile? {
    val saveParent = context.getExternalFilesDir(CACHE_IMAGE_FILE_NAME) ?: return null
    if (!saveParent.exists())
        saveParent.mkdirs()
    if (saveParent.isDirectory || saveParent.delete() && saveParent.mkdirs()) {
        val md5Name = file.absolutePath().md5()
        return PlatformFile(File(saveParent, md5Name))
    }
    return null
}

actual fun getCacheFilePathBySavedCacheFilePath(path: String): String = path