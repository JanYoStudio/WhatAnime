package pw.janyo.whatanime

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

import org.litepal.LitePal

import vip.mystery0.logs.Logs

/**
 * Created by mystery0.
 */

class APP : Application() {

	override fun onCreate() {
		super.onCreate()
		context = applicationContext
		instance = this
	}

	companion object {
		@SuppressLint("StaticFieldLeak")
		var context: Context? = null
			private set

		var instance: Application? = null
			private set
	}
}
