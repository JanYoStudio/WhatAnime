//package pw.janyo.whatanime.activity
//
//import android.content.Intent
//import android.media.MediaPlayer
//import android.net.Uri
//import android.os.Bundle
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import android.view.View
//import android.widget.ImageView
//import android.widget.ProgressBar
//import android.widget.Toast
//import android.widget.VideoView
//
//import com.bumptech.glide.Glide
//import com.bumptech.glide.load.engine.DiskCacheStrategy
//import com.bumptech.glide.request.RequestOptions
//
//import java.io.File
//import java.io.UnsupportedEncodingException
//import java.net.URLEncoder
//import java.util.ArrayList
//
//import io.reactivex.Observable
//import io.reactivex.ObservableEmitter
//import io.reactivex.ObservableOnSubscribe
//import io.reactivex.RxObserver
//import io.reactivex.android.schedulers.AndroidSchedulers
//import io.reactivex.disposables.Disposable
//import io.reactivex.schedulers.Schedulers
//import pw.janyo.whatanime.R
//import pw.janyo.whatanime.adapter.AnimationAdapter
//import pw.janyo.whatanime.classes.Animation
//import pw.janyo.whatanime.classes.Dock
//import pw.janyo.whatanime.classes.History
//import pw.janyo.whatanime.util.WAFileUtil
//import vip.mystery0.tools.base.BaseActivity
//
//class DetailActivity : BaseActivity(R.layout.content_detail) {
//	private var imageView: ImageView? = null
//	private var videoView: VideoView? = null
//	private var progressBar: ProgressBar? = null
//	private val list = ArrayList<Dock>()
//	private var nowPlayUrl = ""
//	private val options = RequestOptions()
//			.diskCacheStrategy(DiskCacheStrategy.NONE)
//
//	override fun onCreate(savedInstanceState: Bundle?) {
//		super.onCreate(savedInstanceState)
//		setContentView(R.layout.content_detail)
//
//		imageView = findViewById(R.id.imageView)
//		videoView = findViewById(R.id.videoView)
//		progressBar = findViewById(R.id.progressBar)
//		val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
//
//		val intent = intent
//		var history: History? = null
//		if (intent?.getBundleExtra("history") != null)
//			history = intent.getBundleExtra("history").getSerializable("history") as History
//		if (history == null) {
//			finish()
//			return
//		}
//		Glide.with(this).load(history.cachePath).apply(options).into(imageView!!)
//		recyclerView.layoutManager = LinearLayoutManager(this)
//		val adapter = AnimationAdapter(this@DetailActivity, list)
//		recyclerView.adapter = adapter
//		adapter.setOnClickListener { dock ->
//			try {
//				val requestUrl = "https://whatanime.ga/preview.php?season=" + dock.season + "&anime=" + URLEncoder.encode(dock.anime, "UTF-8") + "&file=" + URLEncoder.encode(dock.filename, "UTF-8") + "&t=" + dock.at + "&token=" + dock.tokenthumb
//				if (nowPlayUrl != requestUrl) {
//					nowPlayUrl = requestUrl
//					videoView!!.stopPlayback()
//					videoView!!.setVideoURI(Uri.parse(requestUrl))
//				}
//				imageView!!.visibility = View.GONE
//				videoView!!.visibility = View.VISIBLE
//				progressBar!!.visibility = View.VISIBLE
//				videoView!!.setOnPreparedListener { progressBar!!.visibility = View.GONE }
//				videoView!!.setOnCompletionListener {
//					videoView!!.visibility = View.GONE
//					imageView!!.visibility = View.VISIBLE
//				}
//				videoView!!.start()
//			} catch (e: UnsupportedEncodingException) {
//				e.printStackTrace()
//			}
//		}
//
//		val finalHistory = history
//		Observable.create(ObservableOnSubscribe<Animation> { emitter ->
//			val animation = WAFileUtil.getSavedObject(File(finalHistory.saveFilePath), Animation::class.java)
//			emitter.onNext(animation)
//			emitter.onComplete()
//		})
//				.subscribeOn(Schedulers.newThread())
//				.unsubscribeOn(Schedulers.newThread())
//				.observeOn(AndroidSchedulers.mainThread())
//				.subscribe(object : RxObserver<Animation> {
//					private var animation: Animation? = null
//
//					override fun onSubscribe(d: Disposable) {}
//
//					override fun onNext(animation: Animation) {
//						this.animation = animation
//					}
//
//					override fun onError(e: Throwable) {
//						//						toastMessage(e.getMessage(), Toast.LENGTH_SHORT);
//					}
//
//					override fun onComplete() {
//						list.clear()
//						list.addAll(animation!!.docs)
//						adapter.notifyDataSetChanged()
//					}
//				})
//	}
//}
