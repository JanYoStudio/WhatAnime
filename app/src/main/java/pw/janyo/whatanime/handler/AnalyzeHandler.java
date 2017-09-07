package pw.janyo.whatanime.handler;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mystery0.tools.Logs.Logs;

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
				Animation animation = new Gson().fromJson(response, Animation.class);
				list.clear();
				list.addAll(animation.docs);
				adapter.notifyDataSetChanged();
				break;
			case 1:
				Toast.makeText(context, "解析错误", Toast.LENGTH_SHORT).show();
				break;
		}
		progressDialog.dismiss();
	}
}
