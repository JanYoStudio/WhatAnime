package pw.janyo.whatanime.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by mystery0.
 */

public class Encryption
{
	private static final String TAG = "Encryption";

	public static String encodeFileToBase64(String path)
	{
		String base64 = "";
		try
		{
			Bitmap bitmap = BitmapFactory.decodeFile(path);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
			int options = 100;
			while (outputStream.toByteArray().length / 1024 > 800 && options > 0)
			{
				outputStream.reset();
				bitmap.compress(Bitmap.CompressFormat.JPEG, options, outputStream);
				options -= 10;
			}
			byte[] data = outputStream.toByteArray();
			int length = data.length;
			base64 = Base64.encodeToString(data, 0, length, Base64.NO_WRAP);
			outputStream.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return base64;
	}
}
