package pw.janyo.whatanime.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import pw.janyo.whatanime.config.APP
import vip.mystery0.tools.utils.FileTools
import vip.mystery0.tools.utils.StringTools
import java.io.File

object FileUtil {
	private const val CACHE_IMAGE_FILE_NAME = "cacheImage"

	/**
	 * 获取缓存文件
	 * @param file 当前搜索的图片的路径
	 * @return 缓存文件（需要判断是否存在，如果返回为空说明目录权限有问题）
	 */
	fun getCacheFile(file: File): File? {
		val saveParent = APP.context.getExternalFilesDir(CACHE_IMAGE_FILE_NAME) ?: return null
		if (!saveParent.exists())
			saveParent.mkdirs()
		if (saveParent.isDirectory || saveParent.delete() && saveParent.mkdirs()) {
			val md5Name = StringTools.md5(file.absolutePath)
			return File(saveParent, md5Name)
		}
		return null
	}

	/**
	 * 将Uri的内容克隆到临时文件，然后返回临时文件
	 *
	 * @param context 上下文
	 * @param uri 选择器返回的Uri
	 *
	 * @return 临时文件
	 */
	fun cloneUriToFile(context: Context, uri: Uri): File? {
		val parent = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return null
		if (!parent.exists())
			parent.mkdirs()
		if (parent.isDirectory || parent.delete() && parent.mkdirs()) {
			val file = File(parent, StringTools.sha1(uri.toString()))
			val contentResolver = context.contentResolver
			FileTools.saveFile(contentResolver.openInputStream(uri), file)
			return file
		}
		return null
	}
}