package pw.janyo.whatanime.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import pw.janyo.whatanime.R;
import pw.janyo.whatanime.adapter.AnimationAdapter;
import pw.janyo.whatanime.classes.Dock;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import pw.janyo.whatanime.util.Settings;
import pw.janyo.whatanime.util.WAFileUtil;
import pw.janyo.whatanime.util.whatanime.WhatAnimeBuilder;
import vip.mystery0.logs.Logs;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";
	private final static int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 233;
	private final static int REQUEST_CODE = 322;
	private ImageView imageView;
	private VideoView videoView;
	private ProgressBar progressBar;
	private FloatingActionButton main_fab_upload;
	private AnimationAdapter adapter;
	private List<Dock> list = new ArrayList<>();
	private String nowPlayUrl = "";
	private RequestOptions options = new RequestOptions()
			.diskCacheStrategy(DiskCacheStrategy.NONE);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestPermission();
		initialization();
		monitor();
	}

	private void initialization() {
		setContentView(R.layout.activity_main);
		imageView = findViewById(R.id.imageView);
		videoView = findViewById(R.id.videoView);
		progressBar = findViewById(R.id.progressBar);
		main_fab_upload = findViewById(R.id.main_fab_upload);
		Toolbar toolbar = findViewById(R.id.toolbar);
		RecyclerView recyclerView = findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
		recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
		adapter = new AnimationAdapter(MainActivity.this, list);
		recyclerView.setAdapter(adapter);

		setToolbar(toolbar);

		showcase();
	}

	private void showcase() {
		if (Settings.isFirst())
			new TapTargetSequence(this)
					.targets(TapTarget.forView(main_fab_upload, "点击这个按钮上传动漫截图。").tintTarget(false))
					.continueOnCancel(true)
					.considerOuterCircleCanceled(true)
					.listener(new TapTargetSequence.Listener() {
						@Override
						public void onSequenceFinish() {
							Settings.setFirst();
						}

						@Override
						public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
						}

						@Override
						public void onSequenceCanceled(TapTarget lastTarget) {
						}
					}).start();
	}

	private void monitor() {
		main_fab_upload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View p1) {
				doChoose(REQUEST_CODE);
			}
		});
		adapter.setOnClickListener(new AnimationAdapter.OnClickListener() {
			@Override
			public void onClick(Dock dock) {
				try {
					String requestUrl = "https://whatanime.ga/preview.php?season=" + dock.season + "&anime=" + URLEncoder.encode(dock.anime, "UTF-8") + "&file=" + URLEncoder.encode(dock.filename, "UTF-8") + "&t=" + dock.at + "&token=" + dock.tokenthumb;
					if (!nowPlayUrl.equals(requestUrl)) {
						nowPlayUrl = requestUrl;
						videoView.stopPlayback();
						videoView.setVideoURI(Uri.parse(requestUrl));
					}
					imageView.setVisibility(View.GONE);
					videoView.setVisibility(View.VISIBLE);
					progressBar.setVisibility(View.VISIBLE);
					videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
						@Override
						public void onPrepared(MediaPlayer mp) {
							progressBar.setVisibility(View.GONE);
						}
					});
					videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
						@Override
						public void onCompletion(MediaPlayer mp) {
							videoView.setVisibility(View.GONE);
							imageView.setVisibility(View.VISIBLE);
						}
					});
					videoView.start();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void doChoose(int code) {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intent, code);
	}

	private void requestPermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
					WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
		}
	}

	private void setToolbar(Toolbar toolbar) {
		toolbar.setTitle(getTitle());
		toolbar.inflateMenu(R.menu.menu_main);
		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case R.id.action_history:
						startActivity(new Intent(MainActivity.this, HistoryActivity.class));
						break;
					case R.id.action_settings:
						startActivity(new Intent(MainActivity.this, SettingsActivity.class));
						break;
				}
				return true;
			}
		});
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			Log.i(TAG, "onRequestPermissionsResult: 获得权限");
		} else {
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK)
			return;
		Uri uri = data.getData();
		final String path = WAFileUtil.getPath(MainActivity.this, uri);
		Glide.with(this).load(path).apply(options).into(imageView);
		switch (requestCode) {
			case REQUEST_CODE:
				search(path);
				break;
		}
	}

	private void search(String path) {
		WhatAnimeBuilder builder = new WhatAnimeBuilder(MainActivity.this);
		builder.setImgFile(path);
		builder.build(MainActivity.this, list, adapter);
	}
}
