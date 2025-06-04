package pw.janyo.whatanime.utils

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

private const val CACHE_IMAGE_FILE_NAME = "cacheImage"

@OptIn(ExperimentalForeignApi::class)
actual fun getCacheFile(file: PlatformFile): PlatformFile? {
    val cacheDirectoryUrl = NSFileManager.defaultManager.URLForDirectory(
        directory = NSCachesDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    ) ?: return null

    val saveParentPath = "${cacheDirectoryUrl.path}/$CACHE_IMAGE_FILE_NAME"
    val originalFilePath = file.absolutePath()
    val md5Name = originalFilePath.md5()
    val cachedFilePath = "$saveParentPath/$md5Name"
    return PlatformFile(cachedFilePath)
}