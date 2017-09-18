package pw.janyo.whatanime;

import android.app.Application;

import java.io.File;

import vip.mystery0.tools.CrashHandler.CatchExceptionListener;
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
				.sendException(new CatchExceptionListener()
				{
					@Override
					public void onException(String date, File file, String appVersionName, int appVersionCode, String androidVersion, int sdk, String vendor, String model, Throwable throwable)
					{

					}
				})
				.init();
	}
}
