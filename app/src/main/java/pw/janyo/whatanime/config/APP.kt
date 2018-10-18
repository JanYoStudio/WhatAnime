package pw.janyo.whatanime.config

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import pw.janyo.whatanime.repository.local.db.DBHelper
import vip.mystery0.crashhandler.CrashConfig
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
				.setConfig(CrashConfig()
						.setDirName("log")
						.setFileNameSuffix("log")
						.setInSDCard(true))
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
