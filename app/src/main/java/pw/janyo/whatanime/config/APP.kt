package pw.janyo.whatanime.config

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import pw.janyo.whatanime.repository.local.db.DBHelper
import vip.mystery0.crashhandler.CrashHandler

/**
 * Created by mystery0.
 */

class APP : Application() {

	override fun onCreate() {
		super.onCreate()
		context = applicationContext
		instance = this
		DBHelper.init(this)
		CrashHandler.getInstance(this)
				.setDir(getExternalFilesDir("log")!!)
				.setPrefix("log")
				.setSuffix("txt")
				.init()
	}

	companion object {
		@SuppressLint("StaticFieldLeak")
		lateinit var context: Context
			private set

		lateinit var instance: Application
			private set
	}
}
