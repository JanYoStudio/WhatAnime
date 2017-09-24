package pw.janyo.whatanime.util.whatanime;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by myste.
 */

public class WhatAnime
{
	private String path;

	public void setPath(String path)
	{
		this.path = path;
	}

	Bitmap getBitmapFromFile()
	{
		return BitmapFactory.decodeFile(path);
	}

	byte[] compressBitmap(Bitmap bitmap)
	{
		byte[] data;
		try
		{
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
			int options = 100;
			while (outputStream.toByteArray().length / 1024 > 800 && options > 0)
			{
				outputStream.reset();
				bitmap.compress(Bitmap.CompressFormat.JPEG, options, outputStream);
				options -= 10;
			}
			data = outputStream.toByteArray();
			outputStream.close();
		} catch (IOException e)
		{
			e.printStackTrace();
			data = new byte[0];
		}
		return data;
	}

	String base64Data(byte[] data)
	{
		int length = data.length;
		return Base64.encodeToString(data, 0, length, Base64.NO_WRAP);
	}
}
