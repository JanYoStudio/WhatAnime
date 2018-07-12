package pw.janyo.whatanime.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide

import kotlinx.android.synthetic.main.activity_main.*
import pw.janyo.whatanime.R
import pw.janyo.whatanime.databinding.ActivityMainBinding
import pw.janyo.whatanime.databinding.ContentMainBinding
import pw.janyo.whatanime.model.Docs
import pw.janyo.whatanime.viewModel.MainViewModel
import vip.mystery0.logs.Logs
import vip.mystery0.tools.base.BaseActivity
import vip.mystery0.tools.utils.FileTools
import java.io.File

class MainActivity : BaseActivity(R.layout.activity_main) {
	companion object {
		private const val REQUEST_CODE = 123
		private const val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 233
	}

	private lateinit var activityMainBinding: ActivityMainBinding
	private lateinit var contentMainBinding: ContentMainBinding
	private lateinit var mainViewModel: MainViewModel
	private lateinit var mainRecyclerAdapter: MainRecyclerAdapter
	private val docsList = ArrayList<Docs>()
	private val docsListObserver = Observer<ArrayList<Docs>> {
		docsList.clear()
		docsList.addAll(it)
		mainRecyclerAdapter.notifyDataSetChanged()
	}
	private val imageFileObserver = Observer<File> {
		if (!it.exists())
			return@Observer
		Glide.with(this).load(it.absolutePath).into(contentMainBinding.imageView)
		mainViewModel.getSearchResultList(it).observe(this, docsListObserver)
	}

	override fun inflateView(layoutId: Int) {
		activityMainBinding = DataBindingUtil.setContentView(this, layoutId)
		contentMainBinding = activityMainBinding.include
	}

	override fun initView() {
		super.initView()
		setSupportActionBar(toolbar)
		contentMainBinding.recyclerView.layoutManager = LinearLayoutManager(this)
		mainRecyclerAdapter = MainRecyclerAdapter(this, docsList)
		contentMainBinding.recyclerView.adapter = mainRecyclerAdapter
	}

	override fun initData() {
		super.initData()
		requestPermission()
		mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
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

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			Logs.i("onRequestPermissionsResult: 获得权限")
		} else {
			finish()
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (resultCode != Activity.RESULT_OK)
			return
		val uri = data!!.data
		val path = FileTools.getPath(this, uri)
		if (path == null) {
			Logs.i("onActivityResult: null")
			return
		}
		mainViewModel.getSearchFile(path)
				.observe(this, imageFileObserver)
	}
}
