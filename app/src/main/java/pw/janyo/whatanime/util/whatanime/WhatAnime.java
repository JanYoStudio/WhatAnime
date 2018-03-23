package pw.janyo.whatanime.util.whatanime;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import pw.janyo.whatanime.util.WAFileUtil;
import vip.mystery0.logs.Logs;

public class WhatAnime {
	private static final String TAG = "WhatAnime";
	private String path;

	public void setPath(String path) {
		this.path = path;
	}

	byte[] compressBitmap() {
		byte[] data;
		try {
			Bitmap bitmap = BitmapFactory.decodeFile(path);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			int quality = 100;
			do {
				outputStream.reset();
				bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
				Logs.i(TAG, "compressBitmap: quality: " + quality + " " + WAFileUtil.formatFileSize(outputStream.toByteArray().length, 2));
				quality -= 10;
			}
			while (outputStream.toByteArray().length / 1024 > 512 && quality > 0);
			data = outputStream.toByteArray();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			data = new byte[0];
		}
		Logs.i(TAG, "compressBitmap: " + WAFileUtil.formatFileSize(data.length, 2));
		return data;
	}

	String base64Data(byte[] data) {
		int length = data.length;
		return Base64.encodeToString(data, 0, length, Base64.NO_WRAP);
	}
}
