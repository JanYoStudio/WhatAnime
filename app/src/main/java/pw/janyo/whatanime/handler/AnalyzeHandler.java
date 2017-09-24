package pw.janyo.whatanime.handler;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import dmax.dialog.SpotsDialog;

import java.util.List;

import pw.janyo.whatanime.adapter.AnimationAdapter;
import pw.janyo.whatanime.classes.Dock;

public class AnalyzeHandler extends Handler
{
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
				adapter.notifyDataSetChanged();
				break;
			case 1:
				Toast.makeText(context, "解析错误", Toast.LENGTH_SHORT).show();
				list.clear();
				adapter.notifyDataSetChanged();
				break;
		}
		progressDialog.dismiss();
	}
}
