package pw.janyo.whatanime.utils

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import kotlinx.cinterop.ExperimentalForeignApi

private const val CACHE_IMAGE_FILE_NAME = "cacheImage"

@OptIn(ExperimentalForeignApi::class)
actual suspend fun getCacheFile(file: PlatformFile): PlatformFile? {
    val dir = PlatformFile(FileKit.filesDir, CACHE_IMAGE_FILE_NAME)
    if (!dir.exists()) {
        dir.createDirectories()
    }
    val originalFilePath = file.absolutePath()
    val md5Name = originalFilePath.md5()
    return PlatformFile(dir, md5Name)
}