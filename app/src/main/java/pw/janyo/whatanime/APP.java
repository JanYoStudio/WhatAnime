package pw.janyo.whatanime;

import android.app.Application;

import vip.mystery0.tools.CrashHandler.CrashHandler;
import vip.mystery0.tools.Logs.Logs;

/**
 * Created by mystery0.
 */

public class APP extends Application
{
	@Override
	public void onCreate()
	{
		super.onCreate();
		Logs.setLevel(Logs.LogLevel.Debug);
		CrashHandler.getInstance(this)
				.setDirectory(getExternalCacheDir())
				.init();
	}
}
