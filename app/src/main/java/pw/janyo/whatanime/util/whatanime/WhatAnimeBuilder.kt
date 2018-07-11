package pw.janyo.whatanime.util.whatanime

import android.content.Context
import android.os.Environment
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat

import com.google.gson.Gson
import com.zyao89.view.zloading.ZLoadingDialog
import com.zyao89.view.zloading.Z_TYPE

import java.io.File
import java.io.InputStreamReader
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Calendar
import java.util.concurrent.TimeUnit

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import pw.janyo.whatanime.R
import pw.janyo.whatanime.activity.MainActivity
import pw.janyo.whatanime.adapter.AnimationAdapter
import pw.janyo.whatanime.classes.Animation
import pw.janyo.whatanime.classes.Dock
import pw.janyo.whatanime.classes.History
import pw.janyo.whatanime.interfaces.SearchService
import pw.janyo.whatanime.util.Base64
import pw.janyo.whatanime.util.Base64DecoderException
import pw.janyo.whatanime.util.Settings
import pw.janyo.whatanime.util.WAFileUtil
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import vip.mystery0.logs.Logs

class WhatAnimeBuilder(context: Context) {
	private val coordinatorLayout: CoordinatorLayout
	private var token: String? = null
	private val whatAnime: WhatAnime
	private val retrofit: Retrofit
	private val zLoadingDialog: ZLoadingDialog
	private val history: History

	init {
		whatAnime = WhatAnime()
		coordinatorLayout = (context as MainActivity).findViewById(R.id.coordinatorLayout)
		try {
			token = String(Base64.decode(context.getString(R.string.token)))
		} catch (e: Base64DecoderException) {
			e.printStackTrace()
		}

		val mOkHttpClient = OkHttpClient.Builder()
				.connectTimeout(20, TimeUnit.SECONDS)
				.readTimeout(20, TimeUnit.SECONDS)
				.build()
		retrofit = Retrofit.Builder()
				.baseUrl(context.getString(R.string.requestUrl))
				.client(mOkHttpClient)
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.addConverterFactory(GsonConverterFactory.create())
				.build()
		zLoadingDialog = ZLoadingDialog(context)
				.setLoadingBuilder(Z_TYPE.STAR_LOADING)
				.setHintText("正在搜索……")
				.setHintTextSize(16f)
				.setCanceledOnTouchOutside(false)
				.setLoadingColor(ContextCompat.getColor(context, R.color.colorAccent))
				.setHintTextColor(ContextCompat.getColor(context, R.color.colorAccent))
		history = History()
	}

	fun setImgFile(path: String) {
		whatAnime.setPath(path)
		history.imaPath = path
	}

	fun build(context: Context, list: MutableList<Dock>, adapter: AnimationAdapter) {
		Observable.create(ObservableOnSubscribe<Animation> { subscriber ->
			val base64 = whatAnime.base64Data(whatAnime.compressBitmap())
			retrofit.create(SearchService::class.java)
					.search(token!!, base64, null!!)
					.subscribeOn(Schedulers.newThread())
					.unsubscribeOn(Schedulers.newThread())
					.map { responseBody -> Gson().fromJson(InputStreamReader(responseBody.byteStream()), Animation::class.java) }
					.observeOn(Schedulers.newThread())
					.subscribe(object : Observer<Animation> {
						private var animation: Animation? = null

						override fun onSubscribe(d: Disposable) {

						}

						override fun onNext(animation: Animation) {
							this.animation = animation
						}

						override fun onError(e: Throwable) {
							Logs.wtf(TAG, "onError: ", e)
							subscriber.onComplete()
						}

						override fun onComplete() {
							subscriber.onNext(animation)
							try {
								val messageDigest = MessageDigest.getInstance("MD5")
								messageDigest.update(Calendar.getInstance().time.toString().toByteArray())
								val md5 = BigInteger(1, messageDigest.digest()).toString(16)
								val jsonFile = File(context.getExternalFilesDir(null).toString() + File.separator + "json" + File.separator + md5)
								WAFileUtil.saveJson(animation, jsonFile)
								val cacheImgPath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + File.separator + md5
								WAFileUtil.fileCopy(history.imaPath, cacheImgPath)
								history.cachePath = cacheImgPath
								history.title = animation!!.docs!![0].title
								history.saveFilePath = jsonFile.absolutePath
								history.saveOrUpdate("imaPath = ?", history.imaPath)
								list.clear()
								if (Settings.resultNumber < list.size) {
									list.addAll(animation!!.docs!!.subList(0, Settings.resultNumber))
								} else {
									list.addAll(animation!!.docs)
								}
								if (Settings.similarity != 0f) {
									val iterator = list.iterator()
									while (iterator.hasNext()) {
										val dock = iterator.next()
										if (dock.similarity < Settings.similarity)
											iterator.remove()
									}
								}
								subscriber.onComplete()
							} catch (e: NoSuchAlgorithmException) {
								subscriber.onError(e)
							}

						}
					})
		})
				.subscribeOn(Schedulers.newThread())
				.unsubscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(object : Observer<Animation> {
					override fun onSubscribe(d: Disposable) {
						zLoadingDialog.show()
					}

					override fun onNext(animation: Animation) {}

					override fun onError(e: Throwable) {
						zLoadingDialog.dismiss()
						if (e is HttpException) {
							val message = e.response().message()
							val code = e.response().code()
							when (code) {
								429 -> Snackbar.make(coordinatorLayout, R.string.hint_http_exception_busy, Snackbar.LENGTH_LONG)
										.show()
								413 -> Snackbar.make(coordinatorLayout, R.string.hint_request_entity_too_large, Snackbar.LENGTH_LONG)
										.show()
								else -> Snackbar.make(coordinatorLayout, context.getString(R.string.hint_http_exception_error, code, message), Snackbar.LENGTH_LONG)
										.show()
							}
						}
						Snackbar.make(coordinatorLayout, context.getString(R.string.hint_other_error, e.message), Snackbar.LENGTH_LONG)
								.show()
						e.printStackTrace()
					}

					override fun onComplete() {
						zLoadingDialog.dismiss()
						adapter.notifyDataSetChanged()
					}
				})
	}

	companion object {
		private val TAG = "WhatAnimeBuilder"
	}
}
