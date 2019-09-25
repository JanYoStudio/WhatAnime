package pw.janyo.whatanime.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.mystery0.tools.context
import vip.mystery0.tools.utils.cloneToFile
import vip.mystery0.tools.utils.closeQuietly
import vip.mystery0.tools.utils.md5
import vip.mystery0.tools.utils.sha1
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
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

/**
 * 获取Markdown文件的内容
 * @param context 上下文
 * @param fileName 文件名
 *
 * @return 内容
 */
fun getMarkdown(context: Context, fileName: String): String {
	val inputStream = context.assets.open(fileName)
	val length = inputStream.available()
	val byteArray = ByteArray(length)
	inputStream.read(byteArray)
	inputStream.close()
	return String(byteArray)
}

/**
 * 压缩图片并进行base64加密
 * @param compressFormat 压缩的格式
 * @param maxSize 压缩之后最大的大小
 * @param interval 每次压缩的压缩率差值
 * @return 字节数组
 */
@Throws(IOException::class)
suspend fun File.base64CompressImage(compressFormat: Bitmap.CompressFormat, maxSize: Int, interval: Int): String {
	return withContext(Dispatchers.IO) {
		val outputStream = ByteArrayOutputStream()
		var option = 100
		var base64String: String? = null
		while (option >= 0) {
			outputStream.reset()
			val bitmap = zoomBitmap()
			bitmap.compress(compressFormat, option, outputStream)
			base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
			if (base64String.length <= maxSize)
				break
			option -= interval
		}
		outputStream.closeQuietly(true)
		base64String!!
	}
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