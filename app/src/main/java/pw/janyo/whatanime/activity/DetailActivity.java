package pw.janyo.whatanime.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import pw.janyo.whatanime.R;
import pw.janyo.whatanime.adapter.AnimationAdapter;
import pw.janyo.whatanime.classes.Animation;
import pw.janyo.whatanime.classes.Dock;
import pw.janyo.whatanime.classes.History;
import pw.janyo.whatanime.util.WAFileUtil;
import vip.mystery0.tools.base.BaseActivity;

public class DetailActivity extends BaseActivity {
	private ImageView imageView;
	private VideoView videoView;
	private ProgressBar progressBar;
	private List<Dock> list = new ArrayList<>();
	private String nowPlayUrl = "";
	private RequestOptions options = new RequestOptions()
			.diskCacheStrategy(DiskCacheStrategy.NONE);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_detail);

		imageView = findViewById(R.id.imageView);
		videoView = findViewById(R.id.videoView);
		progressBar = findViewById(R.id.progressBar);
		RecyclerView recyclerView = findViewById(R.id.recyclerView);

		Intent intent = getIntent();
		History history = null;
		if (intent != null && intent.getBundleExtra("history") != null)
			history = (History) intent.getBundleExtra("history").getSerializable("history");
		if (history == null) {
			finish();
			return;
		}
		Glide.with(this).load(history.getCachePath()).apply(options).into(imageView);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		final AnimationAdapter adapter = new AnimationAdapter(DetailActivity.this, list);
		recyclerView.setAdapter(adapter);
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

		final History finalHistory = history;
		Observable.create(new ObservableOnSubscribe<Animation>() {
			@Override
			public void subscribe(ObservableEmitter<Animation> emitter) {
				Animation animation = WAFileUtil.getSavedObject(new File(finalHistory.getSaveFilePath()), Animation.class);
				emitter.onNext(animation);
				emitter.onComplete();
			}
		})
				.subscribeOn(Schedulers.newThread())
				.unsubscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Observer<Animation>() {
					private Animation animation = null;

					@Override
					public void onSubscribe(Disposable d) {
					}

					@Override
					public void onNext(Animation animation) {
						this.animation = animation;
					}

					@Override
					public void onError(Throwable e) {
						toastMessage(e.getMessage(), Toast.LENGTH_SHORT);
					}

					@Override
					public void onComplete() {
						list.clear();
						list.addAll(animation.docs);
						adapter.notifyDataSetChanged();
					}
				});
	}
}
