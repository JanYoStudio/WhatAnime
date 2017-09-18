package pw.janyo.whatanime.handler;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import dmax.dialog.SpotsDialog;
import pw.janyo.whatanime.util.Settings;
import vip.mystery0.tools.HTTPok.HTTPokResponse;

import java.util.List;

import pw.janyo.whatanime.adapter.AnimationAdapter;
import pw.janyo.whatanime.classes.Animation;
import pw.janyo.whatanime.classes.Dock;

public class AnalyzeHandler extends Handler
{
	private static final String TAG = "AnalyzeHandler";
	public Context context;
	public SpotsDialog progressDialog;
	public List<Dock> list;
	public AnimationAdapter adapter;

	@Override
	public void handleMessage(Message msg)
	{
		progressDialog.setMessage("解析中……");
		switch (msg.what)
		{
			case 0:
				HTTPokResponse response = (HTTPokResponse) msg.obj;
				try
				{
					Animation animation = response.getJSON(Animation.class);
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
					sendEmptyMessage(2);
				}
				break;
			case 1:
				String error = msg.obj.toString();
				Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
				list.clear();
				break;
			case 2:
				Toast.makeText(context, "解析错误", Toast.LENGTH_SHORT).show();
				list.clear();
				break;
		}
		progressDialog.dismiss();
	}
}
