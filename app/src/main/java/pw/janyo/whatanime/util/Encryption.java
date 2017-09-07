package pw.janyo.whatanime.util;

import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by mystery0.
 */

public class Encryption
{
	public static String encodeFileToBase64(String path)
	{
		String base64 = "";
		try
		{
			File file = new File(path);
			FileInputStream inputFile = new FileInputStream(file);
			byte[] buffer = new byte[(int) file.length()];
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
