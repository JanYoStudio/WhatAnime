package pw.janyo.whatanime;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mystery0.tools.Logs.Logs;

import java.util.Calendar;

public class TempHandler extends Handler
{
	private static final String TAG = "TempHandler";
	Context context;
	ProgressDialog progressDialog;
	TextView text_name;
	TextView text_chinese_name;
	TextView text_number;
	TextView text_time;

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
			Dock dock=animation.docs.get(0);
			text_name.setText(context.getString(R.string.text_name, dock.title));
			StringBuilder temp = new StringBuilder();
			for (String t : dock.synonyms_chinese)
			{
				temp.append(t).append("，");
			}
			text_chinese_name.setText(context.getString(R.string.text_chinese_name, temp.toString()));
			text_number.setText(context.getString(R.string.text_number, dock.episode));
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(((long) dock.at));
			int time_s = calendar.get(Calendar.SECOND);
			int time_m = calendar.get(Calendar.MINUTE);
			text_time.setText(context.getString(R.string.text_time, time_m, time_s));
		} catch (Exception e)
		{
			Toast.makeText(context, "解析失败！", Toast.LENGTH_SHORT)
					.show();
			e.printStackTrace();
		}
		progressDialog.dismiss();
	}
}
