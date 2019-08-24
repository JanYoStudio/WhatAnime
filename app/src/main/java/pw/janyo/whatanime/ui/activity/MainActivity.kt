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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.WABaseActivity
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.databinding.ActivityMainBinding
import pw.janyo.whatanime.databinding.ContentMainBinding
import pw.janyo.whatanime.handler.MainItemListener
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.repository.MainRepository
import pw.janyo.whatanime.ui.CustomGlideEngine
import pw.janyo.whatanime.ui.adapter.MainRecyclerAdapter
import pw.janyo.whatanime.viewModel.MainViewModel
import vip.mystery0.logs.Logs
import vip.mystery0.rx.PackageDataObserver
import vip.mystery0.rx.content
import vip.mystery0.tools.toast
import vip.mystery0.tools.toastLong
import vip.mystery0.tools.utils.PackageTools
import vip.mystery0.tools.utils.formatTime
import java.io.File

class MainActivity : WABaseActivity<ActivityMainBinding>(R.layout.activity_main) {
	companion object {
		private const val REQUEST_CODE = 123
		private const val FILE_SELECT_CODE = 124
		private const val INTENT_ORIGIN_FILE = "INTENT_ORIGIN_FILE"
		private const val INTENT_CACHE_FILE = "INTENT_CACHE_FILE"
		private const val INTENT_TITLE = "INTENT_TITLE"
		private const val INTENT_URI = "INTENT_URI"

		fun showDetail(context: Context, originFile: File, cacheFile: File, title: String) {
			val intent = Intent(context, MainActivity::class.java)
			intent.putExtra(INTENT_ORIGIN_FILE, originFile)
			intent.putExtra(INTENT_CACHE_FILE, cacheFile)
			intent.putExtra(INTENT_TITLE, title)
			context.startActivity(intent)
		}

		fun receiveShare(context: Context, uri: Uri) {
			val intent = Intent(context, MainActivity::class.java)
			intent.putExtra(INTENT_URI, uri)
			context.startActivity(intent)
		}
	}

	private lateinit var contentMainBinding: ContentMainBinding
	private val mainViewModel: MainViewModel by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }
	private lateinit var mainRecyclerAdapter: MainRecyclerAdapter
	private var isShowDetail = false
	private var cacheFile: File? = null
	private lateinit var dialog: Dialog
	private val options = RequestOptions()
			.diskCacheStrategy(DiskCacheStrategy.NONE)

	private val quotaObserver = object : PackageDataObserver<SearchQuota> {
		override fun content(data: SearchQuota?) {
			val quota = data!!
			searchQuota.text = quota.quota.toString()
			searchQuotaTtl.text = (quota.quota_ttl * 1000).toLong().formatTime()
		}

		override fun error(data: SearchQuota?, e: Throwable?) {
			Logs.wtf("quotaObserver: ", e)
			e.toastLong(this@MainActivity)
		}
	}

	private val animationObserver = object : PackageDataObserver<Animation> {
		override fun content(data: Animation?) {
			mainRecyclerAdapter.items.clear()
			mainRecyclerAdapter.items.addAll(data!!.docs)
			hideDialog()
		}

		override fun loading() {
			showDialog()
		}

		override fun empty(data: Animation?) {
			hideDialog()
			Snackbar.make(binding.coordinatorLayout, R.string.hint_no_result, Snackbar.LENGTH_SHORT)
					.show()
		}

		override fun error(data: Animation?, e: Throwable?) {
			Logs.wtf("animationObserver: ", e)
			hideDialog()
			e.toastLong(this@MainActivity)
		}
	}
	private val imageFileObserver = object : PackageDataObserver<File> {
		override fun content(data: File?) {
			if (!data!!.exists()) {
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
			if (isShowDetail && cacheFile != null)
				Glide.with(this@MainActivity)
						.load(cacheFile)
						.apply(options)
						.into(contentMainBinding.imageView)
			else
				Glide.with(this@MainActivity)
						.load(data.absolutePath)
						.apply(options)
						.into(contentMainBinding.imageView)
			MainRepository.search(data, null, mainViewModel)
		}

		override fun error(data: File?, e: Throwable?) {
			e.toastLong(this@MainActivity)
		}
	}

	override fun inflateView(layoutId: Int) {
		super.inflateView(layoutId)
		contentMainBinding = binding.include
	}

	override fun initView() {
		super.initView()
		title = getString(R.string.title_activity_main)
		setSupportActionBar(binding.toolbar)
		contentMainBinding.recyclerView.layoutManager = LinearLayoutManager(this)
		mainRecyclerAdapter = MainRecyclerAdapter(this, MainItemListener(binding))
		contentMainBinding.recyclerView.adapter = mainRecyclerAdapter
	}

	override fun initData() {
		super.initData()
		initViewModel()
		initDialog()
		MainRepository.showQuota(mainViewModel)
		initIntent()
	}

	private fun initViewModel() {
		mainViewModel.quota.observe(this, quotaObserver)
		mainViewModel.imageFile.observe(this, imageFileObserver)
		mainViewModel.resultList.observe(this, animationObserver)
		mainViewModel.isShowDetail.observe(this, Observer<Boolean> { isShowDetail = it })
	}

	private fun initDialog() {
		dialog = buildZLoadingDialog().create()
	}

	@SuppressLint("RestrictedApi")
	private fun initIntent() {
		if (intent.hasExtra(INTENT_URI)) {
			//接收其他来源的图片
			try {
				val uri = intent.getParcelableExtra<Uri>(INTENT_URI)
				intent.data = uri
				MainRepository.parseImageFile(mainViewModel, intent)
			} catch (e: Exception) {
				getString(R.string.hint_select_file_path_null).toast(this)
			}
		}
		if (intent.hasExtra(INTENT_CACHE_FILE) && intent.hasExtra(INTENT_TITLE)) {
			mainViewModel.isShowDetail.value = true
			binding.fab.visibility = View.GONE
			val originFile: File = intent.getSerializableExtra(INTENT_ORIGIN_FILE) as File
			val cacheFile: File = intent.getSerializableExtra(INTENT_CACHE_FILE) as File
			this.cacheFile = cacheFile
			mainViewModel.imageFile.content(originFile)
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
	}

	private fun doSelect() {
		if (Configure.useInAppImageSelect && PackageTools.instance.isBefore(PackageTools.VERSION_Q, exclude = true))
			requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)) { code, result ->
				if (result.isEmpty() || result[0] == PackageManager.PERMISSION_GRANTED) {
					Matisse.from(this)
							.choose(MimeType.of(MimeType.JPEG, MimeType.PNG, MimeType.BMP, MimeType.GIF))
							.showSingleMediaType(true)
							.countable(false)
							.maxSelectable(1)
							.restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
							.thumbnailScale(0.85f)
							.imageEngine(CustomGlideEngine())
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
		if (!isShowDetail)
			menuInflater.inflate(R.menu.menu_main, menu)
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
				MarkdownActivity.intentTo(this, "about.md")
				true
			}
			R.id.action_faq -> {
				MarkdownActivity.intentTo(this, "faq.md")
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
					MainRepository.parseImageFileByMatisse(mainViewModel, data!!)
				}
			}
			FILE_SELECT_CODE -> {
				if (resultCode == Activity.RESULT_OK) {
					MainRepository.parseImageFile(mainViewModel, data!!)
				}
			}
		}
	}
}
