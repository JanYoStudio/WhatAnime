package pw.janyo.whatanime.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View

import pw.janyo.whatanime.R
import pw.janyo.whatanime.adapter.AnimationAdapter
import pw.janyo.whatanime.classes.Dock

import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.VideoView

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence

import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.ArrayList

import pw.janyo.whatanime.util.Settings
import pw.janyo.whatanime.util.WAFileUtil
import pw.janyo.whatanime.util.whatanime.WhatAnimeBuilder
import vip.mystery0.logs.Logs
import vip.mystery0.tools.utils.FileTools

class MainActivity : AppCompatActivity() {
	private var imageView: ImageView? = null
	private var videoView: VideoView? = null
	private var progressBar: ProgressBar? = null
	private var main_fab_upload: FloatingActionButton? = null
	private var adapter: AnimationAdapter? = null
	private val list = ArrayList<Dock>()
	private var nowPlayUrl = ""
	private val options = RequestOptions()
			.diskCacheStrategy(DiskCacheStrategy.NONE)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		requestPermission()
		initialization()
		monitor()
	}

	private fun initialization() {
		setContentView(R.layout.activity_main)
		imageView = findViewById(R.id.imageView)
		videoView = findViewById(R.id.videoView)
		progressBar = findViewById(R.id.progressBar)
		main_fab_upload = findViewById(R.id.main_fab_upload)
		val toolbar = findViewById<Toolbar>(R.id.toolbar)
		val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
		recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
		recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
		adapter = AnimationAdapter(this@MainActivity, list)
		recyclerView.adapter = adapter

		setToolbar(toolbar)

		showcase()
	}

	private fun showcase() {
		if (Settings.isFirst)
			TapTargetSequence(this)
					.targets(TapTarget.forView(main_fab_upload!!, "点击这个按钮上传动漫截图。").tintTarget(false))
					.continueOnCancel(true)
					.considerOuterCircleCanceled(true)
					.listener(object : TapTargetSequence.Listener {
						override fun onSequenceFinish() {
							Settings.setFirst()
						}

						override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {}

						override fun onSequenceCanceled(lastTarget: TapTarget) {}
					}).start()
	}

	private fun monitor() {
		main_fab_upload!!.setOnClickListener { doChoose(REQUEST_CODE) }
		adapter!!.setOnClickListener { dock ->
			try {
				val requestUrl = "https://whatanime.ga/preview.php?season=" + dock.season + "&anime=" + URLEncoder.encode(dock.anime, "UTF-8") + "&file=" + URLEncoder.encode(dock.filename, "UTF-8") + "&t=" + dock.at + "&token=" + dock.tokenthumb
				if (nowPlayUrl != requestUrl) {
					nowPlayUrl = requestUrl
					videoView!!.stopPlayback()
					videoView!!.setVideoURI(Uri.parse(requestUrl))
				}
				imageView!!.visibility = View.GONE
				videoView!!.visibility = View.VISIBLE
				progressBar!!.visibility = View.VISIBLE
				videoView!!.setOnPreparedListener { progressBar!!.visibility = View.GONE }
				videoView!!.setOnCompletionListener {
					videoView!!.visibility = View.GONE
					imageView!!.visibility = View.VISIBLE
				}
				videoView!!.start()
			} catch (e: UnsupportedEncodingException) {
				e.printStackTrace()
			}
		}
	}

	private fun doChoose(code: Int) {
		val intent = Intent()
		intent.type = "image/*"
		intent.action = Intent.ACTION_GET_CONTENT
		startActivityForResult(intent, code)
	}

	private fun requestPermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
					WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
		}
	}

	private fun setToolbar(toolbar: Toolbar) {
		toolbar.title = title
		toolbar.inflateMenu(R.menu.menu_main)
		toolbar.setOnMenuItemClickListener { item ->
			when (item.itemId) {
				R.id.action_history -> startActivity(Intent(this@MainActivity, HistoryActivity::class.java))
				R.id.action_settings -> startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
			}
			true
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			Log.i(TAG, "onRequestPermissionsResult: 获得权限")
		} else {
			finish()
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (resultCode != Activity.RESULT_OK)
			return
		val uri = data!!.data
		val path = FileTools.getPath(this@MainActivity, uri)
		Glide.with(this).load(path).apply(options).into(imageView!!)
		when (requestCode) {
			REQUEST_CODE -> search(path)
		}
	}

	private fun search(path: String?) {
		val builder = WhatAnimeBuilder(this@MainActivity)
		builder.setImgFile(path)
		builder.build(this@MainActivity, list, adapter)
	}

	companion object {
		private val TAG = "MainActivity"
		private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 233
		private val REQUEST_CODE = 322
	}
}
