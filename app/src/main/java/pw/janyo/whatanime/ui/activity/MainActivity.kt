package pw.janyo.whatanime.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.zyao89.view.zloading.ZLoadingDialog
import com.zyao89.view.zloading.Z_TYPE

import kotlinx.android.synthetic.main.activity_main.*
import pw.janyo.whatanime.R
import pw.janyo.whatanime.databinding.ActivityMainBinding
import pw.janyo.whatanime.databinding.ContentMainBinding
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.Docs
import pw.janyo.whatanime.repository.MainRepository
import pw.janyo.whatanime.ui.adapter.MainRecyclerAdapter
import pw.janyo.whatanime.viewModel.MainViewModel
import vip.mystery0.logs.Logs
import vip.mystery0.tools.base.BaseActivity
import vip.mystery0.tools.utils.FileTools
import java.io.File

class MainActivity : BaseActivity(R.layout.activity_main) {
	companion object {
		private const val REQUEST_CODE = 123
		private const val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 233
		private const val INTENT_ORIGIN_FILE = "INTENT_ORIGIN_FILE"
		private const val INTENT_CACHE_FILE = "INTENT_CACHE_FILE"
		private const val INTENT_TITLE = "INTENT_TITLE"

		fun showDetail(context: Context, originFile: File, cacheFile: File, title: String) {
			val intent = Intent(context, MainActivity::class.java)
			intent.putExtra(INTENT_ORIGIN_FILE, originFile)
			intent.putExtra(INTENT_CACHE_FILE, cacheFile)
			intent.putExtra(INTENT_TITLE, title)
			context.startActivity(intent)
		}
	}

	private lateinit var activityMainBinding: ActivityMainBinding
	private lateinit var contentMainBinding: ContentMainBinding
	private lateinit var mainViewModel: MainViewModel
	private lateinit var mainRecyclerAdapter: MainRecyclerAdapter
	private val docsList = ArrayList<Docs>()
	private var isShowDetail = false
	private var cacheFile: File? = null
	private lateinit var dialog: Dialog
	private val options = RequestOptions()
			.diskCacheStrategy(DiskCacheStrategy.NONE)

	private val animationObserver = Observer<Animation> {
		docsList.clear()
		docsList.addAll(it.docs)
		mainRecyclerAdapter.notifyDataSetChanged()
		hideDialog()
	}
	private val imageFileObserver = Observer<File> {
		if (!it.exists()) {
			mainViewModel.message.value = getString(R.string.hint_select_file_not_exist)
			return@Observer
		}
		if (isShowDetail && cacheFile != null)
			Glide.with(this)
					.load(cacheFile)
					.apply(options)
					.into(contentMainBinding.imageView)
		else
			Glide.with(this)
					.load(it.absolutePath)
					.apply(options)
					.into(contentMainBinding.imageView)
		MainRepository.search(it, null, mainViewModel)
	}
	private val messageObserver = Observer<String> {
		hideDialog()
		Snackbar.make(activityMainBinding.coordinatorLayout, it, Snackbar.LENGTH_LONG)
				.show()
	}

	override fun inflateView(layoutId: Int) {
		activityMainBinding = DataBindingUtil.setContentView(this, layoutId)
		contentMainBinding = activityMainBinding.include
	}

	override fun initView() {
		super.initView()
		setSupportActionBar(activityMainBinding.toolbar)
		contentMainBinding.recyclerView.layoutManager = LinearLayoutManager(this)
		mainRecyclerAdapter = MainRecyclerAdapter(this, activityMainBinding, docsList)
		contentMainBinding.recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
		contentMainBinding.recyclerView.adapter = mainRecyclerAdapter
	}

	override fun initData() {
		super.initData()
		requestPermission()
		initViewModel()
		initDialog()
		initIntent()
	}

	private fun initViewModel() {
		mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
		mainViewModel.imageFile.observe(this, imageFileObserver)
		mainViewModel.resultList.observe(this, animationObserver)
		mainViewModel.message.observe(this, messageObserver)
		mainViewModel.isShowDetail.observe(this, Observer<Boolean> { isShowDetail = it })
	}

	private fun initDialog() {
		dialog = ZLoadingDialog(this)
				.setLoadingBuilder(Z_TYPE.STAR_LOADING)
				.setHintText(" ")
				.setCanceledOnTouchOutside(false)
				.setLoadingColor(ContextCompat.getColor(this, R.color.colorAccent))
				.setHintTextColor(ContextCompat.getColor(this, R.color.colorAccent))
				.create()
	}

	@SuppressLint("RestrictedApi")
	private fun initIntent() {
		if (intent.hasExtra(INTENT_CACHE_FILE) && intent.hasExtra(INTENT_TITLE)) {
			mainViewModel.isShowDetail.value = true
			activityMainBinding.fab.visibility = View.GONE
			val originFile: File = intent.getSerializableExtra(INTENT_ORIGIN_FILE) as File
			val cacheFile: File = intent.getSerializableExtra(INTENT_CACHE_FILE) as File
			this.cacheFile = cacheFile
			mainViewModel.imageFile.value = originFile
			title = intent.getStringExtra(INTENT_TITLE)
			supportActionBar!!.setDisplayHomeAsUpEnabled(true)
			activityMainBinding.toolbar.setNavigationOnClickListener {
				finish()
			}
		}
	}

	override fun monitor() {
		super.monitor()
		fab.setOnClickListener { _ ->
			val intent = Intent()
			intent.type = "image/*"
			intent.action = Intent.ACTION_GET_CONTENT
			startActivityForResult(intent, REQUEST_CODE)
		}
	}

	private fun requestPermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
			ActivityCompat.requestPermissions(this,
					arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
					WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
	}

	private fun showDialog() {
		if (!dialog.isShowing)
			dialog.show()
	}

	private fun hideDialog() {
		dialog.dismiss()
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		if (!isShowDetail)
			menuInflater.inflate(R.menu.menu_main, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		return when (item?.itemId) {
			R.id.action_history -> {
				startActivity(Intent(this, HistoryActivity::class.java))
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		when (requestCode) {
			WRITE_EXTERNAL_STORAGE_REQUEST_CODE -> {
				if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
					Snackbar.make(activityMainBinding.coordinatorLayout, R.string.hint_permission_deny, Snackbar.LENGTH_LONG)
							.setAction(R.string.action_re_request_permission) {
								requestPermission()
							}
							.addCallback(object : Snackbar.Callback() {
								override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
									if (event != Snackbar.Callback.DISMISS_EVENT_ACTION)
										finish()
								}
							})
							.show()
			}
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (resultCode != Activity.RESULT_OK)
			return
		showDialog()
		val uri = data!!.data
		Logs.i("onActivityResult: $uri")
		val path = FileTools.getPath(this, uri)
		if (path == null) {
			mainViewModel.message.value = getString(R.string.hint_select_file_path_null)
			return
		}
		mainViewModel.imageFile.value = File(path)
	}
}
