package pw.janyo.whatanime.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pw.janyo.whatanime.config.APP
import vip.mystery0.rx.PackageData
import vip.mystery0.rx.error
import vip.mystery0.tools.context
import vip.mystery0.tools.doByTry
import vip.mystery0.tools.utils.cloneToFile
import vip.mystery0.tools.utils.md5
import vip.mystery0.tools.utils.sha1
import java.io.File

object FileUtil {
	private const val CACHE_IMAGE_FILE_NAME = "cacheImage"

	/**
	 * 获取缓存文件
	 * @param file 当前搜索的图片的路径
	 * @return 缓存文件（需要判断是否存在，如果返回为空说明目录权限有问题）
	 */
	fun getCacheFile(file: File): File? {
		val saveParent = context().getExternalFilesDir(CACHE_IMAGE_FILE_NAME) ?: return null
		if (!saveParent.exists())
			saveParent.mkdirs()
		if (saveParent.isDirectory || saveParent.delete() && saveParent.mkdirs()) {
			val md5Name = file.absolutePath.md5()
			return File(saveParent, md5Name)
		}
		return null
	}

	/**
	 * 将Uri的内容克隆到临时文件，然后返回临时文件
	 *
	 * @param uri 选择器返回的Uri
	 *
	 * @return 临时文件
	 */
	suspend fun cloneUriToFile(uri: Uri): File? {
		val parent = context().getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return null
		if (!parent.exists())
			parent.mkdirs()
		if (parent.isDirectory || parent.delete() && parent.mkdirs()) {
			val file = File(parent, uri.toString().sha1())
			uri.cloneToFile(file)
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
}