package pw.janyo.whatanime.config

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

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
