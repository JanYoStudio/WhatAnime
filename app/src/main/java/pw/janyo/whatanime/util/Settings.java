package pw.janyo.whatanime.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by myste.
 */

public class Settings
{
	private static Settings settings;
	private static SharedPreferences sharedPreferences;

	private Settings(Context context)
	{
		sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
	}

	public static Settings getInstance(Context context)
	{
		if (settings == null)
		{
			settings = new Settings(context);
		}
		return settings;
	}

	public void setResultNumber(int number)
	{
		sharedPreferences.edit().putInt("resultNumber", number).apply();
	}

	public int getResultNumber()
	{
		return sharedPreferences.getInt("resultNumber", 1);
	}
}
