package pw.janyo.whatanime.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import vip.mystery0.tools.context
import vip.mystery0.tools.utils.cloneToFile
import vip.mystery0.tools.utils.md5
import vip.mystery0.tools.utils.sha1
import java.io.File
import kotlin.math.max
import kotlin.math.min


private const val CACHE_IMAGE_FILE_NAME = "cacheImage"

/**
 * 获取缓存文件
 * @return 缓存文件（需要判断是否存在，如果返回为空说明目录权限有问题）
 */
fun File.getCacheFile(): File? {
	val saveParent = context().getExternalFilesDir(CACHE_IMAGE_FILE_NAME) ?: return null
	if (!saveParent.exists())
		saveParent.mkdirs()
	if (saveParent.isDirectory || saveParent.delete() && saveParent.mkdirs()) {
		val md5Name = absolutePath.md5()
		return File(saveParent, md5Name)
	}
	return null
}

/**
 * 将Uri的内容克隆到临时文件，然后返回临时文件
 *
 * @return 临时文件
 */
suspend fun Uri.cloneUriToFile(): File? {
	val parent = context().getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return null
	if (!parent.exists())
		parent.mkdirs()
	if (parent.isDirectory || parent.delete() && parent.mkdirs()) {
		val file = File(parent, this.toString().sha1())
		cloneToFile(file)
		return file
	}
	return null
}

private fun File.zoomBitmap(): Bitmap {
	val maxHeight = 640
	val maxWidth = 640
	val options = BitmapFactory.Options()
	options.inJustDecodeBounds = true
	BitmapFactory.decodeFile(absolutePath, options)
	val scaleW = max(maxWidth, options.outWidth) / (min(maxWidth, options.outWidth) * 1.0) - 0.5
	val scaleH = max(maxHeight, options.outHeight) / (min(maxHeight, options.outHeight) * 1.0) - 0.5
	options.inSampleSize = max(scaleW, scaleH).toInt()
	options.inJustDecodeBounds = false
	return BitmapFactory.decodeFile(absolutePath, options)
}