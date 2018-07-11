package pw.janyo.whatanime.util

import com.google.gson.Gson

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader

import pw.janyo.whatanime.classes.History

/**
 * Created by myste.
 */

object WAFileUtil {

	fun fileCopy(oldPath: String, newPath: String) {
		try {
			val oldFile = File(oldPath)
			if (!oldFile.exists())
				return
			val newFile = File(newPath)
			if (!newFile.parentFile.exists())
				newFile.parentFile.mkdirs()
			if (!newFile.exists())
				newFile.createNewFile()
			val fileInputStream = FileInputStream(oldFile)
			val fileOutputStream = FileOutputStream(newFile)
			val bytes = ByteArray(1024 * 1024 * 10)
			var read: Int
			while ((read = fileInputStream.read(bytes)) != -1) {
				fileOutputStream.write(bytes, 0, read)
			}
		} catch (e: IOException) {
			e.printStackTrace()
		}

	}

	fun saveJson(`object`: Any, file: File): Boolean {
		if (!file.parentFile.exists())
			file.parentFile.mkdirs()
		if (file.exists())
			file.delete()
		var fileOutputStream: FileOutputStream? = null
		try {
			fileOutputStream = FileOutputStream(file)
			fileOutputStream.write(Gson().toJson(`object`).toByteArray())
		} catch (e: IOException) {
			e.printStackTrace()
			return false
		} finally {
			if (fileOutputStream != null)
				try {
					fileOutputStream.close()
				} catch (e: IOException) {
					e.printStackTrace()
				}

		}
		return true
	}

	fun <T> getSavedObject(file: File, classOfT: Class<T>): T? {
		if (!file.exists())
			return null
		try {
			return Gson().fromJson(InputStreamReader(FileInputStream(file)), classOfT)
		} catch (e: FileNotFoundException) {
			e.printStackTrace()
			return null
		}

	}

	fun checkList(list: MutableList<History>): List<History> {
		val iterator = list.iterator()
		while (iterator.hasNext()) {
			val history = iterator.next()
			val cacheFile = File(history.saveFilePath!!)
			if (cacheFile.exists())
				continue
			if (File(history.imaPath!!).exists()) {
				Thread(Runnable { File(history.cachePath!!).delete() }).start()
				continue
			}
			history.delete()
			iterator.remove()
		}
		return list
	}

	fun deleteHistory(history: History) {
		File(history.cachePath!!).deleteOnExit()
		File(history.saveFilePath!!).deleteOnExit()
		history.delete()
	}
}
