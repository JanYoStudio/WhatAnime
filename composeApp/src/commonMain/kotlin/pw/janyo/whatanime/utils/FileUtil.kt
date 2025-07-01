package pw.janyo.whatanime.utils

import io.github.vinceglb.filekit.PlatformFile

private val map = hashMapOf(
    "jpeg" to "image/jpeg",
    "jpg" to "image/jpeg",
    "png" to "image/png",
    "gif" to "image/gif",
    "bmp" to "image/bmp",
    "svg" to "image/svg+xml",
    "webp" to "image/webp",
    "tiff" to "image/tiff",
    "tif" to "image/tiff",
    "ico" to "image/vnd.microsoft.icon",
    "avif" to "image/avif",
)

fun getMimeType(extension: String): String? = map[extension]

expect suspend fun getCacheFile(file: PlatformFile): PlatformFile?

expect fun getCacheFilePathBySavedCacheFilePath(path: String): String