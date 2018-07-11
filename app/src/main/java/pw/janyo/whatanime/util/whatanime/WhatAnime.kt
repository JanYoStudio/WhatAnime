package pw.janyo.whatanime.util.whatanime

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

import java.io.ByteArrayOutputStream
import java.io.IOException

class WhatAnime {
	private var path: String? = null

	fun setPath(path: String) {
		this.path = path
	}

	 fun compressBitmap(): ByteArray {
		var data: ByteArray
		try {
			val bitmap = BitmapFactory.decodeFile(path)
			val outputStream = ByteArrayOutputStream()
			var quality = 100
			do {
				outputStream.reset()
				bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
				quality -= 10
			} while (outputStream.toByteArray().size / 1024 > 512 && quality > 0)
			data = outputStream.toByteArray()
			outputStream.close()
		} catch (e: IOException) {
			e.printStackTrace()
			data = ByteArray(0)
		}

		return data
	}

	 fun base64Data(data: ByteArray): String {
		val length = data.size
		return Base64.encodeToString(data, 0, length, Base64.NO_WRAP)
	}
}
