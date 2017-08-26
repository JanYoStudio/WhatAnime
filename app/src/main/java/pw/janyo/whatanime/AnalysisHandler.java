package pw.janyo.whatanime;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mystery0.tools.Logs.Logs;

import java.util.List;

public class AnalysisHandler extends Handler
{
	private static final String TAG = "AnalysisHandler";
	Context context;
	ProgressDialog progressDialog;
	AnimationAdapter adapter;
	List<Dock> list;

	@Override
	public void handleMessage(Message msg)
	{
		progressDialog.setMessage("解析中……");
		String response = msg.obj.toString();
		Logs.i(TAG, ": "+response);
		try
		{
			Gson gson = new Gson();
			Animation animation = gson.fromJson(response, Animation.class);
			list.clear();
			list.addAll(animation.docs);
			adapter.notifyDataSetChanged();
		} catch (Exception e)
		{
			Toast.makeText(context, "解析失败！", Toast.LENGTH_SHORT)
					.show();
			e.printStackTrace();
		}
		progressDialog.dismiss();
	}
}
