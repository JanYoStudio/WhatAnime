package pw.janyo.whatanime.config

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import pw.janyo.whatanime.repository.local.db.DBHelper
import vip.mystery0.crashhandler.CrashHandler
import vip.mystery0.rx.DataManager

/**
 * Created by mystery0.
 */

class APP : Application() {

	override fun onCreate() {
		super.onCreate()
		context = applicationContext
		instance = this
		DBHelper.init(this)
		CrashHandler.config {
			it.setDirName("log")
			it.setFileNameSuffix("log")
			it.setDir(externalCacheDir!!)
		}.initWithContext(this)
		DataManager.init(5)
	}

	companion object {
		@SuppressLint("StaticFieldLeak")
		lateinit var context: Context
			private set

		lateinit var instance: Application
			private set
	}
}
