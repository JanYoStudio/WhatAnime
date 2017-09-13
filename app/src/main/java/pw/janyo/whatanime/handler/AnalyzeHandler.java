package pw.janyo.whatanime.handler;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import pw.janyo.whatanime.util.Settings;
import vip.mystery0.tools.Logs.Logs;

import java.util.List;

import pw.janyo.whatanime.adapter.AnimationAdapter;
import pw.janyo.whatanime.classes.Animation;
import pw.janyo.whatanime.classes.Dock;

public class AnalyzeHandler extends Handler
{
	private static final String TAG = "AnalyzeHandler";
	public Context context;
	public ProgressDialog progressDialog;
	public List<Dock> list;
	public AnimationAdapter adapter;

	@Override
	public void handleMessage(Message msg)
	{
		progressDialog.setMessage("解析中……");
		switch (msg.what)
		{
			case 0:
				String response = msg.obj.toString();
				Logs.i(TAG, "handleMessage: " + response);
				try
				{
					Animation animation = new Gson().fromJson(response, Animation.class);
					list.clear();
					Settings settings = Settings.getInstance(context);
					if (settings.getResultNumber() == 0 || settings.getResultNumber() > animation.docs.size())
						list.addAll(animation.docs);
					else
						list.addAll(animation.docs.subList(0, settings.getResultNumber()));
					adapter.notifyDataSetChanged();
				} catch (Exception e)
				{
					Log.wtf(TAG, "handleMessage: 解析json错误", e);
					switch (response.charAt(0))
					{
						case '{':
							sendEmptyMessage(3);
							break;
						case '<':
							sendEmptyMessage(4);
							break;
						default:
							sendEmptyMessage(2);
							break;
					}
				}
				break;
			case 1:
				String error = msg.obj.toString();
				Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
				break;
			case 2:
				Toast.makeText(context, "解析错误", Toast.LENGTH_SHORT).show();
				break;
			case 3:
				Toast.makeText(context, "返回数据过长，已知BUG，将会在不久后修复！", Toast.LENGTH_SHORT).show();
				break;
			case 4:
				Toast.makeText(context, "图片太大，将会在之后的版本中尝试修复！", Toast.LENGTH_SHORT).show();
				break;
		}
		progressDialog.dismiss();
	}
}
