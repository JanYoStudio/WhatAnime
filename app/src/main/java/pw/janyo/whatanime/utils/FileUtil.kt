package pw.janyo.whatanime.utils

import pw.janyo.whatanime.config.APP
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
		val saveParent = APP.context.getExternalFilesDir(CACHE_IMAGE_FILE_NAME)
		if (!saveParent.exists())
			saveParent.mkdirs()
		if (saveParent.isDirectory || saveParent.delete() && saveParent.mkdirs()) {
			val md5Name = StringTools.getMD5(file.absolutePath)
			return File(saveParent, md5Name)
		}
		return null
	}
}