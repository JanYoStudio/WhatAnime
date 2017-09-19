package pw.janyo.whatanime.handler;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;

import dmax.dialog.SpotsDialog;
import pw.janyo.whatanime.R;
import pw.janyo.whatanime.classes.Response;

/**
 * Created by myste.
 */

public class UploadHandler extends Handler
{
	public SpotsDialog spotsDialog;
	public Activity activity;
	public CoordinatorLayout coordinatorLayout;
	public Response response;

	@Override
	public void handleMessage(Message msg)
	{
		spotsDialog.dismiss();
		int code = response == null ? -1 : response.code;
		if (code == 0)
		{
			Snackbar.make(coordinatorLayout, R.string.hint_upload_log_done, Snackbar.LENGTH_SHORT)
					.addCallback(new Snackbar.Callback()
					{
						@Override
						public void onDismissed(Snackbar transientBottomBar, int event)
						{
							activity.finish();
						}
					})
					.show();
		} else
		{
			Snackbar.make(coordinatorLayout, R.string.hint_upload_log_error, Snackbar.LENGTH_SHORT)
					.addCallback(new Snackbar.Callback()
					{
						@Override
						public void onDismissed(Snackbar transientBottomBar, int event)
						{
							activity.finish();
						}
					})
					.show();
		}
	}
}
