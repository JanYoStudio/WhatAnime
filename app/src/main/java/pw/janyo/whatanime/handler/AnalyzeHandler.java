package pw.janyo.whatanime.handler;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
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
					Logs.i(TAG, "handleMessage: ");
				} catch (Exception e)
				{
					e.printStackTrace();
					sendEmptyMessage(1);
				}
				break;
			case 1:
				Toast.makeText(context, "解析错误", Toast.LENGTH_SHORT).show();
				break;
		}
		progressDialog.dismiss();
	}
}
