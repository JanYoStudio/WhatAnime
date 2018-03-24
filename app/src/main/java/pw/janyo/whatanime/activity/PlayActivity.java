package pw.janyo.whatanime.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.MediaController;
import android.widget.VideoView;

import pw.janyo.whatanime.R;
import vip.mystery0.logs.Logs;
import vip.mystery0.tools.base.BaseActivity;

public class PlayActivity extends BaseActivity {
	private VideoView videoView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play);

		Intent intent = getIntent();
		String url = intent.getStringExtra("url");
		if (TextUtils.isEmpty(url)) {
			finish();
			return;
		}
		videoView = findViewById(R.id.videoView);

		videoView.setMediaController(new MediaController(this));
		videoView.setVideoURI(Uri.parse(url));
	}

	@Override
	protected void onPause() {
		super.onPause();
		videoView.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		videoView.start();
	}

	@Override
	protected void onStop() {
		super.onStop();
		videoView.stopPlayback();
	}
}
