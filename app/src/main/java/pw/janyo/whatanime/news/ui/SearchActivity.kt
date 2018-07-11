package pw.janyo.whatanime.news.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import pw.janyo.whatanime.R

import kotlinx.android.synthetic.main.activity_search.*
import pw.janyo.whatanime.databinding.ActivitySearchBinding
import pw.janyo.whatanime.databinding.ContentSearchBinding
import pw.janyo.whatanime.databinding.ItemImageBinding
import pw.janyo.whatanime.news.model.Docs
import pw.janyo.whatanime.news.viewModel.SearchViewModel
import vip.mystery0.logs.Logs
import vip.mystery0.tools.base.BaseActivity
import vip.mystery0.tools.utils.FileTools
import java.io.File

class SearchActivity : BaseActivity(R.layout.activity_search) {
	companion object {
		private const val REQUEST_CODE = 123
		private const val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 233
	}

	private lateinit var activitySearchBinding: ActivitySearchBinding
	private lateinit var contentSearchBinding: ContentSearchBinding
	private lateinit var itemImageBinding: ItemImageBinding
	private lateinit var searchViewModel: SearchViewModel
	private lateinit var searchResultAdapter: SearchResultAdapter
	private val searchResultList = ArrayList<Docs>()

	private val searchListObserver = Observer<ArrayList<Docs>> {
		Logs.i("列表数据变化")
		Toast.makeText(this, it.size.toString(), Toast.LENGTH_LONG)
				.show()
		searchResultList.clear()
		searchResultList.addAll(it)
		searchResultAdapter.notifyDataSetChanged()
	}

	private val searchImageFileObserver = Observer<File> {
		if (!it.exists())
			return@Observer
		Glide.with(this).load(it.absolutePath).into(itemImageBinding.imageView)
		searchViewModel.getSearchResultList(it).observe(this, searchListObserver)
	}

	override fun inflateView(layoutId: Int) {
		activitySearchBinding = DataBindingUtil.setContentView(this, layoutId)
		contentSearchBinding = activitySearchBinding.include
		itemImageBinding = contentSearchBinding.include
	}

	override fun initView() {
		super.initView()
		setSupportActionBar(toolbar)
		initRecyclerView()
	}

	override fun initData() {
		super.initData()
		requestPermission()
		searchViewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
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

	private fun initRecyclerView() {
		contentSearchBinding.recyclerView.layoutManager = LinearLayoutManager(this)
		searchResultAdapter = SearchResultAdapter(this, searchResultList)
		contentSearchBinding.recyclerView.adapter = searchResultAdapter
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
		searchViewModel.getSearchFile(path)
				.observe(this, searchImageFileObserver)
	}
}
