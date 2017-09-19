package pw.janyo.whatanime;

import android.app.Application;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import pw.janyo.whatanime.classes.Response;
import vip.mystery0.tools.CrashHandler.CatchExceptionListener;
import vip.mystery0.tools.CrashHandler.CrashHandler;
import vip.mystery0.tools.HTTPok.HTTPok;
import vip.mystery0.tools.HTTPok.HTTPokResponse;
import vip.mystery0.tools.HTTPok.HTTPokResponseListener;
import vip.mystery0.tools.Logs.Logs;

/**
 * Created by mystery0.
 */

public class APP extends Application
{
	private static final String TAG = "APP";

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
						Map<String, Object> map = new HashMap<>();
						map.put("logFile", file);
						map.put("date", date);
						map.put("appName", getString(R.string.app_name));
						map.put("appVersionName", appVersionName);
						map.put("appVersionCode", appVersionCode);
						map.put("androidVersion", androidVersion);
						map.put("sdk", sdk);
						map.put("vendor", vendor);
						map.put("model", model);
						new HTTPok()
								.setURL("http://123.206.186.70/php/uploadLog/upload_file.php")
								.setRequestMethod(HTTPok.Companion.getPOST())
								.setParams(map)
								.isFileRequest()
								.setListener(new HTTPokResponseListener()
								{
									@Override
									public void onError(String s)
									{
										Logs.e(TAG, "onError: " + s);
										Toast.makeText(getApplicationContext(), "日志上传失败！", Toast.LENGTH_SHORT)
												.show();
									}

									@Override
									public void onResponse(HTTPokResponse httPokResponse)
									{
										Response response = httPokResponse.getJSON(Response.class);
										if (response.code == 0)
											Toast.makeText(getApplicationContext(), "日志上传成功！", Toast.LENGTH_SHORT)
													.show();
										else
											Toast.makeText(getApplicationContext(), "日志上传失败！", Toast.LENGTH_SHORT)
													.show();
									}
								})
								.open();

					}
				})
				.init();
	}
}
