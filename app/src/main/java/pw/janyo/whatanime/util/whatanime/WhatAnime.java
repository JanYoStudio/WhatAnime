package pw.janyo.whatanime.util.whatanime;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by myste.
 */

public class WhatAnime {
    private String path;

    public void setPath(String path) {
        this.path = path;
    }

    byte[] compressBitmap() {
        byte[] data;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int quality = 100;
            do {
				outputStream.reset();
				Bitmap bitmap=BitmapFactory.decodeFile(path);
				bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
				quality -= 10;
			}
            while (outputStream.toByteArray().length / 1024 > 800 && quality > 0);
            data = outputStream.toByteArray();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            data = new byte[0];
        }
        return data;
    }

    String base64Data(byte[] data) {
        int length = data.length;
        return Base64.encodeToString(data, 0, length, Base64.NO_WRAP);
    }
}
