package pw.janyo.whatanime.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import vip.mystery0.tools.Logs.Logs;

/**
 * Created by mystery0.
 */

public class Encryption
{
	private static final String TAG = "Encryption";

	public static String encodeFileToBase64(Context context, String path)
	{
		String base64 = "";
		try
		{
			Bitmap bitmap = BitmapFactory.decodeFile(path);
			File tempFile = new File(context.getExternalCacheDir().getAbsolutePath() + File.separator + new File(path).getName());
			Logs.i(TAG, "encodeFileToBase64: " + tempFile.getAbsolutePath());
			FileOutputStream outputStream = new FileOutputStream(tempFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outputStream);
			outputStream.close();
			FileInputStream inputFile = new FileInputStream(tempFile);
			byte[] buffer = new byte[(int) tempFile.length()];
			int read = inputFile.read(buffer);
			inputFile.close();
			base64 = Base64.encodeToString(buffer, 0, read, Base64.NO_WRAP);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return base64;
	}
}
