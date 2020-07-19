package pw.janyo.whatanime.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.WABaseActivity
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.config.connectServer
import pw.janyo.whatanime.config.inBlackList
import pw.janyo.whatanime.config.toCustomTabs
import pw.janyo.whatanime.databinding.ActivityMainBinding
import pw.janyo.whatanime.databinding.ContentMainBinding
import pw.janyo.whatanime.model.Docs
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.model.ShowImage
import pw.janyo.whatanime.ui.CoilImageEngine
import pw.janyo.whatanime.ui.adapter.MainRecyclerAdapter
import pw.janyo.whatanime.utils.loadWithoutCache
import pw.janyo.whatanime.viewModel.MainViewModel
import vip.mystery0.logs.Logs
import vip.mystery0.rx.DataObserver
import vip.mystery0.rx.PackageDataObserver
import vip.mystery0.rx.content
import vip.mystery0.tools.ResourceException
import vip.mystery0.tools.utils.AndroidVersionCode
import vip.mystery0.tools.utils.formatTime
import vip.mystery0.tools.utils.sdkIsBefore
import java.io.File
import kotlin.system.exitProcess

class MainActivity : WABaseActivity<ActivityMainBinding>(R.layout.activity_main) {
	companion object {
		private const val REQUEST_CODE = 123
		private const val FILE_SELECT_CODE = 124
		private const val INTENT_CACHE_FILE = "INTENT_CACHE_FILE"
		private const val INTENT_ORIGIN_PATH = "INTENT_ORIGIN_PATH"
		private const val INTENT_TITLE = "INTENT_TITLE"
		private const val INTENT_URI = "INTENT_URI"
		private const val INTENT_MIME_TYPE = "INTENT_MIME_TYPE"

		fun showDetail(context: Context, cacheFile: File, originPath: String, title: String) {
			val intent = Intent(context, MainActivity::class.java)
			intent.putExtra(INTENT_CACHE_FILE, cacheFile)
			intent.putExtra(INTENT_ORIGIN_PATH, originPath)
			intent.putExtra(INTENT_TITLE, title)
			context.startActivity(intent)
		}

		fun receiveShare(context: Context, uri: Uri, mimeType: String) {
			Logs.i("receiveShare: uri: $uri, mimeType: $mimeType")
			val intent = Intent(context, MainActivity::class.java)
			intent.putExtra(INTENT_URI, uri)
			intent.putExtra(INTENT_MIME_TYPE, mimeType)
			context.startActivity(intent)
		}
	}

	private lateinit var contentMainBinding: ContentMainBinding
	private val mainViewModel: MainViewModel by viewModel()
	private val player: ExoPlayer by lifecycleScope.inject { parametersOf(this) }
	private val mainRecyclerAdapter: MainRecyclerAdapter by lifecycleScope.inject { parametersOf(this) }
	private var isShowDetail = false
	private val dialog: Dialog by lazy { buildZLoadingDialog().create() }
	private val adsDialog: AlertDialog by lazy {
		MaterialAlertDialogBuilder(this)
				.setTitle(R.string.action_why_ad)
				.setMessage(R.string.hint_why_ads_appear)
				.setPositiveButton(android.R.string.ok, null)
				.create()
	}

	private val quotaObserver = object : PackageDataObserver<SearchQuota> {
		override fun content(data: SearchQuota?) {
			val quota = data!!
			val quotaString = "${getString(R.string.hint_search_quota)}${quota.quota}    ${getString(R.string.hint_search_quota_ttl)}${(quota.quota_ttl * 1000).toLong().formatTime()}"
			searchQuota.text = quotaString
		}

		override fun error(data: SearchQuota?, e: Throwable?) {
			if (e !is ResourceException)
				Logs.wtf("quotaObserver: ", e)
			e.toastLong()
		}
	}
	private val animationObserver = object : DataObserver<List<Docs>> {
		override fun contentNoEmpty(data: List<Docs>) {
			mainRecyclerAdapter.items.clear()
			mainRecyclerAdapter.items.addAll(data)
			hideDialog()
		}

		override fun loading() {
			showDialog()
		}

		override fun empty() {
			hideDialog()
			Snackbar.make(binding.coordinatorLayout, R.string.hint_no_result, Snackbar.LENGTH_SHORT)
					.show()
		}

		override fun error(e: Throwable?) {
			if (e !is ResourceException)
				Logs.wtf("animationObserver: ", e)
			hideDialog()
			e.toastLong()
		}
	}
	private val imageFileObserver = object : PackageDataObserver<ShowImage> {
		override fun content(data: ShowImage?) {
			val originFile = File(data!!.originPath)
			//判断图片文件是否存在
			if (!originFile.exists()) {
				//如果不存在，显示错误信息
				Snackbar.make(binding.coordinatorLayout, R.string.hint_select_file_not_exist, Snackbar.LENGTH_LONG)
						.addCallback(object : Snackbar.Callback() {
							override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
								super.onDismissed(transientBottomBar, event)
								finish()
							}
						})
						.show()
				return
			}
			//图片存在，加载图片显示
			contentMainBinding.imageView.loadWithoutCache(originFile)
			//搜索图片
			mainViewModel.search(originFile, null, data.cachePath, data.originPath, data.mimeType, connectServer)
		}

		override fun error(data: ShowImage?, e: Throwable?) {
			if (e !is ResourceException)
				Logs.wtf("imageFileObserver: ", e)
			e.toastLong()
		}
	}
	private val mediaSourceObserver = object : PackageDataObserver<MediaSource> {
		override fun content(data: MediaSource?) {
			if (data == null) {
				//再次播放当前视频
				if (!player.isPlaying)
					player.seekToDefaultPosition()
			} else {
				//播放新的视频
				player.stop(true)
				player.prepare(data)
				player.playWhenReady = true
			}
		}

		override fun error(data: MediaSource?, e: Throwable?) {
			if (e !is ResourceException)
				Logs.wtf("imageFileObserver: ", e)
			e.toastLong()
		}
	}

	override fun inflateView(layoutId: Int) {
		super.inflateView(layoutId)
		contentMainBinding = binding.include
	}

	override fun initView() {
		super.initView()
		if (inBlackList) {
			//连接上了服务器并且在黑名单中
			adView.visibility = View.VISIBLE
			whyAdImageView.visibility = View.VISIBLE
			//初始化AdMod
			MobileAds.initialize(this) {}
			val adRequest = AdRequest.Builder().build()
			adView.loadAd(adRequest)
		} else {
			adView.visibility = View.GONE
			whyAdImageView.visibility = View.GONE
		}
		title = getString(R.string.title_activity_main)
		setSupportActionBar(binding.toolbar)
		contentMainBinding.recyclerView.layoutManager = LinearLayoutManager(this)
		contentMainBinding.recyclerView.adapter = mainRecyclerAdapter
	}

	override fun initData() {
		super.initData()
		initViewModel()
		mainViewModel.showQuota()
		initIntent()
		videoView.player = player
		showNotice()
	}

	private fun initViewModel() {
		mainViewModel.quota.observe(this, quotaObserver)
		mainViewModel.imageFile.observe(this, imageFileObserver)
		mainViewModel.resultList.observe(this, animationObserver)
		mainViewModel.isShowDetail.observe(this, Observer { isShowDetail = it })
		mainViewModel.mediaSource.observe(this, mediaSourceObserver)
	}

	@SuppressLint("RestrictedApi")
	private fun initIntent() {
		if (intent.hasExtra(INTENT_URI)) {
			//接收其他来源的图片
			try {
				val uri = intent.getParcelableExtra<Uri>(INTENT_URI)
				intent.data = uri
				mainViewModel.parseImageFile(intent, intent.getStringExtra(INTENT_MIME_TYPE)!!)
			} catch (e: Exception) {
				getString(R.string.hint_select_file_path_null).toast()
			}
		}
		if (intent.hasExtra(INTENT_CACHE_FILE) && intent.hasExtra(INTENT_TITLE)) {
			//查看历史记录
			mainViewModel.isShowDetail.value = true
			binding.fab.visibility = View.GONE
			val cacheFile: File = intent.getSerializableExtra(INTENT_CACHE_FILE) as File
			val originPath = intent.getStringExtra(INTENT_ORIGIN_PATH)!!
			//加载显示历史记录中的缓存文件
			val showImage = ShowImage()
			showImage.mimeType = ""
			showImage.originPath = originPath
			showImage.cachePath = cacheFile.absolutePath
			mainViewModel.imageFile.content(showImage)
			//设置标题
			title = intent.getStringExtra(INTENT_TITLE)
			supportActionBar!!.setDisplayHomeAsUpEnabled(true)
			binding.toolbar.setNavigationOnClickListener {
				finish()
			}
		}
	}

	override fun monitor() {
		super.monitor()
		fab.setOnClickListener {
			doSelect()
		}
		whyAdImageView.setOnClickListener {
			adsDialog.show()
		}
		adView.adListener = object : AdListener() {
			override fun onAdLoaded() {
				adView.visibility = View.VISIBLE
				whyAdImageView.visibility = View.VISIBLE
			}

			override fun onAdFailedToLoad(errorCode: Int) {
				adView.visibility = View.GONE
				whyAdImageView.visibility = View.GONE
			}
		}
		player.addListener(object : Player.EventListener {
			override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
				when (playbackState) {
					Player.STATE_BUFFERING -> {
						contentMainBinding.imageView.visibility = View.GONE
						contentMainBinding.videoView.visibility = View.VISIBLE
						contentMainBinding.progressBar.visibility = View.VISIBLE
					}
					Player.STATE_READY -> {
						contentMainBinding.progressBar.visibility = View.GONE
					}
					Player.STATE_ENDED -> {
						contentMainBinding.videoView.visibility = View.GONE
						contentMainBinding.imageView.visibility = View.VISIBLE
					}
				}
			}

			override fun onPlayerError(error: ExoPlaybackException?) {
				Logs.e("onPlayerError: ", error)
				contentMainBinding.progressBar.visibility = View.GONE
				contentMainBinding.videoView.visibility = View.GONE
				contentMainBinding.imageView.visibility = View.VISIBLE
				error.toastLong()
			}
		})
	}

	private fun showNotice() {
		if (intent.hasExtra(INTENT_URI) || intent.hasExtra(INTENT_CACHE_FILE)) return
		if (Configure.alreadyReadNotice) return
		MaterialAlertDialogBuilder(this)
				.setTitle(R.string.title_usage_notice)
				.setMessage(R.string.hint_usage_notice)
				.setPositiveButton(android.R.string.ok) { dialog, _ ->
					Configure.alreadyReadNotice = true
					dialog.dismiss()
				}
				.setNegativeButton(R.string.action_disagree) { _, _ ->
					Configure.alreadyReadNotice = false
					finish()
					exitProcess(0)
				}
				.show()
	}

	private fun doSelect() {
		if (Configure.useInAppImageSelect && sdkIsBefore(AndroidVersionCode.VERSION_Q, exclude = true))
			requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)) { code, result ->
				if (result.isEmpty() || result[0] == PackageManager.PERMISSION_GRANTED) {
					Matisse.from(this)
							.choose(MimeType.of(MimeType.JPEG, MimeType.PNG, MimeType.BMP, MimeType.GIF))
							.showSingleMediaType(true)
							.countable(false)
							.maxSelectable(1)
							.restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
							.thumbnailScale(0.85f)
							.imageEngine(CoilImageEngine())
							.forResult(REQUEST_CODE)
				} else {
					Snackbar.make(binding.coordinatorLayout, R.string.hint_permission_deny, Snackbar.LENGTH_LONG)
							.setAction(R.string.action_re_request_permission) {
								reRequestPermission(code)
							}
							.show()
				}
			}
		else {
			val intent = Intent(Intent.ACTION_PICK)
			intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
			startActivityForResult(intent, FILE_SELECT_CODE)
		}
	}

	private fun showDialog() {
		if (!dialog.isShowing)
			dialog.show()
	}

	private fun hideDialog() {
		if (dialog.isShowing)
			dialog.dismiss()
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		if (!isShowDetail) {
			menuInflater.inflate(R.menu.menu_main, menu)
			menu!!.findItem(R.id.action_why_ad).isVisible = inBlackList
		}
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.action_history -> {
				startActivity(Intent(this, HistoryActivity::class.java))
				true
			}
			R.id.action_info -> {
				startActivity(Intent(this, AboutActivity::class.java))
				true
			}
			R.id.action_about -> {
				toCustomTabs("https://janyostudio.mystery0.app/wa/index.html")
				true
			}
			R.id.action_faq -> {
				toCustomTabs("https://janyostudio.mystery0.app/wa/faq.html")
				true
			}
			R.id.action_why_ad -> {
				adsDialog.show()
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		when (requestCode) {
			REQUEST_CODE -> {
				if (resultCode == Activity.RESULT_OK) {
					mainViewModel.parseImageFileByMatisse(data!!)
				}
			}
			FILE_SELECT_CODE -> {
				if (resultCode == Activity.RESULT_OK) {
					val type = contentResolver.getType(data!!.data!!)!!
					mainViewModel.parseImageFile(data, type)
				}
			}
		}
	}
}
